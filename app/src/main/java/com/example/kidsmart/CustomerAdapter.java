package com.example.kidsmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private List<User> customerList;

    public CustomerAdapter(List<User> customerList) {
        this.customerList = customerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = customerList.get(position);

        holder.customerName.setText(user.getName());
        holder.customerEmail.setText("Email: " + user.getEmail());
        holder.customerMobile.setText("Mobile No: " + (user.getMobile().equals("Not set") ? "Not Set" : user.getMobile()));
        holder.customerAddress.setText("Address: " + (user.getAddress().equals("Not set") ? "Not Set" : user.getAddress()));
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, customerEmail, customerMobile, customerAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerName);
            customerEmail = itemView.findViewById(R.id.customerEmail);
            customerMobile = itemView.findViewById(R.id.customerMobile);
            customerAddress = itemView.findViewById(R.id.customerAddress);
        }
    }
}
