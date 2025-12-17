package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.*;

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
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(100L)
                .renter(renter)
                .status(BookingStatus.PENDING)
                .items(Collections.singletonList(BookingItem.builder().item(item).build()))
                .build();
    }

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
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(true);

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> bookingService.createGroupBooking(req));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @Tag("US-4")
    void whenOwnerAccepts_thenStatusApproved() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.acceptBooking(100L, 2L);

        // THEN
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    @Tag("US-4")
    void whenWrongOwnerTriesToAccept_thenThrowException() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> bookingService.acceptBooking(100L, 99L));
    }

    @Test
    @Tag("US-8")
    void whenAllItemsAreInstantBookable_thenStatusIsApproved() {
        // GIVEN
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        Item instantItem = Item.builder()
                .id(10L)
                .pricePerDay(BigDecimal.TEN)
                .instantBookable(true) 
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(instantItem));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createGroupBooking(req);

        // THEN
        assertEquals(BookingStatus.APPROVED, result.getStatus()); 
    }

    @Test
    @Tag("US-8")
    void whenItemIsNotInstant_thenStatusIsPending() {
        // GIVEN
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        Item normalItem = Item.builder()
                .id(10L)
                .pricePerDay(BigDecimal.TEN)
                .instantBookable(false) 
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(normalItem));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createGroupBooking(req);

        // THEN
        assertEquals(BookingStatus.PENDING, result.getStatus()); 
    }
}