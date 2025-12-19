package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.Role;
import com.sportsgear.rentalplatform.data.User;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.dto.LoginRequest;
import com.sportsgear.rentalplatform.dto.RegisterRequestDTO;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private UserRepository userRepository;

    @Test
    @Tag("US-10")
    void login_Success() throws Exception {
        // Setup a user who has RENTER + OWNER roles in the database
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .roles(Set.of(Role.RENTER, Role.OWNER))
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER")) // Matches new login logic
                .andExpect(jsonPath("$.token").value("1"));
    }

    @Test
    @Tag("US-10")
    void register_Success_AsUser() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("New User");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRole("USER");

        User savedUser = User.builder()
                .id(2L)
                .name("New User")
                .email("new@example.com")
                .password("password123")
                .roles(Set.of(Role.RENTER, Role.OWNER))
                .build();

        given(userRepository.findByEmail("new@example.com")).willReturn(null);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @Tag("US-10")
    void register_Success_AsAdmin() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("Admin User");
        request.setEmail("admin@example.com");
        request.setPassword("secure");
        request.setRole("ADMIN");

        User savedUser = User.builder()
                .id(3L)
                .name("Admin User")
                .email("admin@example.com")
                .roles(Set.of(Role.ADMIN, Role.RENTER, Role.OWNER))
                .build();

        given(userRepository.findByEmail("admin@example.com")).willReturn(null);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @Tag("US-10")
    void register_EmailAlreadyExists() throws Exception {
        User existingUser = User.builder().email("existing@example.com").build();
        given(userRepository.findByEmail("existing@example.com")).willReturn(existingUser);

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("existing@example.com");
        request.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}