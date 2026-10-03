package com.petvax.care.service;

import com.petvax.care.dto.DoctorScheduleDto;
import com.petvax.care.dto.ScheduleItemDto;
import com.petvax.care.entity.*;
import com.petvax.care.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DoctorScheduleService {

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get doctor's schedule for a specific date (daily view)
     */
    @Transactional(readOnly = true)
    public List<ScheduleItemDto> getScheduleByDate(User doctor, LocalDate date) {
        log.info("Fetching schedule for doctor ID: {} on date: {}", doctor.getId(), date);
        
        List<Appointment> appointments = appointmentRepository.findDoctorAppointmentsByDate(doctor, date);
        
        return appointments.stream()
                .map(this::mapAppointmentToScheduleItem)
                .collect(Collectors.toList());
    }

    /**
     * Get doctor's weekly schedule with all appointments
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDoctorSchedule(User doctor) {
        log.info("Fetching weekly schedule for doctor ID: {}", doctor.getId());
        
        Map<String, Object> weeklySchedule = new HashMap<>();
        List<DoctorScheduleDto> schedules = new ArrayList<>();
        
        // Get doctor's schedule for all days of week
        List<DoctorSchedule> docSchedules = doctorScheduleRepository.findAvailableSchedules(doctor);
        
        // Build schedule for each day
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            String dayName = DayOfWeek.of(dayOfWeek).toString();
            final int finalDayOfWeek = dayOfWeek;
            
            // Find schedule for this day
            List<DoctorSchedule> daySchedules = docSchedules.stream()
                    .filter(ds -> ds.getDayOfWeek().equals(finalDayOfWeek))
                    .collect(Collectors.toList());
            
            // Get appointments for this day (today onwards)
            LocalDate todayOrNextOccurrence = getNextOccurrenceOfDay(finalDayOfWeek);
            List<Appointment> dayAppointments = appointmentRepository.findDoctorAppointmentsByDate(doctor, todayOrNextOccurrence);
            
            List<ScheduleItemDto> scheduleItems = dayAppointments.stream()
                    .map(this::mapAppointmentToScheduleItem)
                    .collect(Collectors.toList());
            
            if (!daySchedules.isEmpty()) {
                DoctorSchedule ds = daySchedules.get(0);
                DoctorScheduleDto scheduleDto = DoctorScheduleDto.builder()
                        .dayOfWeek(finalDayOfWeek)
                        .dayName(dayName)
                        .startTime(ds.getStartTime())
                        .endTime(ds.getEndTime())
                        .isAvailable(ds.getIsAvailable())
                        .appointments(scheduleItems)
                        .build();
                
                schedules.add(scheduleDto);
            }
        }
        
        weeklySchedule.put("doctorId", doctor.getId());
        weeklySchedule.put("doctorName", doctor.getFullName());
        weeklySchedule.put("schedule", schedules);
        
        return weeklySchedule;
    }

    /**
     * Mark appointment as completed
     */
    public Appointment markAppointmentCompleted(Long appointmentId, User doctor) {
        log.info("Marking appointment ID: {} as COMPLETED", appointmentId);
        
        Appointment appointment = appointmentRepository.findByIdAndDoctor(appointmentId, doctor)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found or unauthorized"));
        
        // Store old status for audit
        String oldValues = "{\"status\": \"" + appointment.getStatus() + "\"}";
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment updated = appointmentRepository.save(appointment);
        
        // Create audit log
        String newValues = "{\"status\": \"" + updated.getStatus() + "\"}";
        createAuditLog(doctor, "MARK_APPOINTMENT_COMPLETED", "Appointment", appointmentId, oldValues, newValues);
        
        log.info("Appointment marked as COMPLETED: {}", appointmentId);
        return updated;
    }

    /**
     * Mark appointment as no-show
     */
    public Appointment markAppointmentNoShow(Long appointmentId, User doctor) {
        log.info("Marking appointment ID: {} as NO_SHOW", appointmentId);
        
        Appointment appointment = appointmentRepository.findByIdAndDoctor(appointmentId, doctor)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found or unauthorized"));
        
        // Store old status for audit
        String oldValues = "{\"status\": \"" + appointment.getStatus() + "\"}";
        
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        Appointment updated = appointmentRepository.save(appointment);
        
        // Create audit log
        String newValues = "{\"status\": \"" + updated.getStatus() + "\"}";
        createAuditLog(doctor, "MARK_APPOINTMENT_NO_SHOW", "Appointment", appointmentId, oldValues, newValues);
        
        log.info("Appointment marked as NO_SHOW: {}", appointmentId);
        return updated;
    }

    /**
     * Get appointment details
     */
    @Transactional(readOnly = true)
    public ScheduleItemDto getAppointmentDetails(Long appointmentId, User doctor) {
        log.info("Fetching appointment details ID: {} for doctor ID: {}", appointmentId, doctor.getId());
        
        Appointment appointment = appointmentRepository.findByIdAndDoctor(appointmentId, doctor)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found or unauthorized"));
        
        return mapAppointmentToScheduleItem(appointment);
    }

    /**
     * Map Appointment to ScheduleItemDto
     */
    private ScheduleItemDto mapAppointmentToScheduleItem(Appointment appointment) {
        return ScheduleItemDto.builder()
                .appointmentId(appointment.getId())
                .petId(appointment.getPet().getId())
                .petName(appointment.getPet().getName())
                .petSpecies(appointment.getPet().getSpecies())
                .ownerId(appointment.getOwner().getId())
                .ownerName(appointment.getOwner().getFullName())
                .ownerPhoneNumber(appointment.getOwner().getPhoneNumber())
                .appointmentDate(appointment.getAppointmentDate())
                .durationMinutes(appointment.getDurationMinutes())
                .status(appointment.getStatus())
                .appointmentPurpose(appointment.getPurpose().toString())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .microchipId(appointment.getPet().getMicrochipId())
                .build();
    }

    /**
     * Create audit log entry
     */
    private void createAuditLog(User user, String action, String entityType, Long entityId, String oldValues, String newValues) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress("N/A")
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for action: {}", action);
        } catch (Exception e) {
            log.error("Error creating audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Get doctor's schedule as a list of DoctorScheduleDto
     */
    @Transactional(readOnly = true)
    public List<DoctorScheduleDto> getDoctorScheduleList(User doctor) {
        log.info("Fetching schedule list for doctor ID: {}", doctor.getId());
        
        List<DoctorScheduleDto> schedules = new ArrayList<>();
        
        // Get doctor's schedule for all days of week
        List<DoctorSchedule> docSchedules = doctorScheduleRepository.findAvailableSchedules(doctor);
        
        // Build schedule for each day
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            String dayName = DayOfWeek.of(dayOfWeek).toString();
            final int finalDayOfWeek = dayOfWeek;
            
            // Find schedule for this day
            List<DoctorSchedule> daySchedules = docSchedules.stream()
                    .filter(ds -> ds.getDayOfWeek().equals(finalDayOfWeek))
                    .collect(Collectors.toList());
            
            if (!daySchedules.isEmpty()) {
                DoctorSchedule ds = daySchedules.get(0);
                DoctorScheduleDto scheduleDto = DoctorScheduleDto.builder()
                        .dayOfWeek(finalDayOfWeek)
                        .dayName(dayName)
                        .startTime(ds.getStartTime())
                        .endTime(ds.getEndTime())
                        .isAvailable(ds.getIsAvailable())
                        .appointments(new ArrayList<>())
                        .build();
                
                schedules.add(scheduleDto);
            }
        }
        
        return schedules;
    }

    /**
     * Get the next occurrence of a given day of week
     */
    private LocalDate getNextOccurrenceOfDay(int dayOfWeek) {
        LocalDate today = LocalDate.now();
        DayOfWeek targetDay = DayOfWeek.of(dayOfWeek);
        
        if (today.getDayOfWeek().getValue() == dayOfWeek) {
            return today;
        }
        
        return today.with(TemporalAdjusters.next(targetDay));
    }
}
