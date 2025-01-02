package com.example.kidsmart;

import java.util.Objects;

public class CartItem {
    private String productId;
    private String productName;
    private String productPrice;
    private int quantity;
    private String size;
    private String imageUrl;

    public CartItem() {
    }

    public CartItem(String productId, String productName, String productPrice, int quantity, String size, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.size = size;
        this.imageUrl = imageUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem cartItem = (CartItem) o;
        return productId.equals(cartItem.productId) && size.equals(cartItem.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, size);
    }

    public void incrementQuantity() {
        this.quantity++;
    }
}
