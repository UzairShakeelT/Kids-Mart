package com.example.kidsmart;

public class Review {
    private String username;
    private String productId;
    private String orderId;
    private float rating;
    private String text;

    public Review() {
    }

    public Review(String username, String productId, String orderId, float rating, String text) {
        this.username = username;
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
