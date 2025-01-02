package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private EditText editName, editMobile, editBirth, accGender, editAddress;
    private Button btnSave;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editMobile = findViewById(R.id.editMobile);
        editBirth = findViewById(R.id.editBirth);
        accGender = findViewById(R.id.accGender);
        editAddress = findViewById(R.id.editAddress);
        btnSave = findViewById(R.id.btnSave);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();

        btnSave.setOnClickListener(view -> {
            saveUserProfile();
        });
    }

    private void loadUserProfile() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        editName.setText(user.getName().equals("Not set") ? "" : user.getName());
                        editMobile.setText(user.getMobile().equals("Not set") ? "" : user.getMobile());
                        editBirth.setText(user.getDateOfBirth().equals("Not set") ? "" : user.getDateOfBirth());
                        accGender.setText(user.getGender().equals("Not set") ? "" : user.getGender());
                        editAddress.setText(user.getAddress().equals("Not set") ? "" : user.getAddress());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String name = editName.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();
        String birthDate = editBirth.getText().toString().trim();
        String gender = accGender.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (!name.isEmpty()) updates.put("name", name);
        if (!mobile.isEmpty()) updates.put("mobile", mobile);
        if (!birthDate.isEmpty()) updates.put("dateOfBirth", birthDate);
        if (!gender.isEmpty()) updates.put("gender", gender);
        if (!address.isEmpty()) updates.put("address", address);

        databaseReference.child(userId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}