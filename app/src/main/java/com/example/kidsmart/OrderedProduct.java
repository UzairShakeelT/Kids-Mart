package com.example.kidsmart;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderedProduct implements Parcelable {
    private String productId;
    private String productName;
    private String productPrice;
    private String selectedSize;
    private int quantity;
    private String imageUrl;

    public OrderedProduct() {
    }

    public OrderedProduct(String productId, String productName, String productPrice, String selectedSize, int quantity, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.selectedSize = selectedSize;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    protected OrderedProduct(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        productPrice = in.readString();
        selectedSize = in.readString();
        quantity = in.readInt();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeString(productPrice);
        dest.writeString(selectedSize);
        dest.writeInt(quantity);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderedProduct> CREATOR = new Creator<OrderedProduct>() {
        @Override
        public OrderedProduct createFromParcel(Parcel in) {
            return new OrderedProduct(in);
        }

        @Override
        public OrderedProduct[] newArray(int size) {
            return new OrderedProduct[size];
        }
    };

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

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
