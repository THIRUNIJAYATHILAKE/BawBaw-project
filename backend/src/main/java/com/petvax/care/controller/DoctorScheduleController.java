package com.petvax.care.controller;

import com.petvax.care.dto.ScheduleItemDto;
import com.petvax.care.dto.AppointmentResponse;
import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.User;
import com.petvax.care.entity.UserRole;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.security.PrincipalUser;
import com.petvax.care.service.DoctorScheduleService;
import com.petvax.care.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private UserService userService;

    /**
     * Get doctor's full weekly schedule
     * GET /api/doctor/schedule
     */
    @GetMapping("/schedule")
    public ResponseEntity<?> getWeeklySchedule() {
        try {
            log.info("Fetching weekly schedule for doctor");
            
            User doctor = getAuthenticatedDoctor();
            
            Map<String, Object> weeklySchedule = doctorScheduleService.getDoctorSchedule(doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Weekly schedule retrieved successfully");
            response.put("data", weeklySchedule);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching weekly schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching weekly schedule: " + e.getMessage()));
        }
    }

    /**
     * Get schedule for a specific date
     * GET /api/doctor/schedule?date=YYYY-MM-DD
     */
    @GetMapping("/schedule/by-date")
    public ResponseEntity<?> getScheduleByDate(@RequestParam(required = false) String date) {
        try {
            log.info("Fetching schedule for date: {}", date);
            
            User doctor = getAuthenticatedDoctor();
            
            LocalDate scheduleDate;
            if (date == null || date.trim().isEmpty()) {
                scheduleDate = LocalDate.now();
            } else {
                scheduleDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            List<ScheduleItemDto> appointments = doctorScheduleService.getScheduleByDate(doctor, scheduleDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Schedule for date retrieved successfully");
            response.put("date", scheduleDate);
            response.put("appointmentCount", appointments.size());
            response.put("appointments", appointments);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching schedule by date: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching schedule: " + e.getMessage()));
        }
    }

    /**
     * Get appointment details
     * GET /api/doctor/appointments/{appointmentId}
     */
    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> getAppointmentDetails(@PathVariable Long appointmentId) {
        try {
            log.info("Fetching appointment details for ID: {}", appointmentId);
            
            User doctor = getAuthenticatedDoctor();
            
            ScheduleItemDto appointmentDetails = doctorScheduleService.getAppointmentDetails(appointmentId, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment details retrieved successfully");
            response.put("data", appointmentDetails);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching appointment details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching appointment details: " + e.getMessage()));
        }
    }

    /**
     * Mark appointment as completed
     * POST /api/doctor/appointments/{appointmentId}/complete
     */
    @PostMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<?> markAppointmentCompleted(@PathVariable Long appointmentId) {
        try {
            log.info("Marking appointment ID: {} as completed", appointmentId);
            
            User doctor = getAuthenticatedDoctor();
            
            Appointment completedAppointment = doctorScheduleService.markAppointmentCompleted(appointmentId, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment marked as completed");
            response.put("appointmentId", completedAppointment.getId());
            response.put("status", completedAppointment.getStatus());
            response.put("timestamp", completedAppointment.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking appointment as completed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error marking appointment: " + e.getMessage()));
        }
    }

    /**
     * Mark appointment as no-show
     * POST /api/doctor/appointments/{appointmentId}/no-show
     */
    @PostMapping("/appointments/{appointmentId}/no-show")
    public ResponseEntity<?> markAppointmentNoShow(@PathVariable Long appointmentId) {
        try {
            log.info("Marking appointment ID: {} as no-show", appointmentId);
            
            User doctor = getAuthenticatedDoctor();
            
            Appointment noShowAppointment = doctorScheduleService.markAppointmentNoShow(appointmentId, doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment marked as no-show");
            response.put("appointmentId", noShowAppointment.getId());
            response.put("status", noShowAppointment.getStatus());
            response.put("timestamp", noShowAppointment.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking appointment as no-show: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error marking appointment: " + e.getMessage()));
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
