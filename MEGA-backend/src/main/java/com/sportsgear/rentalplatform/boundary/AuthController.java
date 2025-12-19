package com.sportsgear.rentalplatform.boundary;

import com.sportsgear.rentalplatform.data.Role;
import com.sportsgear.rentalplatform.data.User;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.dto.LoginRequest;
import com.sportsgear.rentalplatform.dto.LoginResponse;
import com.sportsgear.rentalplatform.dto.RegisterRequestDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        
        // Convert Set<Role> to simple string representation for now
        // Or keep the first role as primary (simplified approach)
        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Enum::name)
                .orElse("USER");
        response.setRole(primaryRole);
        
        response.setToken(user.getId().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequestDTO request) {
        // 1. Check if email exists
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // 2. Map DTO to User Entity
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // In production, encrypt this!

        // 3. Handle Role Logic
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            newUser.setRoles(Set.of(Role.ADMIN));
        } else {
            // Default to RENTER
            newUser.setRoles(Set.of(Role.RENTER, Role.OWNER));
        }

        // 4. Save and Respond
        User savedUser = userRepository.save(newUser);
        
        // ... rest of your response logic ...
        LoginResponse response = new LoginResponse();
        response.setUserId(savedUser.getId());
        // ...
        return ResponseEntity.ok(response);
    }
}