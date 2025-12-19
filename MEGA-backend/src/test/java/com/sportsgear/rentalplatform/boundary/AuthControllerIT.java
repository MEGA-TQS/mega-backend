package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.data.BookingRepository;
import com.sportsgear.rentalplatform.data.ItemRepository;
import com.sportsgear.rentalplatform.dto.LoginRequest;
import com.sportsgear.rentalplatform.dto.RegisterRequestDTO; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Tag("US-10")
    void registerAndLogin_Integration_ShouldWorkEndToEnd() throws Exception {
        // 1. REGISTER
        RegisterRequestDTO registerReq = new RegisterRequestDTO();
        registerReq.setName("Integration User");
        registerReq.setEmail("it@test.com");
        registerReq.setPassword("securePass");
        registerReq.setRole("USER");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("USER")); // Verified Registration Role

        // 2. LOGIN
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("it@test.com");
        loginReq.setPassword("securePass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                // --- THIS IS THE FIX ---
                // We verify that Login ALSO returns "USER" so the frontend menu works
                .andExpect(jsonPath("$.role").value("USER")); 
    }
}