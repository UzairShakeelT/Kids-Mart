package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.annotation.NonNull;
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

public class OrderProcessingActivity extends AppCompatActivity {
    private TextView tvOrderID, orderDate, trackingStatus, mobileNum, paymentMethod, address, orderTotal, btndeleteOrder;
    private RecyclerView detailProduct;
    private OrderDetailsAdapter orderDetailsAdapter;
    private DatabaseReference mDatabase;
    private Button btnShip, btnDelivery;
    private List<OrderedProduct> orderedProductsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_processing);

        tvOrderID = findViewById(R.id.tvOrderID);
        orderDate = findViewById(R.id.orderDate);
        trackingStatus = findViewById(R.id.trackingStatus);
        mobileNum = findViewById(R.id.MobileNum);
        paymentMethod = findViewById(R.id.paymentMethod);
        address = findViewById(R.id.address);
        orderTotal = findViewById(R.id.orderTotal);
        detailProduct = findViewById(R.id.detailProduct);
        btnShip = findViewById(R.id.btnShip);
        btnDelivery = findViewById(R.id.btnDeliver);
        btndeleteOrder = findViewById(R.id.deleteOrder);

        detailProduct.setLayoutManager(new LinearLayoutManager(this));
        orderedProductsList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        String orderId = intent.getStringExtra("order_id");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvOrderID.setText("OrderID: " + orderId);
        fetchOrderDetails(orderId, currentUserId);

        btnShip.setOnClickListener(v -> updateOrderStatus(orderId, "Shipped"));
        btnDelivery.setOnClickListener(v -> updateOrderStatus(orderId, "Delivered"));
        btndeleteOrder.setOnClickListener(v -> deleteOrder(orderId));
    }

    private void fetchOrderDetails(String orderId, String userId) {
        mDatabase.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    orderedProductsList.clear();
                    String mobile = dataSnapshot.child("mobile").getValue(String.class);
                    String paymentMethodValue = dataSnapshot.child("paymentMethod").getValue(String.class);
                    String addressValue = dataSnapshot.child("address").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);
                    long createdDateMillis = dataSnapshot.child("createdDate").getValue(Long.class);
                    Long totalAmountValueLong = dataSnapshot.child("totalAmount").getValue(Long.class);
                    String totalAmountValue = totalAmountValueLong != null ? String.valueOf(totalAmountValueLong) : "0";
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(createdDateMillis));

                    mobileNum.setText(mobile);
                    paymentMethod.setText(paymentMethodValue);
                    address.setText(addressValue);
                    orderDate.setText(formattedDate);
                    trackingStatus.setText(status);
                    orderTotal.setText(String.format("Rs. %s", totalAmountValue));

                    for (DataSnapshot productSnapshot : dataSnapshot.child("products").getChildren()) {
                        OrderedProduct product = productSnapshot.getValue(OrderedProduct.class);
                        if (product != null) {
                            orderedProductsList.add(product);
                        }
                    }

                    orderDetailsAdapter = new OrderDetailsAdapter(orderedProductsList);
                    detailProduct.setAdapter(orderDetailsAdapter);
                    checkButtonVisibility(status, userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OrderProcessingActivity.this, "Error fetching order details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkButtonVisibility(String status, String userId) {
        if ("Confirmed".equals(status)) {
            btnShip.setVisibility(View.VISIBLE);
            btnDelivery.setVisibility(View.GONE);
        } else if ("Shipped".equals(status)) {
            btnShip.setVisibility(View.GONE);
            btnDelivery.setVisibility(View.VISIBLE);
        } else {
            btnShip.setVisibility(View.GONE);
            btnDelivery.setVisibility(View.GONE);
        }
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        mDatabase.child("orders").child(orderId).child("status").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(OrderProcessingActivity.this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                trackingStatus.setText(newStatus);
                checkButtonVisibility(newStatus, FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else {
                Toast.makeText(OrderProcessingActivity.this, "Failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteOrder(String orderId) {
        mDatabase.child("orders").child(orderId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OrderProcessingActivity.this, "Order deleted successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OrderProcessingActivity.this, OrderManagementActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OrderProcessingActivity.this, "Failed to cancel order.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}