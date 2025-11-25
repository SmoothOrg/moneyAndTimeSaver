package com.smoothOrg.services.auth;

import com.smoothOrg.domain.entity.User;
import com.smoothOrg.domain.repository.UserRepository;
import com.smoothOrg.services.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Register a new user
     */
    public User register(String email, String password, String name) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);

        return userRepository.save(user);
    }

    /**
     * Authenticate user and generate token
     */
    public String authenticate(String email, String password) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateToken(userDetails);
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update user's default location (home/primary address)
     */
    public User updateUserLocation(String email, Double latitude, Double longitude, String address, String geohash) {
        User user = getUserByEmail(email);
        user.setDefaultLatitude(latitude);
        user.setDefaultLongitude(longitude);
        user.setDefaultAddress(address);
        user.setDefaultGeohash(geohash);
        return userRepository.save(user);
    }

    /**
     * Update user's current browsing location (always called for analytics)
     */
    public User updateCurrentLocation(String email, Double latitude, Double longitude, String address, String geohash) {
        User user = getUserByEmail(email);
        user.setCurrentLatitude(latitude);
        user.setCurrentLongitude(longitude);
        user.setCurrentAddress(address);
        user.setCurrentGeohash(geohash);
        user.setCurrentLocationUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Clear user's default location
     */
    public void clearUserLocation(String email) {
        User user = getUserByEmail(email);
        user.setDefaultLatitude(null);
        user.setDefaultLongitude(null);
        user.setDefaultAddress(null);
        user.setDefaultGeohash(null);
        userRepository.save(user);
    }
}
