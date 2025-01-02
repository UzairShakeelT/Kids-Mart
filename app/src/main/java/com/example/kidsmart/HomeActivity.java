package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private DatabaseReference databaseReference;
    private ImageButton imgApparel, imgToys, imgBooks, imgFootwear;
    private ImageView cart;
    private TextView apparel, toys, books, footwear;
    private ProgressBar progressBar;
    private ImageView menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        SearchView searchView = findViewById(R.id.searchHome);
        imgApparel = findViewById(R.id.imgApparel);
        imgBooks = findViewById(R.id.imgBooks);
        imgToys = findViewById(R.id.imgToys);
        imgFootwear = findViewById(R.id.imgFootWear);
        cart = findViewById(R.id.imgCartHome);
        apparel = findViewById(R.id.tvApparel);
        toys = findViewById(R.id.tvToys);
        books = findViewById(R.id.tvBooks);
        footwear = findViewById(R.id.tvFootwear);
        progressBar = findViewById(R.id.progressBar);
        menuButton = findViewById(R.id.menu);

        recyclerView = findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        setupCategoryButtons();
        cart.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));

        loadProducts();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
                    intent.putExtra("query", query);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "Search query is empty.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                        fetchReviewsForProduct(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReviewsForProduct(Product product) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");

        reviewsRef.orderByChild("productId").equalTo(product.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        float totalRating = 0;
                        int count = 0;
                        for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                            Review review = reviewSnapshot.getValue(Review.class);
                            if (review != null) {
                                totalRating += review.getRating();
                                count++;
                                product.getReviews().add(review);
                            }
                        }
                        float averageRating = count > 0 ? totalRating / count : 0;
                        product.setRating(averageRating);
                        productAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
    }

    private void setupCategoryButtons() {
        imgApparel.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ApparelActivity.class)));
        imgBooks.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, BooksActivity.class)));
        imgToys.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ToysActivity.class)));
        imgFootwear.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, FootwearActivity.class)));

        apparel.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ApparelActivity.class)));
        books.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, BooksActivity.class)));
        toys.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ToysActivity.class)));
        footwear.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, FootwearActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SearchView searchView = findViewById(R.id.searchHome);
        searchView.clearFocus();

        for (Product product : productList) {
            fetchReviewsForProduct(product);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
