package com.petvax.care.service;

import com.petvax.care.dto.AppointmentResponse;
import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.AppointmentStatus;
import com.petvax.care.entity.User;
import com.petvax.care.exception.AuthenticationException;
import com.petvax.care.repository.AppointmentRepository;
import com.petvax.care.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AssistantService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Store check-in and check-out times in-memory (in production, use a database table)
    private final Map<Long, LocalDateTime> checkInTimes = new HashMap<>();
    private final Map<Long, LocalDateTime> checkOutTimes = new HashMap<>();
    private final Map<Long, String> checkInNotes = new HashMap<>();
    private final Map<Long, String> checkOutNotes = new HashMap<>();
    
    /**
     * Get clinic schedule for today or a specific date
     */
    public List<AppointmentResponse> getClinicSchedule(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate().isAfter(startOfDay) && a.getAppointmentDate().isBefore(endOfDay)
                        && a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted((a1, a2) -> a1.getAppointmentDate().compareTo(a2.getAppointmentDate()))
                .collect(Collectors.toList());
        
        return appointments.stream()
                .map(this::buildAppointmentResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get today's appointments
     */
    public List<AppointmentResponse> getTodaySchedule() {
        return getClinicSchedule(LocalDate.now());
    }
    
    /**
     * Mark patient check-in time
     */
    public Map<String, Object> markCheckIn(Long appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AuthenticationException("Appointment is not scheduled", "INVALID_STATUS", 400);
        }
        
        LocalDateTime checkInTime = LocalDateTime.now();
        checkInTimes.put(appointmentId, checkInTime);
        if (notes != null) {
            checkInNotes.put(appointmentId, notes);
        }
        
        log.info("Patient checked in for appointment {}", appointmentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("appointmentId", appointmentId);
        response.put("checkInTime", checkInTime);
        response.put("status", "checked-in");
        response.put("message", "Patient checked in successfully");
        
        return response;
    }
    
    /**
     * Mark patient check-out time
     */
    public Map<String, Object> markCheckOut(Long appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (!checkInTimes.containsKey(appointmentId)) {
            throw new AuthenticationException("Patient has not checked in", "NOT_CHECKED_IN", 400);
        }
        
        LocalDateTime checkOutTime = LocalDateTime.now();
        checkOutTimes.put(appointmentId, checkOutTime);
        if (notes != null) {
            checkOutNotes.put(appointmentId, notes);
        }
        
        // Update appointment status to COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        
        LocalDateTime checkInTime = checkInTimes.get(appointmentId);
        long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(checkInTime, checkOutTime);
        
        log.info("Patient checked out from appointment {}. Duration: {} minutes", appointmentId, durationMinutes);
        
        Map<String, Object> response = new HashMap<>();
        response.put("appointmentId", appointmentId);
        response.put("checkInTime", checkInTime);
        response.put("checkOutTime", checkOutTime);
        response.put("durationMinutes", durationMinutes);
        response.put("status", "completed");
        response.put("message", "Patient checked out successfully");
        
        return response;
    }
    
    /**
     * Update appointment time slot (reschedule)
     */
    public AppointmentResponse updateAppointmentTimeSlot(Long appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AuthenticationException("Only scheduled appointments can be rescheduled", "INVALID_STATUS", 400);
        }
        
        // Check for conflicts with other appointments for the same doctor
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                appointment.getDoctor(),
                newDateTime,
                newDateTime.plusMinutes(appointment.getDurationMinutes())
        );
        
        // Remove the current appointment from conflicts list
        conflicts = conflicts.stream()
                .filter(a -> !a.getId().equals(appointmentId))
                .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            throw new AuthenticationException("Time slot is not available", "SLOT_NOT_AVAILABLE", 409);
        }
        
        appointment.setAppointmentDate(newDateTime);
        appointment.setUpdatedAt(LocalDateTime.now());
        Appointment updated = appointmentRepository.save(appointment);
        
        log.info("Appointment {} rescheduled to {}", appointmentId, newDateTime);
        
        return buildAppointmentResponse(updated);
    }
    
    /**
     * Get check-in information for an appointment
     */
    public Map<String, Object> getCheckInInfo(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        Map<String, Object> response = new HashMap<>();
        response.put("appointmentId", appointmentId);
        response.put("petName", appointment.getPet().getName());
        response.put("ownerName", appointment.getOwner().getFullName());
        response.put("ownerPhone", appointment.getOwner().getPhoneNumber());
        response.put("appointmentTime", appointment.getAppointmentDate());
        response.put("doctorName", appointment.getDoctor().getFullName());
        response.put("isCheckedIn", checkInTimes.containsKey(appointmentId));
        response.put("isCheckedOut", checkOutTimes.containsKey(appointmentId));
        
        if (checkInTimes.containsKey(appointmentId)) {
            response.put("checkInTime", checkInTimes.get(appointmentId));
            response.put("checkInNotes", checkInNotes.getOrDefault(appointmentId, ""));
        }
        
        if (checkOutTimes.containsKey(appointmentId)) {
            response.put("checkOutTime", checkOutTimes.get(appointmentId));
            response.put("checkOutNotes", checkOutNotes.getOrDefault(appointmentId, ""));
        }
        
        return response;
    }
    
    /**
     * Get all appointments for today with check-in status
     */
    public List<Map<String, Object>> getTodayScheduleWithCheckInStatus() {
        List<AppointmentResponse> appointmentResponses = getTodaySchedule();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate().isAfter(startOfDay) && a.getAppointmentDate().isBefore(endOfDay)
                        && a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted((a1, a2) -> a1.getAppointmentDate().compareTo(a2.getAppointmentDate()))
                .collect(Collectors.toList());
        
        return appointments.stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appointmentId", a.getId());
                    map.put("petName", a.getPet().getName());
                    map.put("ownerName", a.getOwner().getFullName());
                    map.put("appointmentTime", a.getAppointmentDate());
                    map.put("doctorName", a.getDoctor().getFullName());
                    map.put("isCheckedIn", checkInTimes.containsKey(a.getId()));
                    map.put("isCheckedOut", checkOutTimes.containsKey(a.getId()));
                    return map;
                })
                .collect(Collectors.toList());
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
    
    private Appointment mapResponseToAppointment(AppointmentResponse response) {
        // This is a placeholder conversion - in a real scenario, fetch from repository
        return appointmentRepository.findById(response.getId()).orElse(null);
    }
}
