package com.example.kidsmart;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class DeleteProductActivity extends AppCompatActivity {
    private TextView productName, productDescription, productPrice, productQuantity, productCategory, productColor;
    private ImageView productImage;
    private Button deleteButton;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String productId;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_product);

        productName = findViewById(R.id.productName);
        productDescription = findViewById(R.id.productDescription);
        productPrice = findViewById(R.id.productPrice);
        productQuantity = findViewById(R.id.productQuantity);
        productCategory = findViewById(R.id.productCategory);
        productColor = findViewById(R.id.productColor);
        productImage = findViewById(R.id.productImage);
        deleteButton = findViewById(R.id.btnDelete);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        String quantity = intent.getStringExtra("quantity");
        String category = intent.getStringExtra("category");
        String color = intent.getStringExtra("color");
        String imageUrl = intent.getStringExtra("imageUrl");

        productName.setText(name);
        productDescription.setText(description);
        productPrice.setText("Price: " + price + " Rs");
        productQuantity.setText("Quantity: " + quantity);
        productCategory.setText("Category: " + category);
        productColor.setText("Color: " + color);

        Glide.with(this)
                .load(imageUrl)
                .into(productImage);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct();
            }
        });
    }
    private void deleteProduct() {
        databaseReference.child(productId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DeleteProductActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(DeleteProductActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}