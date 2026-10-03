package com.petvax.care.controller;

import com.petvax.care.dto.*;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.service.AdminScheduleService;
import com.petvax.care.service.AdminUserService;
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
@RequestMapping("/api/admin")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class AdminController {
    
    @Autowired
    private AdminUserService adminUserService;
    
    @Autowired
    private AdminScheduleService adminScheduleService;
    
    /**
     * GET /api/admin/users - List all users with optional filters
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        try {
            log.info("Fetching users with role: {}, status: {}", role, status);
            
            List<UserManagementResponse> users = adminUserService.getAllUsers(role, status, pageNumber, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("totalCount", users.size());
            response.put("pageNumber", pageNumber);
            response.put("pageSize", pageSize);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch users", "FETCH_ERROR"));
        }
    }
    
    /**
     * GET /api/admin/users/pending-verification - Get pending doctor/assistant verifications
     */
    @GetMapping("/users/pending-verification")
    public ResponseEntity<?> getPendingVerifications() {
        try {
            log.info("Fetching pending verifications");
            
            List<UserManagementResponse> pendingUsers = adminUserService.getPendingVerifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pendingUsers", pendingUsers);
            response.put("totalPending", pendingUsers.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching pending verifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch pending verifications", "FETCH_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/{userId}/verify-doctor - Verify/Approve doctor
     */
    @PostMapping("/users/{userId}/verify-doctor")
    public ResponseEntity<?> verifyDoctor(@PathVariable Long userId) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Verifying doctor: {}", userId);
            
            UserManagementResponse response = adminUserService.verifyDoctor(userId, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error verifying doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to verify doctor", "VERIFICATION_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/{userId}/verify-assistant - Verify/Approve assistant
     */
    @PostMapping("/users/{userId}/verify-assistant")
    public ResponseEntity<?> verifyAssistant(@PathVariable Long userId) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Verifying assistant: {}", userId);
            
            UserManagementResponse response = adminUserService.verifyAssistant(userId, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error verifying assistant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to verify assistant", "VERIFICATION_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/{userId}/reject - Reject pending account
     */
    @PostMapping("/users/{userId}/reject")
    public ResponseEntity<?> rejectUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Rejecting user: {}", userId);
            
            UserManagementResponse response = adminUserService.rejectPendingUser(userId, reason, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error rejecting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to reject user", "REJECTION_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/{userId}/suspend - Suspend user
     */
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Suspending user: {}", userId);
            
            UserManagementResponse response = adminUserService.suspendUser(userId, reason, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error suspending user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to suspend user", "SUSPENSION_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/{userId}/enable - Enable suspended/inactive user
     */
    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Enabling user: {}", userId);
            
            UserManagementResponse response = adminUserService.enableUser(userId, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error enabling user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to enable user", "ENABLE_ERROR"));
        }
    }
    
    /**
     * DELETE /api/admin/users/{userId} - Soft delete user
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Soft deleting user: {}", userId);
            
            UserManagementResponse response = adminUserService.deleteUser(userId, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete user", "DELETE_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/add-doctor - Add new doctor account
     */
    @PostMapping("/users/add-doctor")
    public ResponseEntity<?> addNewDoctor(@Valid @RequestBody AddDoctorRequest request) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Adding new doctor: {}", request.getUsername());
            
            UserManagementResponse response = adminUserService.addNewDoctor(request, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error adding doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to add doctor", "ADD_DOCTOR_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/users/add-assistant - Add new assistant account
     */
    @PostMapping("/users/add-assistant")
    public ResponseEntity<?> addNewAssistant(@Valid @RequestBody AddAssistantRequest request) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Adding new assistant: {}", request.getUsername());
            
            UserManagementResponse response = adminUserService.addNewAssistant(request, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error adding assistant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to add assistant", "ADD_ASSISTANT_ERROR"));
        }
    }
    
    /**
     * GET /api/admin/appointments - Get all appointments with optional filters
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("Fetching appointments with filters: status={}, startDate={}, endDate={}", 
                    status, startDate, endDate);
            
            List<AppointmentResponse> appointments;
            
            if (startDate != null && endDate != null) {
                appointments = adminScheduleService.filterAppointmentsByDate(startDate, endDate);
            } else if (status != null) {
                appointments = adminScheduleService.filterAppointmentsByStatus(status);
            } else {
                appointments = adminScheduleService.getGlobalAppointmentSchedule(
                        java.time.LocalDate.now().toString(),
                        java.time.LocalDate.now().plusDays(30).toString()
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointments", appointments);
            response.put("totalCount", appointments.size());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error fetching appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch appointments", "FETCH_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/appointments/filter - Filter appointments with advanced options
     */
    @PostMapping("/appointments/filter")
    public ResponseEntity<?> filterAppointments(@Valid @RequestBody AppointmentFilterRequest request) {
        try {
            log.info("Filtering appointments with advanced filters");
            
            List<AppointmentResponse> appointments = adminScheduleService.filterAppointments(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointments", appointments);
            response.put("totalCount", appointments.size());
            response.put("pageNumber", request.getPageNumber());
            response.put("pageSize", request.getPageSize());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error filtering appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to filter appointments", "FILTER_ERROR"));
        }
    }
    
    /**
     * POST /api/admin/schedule/configure - Configure clinic time slots
     */
    @PostMapping("/schedule/configure")
    public ResponseEntity<?> configureClinicSchedule(
            @RequestParam String dayOfWeek,
            @RequestParam String openingTime,
            @RequestParam String closingTime) {
        try {
            String adminUsername = getAuthenticatedUsername();
            log.info("Configuring clinic schedule for {}: {} - {}", dayOfWeek, openingTime, closingTime);
            
            AdminScheduleService.ClinicScheduleResponse response = adminScheduleService.configureClinicTimeSlots(
                    dayOfWeek, openingTime, closingTime, adminUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("schedule", response);
            
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error configuring clinic schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to configure schedule", "CONFIG_ERROR"));
        }
    }
    
    // Helper methods
    
    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
    
    private ResponseEntity<?> handleAuthException(AuthenticationException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", e.getMessage());
        error.put("errorCode", e.getErrorCode());
        return ResponseEntity.status(e.getStatusCode()).body(error);
    }
    
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("errorCode", errorCode);
        return error;
    }
}
