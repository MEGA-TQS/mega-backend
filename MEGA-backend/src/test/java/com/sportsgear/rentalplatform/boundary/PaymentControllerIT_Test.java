package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingRepository;
import com.sportsgear.rentalplatform.data.BookingStatus;
import com.sportsgear.rentalplatform.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PaymentControllerIT_Test {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
    }

    @Test
    @Tag("US-5")
    void processPayment_Integration_ShouldUpdateBookingStatus() throws Exception {
        // Criar Reserva Aprovada na BD
        Booking booking = Booking.builder()
                .status(BookingStatus.APPROVED)
                .totalPrice(new BigDecimal("50.00"))
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();
        booking = bookingRepository.save(booking);

        // Criar Request de Pagamento
        PaymentRequest req = new PaymentRequest();
        req.setBookingId(booking.getId());
        req.setCardNumber("4242"); // Cartão válido
        req.setCardHolder("Rich User");

        // Chamar API
        mockMvc.perform(post("/api/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verificar na BD se mudou para PAID
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assert(updatedBooking.getStatus() == BookingStatus.PAID);
    }
}