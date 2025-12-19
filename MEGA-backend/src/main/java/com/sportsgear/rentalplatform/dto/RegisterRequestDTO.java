package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String name;
    private String email;
    private String password;
    private String role; // Accepts "OWNER", "RENTER", "ADMIN" as a String
}

