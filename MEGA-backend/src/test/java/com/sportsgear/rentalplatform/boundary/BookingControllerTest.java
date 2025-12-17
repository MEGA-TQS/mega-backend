package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingStatus;
import com.sportsgear.rentalplatform.data.BookingRequest;
import com.sportsgear.rentalplatform.service.BookingService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    @MockitoBean private BookingService bookingService;

    @Test
    @Tag("US-3")
    void whenStartDateInPast_thenReturn400() throws Exception {
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L));
        req.setStartDate(LocalDate.now().minusDays(1));
        req.setEndDate(LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("US-3")
    void whenValidRequest_thenReturn201() throws Exception {
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(2));

        Booking mockBooking = Booking.builder().id(1L).status(BookingStatus.PENDING).build();
        given(bookingService.createGroupBooking(any())).willReturn(mockBooking);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @Tag("US-4")
    void whenOwnerAccepts_thenReturn200() throws Exception {
        Booking mockBooking = Booking.builder().id(1L).status(BookingStatus.APPROVED).build();
        
        given(bookingService.acceptBooking(1L, 5L)).willReturn(mockBooking);

        mockMvc.perform(patch("/api/bookings/1/accept")
                .param("ownerId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
    @Test
    @Tag("US-8") // Serve para US9 e US10 também
    public void whenPostValidBooking_thenReturn201() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setRenterId(1L);
        request.setItemIds(Arrays.asList(10L, 20L)); // US10 (Multi-item)
        request.setStartDate(LocalDate.now().plusDays(5)); // US9 (Future)
        request.setEndDate(LocalDate.now().plusDays(7));

        // Mock do retorno do Service
        Booking mockBooking = Booking.builder()
                .id(100L)
                .status(BookingStatus.PENDING) // Ou APPROVED se fosse US8 instant
                .build();

        given(bookingService.createGroupBooking(any(BookingRequest.class))).willReturn(mockBooking);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Espera HTTP 201
                .andExpect(jsonPath("$.id", is(100)));
    }
    
    // Teste de Erro (Validation)
    @Test
    public void whenPostInvalidBooking_thenReturn400() throws Exception {
        BookingRequest request = new BookingRequest();
        // Falta renterId e dates -> Inválido
        
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Espera HTTP 400
    }

    @Test
    @Tag("US-9")
    void whenValidFutureDate_thenReturn201() throws Exception {
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L));
        // Data futura válida (AC: "weeks/months ahead")
        req.setStartDate(LocalDate.now().plusMonths(2)); 
        req.setEndDate(LocalDate.now().plusMonths(2).plusDays(3));

        Booking mockBooking = Booking.builder().status(BookingStatus.PENDING).build();
        given(bookingService.createGroupBooking(any())).willReturn(mockBooking);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

}