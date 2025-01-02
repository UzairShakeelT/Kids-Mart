package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class OrderDetailsActivity extends AppCompatActivity {
    private TextView tvOrderID, orderDate, trackingStatus, mobileNum, paymentMethod, address, orderTotal, estDelivery;
    private RecyclerView detailProduct;
    private OrderDetailsAdapter orderDetailsAdapter;
    private DatabaseReference mDatabase;
    private Button btnReview, btnCancelOrder;
    private List<OrderedProduct> orderedProductsList;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);

        tvOrderID = findViewById(R.id.tvOrderID);
        orderDate = findViewById(R.id.orderDate);
        trackingStatus = findViewById(R.id.trackingStatus);
        mobileNum = findViewById(R.id.MobileNum);
        paymentMethod = findViewById(R.id.paymentMethod);
        address = findViewById(R.id.address);
        orderTotal = findViewById(R.id.orderTotal);
        detailProduct = findViewById(R.id.detailProduct);
        btnReview = findViewById(R.id.btnReview);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        estDelivery = findViewById(R.id.estDelivery);

        detailProduct.setLayoutManager(new LinearLayoutManager(this));
        orderedProductsList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        orderId = intent.getStringExtra("order_id");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvOrderID.setText("OrderID: " + orderId);
        fetchOrderDetails(orderId, currentUserId);

        btnReview.setOnClickListener(v -> {
            if (!orderedProductsList.isEmpty()) {
                OrderedProduct orderedProduct = orderedProductsList.get(0);
                Intent reviewIntent = new Intent(OrderDetailsActivity.this, ReviewActivity.class);
                reviewIntent.putParcelableArrayListExtra("orderedProducts", new ArrayList<>(orderedProductsList));
                reviewIntent.putExtra("orderId", orderId);
                startActivity(reviewIntent);
            }
        });

        btnCancelOrder.setOnClickListener(v -> cancelOrder(orderId));
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

                    long minDeliveryMillis = createdDateMillis + 3 * 24 * 60 * 60 * 1000;
                    long maxDeliveryMillis = createdDateMillis + 5 * 24 * 60 * 60 * 1000;
                    String minEstimatedDeliveryDate = sdf.format(new Date(minDeliveryMillis));
                    String maxEstimatedDeliveryDate = sdf.format(new Date(maxDeliveryMillis));

                    mobileNum.setText(mobile);
                    paymentMethod.setText(paymentMethodValue);
                    address.setText(addressValue);
                    orderDate.setText(formattedDate);
                    trackingStatus.setText(status);
                    orderTotal.setText(String.format("Rs. %s", totalAmountValue));
                    estDelivery.setText(minEstimatedDeliveryDate + " - " + maxEstimatedDeliveryDate);

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
                Toast.makeText(OrderDetailsActivity.this, "Error fetching order details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkButtonVisibility(String status, String userId) {
        btnReview.setVisibility(View.GONE);
        if ("Delivered".equalsIgnoreCase(status)) {
            btnCancelOrder.setVisibility(View.GONE);
            checkExistingReviews(userId);
        } else if ("Shipped".equalsIgnoreCase(status)) {
            btnCancelOrder.setVisibility(View.GONE);
        } else {
            btnCancelOrder.setVisibility(View.VISIBLE);
        }
    }

    private void checkExistingReviews(String userId) {
        for (OrderedProduct orderedProduct : orderedProductsList) {
            mDatabase.child("reviews").orderByChild("productId")
                    .equalTo(orderedProduct.getProductId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean reviewExists = false;
                            for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                                String reviewUserId = reviewSnapshot.child("username").getValue(String.class);
                                String reviewOrderId = reviewSnapshot.child("orderId").getValue(String.class);

                                if (reviewUserId != null && reviewUserId.equals(userId) && reviewOrderId != null && reviewOrderId.equals(orderId)) {
                                    reviewExists = true;
                                    break;
                                }
                            }
                            if (reviewExists) {
                                btnReview.setVisibility(View.GONE);
                            } else {
                                btnReview.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }
    }

    private void cancelOrder(String orderId) {
        mDatabase.child("orders").child(orderId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OrderDetailsActivity.this, "Order canceled successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OrderDetailsActivity.this, OrdersActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OrderDetailsActivity.this, "Failed to cancel order.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String orderId = getIntent().getStringExtra("order_id");
        fetchOrderDetails(orderId, currentUserId);
    }

}