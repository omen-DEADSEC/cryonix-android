package com.example.cryonix.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.signup.PermissionsActivity;
import com.google.android.material.card.MaterialCardView;

/**
 * A03 Choose role.
 * Community members go to the sign-up steps (A05 -> A06 -> A07),
 * staff go to the staff sign in (A04).
 */
public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        MaterialCardView cardChooseRoleCommunity = findViewById(R.id.cardChooseRoleCommunity);
        MaterialCardView cardChooseRoleStaff = findViewById(R.id.cardChooseRoleStaff);

        cardChooseRoleCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseRoleActivity.this, PermissionsActivity.class));
            }
        });

        cardChooseRoleStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseRoleActivity.this, StaffSignInActivity.class));
            }
        });
    }
}
