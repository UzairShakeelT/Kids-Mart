package com.example.kidsmart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    private List<Order> orders;
    private List<String> orderIds;
    private List<String> orderStatuses;

    public OrdersAdapter(List<Order> orders, List<String> orderIds, List<String> orderStatuses) {
        this.orders = orders;
        this.orderIds = orderIds;
        this.orderStatuses = orderStatuses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        String orderId = orderIds.get(position);
        holder.orderID.setText(orderId);

        String status = orderStatuses.get(position);
        holder.tvStatus.setText(status);

        int totalQuantity = 0;
        for (OrderedProduct product : order.getProducts()) {
            totalQuantity += product.getQuantity();
        }
        holder.tvQuantity.setText("Item: " + totalQuantity);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String date = sdf.format(new Date(order.getCreatedDate()));
        holder.recDate.setText(date);

        holder.recPrice.setText("Rs. " + order.getTotalAmount());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), OrderDetailsActivity.class);
            intent.putExtra("order_id", orderId);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderID, tvStatus, recPrice, recDate, tvQuantity;

        ViewHolder(View itemView) {
            super(itemView);
            orderID = itemView.findViewById(R.id.orderID);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            recPrice = itemView.findViewById(R.id.recPrice);
            recDate = itemView.findViewById(R.id.recDate);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
