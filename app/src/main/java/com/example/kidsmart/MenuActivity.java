package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    private TextView profile, order, help, about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        profile = findViewById(R.id.tabProfile);
        order = findViewById(R.id.tabOrder);
        help = findViewById(R.id.tabHelp);
        about = findViewById(R.id.tabAbout);

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        order.setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        help.setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        about.setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }
}