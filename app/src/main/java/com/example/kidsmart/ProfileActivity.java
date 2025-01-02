package com.example.kidsmart;

import static android.preference.PreferenceManager.setDefaultValues;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileActivity extends AppCompatActivity {
    private TextView accName, accEmail, accMobile, accAddress, accGender, accBirth, resetPass;
    private ImageView btnEdit;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        accName = findViewById(R.id.accName);
        accEmail = findViewById(R.id.accEmail);
        accMobile = findViewById(R.id.accMobile);
        accAddress = findViewById(R.id.accAddress);
        accGender = findViewById(R.id.accGender);
        accBirth = findViewById(R.id.accBirth);
        resetPass = findViewById(R.id.accPass);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadUserProfile();
        } else {
            Toast.makeText(this, "You need to be logged in to view your profile.", Toast.LENGTH_SHORT).show();
            finish();
        }
        resetPass.setOnClickListener(v -> resetPassword());

        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User userProfile = dataSnapshot.getValue(User.class);
                        if (userProfile != null) {
                            accName.setText(userProfile.getName() != null ? userProfile.getName() : "Not set");
                            accEmail.setText(userProfile.getEmail() != null ? userProfile.getEmail() : "Not set");
                            accMobile.setText(userProfile.getMobile() != null ? userProfile.getMobile() : "Not set");
                            accAddress.setText(userProfile.getAddress() != null ? userProfile.getAddress() : "Not set");
                            accGender.setText(userProfile.getGender() != null ? userProfile.getGender() : "Not set");
                            accBirth.setText(userProfile.getDateOfBirth() != null ? userProfile.getDateOfBirth() : "Not set");
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        setDefaultValues();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        accName.setText("Not set");
        accEmail.setText("Not set");
        accMobile.setText("Not set");
        accAddress.setText("Not set");
        accGender.setText("Not set");
        accBirth.setText("Not set");
    }

    private void resetPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String emailAddress = user.getEmail();
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Reset password email sent to " + emailAddress, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
}