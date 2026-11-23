package com.example.cryonix.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.admin.AuditLogActivity;
import com.example.cryonix.admin.OrganisationsActivity;
import com.example.cryonix.admin.RolePermissionsActivity;
import com.example.cryonix.admin.UsersRolesActivity;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.StaffBottomBarHelper;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.network.NetworkHealthActivity;
import com.example.cryonix.notifications.NotificationsActivity;
import com.example.cryonix.profile.ProfileSettingsActivity;
import com.example.cryonix.shelters.ShelterManageActivity;
import com.google.android.material.button.MaterialButton;

/**
 * A31 More (staff and admin menu).
 * Each row is added ONLY if the signed-in role has the permission for it (DR-02: menu follows the role).
 */
public class StaffMoreActivity extends AppCompatActivity {

    private LinearLayout layoutStaffMoreMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserRole currentRole = new SessionManager(this).getUserRole();
        if (currentRole == null || !currentRole.isStaff()) {
            ScreenRouter.openHomeForRole(this, currentRole);
            return;
        }

        setContentView(R.layout.activity_staff_more);
        TopBarHelper.setupWithoutBack(this, R.string.more_title);
        StaffBottomBarHelper.setup(this, StaffBottomBarHelper.TAB_MORE);

        layoutStaffMoreMenu = findViewById(R.id.layoutStaffMoreMenu);
        MaterialButton buttonStaffMoreSignOut = findViewById(R.id.buttonStaffMoreSignOut);

        addMenuRow(R.drawable.ic_alert_bell, R.string.menu_notifications, R.string.menu_notifications_text, NotificationsActivity.class);

        if (currentRole.can(Permission.VIEW_NETWORK_HEALTH)) {
            addMenuRow(R.drawable.ic_node, R.string.menu_network, R.string.menu_network_text, NetworkHealthActivity.class);
        }
        if (currentRole.can(Permission.MANAGE_SHELTERS)) {
            addMenuRow(R.drawable.ic_shelter, R.string.menu_shelters, R.string.menu_shelters_text, ShelterManageActivity.class);
        }
        if (currentRole.can(Permission.MANAGE_USERS)) {
            addMenuRow(R.drawable.ic_users, R.string.menu_users_roles, R.string.menu_users_roles_text, UsersRolesActivity.class);
        }
        if (currentRole.can(Permission.MANAGE_ORGANISATIONS)) {
            addMenuRow(R.drawable.ic_organisation, R.string.menu_organisations, R.string.menu_organisations_text, OrganisationsActivity.class);
        }
        if (currentRole.can(Permission.CHANGE_SETTINGS)) {
            addMenuRow(R.drawable.ic_shield, R.string.menu_role_permissions, R.string.menu_role_permissions_text, RolePermissionsActivity.class);
        }
        if (currentRole.can(Permission.VIEW_AUDIT_LOG)) {
            addMenuRow(R.drawable.ic_list, R.string.menu_audit_log, R.string.menu_audit_log_text, AuditLogActivity.class);
        }
        addMenuRow(R.drawable.ic_person, R.string.menu_profile_settings, R.string.menu_profile_settings_text, ProfileSettingsActivity.class);

        buttonStaffMoreSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenRouter.signOut(StaffMoreActivity.this);
            }
        });
    }

    private void addMenuRow(int iconId, int titleId, int textId, final Class<?> screenClass) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_menu_row, layoutStaffMoreMenu, false);
        ImageView imageMenuRowIcon = rowView.findViewById(R.id.imageMenuRowIcon);
        TextView textMenuRowTitle = rowView.findViewById(R.id.textMenuRowTitle);
        TextView textMenuRowText = rowView.findViewById(R.id.textMenuRowText);

        imageMenuRowIcon.setImageResource(iconId);
        textMenuRowTitle.setText(titleId);
        textMenuRowText.setText(textId);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StaffMoreActivity.this, screenClass));
            }
        });
        layoutStaffMoreMenu.addView(rowView);
    }
}
