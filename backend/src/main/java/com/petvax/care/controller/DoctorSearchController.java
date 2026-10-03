package com.petvax.care.controller;

import com.petvax.care.dto.*;
import com.petvax.care.entity.MedicalRecord;
import com.petvax.care.entity.User;
import com.petvax.care.entity.UserRole;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.DoctorSearchService;
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
@RequestMapping("/api/doctor")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class DoctorSearchController {

    @Autowired
    private DoctorSearchService doctorSearchService;

    @Autowired
    private UserService userService;

    /**
     * Search for pets by name, owner name, microchip ID, or pet ID
     * GET /api/doctor/search?query=searchTerm
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPets(@RequestParam(required = false) String query,
                                        @RequestParam(required = false) String type) {
        try {
            log.info("Doctor searching for pets with query: {}, type: {}", query, type);
            
            User doctor = getAuthenticatedDoctor();
            
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Search query is required"));
            }
            
            List<PetResponse> results = doctorSearchService.searchPets(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Search completed successfully");
            response.put("count", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error searching pets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error searching pets: " + e.getMessage()));
        }
    }

    /**
     * Get full medical view of a pet
     * GET /api/doctor/pets/{petId}/medical-view
     */
    @GetMapping("/pets/{petId}/medical-view")
    public ResponseEntity<?> getPetMedicalView(@PathVariable Long petId) {
        try {
            log.info("Doctor fetching medical view for pet ID: {}", petId);
            
            User doctor = getAuthenticatedDoctor();
            
            PetMedicalViewDto medicalView = doctorSearchService.getPetMedicalView(petId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical view retrieved successfully");
            response.put("data", medicalView);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching medical view: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching medical view: " + e.getMessage()));
        }
    }

    /**
     * Get owner details
     * GET /api/doctor/pets/{petId}/owner-details
     */
    @GetMapping("/pets/{petId}/owner-details")
    public ResponseEntity<?> getOwnerDetails(@PathVariable Long petId) {
        try {
            log.info("Doctor fetching owner details for pet ID: {}", petId);
            
            User doctor = getAuthenticatedDoctor();
            
            OwnerDetailsDto ownerDetails = doctorSearchService.getOwnerDetails(petId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Owner details retrieved successfully");
            response.put("data", ownerDetails);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching owner details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching owner details: " + e.getMessage()));
        }
    }

    /**
     * Record a diagnosis/medical record for a pet
     * POST /api/doctor/pets/{petId}/diagnosis
     */
    @PostMapping("/pets/{petId}/diagnosis")
    public ResponseEntity<?> recordDiagnosis(@PathVariable Long petId,
                                            @Valid @RequestBody DiagnosisRecordRequest request) {
        try {
            log.info("Doctor recording diagnosis for pet ID: {}", petId);
            
            User doctor = getAuthenticatedDoctor();
            
            MedicalRecord medicalRecord = doctorSearchService.recordDiagnosis(petId, doctor, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Diagnosis recorded successfully");
            response.put("recordId", medicalRecord.getId());
            response.put("timestamp", medicalRecord.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            log.error("Error recording diagnosis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error recording diagnosis: " + e.getMessage()));
        }
    }

    /**
     * Edit medical record
     * PUT /api/doctor/medical-records/{recordId}
     */
    @PutMapping("/medical-records/{recordId}")
    public ResponseEntity<?> editMedicalRecord(@PathVariable Long recordId,
                                              @Valid @RequestBody DiagnosisRecordRequest request) {
        try {
            log.info("Doctor editing medical record ID: {}", recordId);
            
            User doctor = getAuthenticatedDoctor();
            
            MedicalRecord updatedRecord = doctorSearchService.editMedicalRecord(recordId, doctor, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical record updated successfully");
            response.put("recordId", updatedRecord.getId());
            response.put("timestamp", updatedRecord.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error editing medical record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error editing medical record: " + e.getMessage()));
        }
    }

    /**
     * Get authenticated user and verify they are a doctor
     */
    private User getAuthenticatedDoctor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalUser)) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        PrincipalUser principalUser = (PrincipalUser) authentication.getPrincipal();
        User user = userService.getUserById(principalUser.getUserId());
        
        if (user.getRole() != UserRole.DOCTOR) {
            throw new UnauthorizedException("Only doctors can access this resource");
        }
        
        return user;
    }
}
