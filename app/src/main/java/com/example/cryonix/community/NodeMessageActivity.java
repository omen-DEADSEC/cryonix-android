package com.example.cryonix.community;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.AlertLevel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * LR-06: get the alert text from the village node WITHOUT internet and WITHOUT pairing.
 *
 * 1. Bluetooth: the node sends a BLE advertisement with the Cryonix service UUID.
 *    Its service data is the text "LEVEL|message" in UTF-8, e.g. "WARNING|Go uphill now".
 *    The phone only listens (scans); it never connects, so no pairing is needed.
 * 2. Wi-Fi: the node also runs an open Wi-Fi network "CRYONIX-NODE" with a small page
 *    at http://192.168.4.1 that shows the same text.
 *
 * The node firmware must use the same UUID as NODE_SERVICE_UUID below (LoRa Mesh Channel epic, device work).
 */
public class NodeMessageActivity extends AppCompatActivity {

    public static final String NODE_SERVICE_UUID = "0000c7a1-0000-1000-8000-00805f9b34fb";
    private static final String NODE_WIFI_PAGE = "http://192.168.4.1/";
    private static final long SCAN_MILLIS = 20000;
    private static final int REQUEST_CODE_BLUETOOTH = 401;

    private MaterialCardView cardNodeMessage;
    private TextView textNodeMessageLevel;
    private TextView textNodeMessageBody;
    private TextView textNodeMessageTime;
    private TextView textNodeMessageStatus;

    private BluetoothLeScanner bluetoothScanner;
    private boolean isScanning = false;
    private final Handler stopHandler = new Handler(Looper.getMainLooper());

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord record = result.getScanRecord();
            if (record == null) {
                return;
            }
            byte[] data = record.getServiceData(ParcelUuid.fromString(NODE_SERVICE_UUID));
            if (data != null && data.length > 0) {
                String text = new String(data, StandardCharsets.UTF_8);
                stopScan();
                showMessage(text);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            isScanning = false;
            showStatus(getString(R.string.node_message_not_found));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_node_message);
        TopBarHelper.setup(this, R.string.node_message_title);

        cardNodeMessage = findViewById(R.id.cardNodeMessage);
        textNodeMessageLevel = findViewById(R.id.textNodeMessageLevel);
        textNodeMessageBody = findViewById(R.id.textNodeMessageBody);
        textNodeMessageTime = findViewById(R.id.textNodeMessageTime);
        textNodeMessageStatus = findViewById(R.id.textNodeMessageStatus);
        MaterialButton buttonNodeMessageScan = findViewById(R.id.buttonNodeMessageScan);
        MaterialButton buttonNodeMessageWifi = findViewById(R.id.buttonNodeMessageWifi);
        TextView textNodeMessageSample = findViewById(R.id.textNodeMessageSample);

        buttonNodeMessageScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanIfAllowed();
            }
        });

        buttonNodeMessageWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(NODE_WIFI_PAGE)));
                } catch (ActivityNotFoundException e) {
                    showStatus(NODE_WIFI_PAGE);
                }
            }
        });

        // DEMO: shows what a received message looks like, without a real node
        textNodeMessageSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String village = new SessionManager(NodeMessageActivity.this).getUserVillage();
                showMessage(FakeDatabase.getSampleNodeBroadcast(village));
            }
        });
    }

    private String getNeededPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            return Manifest.permission.BLUETOOTH_SCAN;
        }
        return Manifest.permission.ACCESS_FINE_LOCATION;   // older Android needs location for Bluetooth scans
    }

    private void startScanIfAllowed() {
        String permission = getNeededPermission();
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE_BLUETOOTH);
            return;
        }
        startScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                showStatus(getString(R.string.node_message_permission));
            }
        }
    }

    private void startScan() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            showStatus(getString(R.string.node_message_bt_off));
            return;
        }
        bluetoothScanner = adapter.getBluetoothLeScanner();
        if (bluetoothScanner == null) {
            showStatus(getString(R.string.node_message_bt_off));
            return;
        }

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(NODE_SERVICE_UUID)).build());
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        try {
            bluetoothScanner.startScan(filters, settings, scanCallback);
            isScanning = true;
            showStatus(getString(R.string.node_message_scanning));
            stopHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isScanning) {
                        stopScan();
                        showStatus(getString(R.string.node_message_not_found));
                    }
                }
            }, SCAN_MILLIS);
        } catch (SecurityException e) {
            showStatus(getString(R.string.node_message_permission));
        }
    }

    private void stopScan() {
        if (bluetoothScanner != null && isScanning) {
            try {
                bluetoothScanner.stopScan(scanCallback);
            } catch (SecurityException e) {
                // permission removed while scanning
            }
        }
        isScanning = false;
        stopHandler.removeCallbacksAndMessages(null);
    }

    /** Text format from the node: "LEVEL|message". */
    private void showMessage(String text) {
        AlertLevel level = AlertLevel.BASELINE;
        String body = text;
        int bar = text.indexOf('|');
        if (bar > 0) {
            try {
                level = AlertLevel.valueOf(text.substring(0, bar).trim());
            } catch (IllegalArgumentException e) {
                level = AlertLevel.BASELINE;
            }
            body = text.substring(bar + 1);
        }

        LevelBadgeHelper.show(textNodeMessageLevel, level);
        textNodeMessageBody.setText(body);
        String time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date());
        textNodeMessageTime.setText(getString(R.string.node_message_received, time));
        cardNodeMessage.setVisibility(View.VISIBLE);
        textNodeMessageStatus.setVisibility(View.GONE);
    }

    private void showStatus(String text) {
        textNodeMessageStatus.setText(text);
        textNodeMessageStatus.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }
}
