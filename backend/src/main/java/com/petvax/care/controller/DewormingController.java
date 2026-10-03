package com.petvax.care.controller;

import com.petvax.care.dto.CreateDewormingRequest;
import com.petvax.care.dto.DewormingResponse;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.DewormingService;
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
@RequestMapping("/api")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class DewormingController {
    
    @Autowired
    private DewormingService dewormingService;
    
    @Autowired
    private UserService userService;
    
    /**
     * FR-DEW-001: Get deworming history for a pet
     * GET /api/owner/pets/{petId}/deworming
     */
    @GetMapping("/owner/pets/{petId}/deworming")
    public ResponseEntity<?> getDewormingByPet(@PathVariable Long petId) {
        try {
            log.info("Fetching deworming history for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            List<DewormingResponse> deworming = dewormingService.getDewormingByPet(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deworming history retrieved successfully");
            response.put("data", deworming);
            response.put("count", deworming.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving deworming history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve deworming history"));
        }
    }
    
    /**
     * FR-DEW-002: Record deworming (doctor only)
     * POST /api/doctor/pets/{petId}/deworming
     */
    @PostMapping("/doctor/pets/{petId}/deworming")
    public ResponseEntity<?> recordDeworming(
            @PathVariable Long petId,
            @Valid @RequestBody CreateDewormingRequest request) {
        try {
            log.info("Recording deworming for pet ID: {}", petId);
            
            User doctor = getAuthenticatedUser();
            DewormingResponse deworming = dewormingService.recordDeworming(petId, request, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deworming recorded successfully");
            response.put("data", deworming);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error recording deworming: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record deworming"));
        }
    }
    
    /**
     * FR-DEW-002: Get due deworming records across all pets for an owner
     * GET /api/owner/deworming-due
     */
    @GetMapping("/owner/deworming-due")
    public ResponseEntity<?> getDueDeworm() {
        try {
            log.info("Fetching due deworming for owner");
            
            User owner = getAuthenticatedUser();
            List<Map<String, Object>> dueDeworming = dewormingService.getDueDewormingForOwner(owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Due deworming retrieved successfully");
            response.put("data", dueDeworming);
            response.put("count", dueDeworming.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving due deworming: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve due deworming"));
        }
    }
    
    /**
     * Get upcoming deworming for a pet (within 30 days)
     * GET /api/owner/pets/{petId}/deworming/upcoming
     */
    @GetMapping("/owner/pets/{petId}/deworming/upcoming")
    public ResponseEntity<?> getUpcomingDeworming(@PathVariable Long petId) {
        try {
            log.info("Fetching upcoming deworming for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            List<DewormingResponse> upcomingDeworming = dewormingService.getUpcomingDeworming(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Upcoming deworming retrieved successfully");
            response.put("data", upcomingDeworming);
            response.put("count", upcomingDeworming.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving upcoming deworming: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve upcoming deworming"));
        }
    }
    
    /**
     * Helper method to get authenticated user
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalUser)) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        PrincipalUser principalUser = (PrincipalUser) authentication.getPrincipal();
        return userService.getUserById(principalUser.getUserId());
    }
}
