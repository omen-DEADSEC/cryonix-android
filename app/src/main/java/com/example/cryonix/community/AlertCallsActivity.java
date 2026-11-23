package com.example.cryonix.community;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.RegisteredPhone;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * MN-01 Alert calls and SMS (Mobile Network Channel).
 * - Shows the numbers this person manages (own number + family numbers).
 * - Every number is confirmed with a code and linked to ONE village (the user's village).
 * - Any number can be removed at any time.
 * DEMO: the code is 123456 until the SMS gateway is connected.
 */
public class AlertCallsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private String myPhone;
    private String myVillage;

    private LinearLayout layoutCallsList;
    private TextView textCallsNone;
    private MaterialButton buttonCallsAddMine;
    private TextInputLayout inputLayoutCallsName;
    private TextInputEditText editCallsName;
    private TextInputLayout inputLayoutCallsNumber;
    private TextInputEditText editCallsNumber;
    private LinearLayout layoutCallsCode;
    private TextView textCallsCodeSent;
    private TextInputLayout inputLayoutCallsCode;
    private TextInputEditText editCallsCode;

    private String pendingNumber;   // number waiting for its code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_alert_calls);
        TopBarHelper.setup(this, R.string.calls_title);

        sessionManager = new SessionManager(this);
        myPhone = sessionManager.getUserPhone();
        myVillage = sessionManager.getUserVillage();

        layoutCallsList = findViewById(R.id.layoutCallsList);
        textCallsNone = findViewById(R.id.textCallsNone);
        buttonCallsAddMine = findViewById(R.id.buttonCallsAddMine);
        inputLayoutCallsName = findViewById(R.id.inputLayoutCallsName);
        editCallsName = findViewById(R.id.editCallsName);
        inputLayoutCallsNumber = findViewById(R.id.inputLayoutCallsNumber);
        editCallsNumber = findViewById(R.id.editCallsNumber);
        layoutCallsCode = findViewById(R.id.layoutCallsCode);
        textCallsCodeSent = findViewById(R.id.textCallsCodeSent);
        inputLayoutCallsCode = findViewById(R.id.inputLayoutCallsCode);
        editCallsCode = findViewById(R.id.editCallsCode);
        TextView textCallsAddText = findViewById(R.id.textCallsAddText);
        MaterialButton buttonCallsSendCode = findViewById(R.id.buttonCallsSendCode);
        MaterialButton buttonCallsVerify = findViewById(R.id.buttonCallsVerify);

        textCallsAddText.setText(getString(R.string.calls_add_text, myVillage));

        buttonCallsAddMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Own number was already confirmed at sign-up, so no new code is needed
                FakeDatabase.registerPhone(myPhone, myVillage, getString(R.string.calls_owner_me), myPhone);
                Toast.makeText(AlertCallsActivity.this, R.string.calls_added, Toast.LENGTH_SHORT).show();
                showNumbers();
            }
        });

        buttonCallsSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });

        buttonCallsVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyAndAdd();
            }
        });

        showNumbers();
    }

    private void showNumbers() {
        layoutCallsList.removeAllViews();
        List<RegisteredPhone> phoneList = FakeDatabase.getPhonesManagedBy(myPhone);
        textCallsNone.setVisibility(phoneList.isEmpty() ? View.VISIBLE : View.GONE);

        boolean isMineRegistered = false;
        for (final RegisteredPhone phone : phoneList) {
            if (phone.getNumber().equals(myPhone)) {
                isMineRegistered = true;
            }
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_call_number, layoutCallsList, false);
            TextView textCallNumber = rowView.findViewById(R.id.textCallNumber);
            TextView textCallDetails = rowView.findViewById(R.id.textCallDetails);
            MaterialButton buttonCallRemove = rowView.findViewById(R.id.buttonCallRemove);

            textCallNumber.setText(getString(R.string.calls_row, phone.getNumber(), phone.getOwnerName()));
            textCallDetails.setText(getString(R.string.calls_row_added, phone.getVillage(), phone.getAddedTime()));
            buttonCallRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askRemove(phone);
                }
            });
            layoutCallsList.addView(rowView);
        }
        buttonCallsAddMine.setVisibility(isMineRegistered ? View.GONE : View.VISIBLE);
    }

    private void askRemove(final RegisteredPhone phone) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.calls_remove_question, phone.getNumber()))
                .setPositiveButton(R.string.calls_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FakeDatabase.removePhone(phone.getNumber(), myPhone);
                        int messageId = phone.getNumber().equals(myPhone)
                                ? R.string.calls_my_number_removed_note : R.string.calls_removed;
                        Toast.makeText(AlertCallsActivity.this, messageId, Toast.LENGTH_LONG).show();
                        showNumbers();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void sendCode() {
        String name = editCallsName.getText() == null ? "" : editCallsName.getText().toString().trim();
        String typedNumber = editCallsNumber.getText() == null ? "" : editCallsNumber.getText().toString();
        String cleanNumber = FakeDatabase.cleanPhoneNumber(typedNumber);

        inputLayoutCallsName.setError(name.isEmpty() ? getString(R.string.calls_error_name) : null);
        inputLayoutCallsNumber.setError(cleanNumber == null ? getString(R.string.register_phone_error_number) : null);
        if (name.isEmpty() || cleanNumber == null) {
            return;
        }

        // TODO: ask the backend to send the SMS code to this number
        pendingNumber = cleanNumber;
        textCallsCodeSent.setText(getString(R.string.calls_code_text, cleanNumber));
        layoutCallsCode.setVisibility(View.VISIBLE);
    }

    private void verifyAndAdd() {
        String code = editCallsCode.getText() == null ? "" : editCallsCode.getText().toString().trim();
        if (pendingNumber == null || !code.equals(FakeDatabase.DEMO_ONE_TIME_CODE)) {
            inputLayoutCallsCode.setError(getString(R.string.register_phone_error_code));
            return;
        }
        inputLayoutCallsCode.setError(null);

        String name = editCallsName.getText() == null ? "" : editCallsName.getText().toString().trim();
        FakeDatabase.registerPhone(pendingNumber, myVillage, name, myPhone);
        Toast.makeText(this, R.string.calls_added, Toast.LENGTH_SHORT).show();

        // Clear the form for the next number
        pendingNumber = null;
        editCallsName.setText("");
        editCallsNumber.setText("");
        editCallsCode.setText("");
        layoutCallsCode.setVisibility(View.GONE);
        showNumbers();
    }
}
