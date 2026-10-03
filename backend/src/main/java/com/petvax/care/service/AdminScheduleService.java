package com.petvax.care.service;

import com.petvax.care.dto.AppointmentFilterRequest;
import com.petvax.care.dto.AppointmentResponse;
import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.AppointmentStatus;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.repository.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AdminScheduleService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get global appointment schedule across all doctors
     */
    public List<AppointmentResponse> getGlobalAppointmentSchedule(String startDate, String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", DATETIME_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", DATETIME_FORMATTER);
            
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getAppointmentDate().isAfter(start) && a.getAppointmentDate().isBefore(end)
                            && a.getStatus() != AppointmentStatus.CANCELLED)
                    .collect(Collectors.toList());
            
            return appointments.stream()
                    .map(this::buildAppointmentResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving global appointment schedule: {}", e.getMessage());
            throw new AuthenticationException("Invalid date format", "INVALID_DATE", 400);
        }
    }
    
    /**
     * Filter appointments by status (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW)
     */
    public List<AppointmentResponse> filterAppointmentsByStatus(String status) {
        try {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
            
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getStatus() == appointmentStatus)
                    .collect(Collectors.toList());
            
            return appointments.stream()
                    .map(this::buildAppointmentResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid appointment status", "INVALID_STATUS", 400);
        }
    }
    
    /**
     * Filter appointments by date range
     */
    public List<AppointmentResponse> filterAppointmentsByDate(String startDate, String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", DATETIME_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", DATETIME_FORMATTER);
            
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getAppointmentDate().isAfter(start) && a.getAppointmentDate().isBefore(end))
                    .collect(Collectors.toList());
            
            return appointments.stream()
                    .map(this::buildAppointmentResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error filtering appointments by date: {}", e.getMessage());
            throw new AuthenticationException("Invalid date format", "INVALID_DATE", 400);
        }
    }
    
    /**
     * Filter appointments by status and date range
     */
    public List<AppointmentResponse> filterAppointments(AppointmentFilterRequest request) {
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
                start = LocalDateTime.parse(request.getStartDate() + " 00:00:00", DATETIME_FORMATTER);
            }
            
            if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
                end = LocalDateTime.parse(request.getEndDate() + " 23:59:59", DATETIME_FORMATTER);
            }
            
            AppointmentStatus status = null;
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                status = AppointmentStatus.valueOf(request.getStatus().toUpperCase());
            }
            
            LocalDateTime finalStart = start;
            LocalDateTime finalEnd = end;
            AppointmentStatus finalStatus = status;
            
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(a -> {
                        if (finalStatus != null && a.getStatus() != finalStatus) {
                            return false;
                        }
                        if (finalStart != null && a.getAppointmentDate().isBefore(finalStart)) {
                            return false;
                        }
                        if (finalEnd != null && a.getAppointmentDate().isAfter(finalEnd)) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            
            return appointments.stream()
                    .map(this::buildAppointmentResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error filtering appointments: {}", e.getMessage());
            throw new AuthenticationException("Invalid filter parameters", "INVALID_FILTER", 400);
        }
    }
    
    /**
     * Configure clinic time slots (opening and closing times)
     * This is a simple implementation - can be extended to support multiple clinics, multiple days, etc.
     */
    public ClinicScheduleResponse configureClinicTimeSlots(String dayOfWeek, String openingTime, String closingTime, String adminUsername) {
        try {
            // Validate time format (HH:mm)
            LocalTime opening = LocalTime.parse(openingTime);
            LocalTime closing = LocalTime.parse(closingTime);
            
            if (closing.isBefore(opening) || closing.equals(opening)) {
                throw new AuthenticationException("Closing time must be after opening time", "INVALID_TIME", 400);
            }
            
            // In a real implementation, this would save to a clinic_schedules table
            // For now, we'll return a response indicating the configuration
            log.info("Clinic time slots configured by admin {}: {} {} - {}", adminUsername, dayOfWeek, openingTime, closingTime);
            
            return ClinicScheduleResponse.builder()
                    .dayOfWeek(dayOfWeek)
                    .openingTime(openingTime)
                    .closingTime(closingTime)
                    .message("Clinic time slots configured successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error configuring clinic time slots: {}", e.getMessage());
            throw new AuthenticationException("Invalid time format", "INVALID_TIME", 400);
        }
    }
    
    // Helper methods
    
    private AppointmentResponse buildAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .petId(appointment.getPet().getId())
                .petName(appointment.getPet().getName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .ownerId(appointment.getOwner().getId())
                .ownerName(appointment.getOwner().getFullName())
                .appointmentDate(appointment.getAppointmentDate())
                .durationMinutes(appointment.getDurationMinutes())
                .status(appointment.getStatus())
                .purpose(appointment.getPurpose())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .cancelledAt(appointment.getCancelledAt())
                .cancelledReason(appointment.getCancelledReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ClinicScheduleResponse {
        private String dayOfWeek;
        private String openingTime;
        private String closingTime;
        private String message;
    }
}
