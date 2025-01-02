package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuyNowCheckoutActivity extends AppCompatActivity {
    private EditText phoneNum, checkAddress;
    private DatabaseReference mDatabase;
    private Spinner paymentSpinner;
    private List<OrderedProduct> orderedProducts = new ArrayList<>();
    private ChecklistBuyNowAdapter checklistAdapter;
    private TextView checkName, subtotalTextView, totalTextView, totalTextView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy_now_checkout);

        phoneNum = findViewById(R.id.phoneNum);
        checkAddress = findViewById(R.id.checkAddress);
        Button btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        paymentSpinner = findViewById(R.id.paymentSpinner);
        RecyclerView recyclerView = findViewById(R.id.checkList);
        checkName = findViewById(R.id.checkName);
        subtotalTextView = findViewById(R.id.subtotal);
        totalTextView = findViewById(R.id.total);
        totalTextView2 = findViewById(R.id.total2);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checklistAdapter = new ChecklistBuyNowAdapter(orderedProducts);
        recyclerView.setAdapter(checklistAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentSpinner.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchUserName();

        String productId = getIntent().getStringExtra("productId");
        String productName = getIntent().getStringExtra("productName");
        String productPrice = getIntent().getStringExtra("productPrice");
        String selectedSize = getIntent().getStringExtra("selectedSize");
        String productImageUrl = getIntent().getStringExtra("productImageUrl");

        if (productId != null && productName != null && productPrice != null) {
            OrderedProduct orderedProduct = new OrderedProduct(productId, productName, productPrice, selectedSize, 1, productImageUrl);
            orderedProducts.add(orderedProduct);
            checklistAdapter.notifyDataSetChanged();
            calculateAmounts();
        }

        btnPlaceOrder.setOnClickListener(v -> placeOrder(productId, productName, productPrice, selectedSize, productImageUrl));
    }

    private void calculateAmounts() {
        double subtotal = 0.0;
        for (OrderedProduct product : orderedProducts) {
            subtotal += Double.parseDouble(product.getProductPrice());
        }
        subtotalTextView.setText(String.format("Rs. %.2f", subtotal));

        double total = subtotal + 200;
        totalTextView.setText(String.format("Rs. %.2f", total));
        totalTextView2.setText(String.format("Rs. %.2f", total));
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
                    Toast.makeText(BuyNowCheckoutActivity.this, "User data does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuyNowCheckoutActivity.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void placeOrder(String productId, String productName, String productPrice, String selectedSize, String productImageUrl) {
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

        List<OrderedProduct> orderedProductsList = new ArrayList<>();
        for (OrderedProduct product : orderedProducts) {
            orderedProductsList.add(new OrderedProduct(
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    product.getSelectedSize(),
                    product.getQuantity(),
                    product.getImageUrl()
            ));
        }

        double totalAmount = calculateTotalAmount();
        String initialStatus = "Confirmed";
        long createdDate = System.currentTimeMillis();
        Order order = new Order(userId, orderedProductsList, mobile, address, paymentMethod, totalAmount, initialStatus, createdDate);

        mDatabase.child("orders").child(orderId).setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BuyNowCheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        orderedProducts.clear();
                        checklistAdapter.notifyDataSetChanged();
                        Intent intent = new Intent(BuyNowCheckoutActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(BuyNowCheckoutActivity.this, "Failed to place order.", Toast.LENGTH_SHORT).show();
                    }
                });

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDatabase.child("users").child(userId).child("mobile").setValue(mobile);
                    mDatabase.child("users").child(userId).child("address").setValue(address)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(BuyNowCheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(BuyNowCheckoutActivity.this, "Failed to update user information.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(BuyNowCheckoutActivity.this, "User data does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuyNowCheckoutActivity.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateTotalAmount() {
        double subtotal = 0.0;
        for (OrderedProduct product : orderedProducts) {
            subtotal += Double.parseDouble(product.getProductPrice()) * product.getQuantity();
        }
        return subtotal + 200;
    }

    public void updateTotal() {
        double subtotal = 0.0;
        for (OrderedProduct product : orderedProducts) {
            subtotal += Double.parseDouble(product.getProductPrice()) * product.getQuantity();
        }
        subtotalTextView.setText(String.format("Rs. %.2f", subtotal));

        double total = subtotal + 200;
        totalTextView.setText(String.format("Rs. %.2f", total));
        totalTextView2.setText(String.format("Rs. %.2f", total));
    }

    public void updateTotal(double amountToSubtract) {
        double currentSubtotal = calculateTotalAmount();
        double newSubtotal = currentSubtotal - amountToSubtract;

        if (newSubtotal < 0) {
            newSubtotal = 0;
        }

        subtotalTextView.setText(String.format("Rs. %.2f", newSubtotal));

        if (orderedProducts.isEmpty()) {
            totalTextView.setText("Rs. 0.00");
            totalTextView2.setText("Rs. 0.00");
        } else {
            double total = newSubtotal + 200;
            totalTextView.setText(String.format("Rs. %.2f", total));
            totalTextView2.setText(String.format("Rs. %.2f", total));
        }
    }
}