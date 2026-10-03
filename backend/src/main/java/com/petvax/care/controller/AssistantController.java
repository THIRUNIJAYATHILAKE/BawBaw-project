package com.petvax.care.controller;

import com.petvax.care.dto.AppointmentResponse;
import com.petvax.care.dto.CheckInOutRequest;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.service.AssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class AssistantController {
    
    @Autowired
    private AssistantService assistantService;
    
    /**
     * GET /api/assistant/schedule - Get clinic schedule for a specific date
     */
    @GetMapping("/schedule")
    public ResponseEntity<?> getClinicSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }
            
            log.info("Fetching clinic schedule for date: {}", date);
            
            List<AppointmentResponse> appointments = assistantService.getClinicSchedule(date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", date);
            response.put("appointments", appointments);
            response.put("totalAppointments", appointments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching clinic schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch clinic schedule", "SCHEDULE_ERROR"));
        }
    }
    
    /**
     * GET /api/assistant/appointments/today - Get today's appointments
     */
    @GetMapping("/appointments/today")
    public ResponseEntity<?> getTodayAppointments() {
        try {
            log.info("Fetching today's appointments");
            
            List<AppointmentResponse> appointments = assistantService.getTodaySchedule();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", LocalDate.now());
            response.put("appointments", appointments);
            response.put("totalAppointments", appointments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching today's appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch today's appointments", "SCHEDULE_ERROR"));
        }
    }
    
    /**
     * POST /api/assistant/appointments/{appointmentId}/check-in - Mark patient check-in
     */
    @PostMapping("/appointments/{appointmentId}/check-in")
    public ResponseEntity<?> markCheckIn(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) CheckInOutRequest request) {
        try {
            String notes = request != null ? request.getNotes() : null;
            
            log.info("Marking check-in for appointment: {}", appointmentId);
            
            Map<String, Object> result = assistantService.markCheckIn(appointmentId, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(result);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error marking check-in: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to mark check-in", "CHECK_IN_ERROR"));
        }
    }
    
    /**
     * POST /api/assistant/appointments/{appointmentId}/check-out - Mark patient check-out
     */
    @PostMapping("/appointments/{appointmentId}/check-out")
    public ResponseEntity<?> markCheckOut(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) CheckInOutRequest request) {
        try {
            String notes = request != null ? request.getNotes() : null;
            
            log.info("Marking check-out for appointment: {}", appointmentId);
            
            Map<String, Object> result = assistantService.markCheckOut(appointmentId, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(result);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error marking check-out: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to mark check-out", "CHECK_OUT_ERROR"));
        }
    }
    
    /**
     * PUT /api/assistant/appointments/{appointmentId}/time-slot - Update appointment time slot
     */
    @PutMapping("/appointments/{appointmentId}/time-slot")
    public ResponseEntity<?> updateAppointmentTimeSlot(
            @PathVariable Long appointmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime) {
        try {
            log.info("Updating appointment {} time slot to: {}", appointmentId, newDateTime);
            
            AppointmentResponse appointment = assistantService.updateAppointmentTimeSlot(appointmentId, newDateTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointment", appointment);
            response.put("message", "Appointment time slot updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error updating appointment time slot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update appointment time slot", "UPDATE_ERROR"));
        }
    }
    
    /**
     * GET /api/assistant/appointments/{appointmentId}/check-in-info - Get check-in information
     */
    @GetMapping("/appointments/{appointmentId}/check-in-info")
    public ResponseEntity<?> getCheckInInfo(@PathVariable Long appointmentId) {
        try {
            log.info("Fetching check-in info for appointment: {}", appointmentId);
            
            Map<String, Object> info = assistantService.getCheckInInfo(appointmentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(info);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return handleAuthException(e);
        } catch (Exception e) {
            log.error("Error fetching check-in info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch check-in info", "INFO_ERROR"));
        }
    }
    
    /**
     * GET /api/assistant/schedule/today-with-status - Get today's schedule with check-in status
     */
    @GetMapping("/schedule/today-with-status")
    public ResponseEntity<?> getTodayScheduleWithStatus() {
        try {
            log.info("Fetching today's schedule with check-in status");
            
            List<Map<String, Object>> appointments = assistantService.getTodayScheduleWithCheckInStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", LocalDate.now());
            response.put("appointments", appointments);
            response.put("totalAppointments", appointments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching today's schedule with status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch schedule", "SCHEDULE_ERROR"));
        }
    }
    
    // Helper methods
    
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
