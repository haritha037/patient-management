package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO){
        // Step 1: Find the user by email
        Optional<User> userOptional = userService.findByEmail(loginRequestDTO.getEmail());

        // Step 2: Check if password matches
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean passwordMatches = passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword());

            if (passwordMatches) {
                log.info("Authentication successful for email {}", loginRequestDTO.getEmail());
                // Step 3: Generate token
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                return Optional.of(token);
            }
        }
        // If user not found or password mismatch
        log.warn("Authentication failed for email {}", loginRequestDTO.getEmail());
        return Optional.empty();
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }
}
