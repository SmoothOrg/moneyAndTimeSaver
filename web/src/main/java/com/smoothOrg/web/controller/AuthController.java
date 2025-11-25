package com.smoothOrg.web.controller;

import com.smoothOrg.domain.entity.User;
import com.smoothOrg.services.auth.AuthenticationService;
import com.smoothOrg.web.dto.AuthResponse;
import com.smoothOrg.web.dto.LoginRequest;
import com.smoothOrg.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authenticationService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName()
            );

            String token = authenticationService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );

            AuthResponse response = new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getName(),
                    user.getId()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authenticationService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );

            User user = authenticationService.getUserByEmail(request.getEmail());

            AuthResponse response = new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getName(),
                    user.getId()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }
    }

    /**
     * Error response DTO
     */
    public record ErrorResponse(String message) {}
}
