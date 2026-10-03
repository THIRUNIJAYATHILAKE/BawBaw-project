package com.petvax.care.controller;

import com.petvax.care.dto.CreateVaccinationRequest;
import com.petvax.care.dto.VaccinationResponse;
import com.petvax.care.dto.VaccinationDueDto;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.VaccinationService;
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
public class VaccinationController {
    
    @Autowired
    private VaccinationService vaccinationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * FR-VAC-001: Get list of vaccinations for a pet
     * GET /api/owner/pets/{petId}/vaccinations
     */
    @GetMapping("/owner/pets/{petId}/vaccinations")
    public ResponseEntity<?> getVaccinationsByPet(@PathVariable Long petId) {
        try {
            log.info("Fetching vaccinations for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            List<VaccinationResponse> vaccinations = vaccinationService.getVaccinationsByPet(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vaccinations retrieved successfully");
            response.put("data", vaccinations);
            response.put("count", vaccinations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving vaccinations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve vaccinations"));
        }
    }
    
    /**
     * FR-VAC-001: Get recommended vaccination schedule for a pet
     * GET /api/owner/pets/{petId}/vaccination-schedule
     */
    @GetMapping("/owner/pets/{petId}/vaccination-schedule")
    public ResponseEntity<?> getVaccinationSchedule(@PathVariable Long petId) {
        try {
            log.info("Fetching vaccination schedule for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            Map<String, Object> schedule = vaccinationService.getVaccinationSchedule(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vaccination schedule retrieved successfully");
            response.put("data", schedule);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving vaccination schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve vaccination schedule"));
        }
    }
    
    /**
     * FR-VAC-002: Record a vaccination (doctor only)
     * POST /api/doctor/pets/{petId}/vaccinations
     */
    @PostMapping("/doctor/pets/{petId}/vaccinations")
    public ResponseEntity<?> recordVaccination(
            @PathVariable Long petId,
            @Valid @RequestBody CreateVaccinationRequest request) {
        try {
            log.info("Recording vaccination for pet ID: {}", petId);
            
            User doctor = getAuthenticatedUser();
            VaccinationResponse vaccination = vaccinationService.recordVaccination(petId, request, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vaccination recorded successfully");
            response.put("data", vaccination);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error recording vaccination: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record vaccination"));
        }
    }
    
    /**
     * FR-VAC-002: Get due vaccinations across all pets for an owner
     * GET /api/owner/vaccinations-due
     */
    @GetMapping("/owner/vaccinations-due")
    public ResponseEntity<?> getDueVaccinations() {
        try {
            log.info("Fetching due vaccinations for owner");
            
            User owner = getAuthenticatedUser();
            List<VaccinationDueDto> dueVaccinations = vaccinationService.getDueVaccinationsForOwner(owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Due vaccinations retrieved successfully");
            response.put("data", dueVaccinations);
            response.put("count", dueVaccinations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving due vaccinations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve due vaccinations"));
        }
    }
    
    /**
     * Get upcoming vaccinations for a pet (within 30 days)
     * GET /api/owner/pets/{petId}/vaccinations/upcoming
     */
    @GetMapping("/owner/pets/{petId}/vaccinations/upcoming")
    public ResponseEntity<?> getUpcomingVaccinations(@PathVariable Long petId) {
        try {
            log.info("Fetching upcoming vaccinations for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            List<VaccinationResponse> upcomingVaccinations = vaccinationService.getUpcomingVaccinations(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Upcoming vaccinations retrieved successfully");
            response.put("data", upcomingVaccinations);
            response.put("count", upcomingVaccinations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving upcoming vaccinations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve upcoming vaccinations"));
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
