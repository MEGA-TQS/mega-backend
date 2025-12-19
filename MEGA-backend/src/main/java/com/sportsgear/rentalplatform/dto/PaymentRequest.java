package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long bookingId;
    private String cardNumber;  
    private String cardHolder;
}