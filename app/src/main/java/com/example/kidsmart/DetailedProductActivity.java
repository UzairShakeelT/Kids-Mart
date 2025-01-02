package com.example.kidsmart;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailedProductActivity extends AppCompatActivity {
    private Product product;
    private ImageView imgDetail;
    ImageButton cart;
    private TextView productNameDetail, productPriceDetail, productDescriptionDetail, reviewsTextView, colorTextView, categoryTextView;
    private RatingBar detailRatingBar;
    private RecyclerView reviewRecyclerView;
    private DatabaseReference databaseReference;
    private DatabaseReference reviewsDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private String productId;
    private ProgressBar progressBar;
    private Button btnBuyNow, btnAddToCart;
    private RadioGroup sizeRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detailed_product);

        imgDetail = findViewById(R.id.imgDetail);
        productNameDetail = findViewById(R.id.productNameDetail);
        productPriceDetail = findViewById(R.id.productPriceDetail);
        productDescriptionDetail = findViewById(R.id.productDescriptionDetail);
        reviewsTextView = findViewById(R.id.reviewsTextView);
        detailRatingBar = findViewById(R.id.detailRatingBar);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);
        progressBar = findViewById(R.id.detailProgressBar);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        sizeRadioGroup = findViewById(R.id.sizeRadioGroup);
        colorTextView = findViewById(R.id.color);
        categoryTextView = findViewById(R.id.category);
        cart = findViewById(R.id.iconCart);

        productId = getIntent().getStringExtra("productId");
        databaseReference = FirebaseDatabase.getInstance().getReference("products").child(productId);
        reviewsDatabaseReference = FirebaseDatabase.getInstance().getReference("reviews");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        loadProductDetails();

        btnBuyNow.setOnClickListener(v -> handleBuyNowClick());
        btnAddToCart.setOnClickListener(view -> handleAddToCartClick());

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailedProductActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadProductDetails() {
        if (productId == null) {
            Toast.makeText(this, "Product ID is null.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        populateProductDetails(product);
                        loadProductReviews(productId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetailedProductActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateProductDetails(Product product) {
        this.product = product;
        productNameDetail.setText(product.getName());
        productPriceDetail.setText("Rs. " + product.getPrice());
        productDescriptionDetail.setText(product.getDescription());
        colorTextView.setText(product.getColor());
        categoryTextView.setText(product.getCategory());

        if ("Apparel".equalsIgnoreCase(product.getCategory()) || "Footwear".equalsIgnoreCase(product.getCategory())) {
            sizeRadioGroup.setVisibility(View.VISIBLE);
            findViewById(R.id.tvSize).setVisibility(View.VISIBLE);
        } else {
            sizeRadioGroup.setVisibility(View.GONE);
            findViewById(R.id.tvSize).setVisibility(View.GONE);
        }

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.upload_img)
                .into(imgDetail);

        detailRatingBar.setRating(product.getRating());
    }

    private void loadProductReviews(String productId) {
        reviewsDatabaseReference.orderByChild("productId").equalTo(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                            Review review = reviewSnapshot.getValue(Review.class);
                            if (review != null) {
                                reviews.add(review);
                            }
                        }
                        fetchUsernamesForReviews(reviews);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailedProductActivity.this, "Error loading reviews", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUsernamesForReviews(List<Review> reviews) {
        HashMap<String, String> userIdToUsernameMap = new HashMap<>();
        for (Review review : reviews) {
            userIdToUsernameMap.put(review.getUsername(), null);
        }

        usersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String userId : userIdToUsernameMap.keySet()) {
                    String username = snapshot.child(userId).child("name").getValue(String.class);
                    userIdToUsernameMap.put(userId, username);
                }

                for (Review review : reviews) {
                    String username = userIdToUsernameMap.get(review.getUsername());
                    review.setUsername(username);
                }

                setupReviewsRecyclerView(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailedProductActivity.this, "Error loading usernames", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupReviewsRecyclerView(List<Review> reviews) {
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ReviewAdapter reviewAdapter = new ReviewAdapter(reviews);
        reviewRecyclerView.setAdapter(reviewAdapter);

        if (reviews.isEmpty()) {
            reviewsTextView.setText("0.0 (0)");
        } else {
            float averageRating = calculateAverageRating(reviews);
            reviewsTextView.setText(String.format("%.1f (%d)", averageRating, reviews.size()));
            detailRatingBar.setRating(averageRating);
        }
    }

    private float calculateAverageRating(List<Review> reviews) {
        float total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }
        return total / reviews.size();
    }

    private String getSelectedSize() {
        for (int i = 0; i < sizeRadioGroup.getChildCount(); i++) {
            View child = sizeRadioGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                if (radioButton.isChecked()) {
                    return radioButton.getText().toString();
                }
            }
        }
        return null;
    }

    private void handleBuyNowClick() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String selectedSize = getSelectedSize();
            int quantity = Integer.parseInt(product.getQuantity());
            if (quantity > 0) {
                if (selectedSize != null) {
                    proceedToCheckout(product, selectedSize, 1);
                } else if ("Toys".equalsIgnoreCase(product.getCategory()) || "Books".equalsIgnoreCase(product.getCategory())) {
                    proceedToCheckout(product, "Standard", 1);
                } else {
                    Toast.makeText(this, "Please select a size.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Product is out of stock.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent intent = new Intent(DetailedProductActivity.this, LoginActivity.class);
            intent.putExtra("redirectTo", "checkout");
            intent.putExtra("productId", productId);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("selectedSize", "Standard");
            intent.putExtra("quantity", 1);
            intent.putExtra("productImageUrl", product.getImageUrl());
            startActivity(intent);
        }
    }


    private void proceedToCheckout(Product product, String selectedSize, int quantity) {
        Intent intent = new Intent(DetailedProductActivity.this, BuyNowCheckoutActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("productName", product.getName());
        intent.putExtra("productPrice", product.getPrice());
        intent.putExtra("selectedSize", selectedSize);
        intent.putExtra("quantity", quantity);
        intent.putExtra("productImageUrl", product.getImageUrl());
        startActivity(intent);
    }

    private void handleAddToCartClick() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String selectedSize = getSelectedSize();
            int quantity = Integer.parseInt(product.getQuantity());
            if (quantity > 0) {
                if (selectedSize != null) {
                    addToCart(product, selectedSize);
                } else if ("Toys".equalsIgnoreCase(product.getCategory()) || "Books".equalsIgnoreCase(product.getCategory())) {
                    addToCart(product, "Standard");
                } else {
                    Toast.makeText(this, "Please select a size.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Product is out of stock.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent intent = new Intent(DetailedProductActivity.this, LoginActivity.class);
            intent.putExtra("redirectTo", "cart");
            intent.putExtra("productId", productId);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("selectedSize", "Standard");
            intent.putExtra("quantity", 1);
            intent.putExtra("productImageUrl", product.getImageUrl());
            startActivity(intent);
        }
    }


    private void addToCart(Product product, String selectedSize) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(currentUser.getUid());

            String cartItemKey = product.getId() + "_" + selectedSize;

            cartRef.child(cartItemKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CartItem cartItem;
                    if (snapshot.exists()) {
                        cartItem = snapshot.getValue(CartItem.class);
                        if (cartItem != null) {
                            cartItem.incrementQuantity();
                            cartRef.child(cartItemKey).setValue(cartItem);
                            Toast.makeText(DetailedProductActivity.this, "Product quantity updated.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cartItem = new CartItem(product.getId(), product.getName(), product.getPrice(), 1, selectedSize, product.getImageUrl());
                        cartRef.child(cartItemKey).setValue(cartItem)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(DetailedProductActivity.this, "Product added to cart.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(DetailedProductActivity.this, "Failed to add product to cart.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailedProductActivity.this, "Failed to add product to cart.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}