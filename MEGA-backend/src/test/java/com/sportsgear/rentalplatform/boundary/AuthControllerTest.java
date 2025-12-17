package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.Role;
import com.sportsgear.rentalplatform.data.User;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.dto.LoginRequest;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void login_Success() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .roles(Set.of(Role.RENTER))  // Changed from USER to RENTER
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
                .andExpect(jsonPath("$.token").value("1"))
                .andExpect(jsonPath("$.role").value("RENTER"));  // Changed from USER to RENTER
    }

    @Test
    void login_WrongPassword() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("correctPassword")
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UserNotFound() throws Exception {
        given(userRepository.findByEmail("nonexistent@example.com")).willReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_Success() throws Exception {
        User newUser = User.builder()
                .name("New User")
                .email("new@example.com")
                .password("password123")
                .roles(Set.of(Role.RENTER))  // Changed from USER to RENTER
                .build();

        User savedUser = User.builder()
                .id(2L)
                .name("New User")
                .email("new@example.com")
                .password("password123")
                .roles(Set.of(Role.RENTER))  // Changed from USER to RENTER
                .build();

        given(userRepository.findByEmail("new@example.com")).willReturn(null);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void register_EmailAlreadyExists() throws Exception {
        User existingUser = User.builder()
                .email("existing@example.com")
                .build();

        given(userRepository.findByEmail("existing@example.com")).willReturn(existingUser);

        User newUser = User.builder()
                .name("New User")
                .email("existing@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_DefaultRoleAssigned() throws Exception {

        User newUserNoRole = User.builder()
                .name("No Role User")
                .email("norole@example.com")
                .password("password123")
                .build(); // roles is null/empty by default

        User savedUser = User.builder()
                .id(3L)
                .name("No Role User")
                .email("norole@example.com")
                .password("password123")
                .roles(Set.of(Role.RENTER)) // The DB would return the user with the role set
                .build();

        given(userRepository.findByEmail("norole@example.com")).willReturn(null);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserNoRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("RENTER"));
    }
}