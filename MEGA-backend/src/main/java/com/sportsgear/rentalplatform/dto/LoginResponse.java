package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;  // Changed from Role enum to String
    private String token;
}