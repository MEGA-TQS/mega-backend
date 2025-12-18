package com.sportsgear.rentalplatform.boundary;

import com.sportsgear.rentalplatform.data.User;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.dto.LoginRequest;
import com.sportsgear.rentalplatform.dto.LoginResponse;
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
    public ResponseEntity<LoginResponse> register(@RequestBody User newUser) {
        // Check if email exists
        if (userRepository.findByEmail(newUser.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Set default role if not provided (assign as RENTER by default)
        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(Set.of(com.sportsgear.rentalplatform.data.Role.RENTER));
        }
        
        User savedUser = userRepository.save(newUser);
        
        LoginResponse response = new LoginResponse();
        response.setUserId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        
        String primaryRole = savedUser.getRoles().stream()
                .findFirst()
                .map(Enum::name)
                .orElse("RENTER");
        response.setRole(primaryRole);
        
        response.setToken(savedUser.getId().toString());
        
        return ResponseEntity.ok(response);
    }
}