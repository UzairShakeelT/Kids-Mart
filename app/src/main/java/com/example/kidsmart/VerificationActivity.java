package com.example.kidsmart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button btnResendVerificationEmail;
    private TextView tvCountdownTimer;
    private Handler handler;
    private Runnable countdownRunnable;
    private long timeLeftInMillis = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();

        btnResendVerificationEmail = findViewById(R.id.btnResendVerificationEmail);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        btnResendVerificationEmail.setOnClickListener(v -> resendVerificationEmail());

        handler = new Handler();
        startCountdown();
    }
    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        Toast.makeText(VerificationActivity.this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerificationActivity.this, HomeActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(VerificationActivity.this, "Email not verified yet. Please check your inbox and verify your email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(VerificationActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerificationActivity.this, "Email already sent", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void startCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long seconds = timeLeftInMillis / 1000;
                tvCountdownTimer.setText("Resend in: " + seconds + "s");
                timeLeftInMillis -= 1000;
                checkEmailVerification();

                if (timeLeftInMillis >= 0) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(countdownRunnable);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }
}