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
        // 1. Find the booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElse(null);
        
        if (booking == null) {
            return new PaymentResponse(false, null, "Booking not found");
        }
        
        // 2. Check if booking is approved (ready for payment)
        if (booking.getStatus() != BookingStatus.APPROVED) {
            return new PaymentResponse(false, null, 
                "Booking must be approved before payment. Current status: " + booking.getStatus());
        }
        
        // 3. SUPER SIMPLE payment validation
        // Card ending with "4242" = success, anything else = failure
        if (request.getCardNumber() == null || !request.getCardNumber().endsWith("4242")) {
            return new PaymentResponse(false, null, "Payment failed: invalid card");
        }
        
        // 4. Process payment (mock)
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
        
        // 5. Generate fake transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return new PaymentResponse(true, transactionId, "Payment successful");
    }
}