package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminPanelActivity extends AppCompatActivity {
    private Button inventory, orderManage, customers;
    private ImageView logout;
    private CardView cardViewInventory, cardViewOrderManagement, cardViewCustomers;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panel);

        mAuth = FirebaseAuth.getInstance();

        inventory =(Button) findViewById(R.id.btnInventory);
        orderManage =(Button) findViewById(R.id.btnOrderManagement);
        customers = (Button) findViewById(R.id.btnCustomers);
        cardViewInventory = findViewById(R.id.cardInventory);
        cardViewOrderManagement = findViewById(R.id.cardOrderManagement);
        cardViewCustomers = findViewById(R.id.cardCustomers);
        logout = (ImageView) findViewById(R.id.btnLogout);

        inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(AdminPanelActivity.this, ItemListActivity.class);
                startActivity(intent);
            }
        });

        cardViewInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ItemListActivity.class);
                startActivity(intent);
            }
        });

        orderManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(AdminPanelActivity.this,OrderManagementActivity.class);
                startActivity(intent);
            }
        });

        cardViewOrderManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, OrderManagementActivity.class);
                startActivity(intent);
            }
        });

        customers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPanelActivity.this, CustomerListActivity.class);
                startActivity(intent);
            }
        });

        cardViewCustomers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPanelActivity.this, CustomerListActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminPanelActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finishAffinity();
    }
}