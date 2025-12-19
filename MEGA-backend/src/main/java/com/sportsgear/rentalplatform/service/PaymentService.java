package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingRepository;
import com.sportsgear.rentalplatform.data.BookingStatus;
import com.sportsgear.rentalplatform.dto.PaymentRequest;
import com.sportsgear.rentalplatform.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final BookingRepository bookingRepository;
    
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Validate Booking Exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElse(null);
        
        if (booking == null) {
            return new PaymentResponse(false, null, "Booking not found with ID: " + request.getBookingId());
        }
        
        // 2. Validate Booking Status (Must be APPROVED to pay)
        if (booking.getStatus() != BookingStatus.APPROVED) {
            return new PaymentResponse(false, null, 
                "Booking cannot be paid. Current status: " + booking.getStatus());
        }
        
        // 3. SIMPLIFIED MOCK LOGIC
        // If you specifically type "fail" as the card number, we simulate a decline.
        // Otherwise, we ACCEPT EVERYTHING.
        String cardNum = request.getCardNumber() != null ? request.getCardNumber().trim() : "";
        
        if (cardNum.equalsIgnoreCase("fail")) {
            return new PaymentResponse(false, null, "Payment declined by bank (Mock)");
        }
        
        // 4. Success Path
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
        
        String mockTxnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return new PaymentResponse(true, mockTxnId, "Payment Approved (Mock)");
    }
}