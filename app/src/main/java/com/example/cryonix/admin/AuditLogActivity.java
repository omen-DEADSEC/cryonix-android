package com.example.cryonix.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.AuditEntry;

import java.util.List;

/**
 * Audit log (AC-10). Read only, newest first. Nothing on this screen can edit or delete an entry.
 */
public class AuditLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_AUDIT_LOG)) {
            return;
        }
        setContentView(R.layout.activity_audit_log);
        TopBarHelper.setup(this, R.string.audit_log_title);

        LinearLayout layoutAuditLogList = findViewById(R.id.layoutAuditLogList);
        TextView textAuditLogEmpty = findViewById(R.id.textAuditLogEmpty);

        List<AuditEntry> entryList = FakeDatabase.getAuditEntries();
        textAuditLogEmpty.setVisibility(entryList.isEmpty() ? View.VISIBLE : View.GONE);

        for (AuditEntry entry : entryList) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_audit_entry, layoutAuditLogList, false);
            TextView textAuditAction = rowView.findViewById(R.id.textAuditAction);
            TextView textAuditUser = rowView.findViewById(R.id.textAuditUser);
            TextView textAuditTime = rowView.findViewById(R.id.textAuditTime);

            textAuditAction.setText(entry.getAction());
            textAuditUser.setText(entry.getUserName());
            textAuditTime.setText(entry.getTime());

            layoutAuditLogList.addView(rowView);
        }
    }
}
