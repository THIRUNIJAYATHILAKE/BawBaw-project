package com.petvax.care.controller;

import com.petvax.care.dto.CreateMedicalRecordRequest;
import com.petvax.care.dto.MedicalRecordResponse;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.MedicalRecordService;
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
public class MedicalRecordController {
    
    @Autowired
    private MedicalRecordService medicalRecordService;
    
    @Autowired
    private UserService userService;
    
    /**
     * FR-MED-001: Get medical history for a pet (read-only for owners)
     * GET /api/owner/pets/{petId}/medical-records
     */
    @GetMapping("/owner/pets/{petId}/medical-records")
    public ResponseEntity<?> getMedicalHistory(@PathVariable Long petId) {
        try {
            log.info("Fetching medical history for pet ID: {}", petId);
            
            User owner = getAuthenticatedUser();
            List<MedicalRecordResponse> records = medicalRecordService.getMedicalHistory(petId, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical history retrieved successfully");
            response.put("data", records);
            response.put("count", records.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving medical history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve medical history"));
        }
    }
    
    /**
     * FR-MED-001: Get full medical history for a doctor
     * GET /api/doctor/pets/{petId}/medical-records
     */
    @GetMapping("/doctor/pets/{petId}/medical-records")
    public ResponseEntity<?> getMedicalHistoryForDoctor(@PathVariable Long petId) {
        try {
            log.info("Fetching medical history for pet ID: {} (doctor view)", petId);
            
            User doctor = getAuthenticatedUser();
            List<MedicalRecordResponse> records = medicalRecordService.getMedicalHistoryForDoctor(petId, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical history retrieved successfully");
            response.put("data", records);
            response.put("count", records.size());
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving medical history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve medical history"));
        }
    }
    
    /**
     * FR-MED-002: Create a new medical record (doctor only)
     * POST /api/doctor/pets/{petId}/medical-records
     */
    @PostMapping("/doctor/pets/{petId}/medical-records")
    public ResponseEntity<?> createMedicalRecord(
            @PathVariable Long petId,
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        try {
            log.info("Creating medical record for pet ID: {}", petId);
            
            User doctor = getAuthenticatedUser();
            MedicalRecordResponse record = medicalRecordService.createMedicalRecord(petId, request, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical record created successfully");
            response.put("data", record);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating medical record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create medical record"));
        }
    }
    
    /**
     * FR-MED-002: Update a medical record (doctor only, with audit logging)
     * PUT /api/doctor/pets/{petId}/medical-records/{recordId}
     */
    @PutMapping("/doctor/pets/{petId}/medical-records/{recordId}")
    public ResponseEntity<?> editMedicalRecord(
            @PathVariable Long petId,
            @PathVariable Long recordId,
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        try {
            log.info("Updating medical record ID: {} for pet ID: {}", recordId, petId);
            
            User doctor = getAuthenticatedUser();
            MedicalRecordResponse record = medicalRecordService.editMedicalRecord(petId, recordId, request, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical record updated successfully");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating medical record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update medical record"));
        }
    }
    
    /**
     * FR-MED-003: Get medical records by type for a pet
     * GET /api/owner/pets/{petId}/medical-records/{type}
     */
    @GetMapping("/owner/pets/{petId}/medical-records/{type}")
    public ResponseEntity<?> getMedicalRecordsByType(
            @PathVariable Long petId,
            @PathVariable String type) {
        try {
            log.info("Fetching {} records for pet ID: {}", type, petId);
            
            User owner = getAuthenticatedUser();
            List<MedicalRecordResponse> records = medicalRecordService.getMedicalRecordsByType(petId, type, owner);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", String.format("%s records retrieved successfully", type));
            response.put("data", records);
            response.put("count", records.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving records by type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve records"));
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
