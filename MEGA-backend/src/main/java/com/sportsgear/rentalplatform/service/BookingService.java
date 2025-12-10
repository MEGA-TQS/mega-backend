package com.sportsgear.rentalplatform.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sportsgear.rentalplatform.data.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public Booking createGroupBooking(BookingRequest request) {

        User renter = userRepository.findById(request.getRenterId())
                .orElseThrow(() -> new IllegalArgumentException("Renter not found with ID: " + request.getRenterId()));

        List<Item> itemsToRent = itemRepository.findAllById(request.getItemIds());

        if (itemsToRent.size() != request.getItemIds().size()) {
            throw new IllegalArgumentException("Invalid Item IDs provided. Some items do not exist.");
        }

        boolean hasConflict = bookingRepository.existsOverlappingBookings(
                request.getItemIds(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (hasConflict) {
            throw new IllegalStateException("Selected items are not available for the requested dates.");
        }

        Booking booking = Booking.builder()
                .renter(renter)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BookingStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days < 1) {
            days = 1;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Item item : itemsToRent) {
            BookingItem bookingItem = BookingItem.builder()
                    .booking(booking)
                    .item(item)
                    .priceAtBooking(item.getPricePerDay())
                    .build();

            booking.getItems().add(bookingItem);

            BigDecimal itemCost = item.getPricePerDay().multiply(BigDecimal.valueOf(days));
            total = total.add(itemCost);
        }

        booking.setTotalPrice(total);

        // Gravar
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking acceptBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    
        // Já verificado se é pending
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be accepted.");
        }
    
        // Validar se o owner é realmente o dono dos items
        boolean allBelongToOwner = booking.getItems().stream()
                .allMatch(bi -> bi.getItem().getOwner().getId().equals(ownerId));
    
        if (!allBelongToOwner) {
            throw new IllegalStateException("User is not the owner of all rented items.");
        }
    
        booking.setStatus(BookingStatus.APPROVED);
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public Booking declineBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be declined.");
        }
    
        boolean allBelongToOwner = booking.getItems().stream()
                .allMatch(bi -> bi.getItem().getOwner().getId().equals(ownerId));
    
        if (!allBelongToOwner) {
            throw new IllegalStateException("User is not the owner of all rented items.");
        }
    
        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    // US5 - Processar Pagamento
    @Transactional
    public Booking processPayment(Long bookingId, String paymentToken) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalStateException("Payment failed: Booking is not in APPROVED state.");
        }

        if ("INVALID".equals(paymentToken)) {
            throw new IllegalStateException("Payment rejected by provider.");
        }

        booking.setStatus(BookingStatus.PAID);
        
        
        return bookingRepository.save(booking);
    }
}
