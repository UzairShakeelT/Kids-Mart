package com.example.kidsmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder> {
    private List<OrderedProduct> orderedProducts;

    public OrderDetailsAdapter(List<OrderedProduct> orderedProducts) {
        this.orderedProducts = orderedProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderedProduct product = orderedProducts.get(position);
        holder.recName.setText(product.getProductName());
        holder.recSize.setText("Size: " + product.getSelectedSize());
        holder.recPrice.setText("Rs. " + product.getProductPrice());
        holder.tvQuantity.setText("Qty: " + product.getQuantity());

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.recImage);
    }

    @Override
    public int getItemCount() {
        return orderedProducts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recName, recSize, recPrice, tvQuantity;
        ImageView recImage;

        ViewHolder(View itemView) {
            super(itemView);
            recName = itemView.findViewById(R.id.recName);
            recSize = itemView.findViewById(R.id.recSize);
            recPrice = itemView.findViewById(R.id.recPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            recImage = itemView.findViewById(R.id.recImage);
        }
    }
}
