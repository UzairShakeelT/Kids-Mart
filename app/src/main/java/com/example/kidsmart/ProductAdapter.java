package com.example.kidsmart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.homeName.setText(product.getName());
        holder.homePrice.setText("Rs." + product.getPrice());
        holder.itemRatingBar.setRating(product.getRating());

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.error_image)
                .into(holder.homeImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedProductActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("name", product.getName());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("quantity", product.getQuantity());
            intent.putExtra("category", product.getCategory());
            intent.putExtra("color", product.getColor());
            intent.putExtra("imageUrl", product.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView homeName, homePrice;
        public ImageView homeImage;
        public RatingBar itemRatingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            homeName = itemView.findViewById(R.id.homeName);
            homePrice = itemView.findViewById(R.id.homePrice);
            homeImage = itemView.findViewById(R.id.homeImage);
            itemRatingBar = itemView.findViewById(R.id.itemRatingBar);
        }
    }
}
