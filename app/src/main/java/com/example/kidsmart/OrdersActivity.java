package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView orderList;
    private OrdersAdapter ordersAdapter;
    private List<Order> orders;
    private List<String> orderIds;
    private List<String> orderStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);

        orderList = findViewById(R.id.orderList);
        orders = new ArrayList<>();
        orderIds = new ArrayList<>();
        orderStatuses = new ArrayList<>();

        ordersAdapter = new OrdersAdapter(orders, orderIds, orderStatuses);

        orderList.setLayoutManager(new LinearLayoutManager(this));
        orderList.setAdapter(ordersAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            fetchOrders();
        } else {
            Toast.makeText(this, "You need to be logged in to view your orders.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchOrders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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

                for (int i = 0; i < tempOrders.size(); i++) {
                    for (int j = i + 1; j < tempOrders.size(); j++) {
                        if (tempOrders.get(i).getCreatedDate() < tempOrders.get(j).getCreatedDate()) {
                            Order tempOrder = tempOrders.get(i);
                            tempOrders.set(i, tempOrders.get(j));
                            tempOrders.set(j, tempOrder);

                            String tempId = tempOrderIds.get(i);
                            tempOrderIds.set(i, tempOrderIds.get(j));
                            tempOrderIds.set(j, tempId);
                            String tempStatus = tempOrderStatuses.get(i);
                            tempOrderStatuses.set(i, tempOrderStatuses.get(j));
                            tempOrderStatuses.set(j, tempStatus);
                        }
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
                Toast.makeText(OrdersActivity.this, "Error fetching orders: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

}