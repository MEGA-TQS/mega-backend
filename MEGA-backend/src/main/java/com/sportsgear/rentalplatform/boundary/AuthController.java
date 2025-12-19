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
        response.setToken(user.getId().toString());
        
        if (user.getRoles().contains(com.sportsgear.rentalplatform.data.Role.ADMIN)) {
            response.setRole("ADMIN");
        } else {
            response.setRole("USER"); 
        }
        
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

        // 3. Handle Role Logic (The requested logic)
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            // If they register as Admin, they are an ADMIN
            // (Optional: Add RENTER/OWNER if admins should also book/list items)
            newUser.setRoles(Set.of(Role.ADMIN, Role.RENTER, Role.OWNER)); 
        } else {
            // Default: If they register as "USER" (or anything else), 
            // they become BOTH a Renter and an Owner.
            newUser.setRoles(Set.of(Role.RENTER, Role.OWNER));
        }

        // 4. Save User
        User savedUser = userRepository.save(newUser);
        
        // 5. Build Response
        LoginResponse response = new LoginResponse();
        response.setUserId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());


        if (savedUser.getRoles().contains(Role.ADMIN)) {
            response.setRole("ADMIN");
        } else {
            response.setRole("USER"); 
        }

        response.setToken(savedUser.getId().toString());
        
        return ResponseEntity.ok(response);
    }
}