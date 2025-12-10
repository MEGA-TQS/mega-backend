package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.*;
import com.sportsgear.rentalplatform.data.BookingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookingService bookingService;

    private User renter;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        renter = User.builder().id(1L).name("Renter").build();
        owner = User.builder().id(2L).name("Owner").build();
        
        item = Item.builder()
                .id(10L)
                .name("Bike")
                .pricePerDay(BigDecimal.TEN)
                .owner(owner) // Item pertence ao Owner 2
                .build();

        booking = Booking.builder()
                .id(100L)
                .renter(renter)
                .status(BookingStatus.PENDING)
                .items(Collections.singletonList(BookingItem.builder().item(item).build()))
                .build();
    }

    // US3: CREATE BOOKING

    @Test
    @Tag("US-3")
    void whenDatesOverlap_thenThrowException() {
        // GIVEN
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(itemRepository.findAllById(anyList())).thenReturn(Arrays.asList(item));
        
        // SIMULA CONFLITO
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(true);

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> bookingService.createGroupBooking(req));
        verify(bookingRepository, never()).save(any());
    }

    // US4: OWNER DECISION

    @Test
    @Tag("US-4")
    void whenOwnerAccepts_thenStatusApproved() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.acceptBooking(100L, 2L); // Owner ID 2 (Correto)

        // THEN
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    @Tag("US-4")
    void whenWrongOwnerTriesToAccept_thenThrowException() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN (Owner ID 99 não é dono do item)
        assertThrows(IllegalStateException.class, () -> bookingService.acceptBooking(100L, 99L));
    }

    // US5: PAYMENT 
    @Test
    @Tag("US-5")
    void whenBookingApproved_processPayment_Success() {
        // GIVEN
        booking.setStatus(BookingStatus.APPROVED); // Tem de estar aprovada
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.processPayment(100L, "tok_visa");

        // THEN
        assertEquals(BookingStatus.PAID, result.getStatus());
    }

    @Test
    @Tag("US-5")
    void whenBookingPending_processPayment_Fail() {
        // GIVEN
        booking.setStatus(BookingStatus.PENDING); // Ainda não aprovada
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> bookingService.processPayment(100L, "tok_visa"));
    }
}