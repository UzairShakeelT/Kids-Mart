package com.example.kidsmart;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private String userId;
    private Map<String, CartItem> items;

    public Cart() {
        this.items = new HashMap<>();
    }

    public Cart(String userId) {
        this.userId = userId;
        this.items = new HashMap<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, CartItem> getItems() {
        return items;
    }

    public void setItems(Map<String, CartItem> items) {
        this.items = items;
    }

    public void addItem(CartItem item) {
        items.put(item.getProductId(), item);
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }
}
