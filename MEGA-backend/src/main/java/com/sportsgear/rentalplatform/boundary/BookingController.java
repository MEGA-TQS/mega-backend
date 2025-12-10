package com.sportsgear.rentalplatform.boundary;


import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingRequest;
import com.sportsgear.rentalplatform.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    // POST /api/bookings
    private final BookingService bookingService;

    // POST - Criar Reserva (Já tinhas este)
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody @Valid BookingRequest request) {
        try {
            Booking booking = bookingService.createGroupBooking(request);
            return ResponseEntity.status(201).body(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    // PATCH /api/bookings/{id}/accept?ownerId=5
    // O Owner aprova a reserva
    @PatchMapping("/{id}/accept")
    public ResponseEntity<Booking> acceptBooking(
            @PathVariable Long id, 
            @RequestParam Long ownerId) {
        try {
            Booking booking = bookingService.acceptBooking(id, ownerId);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404 se reserva não existe
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build(); // 400 se não for dono ou status errado
        }
    }

    // PATCH /api/bookings/{id}/decline?ownerId=5
    // O Owner rejeita a reserva
    @PatchMapping("/{id}/decline")
    public ResponseEntity<Booking> declineBooking(
            @PathVariable Long id, 
            @RequestParam Long ownerId) {
        try {
            Booking booking = bookingService.declineBooking(id, ownerId);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // // PATCH /api/bookings/{id}/status
    // @PatchMapping("/{id}/status")
    // public Booking updateStatus(...) {

    
    // ... }

    // US5 - Pagar Reserva
    // POST /api/bookings/10/payment?token=tok_visa
    @PostMapping("/{id}/payment")
    public ResponseEntity<Booking> processPayment(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "tok_visa") String token) {
        
        try {
            Booking booking = bookingService.processPayment(id, token);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); 
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); 
        }
    }
}
