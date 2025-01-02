package com.example.kidsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder> {
    private List<CartItem> cartItems;
    private Context context;

    public CartListAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.recName.setText(item.getProductName());
        holder.recSize.setText("Size: " + item.getSize());
        holder.recPrice.setText("Rs. " + item.getProductPrice());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.error_image)
                .into(holder.recImage);

        holder.btnIncrease.setOnClickListener(v -> updateQuantity(item, holder.tvQuantity, 1));
        holder.btnDecrease.setOnClickListener(v -> updateQuantity(item, holder.tvQuantity, -1));
        holder.btnDelete.setOnClickListener(v -> deleteItem(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private void updateQuantity(CartItem item, TextView quantityView, int change) {
        int newQuantity = item.getQuantity() + change;
        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            quantityView.setText(String.valueOf(newQuantity));
            String itemKey = item.getProductId() + "_" + item.getSize();
            DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("carts")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(itemKey);
            itemRef.setValue(item);
        } else {
            deleteItem(item);
        }
    }

    private void deleteItem(CartItem item) {
        String itemKey = item.getProductId() + "_" + item.getSize();
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("carts")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(itemKey);
        itemRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                cartItems.remove(item);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView recName, recSize, recPrice, tvQuantity;
        Button btnDelete, btnDecrease, btnIncrease;
        ImageView recImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recName = itemView.findViewById(R.id.recName);
            recSize = itemView.findViewById(R.id.recSize);
            recPrice = itemView.findViewById(R.id.recPrice);
            recImage = itemView.findViewById(R.id.recImage);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
        }
    }
}