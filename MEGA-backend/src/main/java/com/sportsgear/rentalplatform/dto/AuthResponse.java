package com.sportsgear.rentalplatform.dto;

import com.sportsgear.rentalplatform.data.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private Long userId;
    private Set<Role> roles;
    private String name;
}