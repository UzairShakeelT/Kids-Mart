package com.example.kidsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CheckListCartAdapter extends RecyclerView.Adapter<CheckListCartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;

    public CheckListCartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_cart_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.recName.setText(item.getProductName());
        holder.recSize.setText("Size: " + item.getSize());
        holder.recPrice.setText(String.format("Rs. %.2f", Double.parseDouble(item.getProductPrice())));
        holder.tvQuantity.setText("Rs. " + item.getQuantity());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.error_image)
                .into(holder.recImage);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
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

