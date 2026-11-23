package com.example.cryonix.signup;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * A07 Register phone (AC-03, community sign-up step 3 of 3).
 * 1. User types a Pakistani mobile number and taps "Send code".
 * 2. User types the 6-digit code. Demo code is 123456 until the SMS gateway is ready.
 * 3. On success the community member is signed in and the community home opens.
 */
public class RegisterPhoneActivity extends AppCompatActivity {

    private static final long RESEND_WAIT_MILLIS = 30000;

    private TextInputLayout inputLayoutRegisterPhoneNumber;
    private TextInputEditText editRegisterPhoneNumber;
    private LinearLayout layoutRegisterPhoneCode;
    private TextView textRegisterPhoneCodeSent;
    private TextInputLayout inputLayoutRegisterPhoneCode;
    private TextInputEditText editRegisterPhoneCode;
    private TextView textRegisterPhoneResend;

    private String villageName;
    private String phoneNumber;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);
        TopBarHelper.setup(this, R.string.register_phone_title);

        villageName = getIntent().getStringExtra(ChooseVillageActivity.EXTRA_VILLAGE_NAME);

        inputLayoutRegisterPhoneNumber = findViewById(R.id.inputLayoutRegisterPhoneNumber);
        editRegisterPhoneNumber = findViewById(R.id.editRegisterPhoneNumber);
        layoutRegisterPhoneCode = findViewById(R.id.layoutRegisterPhoneCode);
        textRegisterPhoneCodeSent = findViewById(R.id.textRegisterPhoneCodeSent);
        inputLayoutRegisterPhoneCode = findViewById(R.id.inputLayoutRegisterPhoneCode);
        editRegisterPhoneCode = findViewById(R.id.editRegisterPhoneCode);
        textRegisterPhoneResend = findViewById(R.id.textRegisterPhoneResend);
        TextView textRegisterPhoneVillage = findViewById(R.id.textRegisterPhoneVillage);
        MaterialButton buttonRegisterPhoneSendCode = findViewById(R.id.buttonRegisterPhoneSendCode);
        MaterialButton buttonRegisterPhoneVerify = findViewById(R.id.buttonRegisterPhoneVerify);

        textRegisterPhoneVillage.setText(getString(R.string.community_home_village, villageName));

        buttonRegisterPhoneSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });

        buttonRegisterPhoneVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });

        textRegisterPhoneResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });
    }

    /** Accepts 03XXXXXXXXX or +923XXXXXXXXX (spaces and dashes are removed first). */
    private boolean isValidPakistaniNumber(String number) {
        return number.matches("03[0-9]{9}") || number.matches("\\+923[0-9]{9}");
    }

    private void sendCode() {
        String typedNumber = editRegisterPhoneNumber.getText() == null ? "" : editRegisterPhoneNumber.getText().toString();
        String cleanNumber = typedNumber.replace(" ", "").replace("-", "");

        if (!isValidPakistaniNumber(cleanNumber)) {
            inputLayoutRegisterPhoneNumber.setError(getString(R.string.register_phone_error_number));
            return;
        }
        inputLayoutRegisterPhoneNumber.setError(null);
        phoneNumber = cleanNumber;

        // TODO: ask the backend to send the SMS code. For now the demo code is 123456.
        layoutRegisterPhoneCode.setVisibility(View.VISIBLE);
        textRegisterPhoneCodeSent.setText(getString(R.string.register_phone_code_sent, phoneNumber));
        Toast.makeText(this, R.string.register_phone_demo_code, Toast.LENGTH_LONG).show();
        startResendTimer();
    }

    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        textRegisterPhoneResend.setEnabled(false);
        textRegisterPhoneResend.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));

        resendTimer = new CountDownTimer(RESEND_WAIT_MILLIS, 1000) {
            @Override
            public void onTick(long millisLeft) {
                int secondsLeft = (int) (millisLeft / 1000) + 1;
                textRegisterPhoneResend.setText(getString(R.string.register_phone_resend_wait, secondsLeft));
            }

            @Override
            public void onFinish() {
                textRegisterPhoneResend.setText(R.string.register_phone_resend);
                textRegisterPhoneResend.setEnabled(true);
                textRegisterPhoneResend.setTextColor(ContextCompat.getColor(RegisterPhoneActivity.this, R.color.color_primary));
            }
        };
        resendTimer.start();
    }

    private void verifyCode() {
        String code = editRegisterPhoneCode.getText() == null ? "" : editRegisterPhoneCode.getText().toString().trim();

        if (!code.equals(FakeDatabase.DEMO_ONE_TIME_CODE)) {
            inputLayoutRegisterPhoneCode.setError(getString(R.string.register_phone_error_code));
            return;
        }
        inputLayoutRegisterPhoneCode.setError(null);

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.saveCommunityLogin(phoneNumber, villageName);
        // MN-01: the confirmed number now gets alert calls and SMS for this village
        FakeDatabase.registerPhone(phoneNumber, villageName, getString(R.string.calls_owner_me), phoneNumber);
        FakeDatabase.addAuditEntry(phoneNumber, "Community member registered for " + villageName);

        ScreenRouter.openHomeForRole(this, UserRole.COMMUNITY_MEMBER);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
