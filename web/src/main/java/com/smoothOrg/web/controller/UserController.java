package com.smoothOrg.web.controller;

import com.smoothOrg.domain.entity.User;
import com.smoothOrg.services.auth.AuthenticationService;
import com.smoothOrg.services.util.GeohashUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthenticationService authenticationService;

    public UserController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Get current logged-in user details
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = authenticationService.getUserByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        
        // Default location (home address)
        response.put("defaultLatitude", user.getDefaultLatitude());
        response.put("defaultLongitude", user.getDefaultLongitude());
        response.put("defaultAddress", user.getDefaultAddress());
        response.put("defaultGeohash", user.getDefaultGeohash());
        response.put("hasDefaultLocation", user.getDefaultLatitude() != null && user.getDefaultLongitude() != null);
        
        // Current location (last browsed)
        response.put("currentLatitude", user.getCurrentLatitude());
        response.put("currentLongitude", user.getCurrentLongitude());
        response.put("currentAddress", user.getCurrentAddress());
        response.put("currentGeohash", user.getCurrentGeohash());
        response.put("hasCurrentLocation", user.getCurrentLatitude() != null && user.getCurrentLongitude() != null);

        return ResponseEntity.ok(response);
    }

    /**
     * Update user's default location (home address)
     */
    @PutMapping("/location/default")
    public ResponseEntity<Map<String, Object>> updateDefaultLocation(
            @RequestBody Map<String, Object> locationData
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Double latitude = ((Number) locationData.get("latitude")).doubleValue();
        Double longitude = ((Number) locationData.get("longitude")).doubleValue();
        String address = (String) locationData.get("address");

        // Generate geohash
        String geohash = GeohashUtils.encode(latitude, longitude);

        // Update default location
        User user = authenticationService.updateUserLocation(email, latitude, longitude, address, geohash);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Default location saved");
        response.put("latitude", user.getDefaultLatitude());
        response.put("longitude", user.getDefaultLongitude());
        response.put("address", user.getDefaultAddress());
        response.put("geohash", user.getDefaultGeohash());

        return ResponseEntity.ok(response);
    }

    /**
     * Update user's current browsing location (for analytics)
     */
    @PutMapping("/location/current")
    public ResponseEntity<Map<String, Object>> updateCurrentLocation(
            @RequestBody Map<String, Object> locationData
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Double latitude = ((Number) locationData.get("latitude")).doubleValue();
        Double longitude = ((Number) locationData.get("longitude")).doubleValue();
        String address = (String) locationData.get("address");

        // Generate geohash
        String geohash = GeohashUtils.encode(latitude, longitude);

        // Update current location (ALWAYS for analytics)
        User user = authenticationService.updateCurrentLocation(email, latitude, longitude, address, geohash);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Current location updated");
        response.put("latitude", user.getCurrentLatitude());
        response.put("longitude", user.getCurrentLongitude());
        response.put("address", user.getCurrentAddress());
        response.put("geohash", user.getCurrentGeohash());

        return ResponseEntity.ok(response);
    }

    /**
     * Clear user's default location
     */
    @DeleteMapping("/location")
    public ResponseEntity<Map<String, Object>> clearDefaultLocation() {
        // Note: In production, we don't actually clear the default location
        // We keep it for analytics and user can always update it
        // This endpoint is kept for API consistency but could be disabled
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        authenticationService.clearUserLocation(email);

        return ResponseEntity.ok(Map.of(
            "message", "Default location cleared",
            "note", "You can set a new location anytime"
        ));
    }

    /**
     * Test authentication endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Authentication working! User: " + 
                SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
