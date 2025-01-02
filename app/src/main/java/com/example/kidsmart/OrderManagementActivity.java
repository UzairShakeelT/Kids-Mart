package com.example.kidsmart;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.List;

public class OrderManagementActivity extends AppCompatActivity {
    private RecyclerView orderList;
    private OrderManagementAdapter ordersAdapter;
    private List<Order> orders;
    private List<String> orderIds;
    private List<String> orderStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_management);

        orderList = findViewById(R.id.orderManagement);
        orders = new ArrayList<>();
        orderIds = new ArrayList<>();
        orderStatuses = new ArrayList<>();

        ordersAdapter = new OrderManagementAdapter(orders, orderIds, orderStatuses);

        orderList.setLayoutManager(new LinearLayoutManager(this));
        orderList.setAdapter(ordersAdapter);

        fetchOrders();
    }

    private void fetchOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Order> tempOrders = new ArrayList<>();
                List<String> tempOrderIds = new ArrayList<>();
                List<String> tempOrderStatuses = new ArrayList<>();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        String orderId = orderSnapshot.getKey();
                        String status = order.getStatus();

                        tempOrders.add(order);
                        tempOrderIds.add(orderId);
                        tempOrderStatuses.add(status);
                    }
                }

                orders.clear();
                orderIds.clear();
                orderStatuses.clear();
                orders.addAll(tempOrders);
                orderIds.addAll(tempOrderIds);
                orderStatuses.addAll(tempOrderStatuses);

                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String errorMessage = databaseError.getMessage();
                Toast.makeText(OrderManagementActivity.this, "Error fetching orders: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrders();
    }
}