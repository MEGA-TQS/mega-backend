package com.sportsgear.rentalplatform.boundary;

import com.sportsgear.rentalplatform.dto.PaymentRequest;
import com.sportsgear.rentalplatform.dto.PaymentResponse;
import com.sportsgear.rentalplatform.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/pay")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        // Log for debugging
        System.out.println("Processing payment for Booking ID: " + request.getBookingId());

        PaymentResponse response = paymentService.processPayment(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Return 400 Bad Request if logic fails (e.g. wrong status or "fail" card)
            return ResponseEntity.badRequest().body(response);
        }
    }
}