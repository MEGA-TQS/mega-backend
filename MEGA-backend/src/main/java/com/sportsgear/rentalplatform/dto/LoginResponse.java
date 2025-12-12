package com.sportsgear.rentalplatform.dto;

import com.sportsgear.rentalplatform.data.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private String token;  
}