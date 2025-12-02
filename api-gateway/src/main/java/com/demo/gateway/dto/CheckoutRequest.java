package com.demo.gateway.dto;

import java.util.List;

/**
 * DTO for checkout request
 * This allows APM to capture revenue data from method parameters
 */
public class CheckoutRequest {
    
    private String userId;
    private List<CheckoutItem> items;
    
    // Calculated fields for APM data collectors
    private Double totalAmount;
    private Integer itemCount;
    
    public CheckoutRequest() {}
    
    public CheckoutRequest(String userId, List<CheckoutItem> items) {
        this.userId = userId;
        this.items = items;
        calculateTotals();
    }
    
    /**
     * Calculate totals for APM visibility
     * Called automatically when items are set
     */
    private void calculateTotals() {
        if (items != null) {
            this.itemCount = items.size();
            this.totalAmount = items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        } else {
            this.itemCount = 0;
            this.totalAmount = 0.0;
        }
    }
    
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
        calculateTotals(); // Recalculate when items change
    }
    
    /**
     * APM DATA COLLECTOR TARGET
     * This getter can be captured via method parameter navigation
     */
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    /**
     * APM DATA COLLECTOR TARGET
     * This getter can be captured via method parameter navigation
     */
    public Integer getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}

