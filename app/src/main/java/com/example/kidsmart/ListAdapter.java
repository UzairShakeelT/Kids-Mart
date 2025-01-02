package com.example.kidsmart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;

    public ListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.recName.setText(product.getName());

        String formattedPrice = "Rs." + product.getPrice();
        holder.recPrice.setText(formattedPrice);

        Glide.with(context)
                .load(product.getImageUrl())
                .into(holder.recImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DeleteProductActivity.class);
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

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView recImage;
        TextView recName, recPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            recImage = itemView.findViewById(R.id.recImage);
            recName = itemView.findViewById(R.id.recName);
            recPrice = itemView.findViewById(R.id.recPrice);
        }
    }

    public void filterList(List<Product> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }
}
