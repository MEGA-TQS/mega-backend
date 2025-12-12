package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String transactionId;
    private String message;
    
    // Helper constructor
    public PaymentResponse(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }
    
    public PaymentResponse() {
        // Default constructor
    }
}