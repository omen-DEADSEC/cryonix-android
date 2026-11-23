package com.example.cryonix.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.AccountRequest;
import com.example.cryonix.data.model.StaffAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A32 Users and roles (AC-06 approve requests, AC-07 manage users).
 * Only the System administrator can open it.
 */
public class UsersRolesActivity extends AppCompatActivity {

    private static final int TAB_REQUESTS = 0;
    private static final int TAB_USERS = 1;

    private TabLayout tabUsersRoles;
    private LinearLayout layoutUsersRolesList;
    private TextView textUsersRolesEmpty;

    private String adminName;
    private String adminEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.MANAGE_USERS)) {
            return;
        }
        setContentView(R.layout.activity_users_roles);
        TopBarHelper.setup(this, R.string.users_roles_title);

        SessionManager sessionManager = new SessionManager(this);
        adminName = sessionManager.getUserName();
        adminEmail = sessionManager.getUserEmail();

        tabUsersRoles = findViewById(R.id.tabUsersRoles);
        layoutUsersRolesList = findViewById(R.id.layoutUsersRolesList);
        textUsersRolesEmpty = findViewById(R.id.textUsersRolesEmpty);

        tabUsersRoles.addTab(tabUsersRoles.newTab());
        tabUsersRoles.addTab(tabUsersRoles.newTab());
        tabUsersRoles.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showSelectedTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        showSelectedTab();
    }

    /** Updates the tab titles (with counts) and shows the list of the selected tab. */
    private void showSelectedTab() {
        TabLayout.Tab tabRequests = tabUsersRoles.getTabAt(TAB_REQUESTS);
        TabLayout.Tab tabUsers = tabUsersRoles.getTabAt(TAB_USERS);
        if (tabRequests != null) {
            tabRequests.setText(getString(R.string.users_roles_tab_requests, FakeDatabase.getAccountRequests().size()));
        }
        if (tabUsers != null) {
            tabUsers.setText(getString(R.string.users_roles_tab_users, FakeDatabase.getStaffAccounts().size()));
        }

        if (tabUsersRoles.getSelectedTabPosition() == TAB_USERS) {
            showUsers();
        } else {
            showRequests();
        }
    }

    // ---------------- Requests tab ----------------

    private void showRequests() {
        layoutUsersRolesList.removeAllViews();
        List<AccountRequest> requestList = FakeDatabase.getAccountRequests();
        textUsersRolesEmpty.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);

        for (final AccountRequest request : requestList) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_account_request, layoutUsersRolesList, false);
            TextView textRequestName = cardView.findViewById(R.id.textRequestName);
            TextView textRequestEmail = cardView.findViewById(R.id.textRequestEmail);
            TextView textRequestRole = cardView.findViewById(R.id.textRequestRole);
            TextView textRequestReason = cardView.findViewById(R.id.textRequestReason);
            MaterialButton buttonRequestApprove = cardView.findViewById(R.id.buttonRequestApprove);
            MaterialButton buttonRequestReject = cardView.findViewById(R.id.buttonRequestReject);

            textRequestName.setText(request.getFullName());
            textRequestEmail.setText(request.getEmail() + "  •  " + request.getOrganisation());
            textRequestRole.setText(getString(R.string.users_roles_requested_role, request.getRequestedRole().getDisplayName()));
            textRequestReason.setText(request.getReason());

            buttonRequestApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askRoleAndApprove(request);
                }
            });

            buttonRequestReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askAndReject(request);
                }
            });

            layoutUsersRolesList.addView(cardView);
        }
    }

    /** The admin picks the final role (it can be different from what was asked). */
    private void askRoleAndApprove(final AccountRequest request) {
        final UserRole[] roles = UserRole.staffRoles();
        String[] roleNames = new String[roles.length];
        int checkedIndex = 0;
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].getDisplayName();
            if (roles[i] == request.getRequestedRole()) {
                checkedIndex = i;
            }
        }
        final int[] chosenIndex = {checkedIndex};

        new AlertDialog.Builder(this)
                .setTitle(R.string.users_roles_choose_role)
                .setSingleChoiceItems(roleNames, checkedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenIndex[0] = which;
                    }
                })
                .setPositiveButton(R.string.users_roles_approve, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FakeDatabase.approveRequest(request, roles[chosenIndex[0]], adminName);
                        Toast.makeText(UsersRolesActivity.this, R.string.users_roles_approved, Toast.LENGTH_SHORT).show();
                        showSelectedTab();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void askAndReject(final AccountRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.users_roles_reject_question, request.getFullName()))
                .setPositiveButton(R.string.users_roles_reject, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FakeDatabase.rejectRequest(request, adminName);
                        Toast.makeText(UsersRolesActivity.this, R.string.users_roles_rejected, Toast.LENGTH_SHORT).show();
                        showSelectedTab();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    // ---------------- Users tab ----------------

    private void showUsers() {
        layoutUsersRolesList.removeAllViews();
        textUsersRolesEmpty.setVisibility(View.GONE);

        for (final StaffAccount account : FakeDatabase.getStaffAccounts()) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_user_account, layoutUsersRolesList, false);
            TextView textUserName = rowView.findViewById(R.id.textUserName);
            TextView textUserEmail = rowView.findViewById(R.id.textUserEmail);
            TextView textUserRole = rowView.findViewById(R.id.textUserRole);
            TextView textUserStatus = rowView.findViewById(R.id.textUserStatus);

            textUserName.setText(account.getFullName());
            textUserEmail.setText(account.getEmail());
            textUserRole.setText(account.getRole().getDisplayName() + "  •  " + account.getOrganisation());

            if (!account.isActive()) {
                setStatus(textUserStatus, R.string.users_roles_status_off, false);
            } else if (account.isLocked()) {
                setStatus(textUserStatus, R.string.users_roles_status_locked, false);
            } else {
                setStatus(textUserStatus, R.string.users_roles_status_active, true);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showUserActions(account);
                }
            });

            layoutUsersRolesList.addView(rowView);
        }
    }

    private void setStatus(TextView textUserStatus, int textId, boolean isGood) {
        textUserStatus.setText(textId);
        textUserStatus.setTextColor(ContextCompat.getColor(this, isGood ? R.color.color_success : R.color.color_error));
        textUserStatus.setBackgroundResource(isGood ? R.drawable.bg_message_success : R.drawable.bg_message_error);
    }

    /** Actions for one user: change role, switch off/on, unlock. */
    private void showUserActions(final StaffAccount account) {
        if (account.getEmail().equalsIgnoreCase(adminEmail)) {
            Toast.makeText(this, R.string.users_roles_cannot_change_self, Toast.LENGTH_LONG).show();
            return;
        }

        final List<String> actionNames = new ArrayList<>();
        final List<Integer> actionIds = new ArrayList<>();

        actionNames.add(getString(R.string.users_roles_change_role));
        actionIds.add(R.string.users_roles_change_role);

        if (account.isActive()) {
            actionNames.add(getString(R.string.users_roles_switch_off));
            actionIds.add(R.string.users_roles_switch_off);
        } else {
            actionNames.add(getString(R.string.users_roles_switch_on));
            actionIds.add(R.string.users_roles_switch_on);
        }

        if (account.isLocked()) {
            actionNames.add(getString(R.string.users_roles_unlock));
            actionIds.add(R.string.users_roles_unlock);
        }

        new AlertDialog.Builder(this)
                .setTitle(account.getFullName())
                .setItems(actionNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int actionId = actionIds.get(which);
                        if (actionId == R.string.users_roles_change_role) {
                            askNewRole(account);
                        } else if (actionId == R.string.users_roles_switch_off) {
                            account.setActive(false);
                            FakeDatabase.addAuditEntry(adminName, "Switched off account of " + account.getEmail());
                            showSelectedTab();
                        } else if (actionId == R.string.users_roles_switch_on) {
                            account.setActive(true);
                            FakeDatabase.addAuditEntry(adminName, "Switched on account of " + account.getEmail());
                            showSelectedTab();
                        } else if (actionId == R.string.users_roles_unlock) {
                            account.setFailedSignInCount(0);
                            FakeDatabase.addAuditEntry(adminName, "Unlocked account of " + account.getEmail());
                            showSelectedTab();
                        }
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void askNewRole(final StaffAccount account) {
        final UserRole[] roles = UserRole.staffRoles();
        String[] roleNames = new String[roles.length];
        int checkedIndex = 0;
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].getDisplayName();
            if (roles[i] == account.getRole()) {
                checkedIndex = i;
            }
        }
        final int[] chosenIndex = {checkedIndex};

        new AlertDialog.Builder(this)
                .setTitle(R.string.users_roles_change_role)
                .setSingleChoiceItems(roleNames, checkedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenIndex[0] = which;
                    }
                })
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserRole newRole = roles[chosenIndex[0]];
                        if (newRole != account.getRole()) {
                            String oldRoleName = account.getRole().getDisplayName();
                            account.setRole(newRole);
                            FakeDatabase.addAuditEntry(adminName, "Changed role of " + account.getEmail()
                                    + " from " + oldRoleName + " to " + newRole.getDisplayName());
                            Toast.makeText(UsersRolesActivity.this, R.string.users_roles_role_changed, Toast.LENGTH_LONG).show();
                        }
                        showSelectedTab();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}
