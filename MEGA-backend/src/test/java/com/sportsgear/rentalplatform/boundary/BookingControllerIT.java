package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.*;
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
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BookingControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Tag("US-3")
    void createBooking_Integration_ShouldCalculatePriceAndSave() throws Exception {
        // Criar User e Item na BD H2
        User renter = userRepository.save(User.builder().email("renter@test.com").name("Renter").build());
        User owner = userRepository.save(User.builder().email("owner@test.com").name("Owner").build());

        Item item = itemRepository.save(Item.builder()
                .name("Skis")
                .pricePerDay(BigDecimal.valueOf(50.00))
                .owner(owner)
                .active(true)
                .build());

        // Criar Request (2 dias de aluguer: 50 * 2 = 100)
        BookingRequest req = new BookingRequest();
        req.setRenterId(renter.getId());
        req.setItemIds(Collections.singletonList(item.getId()));
        req.setStartDate(LocalDate.now().plusDays(10));
        req.setEndDate(LocalDate.now().plusDays(12));

        // Executar POST
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(100.00)) // Valida lógica de preço do Service
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Validação Extra: Confirmar na BD
        Booking savedBooking = bookingRepository.findAll().get(0);
        assert(savedBooking.getRenter().getEmail().equals("renter@test.com"));
    }
}