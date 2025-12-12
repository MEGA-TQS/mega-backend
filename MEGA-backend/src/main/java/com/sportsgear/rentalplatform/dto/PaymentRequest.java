package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long bookingId;
    private String cardNumber;  // Last 4 digits or "4242" for success
    private String cardHolder;
}