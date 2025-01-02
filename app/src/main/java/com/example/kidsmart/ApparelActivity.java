package com.example.kidsmart;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApparelActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private DatabaseReference databaseReference;
    private SearchView searchView;
    private Spinner filterColorSpinner;
    private EditText minPriceEditText;
    private EditText maxPriceEditText;
    private TextView btnresetFilter;
    private Button applyFiltersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apparel);

        recyclerView = findViewById(R.id.recyclerSearchApparel);
        searchView = findViewById(R.id.searchApparelView);
        filterColorSpinner = findViewById(R.id.filterColorApparel);
        minPriceEditText = findViewById(R.id.minPriceApparel);
        maxPriceEditText = findViewById(R.id.maxPriceApparel);
        btnresetFilter = findViewById(R.id.resetFilters);
        applyFiltersButton = findViewById(R.id.applyFiltersButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, filteredList);
        recyclerView.setAdapter(productAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        loadApparelProducts();
        btnresetFilter.setOnClickListener(v -> resetFilters());
        applyFiltersButton.setOnClickListener(v -> applyFilter(searchView.getQuery().toString()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query.trim());
                resetFilters();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText.trim());
                return false;
            }
        });
    }

    private void loadApparelProducts() {
        databaseReference.orderByChild("category").equalTo("Apparel")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        productList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Product product = snapshot.getValue(Product.class);
                            if (product != null) {
                                productList.add(product);
                            }
                        }
                        filterProducts(searchView.getQuery().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ApparelActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetFilters() {
        filterColorSpinner.setSelection(0);
        minPriceEditText.setText("");
        maxPriceEditText.setText("");
        String currentQuery = searchView.getQuery().toString();
        filterProducts(currentQuery);
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void applyFilter(String query) {
        filteredList.clear();

        String minPriceText = minPriceEditText.getText().toString();
        String maxPriceText = maxPriceEditText.getText().toString();
        String selectedColor = filterColorSpinner.getSelectedItem() != null ? filterColorSpinner.getSelectedItem().toString() : "All";

        float minPrice = TextUtils.isEmpty(minPriceText) ? 0 : Float.parseFloat(minPriceText);
        float maxPrice = TextUtils.isEmpty(maxPriceText) ? Float.MAX_VALUE : Float.parseFloat(maxPriceText);

        for (Product product : productList) {
            boolean matchesQuery = TextUtils.isEmpty(query) || product.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesPrice = true;
            boolean matchesColor = true;
            boolean matchesAvailability = true;

            try {
                float productPrice = Float.parseFloat(product.getPrice());
                matchesPrice = productPrice >= minPrice && productPrice <= maxPrice;
            } catch (NumberFormatException e) {
                matchesPrice = false;
            }

            if (!selectedColor.equals("Color")) {
                matchesColor = product.getColor().equalsIgnoreCase(selectedColor);
            }

            if (matchesQuery && matchesPrice && matchesColor && matchesAvailability) {
                filteredList.add(product);
            }
        }

        productAdapter.notifyDataSetChanged();
    }

    private void applyFilters() {
        filterProducts(searchView.getQuery().toString());
    }
}