package com.sportsgear.rentalplatform.dto;

import com.sportsgear.rentalplatform.data.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String phoneNumber;
    private String address;
    
    @Pattern(regexp = "RENTER|OWNER|RENTER,OWNER", message = "Roles must be RENTER, OWNER, or RENTER,OWNER")
    private String roles = "RENTER"; // Default role
}