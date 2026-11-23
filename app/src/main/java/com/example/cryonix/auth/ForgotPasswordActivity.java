package com.example.cryonix.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.StaffAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Forgot password (AC-05).
 * The reset link is sent only to a registered email. The message on screen is the same
 * for every email, so nobody can find out which emails have accounts.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutForgotPasswordEmail;
    private TextInputEditText editForgotPasswordEmail;
    private TextView textForgotPasswordMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        TopBarHelper.setup(this, R.string.forgot_password_title);

        inputLayoutForgotPasswordEmail = findViewById(R.id.inputLayoutForgotPasswordEmail);
        editForgotPasswordEmail = findViewById(R.id.editForgotPasswordEmail);
        textForgotPasswordMessage = findViewById(R.id.textForgotPasswordMessage);
        MaterialButton buttonForgotPasswordSend = findViewById(R.id.buttonForgotPasswordSend);

        buttonForgotPasswordSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResetLink();
            }
        });
    }

    private void sendResetLink() {
        String email = editForgotPasswordEmail.getText() == null ? "" : editForgotPasswordEmail.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutForgotPasswordEmail.setError(getString(R.string.forgot_password_error_email));
            return;
        }
        inputLayoutForgotPasswordEmail.setError(null);

        StaffAccount account = FakeDatabase.findStaffByEmail(email);
        if (account != null) {
            // TODO: call the backend to email the reset link (valid 30 minutes, one use)
            FakeDatabase.addAuditEntry(account.getFullName(), "Asked for a password reset link");
        }

        textForgotPasswordMessage.setText(R.string.forgot_password_sent);
        textForgotPasswordMessage.setVisibility(View.VISIBLE);
    }
}
