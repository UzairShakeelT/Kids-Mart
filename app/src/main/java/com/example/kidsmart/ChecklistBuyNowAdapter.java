package com.example.kidsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChecklistBuyNowAdapter extends RecyclerView.Adapter<ChecklistBuyNowAdapter.ViewHolder> {
    private List<OrderedProduct> orderedProducts;

    public ChecklistBuyNowAdapter(List<OrderedProduct> orderedProducts) {
        this.orderedProducts = orderedProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkout_buy_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderedProduct product = orderedProducts.get(position);
        holder.recName.setText(product.getProductName());
        holder.recPrice.setText("Rs." + product.getProductPrice());
        holder.tvQuantity.setText("Qty: " + product.getQuantity());
        holder.recSize.setText("Size: " + product.getSelectedSize());

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.error_image)
                .into(holder.recImage);

        holder.btnIncrease.setOnClickListener(v -> {
            int quantity = product.getQuantity();
            quantity++;
            product.setQuantity(quantity);
            holder.tvQuantity.setText("Qty: " + quantity);
            notifyItemChanged(position);

            if (holder.itemView.getContext() instanceof BuyNowCheckoutActivity) {
                ((BuyNowCheckoutActivity) holder.itemView.getContext()).updateTotal();
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int quantity = product.getQuantity();
            if (quantity > 1) {
                quantity--;
                product.setQuantity(quantity);
                holder.tvQuantity.setText("Qty: " + quantity);
                notifyItemChanged(position);

                if (holder.itemView.getContext() instanceof BuyNowCheckoutActivity) {
                    ((BuyNowCheckoutActivity) holder.itemView.getContext()).updateTotal();
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            removeItem(position, holder.itemView.getContext());
        });
    }

    private void removeItem(int position, Context context) {
        OrderedProduct removedProduct = orderedProducts.get(position);
        double removedPrice = Double.parseDouble(removedProduct.getProductPrice()) * removedProduct.getQuantity();

        orderedProducts.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, orderedProducts.size());

        if (context instanceof BuyNowCheckoutActivity) {
            ((BuyNowCheckoutActivity) context).updateTotal(removedPrice);
        }

        if (orderedProducts.isEmpty()) {
            ((BuyNowCheckoutActivity) context).finish();
        }
    }

    @Override
    public int getItemCount() {
        return orderedProducts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recName, recPrice, tvQuantity, recSize;
        ImageView recImage;
        View btnIncrease, btnDecrease, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recImage = itemView.findViewById(R.id.recImage);
            recName = itemView.findViewById(R.id.recName);
            recPrice = itemView.findViewById(R.id.recPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            recSize = itemView. findViewById(R.id.recSize);
        }
    }
}

