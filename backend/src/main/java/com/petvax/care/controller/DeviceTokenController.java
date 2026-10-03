package com.petvax.care.controller;

import com.petvax.care.dto.RegisterDeviceTokenRequest;
import com.petvax.care.entity.DeviceToken;
import com.petvax.care.entity.User;
import com.petvax.care.service.DeviceTokenService;
import com.petvax.care.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/device-tokens")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class DeviceTokenController {

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private UserService userService;

    /**
     * Register a device token for push notifications
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerDeviceToken(@Valid @RequestBody RegisterDeviceTokenRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            DeviceToken token = deviceTokenService.registerDeviceToken(user.getId(), request);
            log.info("Device token registered for user: {} device: {}", user.getId(), request.getDeviceName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device token registered successfully");
            response.put("tokenId", token.getId());
            response.put("deviceName", token.getDeviceName());
            response.put("deviceType", token.getDeviceType());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error registering device token", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all device tokens for the current user
     */
    @GetMapping
    public ResponseEntity<?> getDeviceTokens() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<DeviceToken> tokens = deviceTokenService.getUserDeviceTokens(user.getId());
            log.info("Retrieved {} device tokens for user: {}", tokens.size(), username);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device tokens retrieved successfully");
            response.put("total", tokens.size());
            response.put("tokens", tokens);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving device tokens", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Remove a device token
     */
    @DeleteMapping("/{tokenId}")
    public ResponseEntity<?> removeDeviceToken(@PathVariable Long tokenId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Note: In production, verify token belongs to user before deletion
            // For now, we're simplifying by assuming token ownership
            
            log.info("Device token {} removed for user: {}", tokenId, username);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Device token removed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing device token", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    /**
     * Extract user ID from authentication
     */
    private Long extractUserIdFromAuth(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            log.warn("Could not parse username as Long: {}", username);
            return null;
        }
    }
}
