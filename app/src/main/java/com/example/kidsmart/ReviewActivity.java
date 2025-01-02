package com.example.kidsmart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private RatingBar ratingBar;
    private EditText reviewTextInput;
    private Button submitReviewButton;
    private DatabaseReference reviewsDatabaseReference;
    private List<OrderedProduct> orderedProducts;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);

        ratingBar = findViewById(R.id.ratingBar);
        reviewTextInput = findViewById(R.id.reviewTextInput);
        submitReviewButton = findViewById(R.id.submitReviewButton);

        orderedProducts = getIntent().getParcelableArrayListExtra("orderedProducts");
        orderId = getIntent().getStringExtra("orderId");
        reviewsDatabaseReference = FirebaseDatabase.getInstance().getReference("reviews");

        submitReviewButton.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getUid();
            float rating = ratingBar.getRating();
            String reviewText = reviewTextInput.getText().toString().trim();

            if (rating <= 0) {
                Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please enter a review", Toast.LENGTH_SHORT).show();
                return;
            }
            for (OrderedProduct product : orderedProducts) {
                String reviewId = reviewsDatabaseReference.push().getKey();
                Review review = new Review(username, product.getProductId(), orderId, rating, reviewText);
                reviewsDatabaseReference.child(reviewId).setValue(review)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Reviews submitted!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ReviewActivity.this, "Failed to submit review for product: " + product.getProductId(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            finish();
        }
    }
}
