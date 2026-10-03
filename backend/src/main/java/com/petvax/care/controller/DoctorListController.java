package com.petvax.care.controller;

import com.petvax.care.dto.DoctorListResponse;
import com.petvax.care.service.DoctorListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/doctors")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class DoctorListController {
    
    @Autowired
    private DoctorListService doctorListService;
    
    /**
     * Get list of all available doctors
     * GET /api/owner/doctors
     */
    @GetMapping
    public ResponseEntity<?> listDoctors(
            @RequestParam(value = "specialty", required = false) String specialty) {
        try {
            log.info("Fetching list of available doctors. Specialty filter: {}", specialty);
            
            List<DoctorListResponse> doctors;
            if (specialty != null && !specialty.trim().isEmpty()) {
                doctors = doctorListService.filterBySpecialty(specialty);
                log.info("Found {} doctors with specialty: {}", doctors.size(), specialty);
            } else {
                doctors = doctorListService.getAvailableDoctors();
                log.info("Found {} available doctors", doctors.size());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctors retrieved successfully");
            response.put("data", doctors);
            response.put("count", doctors.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving doctors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve doctors"));
        }
    }
    
    /**
     * Get detailed information about a specific doctor
     * GET /api/owner/doctors/{doctorId}
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorDetails(@PathVariable Long doctorId) {
        try {
            log.info("Fetching details for doctor ID: {}", doctorId);
            
            DoctorListResponse doctor = doctorListService.getDoctorDetails(doctorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor details retrieved successfully");
            response.put("data", doctor);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Doctor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving doctor details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve doctor details"));
        }
    }
    
    /**
     * Get availability summary for a specific doctor
     * GET /api/owner/doctors/{doctorId}/availability
     */
    @GetMapping("/{doctorId}/availability")
    public ResponseEntity<?> getDoctorAvailability(@PathVariable Long doctorId) {
        try {
            log.info("Fetching availability for doctor ID: {}", doctorId);
            
            Integer availableSlots = doctorListService.getAvailableSlotCount(doctorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor availability retrieved successfully");
            response.put("doctorId", doctorId);
            response.put("availableSlotsThisWeek", availableSlots);
            response.put("isAvailable", availableSlots > 0);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Doctor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving doctor availability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve doctor availability"));
        }
    }
}
