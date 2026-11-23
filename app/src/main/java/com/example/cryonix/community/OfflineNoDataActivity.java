package com.example.cryonix.community;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.UserRole;
import com.google.android.material.button.MaterialButton;

/** A35 Offline / error: shown when the phone is offline and no village data was saved yet. */
public class OfflineNoDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_no_data);

        MaterialButton buttonOfflineTryAgain = findViewById(R.id.buttonOfflineTryAgain);
        MaterialButton buttonOfflineSignOut = findViewById(R.id.buttonOfflineSignOut);

        buttonOfflineTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenRouter.openHomeForRole(OfflineNoDataActivity.this, UserRole.COMMUNITY_MEMBER);
            }
        });

        buttonOfflineSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenRouter.signOut(OfflineNoDataActivity.this);
            }
        });
    }
}
