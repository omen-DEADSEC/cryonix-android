package com.example.cryonix.signup;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.TopBarHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

/**
 * A05 Permissions (community sign-up step 1 of 3).
 * Asks for notifications (Android 13+) and location. The user can say "Not now";
 * alerts still come by SMS and voice call.
 */
public class PermissionsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        TopBarHelper.setup(this, R.string.choose_role_community_title);

        MaterialButton buttonPermissionsAllow = findViewById(R.id.buttonPermissionsAllow);
        MaterialButton buttonPermissionsNotNow = findViewById(R.id.buttonPermissionsNotNow);

        buttonPermissionsAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissions();
            }
        });

        buttonPermissionsNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChooseVillage();
            }
        });
    }

    private void askPermissions() {
        ArrayList<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= 33) {
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        String[] permissionArray = permissionList.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissionArray, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Whatever the answer, we move on. The app still works without them.
            openChooseVillage();
        }
    }

    private void openChooseVillage() {
        startActivity(new Intent(this, ChooseVillageActivity.class));
    }
}
