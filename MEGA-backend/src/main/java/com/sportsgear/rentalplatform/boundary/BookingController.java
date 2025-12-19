package com.sportsgear.rentalplatform.boundary;


import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingRequest;
import com.sportsgear.rentalplatform.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class BookingController {

    // POST /api/bookings
    private final BookingService bookingService;

    // POST - Criar Reserva 
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody @Valid BookingRequest request) {
        try {
            Booking booking = bookingService.createGroupBooking(request);
            return ResponseEntity.status(201).body(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }


 // PATCH /api/bookings/{id}/accept
    @PatchMapping("/{id}/accept")
    public ResponseEntity<Booking> acceptBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.acceptBooking(id);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /api/bookings/{id}/decline
    @PatchMapping("/{id}/decline")
    public ResponseEntity<Booking> declineBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.declineBooking(id);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/bookings/renter/{renterId}
    @GetMapping("/renter/{renterId}")
    public ResponseEntity<java.util.List<Booking>> getBookingsByRenter(@PathVariable Long renterId) {
        // Vamos assumir que o serviço vai ter este método
        java.util.List<Booking> bookings = bookingService.getBookingsByRenter(renterId);
        return ResponseEntity.ok(bookings);
    }

    // // PATCH /api/bookings/{id}/status
    // @PatchMapping("/{id}/status")
    // PATCH /api/bookings/{id}/status?status=CANCELLED&userId=1
    @PatchMapping("/{id}/status")
    public ResponseEntity<Booking> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam Long userId) {
        try {
            com.sportsgear.rentalplatform.data.BookingStatus newStatus = 
                com.sportsgear.rentalplatform.data.BookingStatus.valueOf(status.toUpperCase());
            
            Booking booking = bookingService.updateStatus(id, newStatus, userId);
            return ResponseEntity.ok(booking);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/bookings/owner/{ownerId}
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Booking>> getBookingsForOwner(@PathVariable Long ownerId) {
        List<Booking> bookings = bookingService.getBookingsForOwner(ownerId);
        return ResponseEntity.ok(bookings);
    }
}
