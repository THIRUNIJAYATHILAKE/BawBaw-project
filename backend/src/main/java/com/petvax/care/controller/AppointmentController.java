package com.petvax.care.controller;

import com.petvax.care.dto.AppointmentResponse;
import com.petvax.care.dto.AvailableDoctorDto;
import com.petvax.care.dto.AvailableSlotDto;
import com.petvax.care.dto.BookAppointmentRequest;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.AppointmentService;
import com.petvax.care.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
@Slf4j
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserService userService;
    
    /**
     * FR-BK-001: Get list of available doctors with specialties and slot counts
     * GET /api/owner/doctors
     */
    @GetMapping("/owner/doctors")
    public ResponseEntity<List<AvailableDoctorDto>> getAvailableDoctors() {
        try {
            log.info("Fetching available doctors");
            return ResponseEntity.ok(appointmentService.getAvailableDoctors());
        } catch (Exception e) {
            log.error("Error fetching available doctors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * FR-BK-002: Get available time slots for a doctor on a specific date (calendar view)
     * GET /api/owner/doctors/{doctorId}/available-slots?date=YYYY-MM-DD
     */
    @GetMapping("/owner/doctors/{doctorId}/available-slots")
    public ResponseEntity<List<AvailableSlotDto>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Fetching available slots for doctor {} on {}", doctorId, date);
            return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
        } catch (Exception e) {
            log.error("Error fetching available slots: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * FR-BK-003 & FR-BK-004: Book appointment with purpose specification and confirmation
     * POST /api/owner/appointments
     * Body: { petId, doctorId, appointmentDate, purpose, reason }
     */
    @PostMapping("/owner/appointments")
    public ResponseEntity<?> bookAppointment(
            @RequestBody BookAppointmentRequest request) {
        try {
            log.info("Booking appointment for pet {} with doctor {}", request.getPetId(), request.getDoctorId());
            
            User owner = getAuthenticatedUser();
            AppointmentResponse appointment = appointmentService.bookAppointment(owner.getId(), request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment booked successfully");
            response.put("appointment", appointment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error booking appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * FR-BK-005: Get owner's appointments
     * GET /api/owner/appointments
     */
    @GetMapping("/owner/appointments")
    public ResponseEntity<List<AppointmentResponse>> getOwnerAppointments() {
        try {
            User owner = getAuthenticatedUser();
            log.info("Fetching appointments for owner {}", owner.getId());
            return ResponseEntity.ok(appointmentService.getAppointmentsByOwner(owner.getId()));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching owner appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get appointment details
     * GET /api/owner/appointments/{appointmentId}
     */
    @GetMapping("/owner/appointments/{appointmentId}")
    public ResponseEntity<?> getAppointmentDetails(@PathVariable Long appointmentId) {
        try {
            User user = getAuthenticatedUser();
            log.info("Fetching appointment details for appointment {}", appointmentId);
            AppointmentResponse appointment = appointmentService.getAppointmentDetails(appointmentId, user.getId());
            return ResponseEntity.ok(appointment);
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching appointment details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * FR-BK-006: Cancel appointment (2-hour cancellation window)
     * POST /api/owner/appointments/{appointmentId}/cancel
     * Body: { cancelReason }
     */
    @PostMapping("/owner/appointments/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {
        try {
            User user = getAuthenticatedUser();
            String cancelReason = request.getOrDefault("cancelReason", "User requested cancellation");
            log.info("Cancelling appointment {} for user {}", appointmentId, user.getId());
            
            AppointmentResponse appointment = appointmentService.cancelAppointment(appointmentId, user.getId(), cancelReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment cancelled successfully");
            response.put("appointment", appointment);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized cancellation attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get doctor's appointments
     * GET /api/doctor/appointments
     */
    @GetMapping("/doctor/appointments")
    public ResponseEntity<?> getDoctorAppointments() {
        try {
            User doctor = getAuthenticatedUser();
            log.info("Fetching appointments for doctor {}", doctor.getId());
            return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctor.getId()));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching doctor appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get doctor's appointments for a specific date
     * GET /api/doctor/appointments/by-date?date=YYYY-MM-DD
     */
    @GetMapping("/doctor/appointments/by-date")
    public ResponseEntity<?> getDoctorAppointmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            User doctor = getAuthenticatedUser();
            log.info("Fetching appointments for doctor {} on {}", doctor.getId(), date);
            return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorAndDate(doctor.getId(), date));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching doctor appointments by date: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * FR-BK-007: Mark appointment as completed or no-show
     * POST /api/doctor/appointments/{appointmentId}/complete
     * Body: { status } (COMPLETED or NO_SHOW)
     */
    @PostMapping("/doctor/appointments/{appointmentId}/complete")
    public ResponseEntity<?> completeAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {
        try {
            User doctor = getAuthenticatedUser();
            String status = request.getOrDefault("status", "COMPLETED");
            log.info("Marking appointment {} as {} by doctor {}", appointmentId, status, doctor.getId());
            
            AppointmentResponse appointment;
            if ("NO_SHOW".equals(status)) {
                appointment = appointmentService.markNoShow(appointmentId, doctor.getId());
            } else {
                appointment = appointmentService.completeAppointment(appointmentId, doctor.getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment status updated successfully");
            response.put("appointment", appointment);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized update attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating appointment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Helper method to get authenticated user from security context
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

