package com.petvax.care.controller;

import com.petvax.care.dto.OwnerProfileResponse;
import com.petvax.care.dto.PetResponse;
import com.petvax.care.dto.UpdateProfileRequest;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.OwnerProfileService;
import com.petvax.care.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/profile")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class OwnerProfileController {
    
    @Autowired
    private OwnerProfileService ownerProfileService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get current user's profile
     * GET /api/owner/profile
     */
    @GetMapping
    public ResponseEntity<?> getProfile() {
        try {
            log.info("Fetching profile for current user");
            
            User user = getAuthenticatedUser();
            OwnerProfileResponse profile = ownerProfileService.getProfileByUser(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile retrieved successfully");
            response.put("data", profile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve profile"));
        }
    }
    
    /**
     * Update current user's profile
     * PUT /api/owner/profile
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            log.info("Updating profile for current user");
            
            User user = getAuthenticatedUser();
            OwnerProfileResponse profile = ownerProfileService.updateProfile(user.getId(), request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("data", profile);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update profile"));
        }
    }
    
    /**
     * Upload profile photo
     * POST /api/owner/profile/photo
     */
    @PostMapping("/photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("photo") MultipartFile file) {
        try {
            log.info("Uploading profile photo for current user");
            
            User user = getAuthenticatedUser();
            String photoUrl = ownerProfileService.uploadProfilePhoto(user.getId(), file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile photo uploaded successfully");
            response.put("photoUrl", photoUrl);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading photo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload photo: " + e.getMessage()));
        }
    }
    
    /**
     * Delete account (soft delete with 90-day retention)
     * DELETE /api/owner/profile
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAccount() {
        try {
            log.info("Deleting account for current user");
            
            User user = getAuthenticatedUser();
            ownerProfileService.deleteAccount(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account deleted successfully. It will be permanently removed in 90 days.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete account"));
        }
    }
    
    /**
     * Get all pets owned by current user
     * GET /api/owner/profile/pets
     */
    @GetMapping("/pets")
    public ResponseEntity<?> getPetsList() {
        try {
            log.info("Fetching pets list for current user");
            
            User user = getAuthenticatedUser();
            List<PetResponse> pets = ownerProfileService.getPetsList(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pets retrieved successfully");
            response.put("data", pets);
            response.put("count", pets.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving pets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve pets"));
        }
    }
    
    /**
     * Get authenticated user from security context
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User not authenticated");
            throw new UnauthorizedException("User not authenticated");
        }
        
        PrincipalUser principal = (PrincipalUser) authentication.getPrincipal();
        User user = userService.getUserById(principal.getUserId());
        
        if (user == null) {
            log.warn("User not found for ID: {}", principal.getUserId());
            throw new UnauthorizedException("User not found");
        }
        
        return user;
    }
}
