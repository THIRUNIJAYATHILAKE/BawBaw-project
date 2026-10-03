package com.petvax.care.service;

import com.petvax.care.dto.ReminderResponse;
import com.petvax.care.entity.*;
import com.petvax.care.exception.UserNotFoundException;
import com.petvax.care.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private DewormingRecordRepository dewormingRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PetRepository petRepository;

    /**
     * Schedule vaccination reminders (T-3, T-1, T-day) for a vaccination
     */
    public void scheduleVaccinationReminders(Vaccination vaccination) {
        if (vaccination.getNextBoosterDate() == null) {
            return;
        }

        User owner = vaccination.getMedicalRecord().getPet().getOwner();
        LocalDateTime boosterDateTime = vaccination.getNextBoosterDate().atStartOfDay();

        List<Reminder> reminders = new ArrayList<>();

        // T-3 days reminder
        Reminder reminder3Days = Reminder.builder()
                .user(owner)
                .medicalRecord(vaccination.getMedicalRecord())
                .reminderType("VACCINATION")
                .scheduledDate(boosterDateTime.minusDays(3))
                .sent(false)
                .build();
        reminders.add(reminder3Days);

        // T-1 day reminder
        Reminder reminder1Day = Reminder.builder()
                .user(owner)
                .medicalRecord(vaccination.getMedicalRecord())
                .reminderType("VACCINATION")
                .scheduledDate(boosterDateTime.minusDays(1))
                .sent(false)
                .build();
        reminders.add(reminder1Day);

        // T-day reminder (at start of day)
        Reminder reminderDay = Reminder.builder()
                .user(owner)
                .medicalRecord(vaccination.getMedicalRecord())
                .reminderType("VACCINATION")
                .scheduledDate(boosterDateTime)
                .sent(false)
                .build();
        reminders.add(reminderDay);

        reminderRepository.saveAll(reminders);
        log.info("Scheduled 3 vaccination reminders for vaccine: {}", vaccination.getVaccineName());
    }

    /**
     * Schedule deworming reminders (T-3, T-1) for a deworming record
     */
    public void scheduledewormingReminders(DewormingRecord dewormingRecord) {
        if (dewormingRecord.getNextDoseDate() == null) {
            return;
        }

        User owner = dewormingRecord.getMedicalRecord().getPet().getOwner();
        LocalDateTime nextDoseDateTime = dewormingRecord.getNextDoseDate().atStartOfDay();

        List<Reminder> reminders = new ArrayList<>();

        // T-3 days reminder
        Reminder reminder3Days = Reminder.builder()
                .user(owner)
                .medicalRecord(dewormingRecord.getMedicalRecord())
                .reminderType("DEWORMING")
                .scheduledDate(nextDoseDateTime.minusDays(3))
                .sent(false)
                .build();
        reminders.add(reminder3Days);

        // T-1 day reminder
        Reminder reminder1Day = Reminder.builder()
                .user(owner)
                .medicalRecord(dewormingRecord.getMedicalRecord())
                .reminderType("DEWORMING")
                .scheduledDate(nextDoseDateTime.minusDays(1))
                .sent(false)
                .build();
        reminders.add(reminder1Day);

        reminderRepository.saveAll(reminders);
        log.info("Scheduled 2 deworming reminders for dewormer: {}", dewormingRecord.getDewormerName());
    }

    /**
     * Schedule appointment reminders (T-1 day, T-day) for an appointment
     */
    public void scheduleAppointmentReminders(Appointment appointment) {
        User owner = appointment.getOwner();
        LocalDateTime appointmentDateTime = appointment.getAppointmentDate();

        List<Reminder> reminders = new ArrayList<>();

        // T-1 day reminder
        Reminder reminder1Day = Reminder.builder()
                .user(owner)
                .appointment(appointment)
                .reminderType("APPOINTMENT")
                .scheduledDate(appointmentDateTime.minusDays(1))
                .sent(false)
                .build();
        reminders.add(reminder1Day);

        // T-day reminder (same day at same time)
        Reminder reminderDay = Reminder.builder()
                .user(owner)
                .appointment(appointment)
                .reminderType("APPOINTMENT")
                .scheduledDate(appointmentDateTime)
                .sent(false)
                .build();
        reminders.add(reminderDay);

        reminderRepository.saveAll(reminders);
        log.info("Scheduled 2 appointment reminders for appointment ID: {}", appointment.getId());
    }

    /**
     * Get all reminders for a user with pagination
     */
    public List<ReminderResponse> getRemindersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Reminder> reminders = reminderRepository.findByUserOrderByScheduledDateDesc(user);
        return convertToResponses(reminders);
    }

    /**
     * Get reminders filtered by type (VACCINATION, DEWORMING, APPOINTMENT)
     */
    public List<ReminderResponse> getRemindersByType(Long userId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Reminder> reminders = reminderRepository.findByUserAndReminderType(user, type.toUpperCase());
        return convertToResponses(reminders);
    }

    /**
     * Get sent reminders (reminder history)
     */
    public List<ReminderResponse> getSentReminders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Reminder> reminders = reminderRepository.findSentRemindersByUser(user);
        return convertToResponses(reminders);
    }

    /**
     * Mark a reminder as read/acknowledged
     */
    public ReminderResponse acknowledgeReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        reminder.setSent(true);
        reminder.setSentAt(LocalDateTime.now());
        Reminder saved = reminderRepository.save(reminder);

        return convertToResponse(saved);
    }

    /**
     * Get all unsent reminders that are due
     */
    public List<Reminder> getUnsentDueReminders() {
        return reminderRepository.findUnsentRemindersByScheduledDate(LocalDateTime.now());
    }

    /**
     * Mark reminders as sent and send notifications
     */
    public void markRemindersAsSent(List<Reminder> reminders) {
        for (Reminder reminder : reminders) {
            reminder.setSent(true);
            reminder.setSentAt(LocalDateTime.now());
        }
        reminderRepository.saveAll(reminders);
        log.info("Marked {} reminders as sent", reminders.size());
    }

    /**
     * Send a reminder notification and mark as sent
     */
    public void sendReminderNotification(Reminder reminder) {
        try {
            String title = buildReminderTitle(reminder);
            String body = buildReminderBody(reminder);
            Map<String, String> data = buildReminderData(reminder);

            boolean sent = notificationService.sendPushNotification(
                    reminder.getUser().getId(),
                    title,
                    body,
                    data
            );

            if (sent) {
                reminder.setSent(true);
                reminder.setSentAt(LocalDateTime.now());
                reminderRepository.save(reminder);
                log.info("Sent reminder notification for user: {}", reminder.getUser().getId());
            }
        } catch (Exception e) {
            log.error("Error sending reminder notification for reminder ID: {}", reminder.getId(), e);
        }
    }

    /**
     * Build reminder title based on type
     */
    private String buildReminderTitle(Reminder reminder) {
        switch (reminder.getReminderType()) {
            case "VACCINATION":
                return "Vaccination Reminder";
            case "DEWORMING":
                return "Deworming Reminder";
            case "APPOINTMENT":
                return "Appointment Reminder";
            default:
                return "Pet Care Reminder";
        }
    }

    /**
     * Build reminder message body
     */
    private String buildReminderBody(Reminder reminder) {
        String petName = getPetName(reminder);

        switch (reminder.getReminderType()) {
            case "VACCINATION":
                String vaccineName = getVaccineName(reminder);
                long daysUntil = java.time.temporal.ChronoUnit.DAYS
                        .between(LocalDateTime.now(), reminder.getScheduledDate());
                if (daysUntil > 0) {
                    return String.format("%s's %s vaccination is due in %d days", petName, vaccineName, daysUntil);
                } else if (daysUntil == 0) {
                    return String.format("Today is %s's %s vaccination day", petName, vaccineName);
                } else {
                    return String.format("%s's %s vaccination is overdue", petName, vaccineName);
                }

            case "DEWORMING":
                String dewormerName = getDewormerName(reminder);
                daysUntil = java.time.temporal.ChronoUnit.DAYS
                        .between(LocalDateTime.now(), reminder.getScheduledDate());
                if (daysUntil > 0) {
                    return String.format("%s's deworming with %s is due in %d days", petName, dewormerName, daysUntil);
                } else if (daysUntil == 0) {
                    return String.format("Today is %s's deworming day", petName);
                } else {
                    return String.format("%s's deworming is overdue", petName);
                }

            case "APPOINTMENT":
                daysUntil = java.time.temporal.ChronoUnit.DAYS
                        .between(LocalDateTime.now(), reminder.getScheduledDate());
                if (daysUntil > 0) {
                    return String.format("Your appointment for %s is in %d days", petName, daysUntil);
                } else if (daysUntil == 0) {
                    return String.format("Your appointment for %s is today", petName);
                } else {
                    return String.format("Your appointment for %s was scheduled", petName);
                }

            default:
                return "You have a pet care reminder";
        }
    }

    /**
     * Build reminder notification data
     */
    private Map<String, String> buildReminderData(Reminder reminder) {
        Map<String, String> data = new HashMap<>();
        data.put("type", reminder.getReminderType());
        data.put("reminderId", reminder.getId().toString());
        data.put("petName", getPetName(reminder));

        if (reminder.getMedicalRecord() != null) {
            data.put("medicalRecordId", reminder.getMedicalRecord().getId().toString());
        }
        if (reminder.getAppointment() != null) {
            data.put("appointmentId", reminder.getAppointment().getId().toString());
        }

        return data;
    }

    /**
     * Get pet name from reminder
     */
    private String getPetName(Reminder reminder) {
        if (reminder.getMedicalRecord() != null) {
            return reminder.getMedicalRecord().getPet().getName();
        }
        if (reminder.getAppointment() != null) {
            return reminder.getAppointment().getPet().getName();
        }
        return "Your pet";
    }

    /**
     * Get vaccine name from reminder
     */
    private String getVaccineName(Reminder reminder) {
        if (reminder.getMedicalRecord() != null) {
            List<Vaccination> vaccinations = vaccinationRepository
                    .findByMedicalRecord(reminder.getMedicalRecord());
            if (!vaccinations.isEmpty()) {
                return vaccinations.get(0).getVaccineName();
            }
        }
        return "vaccination";
    }

    /**
     * Get dewormer name from reminder
     */
    private String getDewormerName(Reminder reminder) {
        if (reminder.getMedicalRecord() != null) {
            List<DewormingRecord> dewormingRecords = dewormingRecordRepository
                    .findByMedicalRecord(reminder.getMedicalRecord());
            if (!dewormingRecords.isEmpty()) {
                return dewormingRecords.get(0).getDewormerName();
            }
        }
        return "dewormer";
    }

    /**
     * Convert Reminder entity to ReminderResponse DTO
     */
    private ReminderResponse convertToResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .reminderType(reminder.getReminderType())
                .title(buildReminderTitle(reminder))
                .message(buildReminderBody(reminder))
                .scheduledDate(reminder.getScheduledDate())
                .sent(reminder.getSent())
                .sentAt(reminder.getSentAt())
                .medicalRecordId(reminder.getMedicalRecord() != null ? reminder.getMedicalRecord().getId() : null)
                .appointmentId(reminder.getAppointment() != null ? reminder.getAppointment().getId() : null)
                .petName(getPetName(reminder))
                .createdAt(reminder.getCreatedAt())
                .updatedAt(reminder.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of Reminder entities to ReminderResponse DTOs
     */
    private List<ReminderResponse> convertToResponses(List<Reminder> reminders) {
        return reminders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
