package com.demo.order.dto;

import java.util.List;

public class CheckoutRequest {
    private String userId;
    private List<CheckoutItem> items;

    public CheckoutRequest() {}

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CheckoutItem> getItems() {
        return items;
    }

    public void setItems(List<CheckoutItem> items) {
        this.items = items;
    }
}


