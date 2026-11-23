package com.example.cryonix.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.StaffAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * A04 Staff sign in (AC-04).
 * - Correct email and password opens the home screen of the user's role.
 * - After 5 wrong passwords the account is locked.
 * - A switched-off account cannot sign in.
 */
public class StaffSignInActivity extends AppCompatActivity {

    private static final int MAX_TRIES = 5;

    private TextInputEditText editStaffSignInEmail;
    private TextInputEditText editStaffSignInPassword;
    private TextView textStaffSignInError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_sign_in);
        TopBarHelper.setup(this, R.string.staff_sign_in_title);

        editStaffSignInEmail = findViewById(R.id.editStaffSignInEmail);
        editStaffSignInPassword = findViewById(R.id.editStaffSignInPassword);
        textStaffSignInError = findViewById(R.id.textStaffSignInError);
        MaterialButton buttonStaffSignIn = findViewById(R.id.buttonStaffSignIn);
        TextView textStaffSignInForgotPassword = findViewById(R.id.textStaffSignInForgotPassword);

        buttonStaffSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        textStaffSignInForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StaffSignInActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void signIn() {
        String email = editStaffSignInEmail.getText() == null ? "" : editStaffSignInEmail.getText().toString().trim();
        String password = editStaffSignInPassword.getText() == null ? "" : editStaffSignInPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.staff_sign_in_error_empty));
            return;
        }

        StaffAccount account = FakeDatabase.findStaffByEmail(email);

        // Unknown email: same message as a wrong password, so nobody can guess emails
        if (account == null) {
            showError(getString(R.string.staff_sign_in_error_wrong, MAX_TRIES));
            return;
        }

        if (!account.isActive()) {
            showError(getString(R.string.staff_sign_in_error_switched_off));
            return;
        }

        if (account.isLocked()) {
            showError(getString(R.string.staff_sign_in_error_locked));
            return;
        }

        if (!account.getPassword().equals(password)) {
            account.setFailedSignInCount(account.getFailedSignInCount() + 1);
            if (account.isLocked()) {
                FakeDatabase.addAuditEntry(account.getFullName(), "Account locked after 5 wrong passwords");
                showError(getString(R.string.staff_sign_in_error_locked));
            } else {
                int triesLeft = MAX_TRIES - account.getFailedSignInCount();
                showError(getString(R.string.staff_sign_in_error_wrong, triesLeft));
            }
            return;
        }

        // Correct password: reset the counter, save the session, open the role's home
        account.setFailedSignInCount(0);
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.saveStaffLogin(account.getRole(), account.getFullName(), account.getEmail());
        sessionManager.setUserPhone(account.getPhone());
        FakeDatabase.addAuditEntry(account.getFullName(), "Signed in");

        ScreenRouter.openHomeForRole(this, account.getRole());
    }

    private void showError(String message) {
        textStaffSignInError.setText(message);
        textStaffSignInError.setVisibility(View.VISIBLE);
    }
}
