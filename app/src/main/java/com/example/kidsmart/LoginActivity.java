package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private TextView register;
    private View progressBar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etEmail = findViewById(R.id.logEmail);
        etPassword = findViewById(R.id.logPass);
        btnLogin = findViewById(R.id.btnLogin);
        register = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());

        register.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                            Intent intent = getIntent();
                            String redirectTo = intent.getStringExtra("redirectTo");

                            if ("checkout".equals(redirectTo)) {
                                Intent checkoutIntent = new Intent(LoginActivity.this, BuyNowCheckoutActivity.class);
                                checkoutIntent.putExtra("productId", intent.getStringExtra("productId"));
                                checkoutIntent.putExtra("productName", intent.getStringExtra("productName"));
                                checkoutIntent.putExtra("productPrice", intent.getStringExtra("productPrice"));
                                checkoutIntent.putExtra("selectedSize", intent.getStringExtra("selectedSize"));
                                checkoutIntent.putExtra("quantity", intent.getIntExtra("quantity", 1));
                                checkoutIntent.putExtra("productImageUrl", intent.getStringExtra("productImageUrl"));

                                checkoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(checkoutIntent);
                            } else if ("cart".equals(redirectTo)) {
                                addToCartAfterLogin(intent);
                            } else {
                                checkCurrentUser();
                            }
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    if (dataSnapshot.exists()) {
                        String userRole = dataSnapshot.child("role").getValue(String.class);
                        if ("admin".equals(userRole)) {
                            startActivity(new Intent(LoginActivity.this, AdminPanelActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "User role not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error retrieving user data. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException invalidCredentialsException = (FirebaseAuthInvalidCredentialsException) exception;
            if (invalidCredentialsException.getErrorCode().equals("ERROR_WRONG_PASSWORD")) {
                Toast.makeText(LoginActivity.this, "Incorrect password. Please check your credentials.", Toast.LENGTH_SHORT).show();
            } else if (invalidCredentialsException.getErrorCode().equals("ERROR_INVALID_EMAIL")) {
                Toast.makeText(LoginActivity.this, "Invalid email address format. Please check your email.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
            }
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            FirebaseAuthInvalidUserException invalidUserException = (FirebaseAuthInvalidUserException) exception;
            if (invalidUserException.getErrorCode().equals("ERROR_USER_NOT_FOUND")) {
                Toast.makeText(LoginActivity.this, "User does not exist. Please register first.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToCartAfterLogin(Intent intent) {
        String productId = intent.getStringExtra("productId");
        String productName = intent.getStringExtra("productName");
        String productPrice = intent.getStringExtra("productPrice");
        String selectedSize = intent.getStringExtra("selectedSize");
        int quantity = intent.getIntExtra("quantity", 1);
        String imageUrl = intent.getStringExtra("productImageUrl");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(currentUser.getUid());

        String itemKey = productId + "_" + selectedSize;
        CartItem cartItem = new CartItem(productId, productName, productPrice, quantity, selectedSize, imageUrl);

        cartRef.child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CartItem existingItem = snapshot.getValue(CartItem.class);
                    if (existingItem != null) {
                        int newQuantity = existingItem.getQuantity() + quantity;
                        existingItem.setQuantity(newQuantity);
                        cartRef.child(itemKey).setValue(existingItem).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Quantity updated in cart.", Toast.LENGTH_SHORT).show();
                                navigateToCart();
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed to update quantity.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    CartItem cartItem = new CartItem(productId, productName, productPrice, quantity, selectedSize, imageUrl);
                    cartRef.child(itemKey).setValue(cartItem).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Product added to cart.", Toast.LENGTH_SHORT).show();
                            navigateToCart();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to add product to cart.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error checking cart item.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToCart() {
        Intent cartIntent = new Intent(LoginActivity.this, CartActivity.class);
        cartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cartIntent);
    }
}