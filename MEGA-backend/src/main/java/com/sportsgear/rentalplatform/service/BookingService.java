package com.sportsgear.rentalplatform.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        

        // Regra: Não permitir reservas com mais de 1 ano de antecedência
        if (request.getStartDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new IllegalArgumentException("Bookings can only be made up to 1 year in advance.");
        }
        
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
        
        boolean isInstantBooking = itemsToRent.stream()
                .allMatch(Item::isInstantBookable);

        BookingStatus initialStatus = isInstantBooking ? BookingStatus.APPROVED : BookingStatus.PENDING;

        Booking booking = Booking.builder()
                .renter(renter)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(initialStatus)
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
    public Booking acceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        booking.setStatus(BookingStatus.APPROVED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking declineBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateStatus(Long bookingId, BookingStatus newStatus, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (newStatus == BookingStatus.PAID) {
            throw new IllegalStateException("Cannot manually set status to PAID. Please use the Payment endpoint.");
        }

        if (newStatus == BookingStatus.CANCELLED) {
            // Verificar quem está a tentar cancelar
            boolean isRenter = booking.getRenter().getId().equals(userId);
            
            // Verifica se é o dono de pelo menos um item 
            boolean isOwner = booking.getItems().stream()
                    .anyMatch(bi -> bi.getItem().getOwner().getId().equals(userId));

            if (!isRenter && !isOwner) {
                throw new IllegalStateException("Only the Renter or the Owner can cancel a booking.");
            }
            // impedir cancelamento se já tiver passado a data de início
            if (LocalDate.now().isAfter(booking.getStartDate())) {
                 throw new IllegalStateException("Cannot cancel a booking that has already started.");
            }
        }

        // Atualiza o estado
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByRenter(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    public List<Booking> getBookingsForOwner(Long ownerId) {
        return bookingRepository.findBookingsByOwner(ownerId);
    }
}
