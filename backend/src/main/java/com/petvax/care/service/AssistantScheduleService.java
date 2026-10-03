package com.petvax.care.service;

import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.AppointmentStatus;
import com.petvax.care.exception.UserNotFoundException;
import com.petvax.care.repository.AppointmentRepository;
import com.petvax.care.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssistantScheduleService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get today's schedule
     */
    public List<Appointment> getTodaySchedule() {
        log.info("Fetching today's schedule");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        return appointmentRepository.findAll().stream()
                .filter(a -> !a.getAppointmentDate().isBefore(startOfDay) &&
                        !a.getAppointmentDate().isAfter(endOfDay))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED ||
                        a.getStatus() == AppointmentStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    /**
     * Get schedule by date
     */
    public List<Appointment> getScheduleByDate(LocalDate date) {
        log.info("Fetching schedule for date: {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return appointmentRepository.findAll().stream()
                .filter(a -> !a.getAppointmentDate().isBefore(startOfDay) &&
                        !a.getAppointmentDate().isAfter(endOfDay))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED ||
                        a.getStatus() == AppointmentStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    /**
     * Mark appointment check-in
     */
    @Transactional
    public Appointment markCheckIn(Long appointmentId) {
        log.info("Marking check-in for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found"));

        if (appointment.getCheckedInAt() != null) {
            log.warn("Appointment {} already checked in", appointmentId);
            return appointment;
        }

        appointment.setCheckedInAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    /**
     * Mark appointment check-out
     */
    @Transactional
    public Appointment markCheckOut(Long appointmentId) {
        log.info("Marking check-out for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found"));

        if (appointment.getCheckedOutAt() != null) {
            log.warn("Appointment {} already checked out", appointmentId);
            return appointment;
        }

        if (appointment.getCheckedInAt() == null) {
            log.warn("Appointment {} has not been checked in yet", appointmentId);
            throw new IllegalStateException("Must check in before checking out");
        }

        appointment.setCheckedOutAt(LocalDateTime.now());
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Get check-in/out status
     */
    public CheckInOutStatus getCheckInStatus(Long appointmentId) {
        log.info("Getting check-in status for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found"));

        return CheckInOutStatus.builder()
                .appointmentId(appointmentId)
                .petName(appointment.getPet().getName())
                .ownerName(appointment.getOwner().getFullName())
                .appointmentTime(appointment.getAppointmentDate())
                .checkedInAt(appointment.getCheckedInAt())
                .checkedOutAt(appointment.getCheckedOutAt())
                .status(appointment.getStatus().toString())
                .isCheckedIn(appointment.getCheckedInAt() != null)
                .isCheckedOut(appointment.getCheckedOutAt() != null)
                .build();
    }

    /**
     * Update appointment time slot
     */
    @Transactional
    public Appointment updateAppointmentSlot(Long appointmentId, LocalDateTime newTime) {
        log.info("Updating appointment {} time to {}", appointmentId, newTime);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found"));

        if (appointment.getCheckedInAt() != null) {
            throw new IllegalStateException("Cannot update time for checked-in appointment");
        }

        appointment.setAppointmentDate(newTime);
        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    /**
     * Get schedule with check-in status
     */
    public List<ScheduleItemWithStatus> getScheduleWithStatus(LocalDate date) {
        log.info("Fetching schedule with status for: {}", date);

        return getScheduleByDate(date).stream()
                .map(a -> ScheduleItemWithStatus.builder()
                        .appointmentId(a.getId())
                        .petName(a.getPet().getName())
                        .ownerName(a.getOwner().getFullName())
                        .doctorName(a.getDoctor().getFullName())
                        .appointmentTime(a.getAppointmentDate())
                        .durationMinutes(a.getDurationMinutes())
                        .purpose(a.getReason())
                        .checkedInAt(a.getCheckedInAt())
                        .checkedOutAt(a.getCheckedOutAt())
                        .status(a.getStatus().toString())
                        .isCheckedIn(a.getCheckedInAt() != null)
                        .isCheckedOut(a.getCheckedOutAt() != null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get summary of today's check-ins
     */
    public ScheduleSummary getTodaySummary() {
        log.info("Fetching today's schedule summary");

        List<Appointment> todayAppointments = getTodaySchedule();

        long checkedIn = todayAppointments.stream()
                .filter(a -> a.getCheckedInAt() != null)
                .count();
        long checkedOut = todayAppointments.stream()
                .filter(a -> a.getCheckedOutAt() != null)
                .count();
        long pending = todayAppointments.stream()
                .filter(a -> a.getCheckedInAt() == null)
                .count();

        return ScheduleSummary.builder()
                .date(LocalDate.now())
                .totalAppointments(todayAppointments.size())
                .checkedInCount(checkedIn)
                .checkedOutCount(checkedOut)
                .pendingCount(pending)
                .build();
    }
}

// DTOs

@lombok.Builder
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
class CheckInOutStatus {
    private Long appointmentId;
    private String petName;
    private String ownerName;
    private LocalDateTime appointmentTime;
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;
    private String status;
    private boolean isCheckedIn;
    private boolean isCheckedOut;
}

@lombok.Builder
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
class ScheduleItemWithStatus {
    private Long appointmentId;
    private String petName;
    private String ownerName;
    private String doctorName;
    private LocalDateTime appointmentTime;
    private Integer durationMinutes;
    private String purpose;
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;
    private String status;
    private boolean isCheckedIn;
    private boolean isCheckedOut;
}

@lombok.Builder
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
class ScheduleSummary {
    private LocalDate date;
    private int totalAppointments;
    private long checkedInCount;
    private long checkedOutCount;
    private long pendingCount;
}
