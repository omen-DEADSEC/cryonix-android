package com.example.cryonix.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.ThemeManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.community.AlertCallsActivity;
import com.example.cryonix.data.model.StaffAccount;
import com.example.cryonix.signup.ChooseVillageActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * A34 Profile and settings (AC-11). Every signed-in role can open it.
 * - Staff: name, phone, email (read only), role (read only), change password, staff notices.
 * - Community: name, phone, village + "Change village".
 * - Alerts for my village are always on and cannot be switched off.
 */
public class ProfileSettingsActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private SessionManager sessionManager;
    private UserRole currentRole;

    private TextInputLayout inputLayoutProfileName;
    private TextInputEditText editProfileName;
    private TextInputEditText editProfilePhone;
    private TextView textProfileVillage;
    private SwitchCompat switchProfileNotifyLakeSummary;
    private SwitchCompat switchProfileNotifyNodeHealth;
    private SwitchCompat switchProfileNotifyEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        currentRole = sessionManager.getUserRole();
        if (currentRole == null) {
            // Not signed in, so there is no profile to show
            ScreenRouter.openChooseRole(this);
            return;
        }

        setContentView(R.layout.activity_profile_settings);
        TopBarHelper.setup(this, R.string.profile_title);

        inputLayoutProfileName = findViewById(R.id.inputLayoutProfileName);
        editProfileName = findViewById(R.id.editProfileName);
        editProfilePhone = findViewById(R.id.editProfilePhone);
        textProfileVillage = findViewById(R.id.textProfileVillage);
        switchProfileNotifyLakeSummary = findViewById(R.id.switchProfileNotifyLakeSummary);
        switchProfileNotifyNodeHealth = findViewById(R.id.switchProfileNotifyNodeHealth);
        switchProfileNotifyEmail = findViewById(R.id.switchProfileNotifyEmail);
        SwitchCompat switchProfileNotifyAlerts = findViewById(R.id.switchProfileNotifyAlerts);
        LinearLayout layoutProfileStaffDetails = findViewById(R.id.layoutProfileStaffDetails);
        LinearLayout layoutProfileCommunityDetails = findViewById(R.id.layoutProfileCommunityDetails);
        LinearLayout layoutProfileStaffNotifications = findViewById(R.id.layoutProfileStaffNotifications);
        TextView textProfileEmail = findViewById(R.id.textProfileEmail);
        TextView textProfileRole = findViewById(R.id.textProfileRole);
        MaterialButton buttonProfileChangePassword = findViewById(R.id.buttonProfileChangePassword);
        MaterialButton buttonProfileChangeVillage = findViewById(R.id.buttonProfileChangeVillage);
        MaterialButton buttonProfileSave = findViewById(R.id.buttonProfileSave);
        MaterialButton buttonProfileSignOut = findViewById(R.id.buttonProfileSignOut);
        final MaterialButton buttonProfileTheme = findViewById(R.id.buttonProfileTheme);

        editProfileName.setText(sessionManager.getUserName());
        editProfilePhone.setText(sessionManager.getUserPhone());

        // Village alerts are always on
        switchProfileNotifyAlerts.setChecked(true);
        switchProfileNotifyAlerts.setEnabled(false);

        if (currentRole.isStaff()) {
            layoutProfileStaffDetails.setVisibility(View.VISIBLE);
            layoutProfileStaffNotifications.setVisibility(View.VISIBLE);
            textProfileEmail.setText(getString(R.string.profile_email, sessionManager.getUserEmail()));
            textProfileRole.setText(getString(R.string.profile_role, currentRole.getDisplayName()));

            switchProfileNotifyLakeSummary.setChecked(sessionManager.getNotificationSetting(SessionManager.KEY_NOTIFY_LAKE_SUMMARY));
            switchProfileNotifyNodeHealth.setChecked(sessionManager.getNotificationSetting(SessionManager.KEY_NOTIFY_NODE_HEALTH));
            switchProfileNotifyEmail.setChecked(sessionManager.getNotificationSetting(SessionManager.KEY_NOTIFY_EMAIL));
        } else {
            layoutProfileCommunityDetails.setVisibility(View.VISIBLE);
        }

        buttonProfileChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });

        MaterialButton buttonProfileAlertCalls = findViewById(R.id.buttonProfileAlertCalls);
        buttonProfileAlertCalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileSettingsActivity.this, AlertCallsActivity.class));
            }
        });

        buttonProfileChangeVillage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileSettingsActivity.this, ChooseVillageActivity.class);
                intent.putExtra(ChooseVillageActivity.EXTRA_FROM_SETTINGS, true);
                startActivity(intent);
            }
        });

        // DR-06: theme choice is remembered for the next visit
        buttonProfileTheme.setText(getString(R.string.profile_theme,
                ThemeManager.getThemeName(this, ThemeManager.getSavedTheme(this))));
        buttonProfileTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThemeDialog();
            }
        });

        buttonProfileSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

        buttonProfileSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenRouter.signOut(ProfileSettingsActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the village after coming back from "Change village"
        if (textProfileVillage != null) {
            textProfileVillage.setText(getString(R.string.profile_village, sessionManager.getUserVillage()));
        }
    }

    private void saveProfile() {
        String name = editProfileName.getText() == null ? "" : editProfileName.getText().toString().trim();
        String phone = editProfilePhone.getText() == null ? "" : editProfilePhone.getText().toString().trim();

        // A name is needed for staff (it shows in the audit log). Community members may leave it empty.
        if (currentRole.isStaff() && name.isEmpty()) {
            inputLayoutProfileName.setError(getString(R.string.profile_name_error));
            return;
        }
        inputLayoutProfileName.setError(null);

        sessionManager.setUserName(name);
        sessionManager.setUserPhone(phone);

        if (currentRole.isStaff()) {
            sessionManager.setNotificationSetting(SessionManager.KEY_NOTIFY_LAKE_SUMMARY, switchProfileNotifyLakeSummary.isChecked());
            sessionManager.setNotificationSetting(SessionManager.KEY_NOTIFY_NODE_HEALTH, switchProfileNotifyNodeHealth.isChecked());
            sessionManager.setNotificationSetting(SessionManager.KEY_NOTIFY_EMAIL, switchProfileNotifyEmail.isChecked());

            // Keep the demo account in step with the session
            StaffAccount account = FakeDatabase.findStaffByEmail(sessionManager.getUserEmail());
            if (account != null) {
                account.setFullName(name);
                account.setPhone(phone);
            }
        }

        String who = name.isEmpty() ? phone : name;
        FakeDatabase.addAuditEntry(who, "Updated profile");
        Toast.makeText(this, R.string.message_saved, Toast.LENGTH_SHORT).show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        final TextInputEditText editChangePasswordCurrent = dialogView.findViewById(R.id.editChangePasswordCurrent);
        final TextInputEditText editChangePasswordNew = dialogView.findViewById(R.id.editChangePasswordNew);
        final TextView textChangePasswordError = dialogView.findViewById(R.id.textChangePasswordError);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.profile_change_password)
                .setView(dialogView)
                .setPositiveButton(R.string.button_save, null)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPassword = editChangePasswordCurrent.getText() == null ? "" : editChangePasswordCurrent.getText().toString();
                String newPassword = editChangePasswordNew.getText() == null ? "" : editChangePasswordNew.getText().toString();

                StaffAccount account = FakeDatabase.findStaffByEmail(sessionManager.getUserEmail());
                boolean isCurrentCorrect = account != null && account.getPassword().equals(currentPassword);
                if (!isCurrentCorrect || newPassword.length() < MIN_PASSWORD_LENGTH) {
                    textChangePasswordError.setVisibility(View.VISIBLE);
                    return;
                }

                account.setPassword(newPassword);
                FakeDatabase.addAuditEntry(account.getFullName(), "Changed password");
                Toast.makeText(ProfileSettingsActivity.this, R.string.profile_password_changed, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void showThemeDialog() {
        String[] themeNames = getResources().getStringArray(R.array.theme_choices);
        int savedTheme = ThemeManager.getSavedTheme(this);

        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_theme_choose)
                .setSingleChoiceItems(themeNames, savedTheme, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // The screen redraws itself in the new theme
                        ThemeManager.saveAndApply(ProfileSettingsActivity.this, which);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}
