package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartCheckoutActivity extends AppCompatActivity {
    private EditText phoneNum, checkAddress;
    private DatabaseReference mDatabase;
    private Spinner paymentSpinner;
    private List<CartItem> cartItems = new ArrayList<>();
    private CheckListCartAdapter checklistAdapter;
    private TextView checkName, subtotalTextView, total1, total2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart_checkout);

        phoneNum = findViewById(R.id.phoneNum);
        checkAddress = findViewById(R.id.checkAddress);
        Button btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        paymentSpinner = findViewById(R.id.paymentSpinner);
        RecyclerView recyclerView = findViewById(R.id.checkList);
        checkName = findViewById(R.id.checkName);
        subtotalTextView = findViewById(R.id.subtotal);
        total1 = findViewById(R.id.total);
        total2 = findViewById(R.id.total2);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checklistAdapter = new CheckListCartAdapter(this, cartItems);
        recyclerView.setAdapter(checklistAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentSpinner.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchUserName();

        retrieveCartItems();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void fetchUserName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    if (userName != null) {
                        checkName.setText(userName);
                    }
                } else {
                    Toast.makeText(CartCheckoutActivity.this, "User data does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CartCheckoutActivity.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveCartItems() {
        int cartSize = getIntent().getIntExtra("cartSize", 0);
        for (int i = 0; i < cartSize; i++) {
            CartItem item = new CartItem(
                    getIntent().getStringExtra("productId" + i),
                    getIntent().getStringExtra("productName" + i),
                    getIntent().getStringExtra("productPrice" + i),
                    getIntent().getIntExtra("quantity" + i, 1),
                    getIntent().getStringExtra("selectedSize" + i),
                    getIntent().getStringExtra("productImageUrl" + i)
            );
            cartItems.add(item);
        }
        checklistAdapter.notifyDataSetChanged();
        calculateAmounts();
    }

    private void calculateAmounts() {
        double subtotal = 0.0;
        for (CartItem product : cartItems) {
            subtotal += Double.parseDouble(product.getProductPrice()) * product.getQuantity();
        }
        subtotalTextView.setText(String.format("Rs. %.2f", subtotal));

        double total = subtotal + 200;
        total1.setText(String.format("Rs. %.2f", total));
        total2.setText(String.format("Rs. %.2f", total));
    }

    private void placeOrder() {
        String mobile = phoneNum.getText().toString().trim();
        String address = checkAddress.getText().toString().trim();
        String paymentMethod = paymentSpinner.getSelectedItem().toString();

        if (mobile.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please provide mobile number and address.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (paymentMethod.equals("Select Payment")) {
            Toast.makeText(this, "Please choose a payment option.", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = mDatabase.child("orders").push().getKey();
        if (orderId == null) {
            Toast.makeText(this, "Failed to generate order ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        double totalAmount = calculateTotalAmount();

        List<OrderedProduct> orderedProductsList = new ArrayList<>();
        for (CartItem product : cartItems) {
            orderedProductsList.add(new OrderedProduct(
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    product.getSize(),
                    product.getQuantity(),
                    product.getImageUrl()
            ));
        }

        String initialStatus = "Confirmed";
        long createdDate = System.currentTimeMillis();
        Order order = new Order(userId, orderedProductsList, mobile, address, paymentMethod, totalAmount, initialStatus, createdDate);

        mDatabase.child("orders").child(orderId).setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clearCart();
                        Toast.makeText(CartCheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        cartItems.clear();
                        checklistAdapter.notifyDataSetChanged();
                        Intent intent = new Intent(CartCheckoutActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CartCheckoutActivity.this, "Failed to place order.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private double calculateTotalAmount() {
        double subtotal = 0.0;
        for (CartItem product : cartItems) {
            subtotal += Double.parseDouble(product.getProductPrice()) * product.getQuantity();
        }
        return subtotal + 200;
    }

    private void clearCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("carts").child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartItems.clear();
                        checklistAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(CartCheckoutActivity.this, "Failed to clear cart.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}