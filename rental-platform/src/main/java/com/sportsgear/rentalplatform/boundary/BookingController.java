package com.sportsgear.rentalplatform.boundary;

import org.springframework.web.bind.annotation.*;

import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingRequest;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    // POST /api/bookings
    @PostMapping
    public Booking createBooking(@RequestBody BookingRequest request) { ... }

    // PATCH /api/bookings/{id}/status
    @PatchMapping("/{id}/status")
    public Booking updateStatus(...) { ... }

    // POST /api/bookings/{id}/payment
    @PostMapping("/{id}/payment")
    public Booking processPayment(...) { ... }
}
