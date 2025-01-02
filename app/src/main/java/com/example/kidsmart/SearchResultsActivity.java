package com.example.kidsmart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> allProducts;
    private DatabaseReference databaseReference;
    private SearchView searchView;
    private Spinner filterCategory;
    private Spinner filterColor;
    private EditText minPriceEditText;
    private EditText maxPriceEditText;
    private TextView btnresetFilter;
    private Button applyFiltersButton;
    private TextView searchError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results);

        recyclerView = findViewById(R.id.recyclerSearchResults);
        searchView = findViewById(R.id.searchResultsView);
        filterCategory = findViewById(R.id.filterCategory);
        filterColor = findViewById(R.id.filterColor);
        minPriceEditText = findViewById(R.id.minPrice);
        maxPriceEditText = findViewById(R.id.maxPrice);
        btnresetFilter = findViewById(R.id.resetFilters);
        applyFiltersButton = findViewById(R.id.applyFiltersButton);
        searchError = findViewById(R.id.searchError);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        allProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        Intent intent = getIntent();
        String initialQuery = intent.getStringExtra("query");

        searchView.setQuery(initialQuery, false);
        loadAllProducts(initialQuery);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query.trim());
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        applyFiltersButton.setOnClickListener(v -> applyFilters());
        btnresetFilter.setOnClickListener(v -> resetFilters());
    }

    private void loadAllProducts(String initialQuery) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allProducts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        allProducts.add(product);
                    }
                }
                filterProducts(initialQuery);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SearchResultsActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetFilters() {
        filterCategory.setSelection(0);
        filterColor.setSelection(0);
        minPriceEditText.setText("");
        maxPriceEditText.setText("");
        productList.clear();
        productList.addAll(allProducts);
        productAdapter.notifyDataSetChanged();
    }

    private void filterProducts(String query) {
        productList.clear();

        if (!query.isEmpty()) {
            for (Product product : allProducts) {
                if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                        product.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    productList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
        if (productList.isEmpty()) {
            searchError.setVisibility(View.VISIBLE);
        } else {
            searchError.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void applyFilters() {
        String selectedCategory = filterCategory.getSelectedItem().toString();
        String selectedColor = filterColor.getSelectedItem().toString();
        String minPriceText = minPriceEditText.getText().toString();
        String maxPriceText = maxPriceEditText.getText().toString();
        double minPrice = TextUtils.isEmpty(minPriceText) ? 0 : Double.parseDouble(minPriceText);
        double maxPrice = TextUtils.isEmpty(maxPriceText) ? Double.MAX_VALUE : Double.parseDouble(maxPriceText);

        productList.clear();
        String currentQuery = searchView.getQuery().toString().trim();

        if (currentQuery.isEmpty()) {
            productAdapter.notifyDataSetChanged();
            return;
        }

        for (Product product : allProducts) {
            boolean matches = true;

            if (!currentQuery.isEmpty() && !product.getName().toLowerCase().contains(currentQuery.toLowerCase())) {
                matches = false;
            }

            if (!selectedCategory.equals("Category") && !product.getCategory().equalsIgnoreCase(selectedCategory)) {
                matches = false;
            }

            if (!selectedColor.equals("Color") && !product.getColor().equalsIgnoreCase(selectedColor)) {
                matches = false;
            }

            try {
                double price = Double.parseDouble(product.getPrice());
                if (price < minPrice || price > maxPrice) {
                    matches = false;
                }
            } catch (NumberFormatException e) {
                matches = false;
            }

            if (matches) {
                productList.add(product);
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}