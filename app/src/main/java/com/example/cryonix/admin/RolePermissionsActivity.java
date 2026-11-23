package com.example.cryonix.admin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;

/**
 * Roles and permissions (AC-09).
 * The table is built from the Permission enum, so it always matches what the app really allows.
 */
public class RolePermissionsActivity extends AppCompatActivity {

    // Column order in the table
    private static final UserRole[] TABLE_ROLES = {
            UserRole.SYSTEM_ADMIN,
            UserRole.AUTHORITY_OFFICER,
            UserRole.ORGANISATION_STAFF,
            UserRole.COMMUNITY_MEMBER
    };

    private LinearLayout layoutRolePermissionsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.CHANGE_SETTINGS)) {
            return;
        }
        setContentView(R.layout.activity_role_permissions);
        TopBarHelper.setup(this, R.string.role_permissions_title);

        layoutRolePermissionsTable = findViewById(R.id.layoutRolePermissionsTable);
        addHeaderRow();
        for (Permission permission : Permission.values()) {
            addPermissionRow(permission);
        }
    }

    private void addHeaderRow() {
        LinearLayout rowLayout = makeRowLayout(true);
        rowLayout.addView(makeCellText(getString(R.string.role_permissions_action), 2f, true, Gravity.START));
        rowLayout.addView(makeCellText(getString(R.string.role_permissions_admin), 1f, true, Gravity.CENTER));
        rowLayout.addView(makeCellText(getString(R.string.role_permissions_officer), 1f, true, Gravity.CENTER));
        rowLayout.addView(makeCellText(getString(R.string.role_permissions_staff), 1f, true, Gravity.CENTER));
        rowLayout.addView(makeCellText(getString(R.string.role_permissions_community), 1f, true, Gravity.CENTER));
        layoutRolePermissionsTable.addView(rowLayout);
    }

    private void addPermissionRow(Permission permission) {
        LinearLayout rowLayout = makeRowLayout(false);
        rowLayout.addView(makeCellText(permission.getLabel(), 2f, false, Gravity.START));

        for (UserRole role : TABLE_ROLES) {
            if (role.can(permission)) {
                rowLayout.addView(makeTickCell());
            } else {
                rowLayout.addView(makeCellText("–", 1f, false, Gravity.CENTER));
            }
        }

        layoutRolePermissionsTable.addView(rowLayout);
        // thin line under each row
        View lineView = new View(this);
        lineView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
        lineView.setBackgroundColor(ContextCompat.getColor(this, R.color.color_border));
        layoutRolePermissionsTable.addView(lineView);
    }

    private LinearLayout makeRowLayout(boolean isHeader) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        rowLayout.setPadding(dp(12), dp(12), dp(12), dp(12));
        if (isHeader) {
            rowLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_soft));
        }
        return rowLayout;
    }

    private TextView makeCellText(String text, float weight, boolean isBold, int gravity) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        textView.setText(text);
        textView.setGravity(gravity);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, isBold ? 12 : 13);
        textView.setTextColor(ContextCompat.getColor(this, isBold ? R.color.color_primary : R.color.color_text_primary));
        if (isBold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private View makeTickCell() {
        LinearLayout cellLayout = new LinearLayout(this);
        cellLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cellLayout.setGravity(Gravity.CENTER);

        ImageView imageTick = new ImageView(this);
        imageTick.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        imageTick.setImageResource(R.drawable.ic_check);
        imageTick.setColorFilter(ContextCompat.getColor(this, R.color.color_success));
        cellLayout.addView(imageTick);
        return cellLayout;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
