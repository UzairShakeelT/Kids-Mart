package com.example.kidsmart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartList;
    private TextView totalAmount, totalAmount2;
    private Button checkoutButton;
    private CartListAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private DatabaseReference cartRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartList = findViewById(R.id.cartList);
        totalAmount = findViewById(R.id.total3);
        totalAmount2 = findViewById(R.id.total4);
        checkoutButton = findViewById(R.id.checkout);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cartRef = FirebaseDatabase.getInstance().getReference("carts").child(currentUser.getUid());
            loadCartItems();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        checkoutButton.setOnClickListener(v -> handleCheckout());
    }

    private void loadCartItems() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItems.add(cartItem);
                    }
                }
                setupRecyclerView();
                calculateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CartListAdapter(this, cartItems);
        cartList.setLayoutManager(new LinearLayoutManager(this));
        cartList.setAdapter(adapter);
    }

    private void calculateTotal() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += Double.parseDouble(item.getProductPrice()) * item.getQuantity();
        }
        totalAmount.setText(String.format("Rs. %.2f", total));
        totalAmount2.setText(String.format("Rs. %.2f", total));
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CartActivity.this, CartCheckoutActivity.class);
        double totalAmountValue = calculateTotalAmount();
        intent.putExtra("totalAmount", totalAmountValue);
        intent.putExtra("cartSize", cartItems.size());

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            intent.putExtra("productId" + i, item.getProductId());
            intent.putExtra("productName" + i, item.getProductName());
            intent.putExtra("productPrice" + i, item.getProductPrice());
            intent.putExtra("selectedSize" + i, item.getSize());
            intent.putExtra("productImageUrl" + i, item.getImageUrl());
            intent.putExtra("quantity" + i, item.getQuantity());
        }

        startActivity(intent);
    }

    private double calculateTotalAmount() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += Double.parseDouble(item.getProductPrice()) * item.getQuantity();
        }
        return total;
    }
}