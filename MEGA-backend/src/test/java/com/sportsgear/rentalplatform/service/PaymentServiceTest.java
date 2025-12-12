package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.*;
import com.sportsgear.rentalplatform.dto.PaymentRequest;
import com.sportsgear.rentalplatform.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Booking booking;
    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        booking = Booking.builder()
                .id(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status(BookingStatus.APPROVED)
                .totalPrice(new BigDecimal("150.00"))
                .build();

        request = new PaymentRequest();
        request.setBookingId(1L);
        request.setCardNumber("4242");
        request.setCardHolder("John Doe");
    }

    @Test
    @Tag("US-5")
    void whenValidCardAndApprovedBooking_thenPaymentSuccess() {
        // GIVEN
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        PaymentResponse response = paymentService.processPayment(request);

        // THEN
        assertTrue(response.isSuccess());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN-"));
        assertEquals("Payment successful", response.getMessage());
        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.PAID, booking.getStatus());
    }

    @Test
    @Tag("US-5")
    void whenCardNotEndingWith4242_thenPaymentFails() {
        // GIVEN
        request.setCardNumber("1234");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking)); // FIXED: Add this mock

        // WHEN
        PaymentResponse response = paymentService.processPayment(request);

        // THEN
        assertFalse(response.isSuccess());
        assertEquals("Payment failed: invalid card", response.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @Tag("US-5")
    void whenBookingNotFound_thenPaymentFails() {
        // GIVEN
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        request.setBookingId(999L);

        // WHEN
        PaymentResponse response = paymentService.processPayment(request);

        // THEN
        assertFalse(response.isSuccess());
        assertEquals("Booking not found", response.getMessage());
    }

    @Test
    @Tag("US-5")
    void whenBookingNotApproved_thenPaymentFails() {
        // GIVEN
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // WHEN
        PaymentResponse response = paymentService.processPayment(request);

        // THEN
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Booking must be approved before payment"));
    }

    @Test
    @Tag("US-5")
    void whenNullCardNumber_thenPaymentFails() {
        // GIVEN
        request.setCardNumber(null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // WHEN
        PaymentResponse response = paymentService.processPayment(request);

        // THEN
        assertFalse(response.isSuccess());
        assertEquals("Payment failed: invalid card", response.getMessage());
    }
}