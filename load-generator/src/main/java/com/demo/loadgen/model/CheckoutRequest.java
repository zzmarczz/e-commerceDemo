package com.demo.loadgen.model;

import java.util.List;

public class CheckoutRequest {
    private String userId;
    private List<CheckoutItem> items;

    public CheckoutRequest() {}

    public CheckoutRequest(String userId, List<CheckoutItem> items) {
        this.userId = userId;
        this.items = items;
    }

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

