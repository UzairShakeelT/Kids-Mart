package com.example.kidsmart;

import java.util.List;

public class Order {
    private String userId;
    private List<OrderedProduct> products;
    private String mobile;
    private String address;
    private String paymentMethod;
    private double totalAmount;
    private String status;
    private long createdDate;

    public Order() {
    }

    public Order(String userId, List<OrderedProduct> products, String mobile, String address, String paymentMethod, double totalAmount, String status, long createdDate) {
        this.userId = userId;
        this.products = products;
        this.mobile = mobile;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderedProduct> getProducts() {
        return products;
    }
    public void setProducts(List<OrderedProduct> products) {
        this.products = products;
    }

    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}
