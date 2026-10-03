package com.petvax.care.service;

import com.petvax.care.entity.*;
import com.petvax.care.exception.UserNotFoundException;
import com.petvax.care.repository.*;
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
public class AdminScheduleManagementService {

        @Autowired
        private AppointmentRepository appointmentRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private DoctorBlockedSlotRepository doctorBlockedSlotRepository;

        @Autowired
        private AuditLogRepository auditLogRepository;

        /**
         * Get global appointment schedule (all appointments)
         */
        public List<Appointment> getGlobalSchedule(LocalDateTime dateFrom, LocalDateTime dateTo) {
                log.info("Fetching global schedule from {} to {}", dateFrom, dateTo);

                return appointmentRepository.findAll().stream()
                                .filter(a -> !a.getAppointmentDate().isBefore(dateFrom) &&
                                                !a.getAppointmentDate().isAfter(dateTo))
                                .collect(Collectors.toList());
        }

        /**
         * Get appointments by doctor
         */
        public List<Appointment> getScheduleByDoctor(Long doctorId, LocalDateTime dateFrom, LocalDateTime dateTo) {
                log.info("Fetching schedule for doctor {} from {} to {}", doctorId, dateFrom, dateTo);

                User doctor = userRepository.findById(doctorId)
                                .orElseThrow(() -> new UserNotFoundException("Doctor not found"));

                return appointmentRepository.findAll().stream()
                                .filter(a -> a.getDoctor().getId().equals(doctorId))
                                .filter(a -> !a.getAppointmentDate().isBefore(dateFrom) &&
                                                !a.getAppointmentDate().isAfter(dateTo))
                                .collect(Collectors.toList());
        }

        /**
         * Get appointments by status
         */
        public List<Appointment> getScheduleByStatus(AppointmentStatus status, LocalDateTime dateFrom,
                        LocalDateTime dateTo) {
                log.info("Fetching appointments with status {} from {} to {}", status, dateFrom, dateTo);

                return appointmentRepository.findAll().stream()
                                .filter(a -> a.getStatus().equals(status))
                                .filter(a -> !a.getAppointmentDate().isBefore(dateFrom) &&
                                                !a.getAppointmentDate().isAfter(dateTo))
                                .collect(Collectors.toList());
        }

        /**
         * Get all appointments with optional filtering
         */
        public List<Appointment> getAllAppointments(String status, LocalDateTime dateFrom, LocalDateTime dateTo) {
                log.info("Fetching all appointments - status: {}", status);

                List<Appointment> appointments = appointmentRepository.findAll();

                if (status != null && !status.isEmpty()) {
                        appointments = appointments.stream()
                                        .filter(a -> a.getStatus().toString().equals(status))
                                        .collect(Collectors.toList());
                }

                if (dateFrom != null && dateTo != null) {
                        appointments = appointments.stream()
                                        .filter(a -> !a.getAppointmentDate().isBefore(dateFrom) &&
                                                        !a.getAppointmentDate().isAfter(dateTo))
                                        .collect(Collectors.toList());
                }

                return appointments;
        }

        /**
         * Block time slot for doctor
         */
        @Transactional
        public void blockTimeSlot(Long doctorId, LocalDateTime blockedFrom, LocalDateTime blockedUntil, String reason,
                        Long adminId) {
                log.info("Blocking time slot for doctor {} from {} to {}", doctorId, blockedFrom, blockedUntil);

                User doctor = userRepository.findById(doctorId)
                                .orElseThrow(() -> new UserNotFoundException("Doctor not found"));

                DoctorBlockedSlot blockedSlot = DoctorBlockedSlot.builder()
                                .doctor(doctor)
                                .blockedFrom(blockedFrom)
                                .blockedUntil(blockedUntil)
                                .reason(reason)
                                .createdBy(userRepository.findById(adminId).orElse(null))
                                .build();

                doctorBlockedSlotRepository.save(blockedSlot);

                logAuditAction(adminId, "TIME_SLOT_BLOCKED", "doctor_blocked_slots", blockedSlot.getId(),
                                null, reason);
        }

        /**
         * Unblock time slot
         */
        @Transactional
        public void unblockTimeSlot(Long slotId, Long adminId) {
                log.info("Unblocking time slot: {}", slotId);

                DoctorBlockedSlot slot = doctorBlockedSlotRepository.findById(slotId)
                                .orElseThrow(() -> new UserNotFoundException("Slot not found"));

                doctorBlockedSlotRepository.delete(slot);

                logAuditAction(adminId, "TIME_SLOT_UNBLOCKED", "doctor_blocked_slots", slotId,
                                null, null);
        }

        /**
         * Get blocked slots for doctor
         */
        public List<DoctorBlockedSlot> getBlockedSlots(Long doctorId) {
                log.info("Fetching blocked slots for doctor: {}", doctorId);

                return doctorBlockedSlotRepository.findAll().stream()
                                .filter(slot -> slot.getDoctor().getId().equals(doctorId))
                                .collect(Collectors.toList());
        }

        /**
         * Get appointment statistics
         */
        public AppointmentStats getAppointmentStats(LocalDate date) {
                log.info("Fetching appointment stats for {}", date);

                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);

                List<Appointment> todayAppointments = appointmentRepository.findAll().stream()
                                .filter(a -> !a.getAppointmentDate().isBefore(startOfDay) &&
                                                !a.getAppointmentDate().isAfter(endOfDay))
                                .collect(Collectors.toList());

                long scheduled = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                                .count();
                long completed = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                                .count();
                long cancelled = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                                .count();
                long noShow = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW)
                                .count();

                return AppointmentStats.builder()
                                .totalAppointments(todayAppointments.size())
                                .scheduledCount(scheduled)
                                .completedCount(completed)
                                .cancelledCount(cancelled)
                                .noShowCount(noShow)
                                .date(date)
                                .build();
        }

        // Helper method

        private void logAuditAction(Long userId, String action, String entityType, Long entityId, String oldValue,
                        String newValue) {
                AuditLog log = AuditLog.builder()
                                .user(userRepository.findById(userId).orElse(null))
                                .action(action)
                                .entityType(entityType)
                                .entityId(entityId)
                                .oldValues(oldValue)
                                .newValues(newValue)
                                .build();
                auditLogRepository.save(log);
        }
}

// DTO for appointment statistics
@lombok.Builder
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
class AppointmentStats {
        private LocalDate date;
        private int totalAppointments;
        private long scheduledCount;
        private long completedCount;
        private long cancelledCount;
        private long noShowCount;
}
