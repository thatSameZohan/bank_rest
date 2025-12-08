package com.bank.controller;

import com.bank.dto.*;
import com.bank.entity.UserEntity;
import com.bank.repository.UserRepository;
import com.bank.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {

     private final UserRepository userRepository;
     private final JwtUtil jwtUtil;
     private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        UserEntity user = UserEntity.builder()
            .username(dto.username())
            .password(passwordEncoder.encode(dto.password()))
            .role(dto.isAdmin() ? com.bank.entity.Role.ROLE_ADMIN : com.bank.entity.Role.ROLE_USER)
            .build();
        userRepository.save(user);
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto) {
        var optional = userRepository.findByUsername(dto.username());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        var user = optional.get();
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
