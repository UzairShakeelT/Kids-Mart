package com.example.kidsmart;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private String price;
    private String quantity;
    private String category;
    private String color;
    private String imageUrl;
    private float rating;
    private List<Review> reviews;

    public Product() {
        this.reviews = new ArrayList<>();
    }

    public Product(String id, String name, String description, String price, String quantity, String category, String color, String imageUrl, List<Review> reviews) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.color = color;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    public Product(String id, String name, String description, String price, String quantity, String category, String color, String imageUrl) {
        this(id, name, description, price, quantity, category, color, imageUrl, new ArrayList<>());
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}