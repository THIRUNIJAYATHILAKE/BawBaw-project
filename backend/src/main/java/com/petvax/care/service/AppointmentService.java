package com.petvax.care.service;

import com.petvax.care.dto.AppointmentResponse;
import com.petvax.care.dto.AvailableDoctorDto;
import com.petvax.care.dto.AvailableSlotDto;
import com.petvax.care.dto.BookAppointmentRequest;
import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.AppointmentPurpose;
import com.petvax.care.entity.AppointmentStatus;
import com.petvax.care.entity.DoctorProfile;
import com.petvax.care.entity.DoctorSchedule;
import com.petvax.care.entity.Pet;
import com.petvax.care.entity.User;
import com.petvax.care.entity.UserRole;
import com.petvax.care.exception.AppointmentConflictException;
import com.petvax.care.exception.PetNotFoundException;
import com.petvax.care.exception.UnauthorizedException;
import com.petvax.care.repository.AppointmentRepository;
import com.petvax.care.repository.DoctorProfileRepository;
import com.petvax.care.repository.DoctorScheduleRepository;
import com.petvax.care.repository.PetRepository;
import com.petvax.care.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ReminderService reminderService;
    private static final int APPOINTMENT_DURATION_MINUTES = 30;
    private static final int CANCELLATION_WINDOW_HOURS = 2;
    private static final int SLOT_INTERVAL_MINUTES = 30;
    
    /**
     * Get list of available doctors with specialties and free slot counts
     */
    public List<AvailableDoctorDto> getAvailableDoctors() {
        List<User> doctors = userRepository.findByRole(UserRole.DOCTOR);
        
        return doctors.stream()
            .map(doctor -> {
                List<DoctorSchedule> schedules = doctorScheduleRepository.findAvailableSchedules(doctor);
                int totalSlots = calculateTotalAvailableSlots(doctor, schedules);
                
                Optional<DoctorProfile> profileOpt = doctorProfileRepository.findByUser(doctor);
                
                DoctorProfile profile = profileOpt.orElse(null);
                String specialization = profile != null ? profile.getSpecialization() : "General";
                Integer yearsOfExperience = profile != null ? profile.getYearsOfExperience() : null;
                String clinicName = profile != null ? profile.getClinicName() : null;
                
                return AvailableDoctorDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getFullName())
                    .specialization(specialization)
                    .yearsOfExperience(yearsOfExperience)
                    .clinicName(clinicName)
                    .availableSlots(totalSlots)
                    .build();
            })
            .filter(dto -> dto.getAvailableSlots() > 0)
            .collect(Collectors.toList());
    }
    
    /**
     * Get available time slots for a doctor on a specific date
     */
    public List<AvailableSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
            .orElseThrow(() -> new UnauthorizedException("Doctor not found"));
        
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("User is not a doctor");
        }
        
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorAndDayOfWeekAndIsAvailableTrue(doctor, dayOfWeek);
        
        List<AvailableSlotDto> slots = new ArrayList<>();
        
        for (DoctorSchedule schedule : schedules) {
            LocalDateTime startDateTime = date.atTime(schedule.getStartTime());
            LocalDateTime endDateTime = date.atTime(schedule.getEndTime());
            
            // Generate slots at SLOT_INTERVAL_MINUTES intervals
            for (LocalDateTime slotTime = startDateTime; slotTime.plusMinutes(APPOINTMENT_DURATION_MINUTES).isBefore(endDateTime) || 
                 slotTime.plusMinutes(APPOINTMENT_DURATION_MINUTES).equals(endDateTime); slotTime = slotTime.plusMinutes(SLOT_INTERVAL_MINUTES)) {
                
                // Check if slot is available (no conflicting appointments)
                LocalDateTime slotEndTime = slotTime.plusMinutes(APPOINTMENT_DURATION_MINUTES);
                List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctor, slotTime, slotEndTime);
                
                if (conflicts.isEmpty()) {
                    slots.add(AvailableSlotDto.builder()
                        .slotTime(slotTime)
                        .durationMinutes(APPOINTMENT_DURATION_MINUTES)
                        .isAvailable(true)
                        .build());
                }
            }
        }
        
        return slots;
    }
    
    /**
     * Book an appointment with double-booking prevention
     */
    public AppointmentResponse bookAppointment(Long ownerId, BookAppointmentRequest request) {
        Pet pet = petRepository.findById(request.getPetId())
            .orElseThrow(() -> new PetNotFoundException("Pet not found"));
        
        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("Pet does not belong to this owner");
        }
        
        User doctor = userRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new UnauthorizedException("Doctor not found"));
        
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("User is not a doctor");
        }
        
        int dayOfWeek = request.getAppointmentDate().getDayOfWeek().getValue();
        LocalTime appointmentTime = request.getAppointmentDate().toLocalTime();
        
        Optional<DoctorSchedule> schedule = doctorScheduleRepository.findScheduleForTime(doctor, dayOfWeek, appointmentTime);
        if (schedule.isEmpty()) {
            throw new AppointmentConflictException("Doctor is not available at the requested time", "DOCTOR_NOT_AVAILABLE");
        }
        
        LocalDateTime appointmentEnd = request.getAppointmentDate().plusMinutes(APPOINTMENT_DURATION_MINUTES);
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctor, request.getAppointmentDate(), appointmentEnd);
        
        if (!conflicts.isEmpty()) {
            throw new AppointmentConflictException("Doctor already has an appointment at this time", "DOUBLE_BOOKING_CONFLICT");
        }
        
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new UnauthorizedException("Owner not found"));
        
        Appointment appointment = Appointment.builder()
            .pet(pet)
            .doctor(doctor)
            .owner(owner)
            .appointmentDate(request.getAppointmentDate())
            .durationMinutes(APPOINTMENT_DURATION_MINUTES)
            .status(AppointmentStatus.SCHEDULED)
            .purpose(request.getPurpose())
            .reason(request.getReason())
            .build();
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Schedule reminders for the appointment
        reminderService.scheduleAppointmentReminders(savedAppointment);
        
        triggerBookingNotification(savedAppointment);
        
        return mapToResponse(savedAppointment);
    }
    
    /**
     * Cancel appointment within 2-hour window before appointment time
     */
    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId, String cancelReason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentConflictException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (!appointment.getOwner().getId().equals(userId) && !appointment.getDoctor().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to cancel this appointment");
        }
        
        if (appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
            throw new AppointmentConflictException("Appointment is already cancelled", "APPOINTMENT_ALREADY_CANCELLED");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = appointment.getAppointmentDate().minusHours(CANCELLATION_WINDOW_HOURS);
        
        if (now.isAfter(cancellationDeadline)) {
            throw new AppointmentConflictException(
                "Cannot cancel appointment within 2 hours of scheduled time",
                "CANCELLATION_WINDOW_EXPIRED"
            );
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledReason(cancelReason);
        
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        return mapToResponse(cancelledAppointment);
    }
    
    /**
     * Get appointments for an owner
     */
    public List<AppointmentResponse> getAppointmentsByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new UnauthorizedException("Owner not found"));
        
        List<Appointment> appointments = appointmentRepository.findActiveAppointmentsByOwner(owner);
        return appointments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get appointments for a doctor
     */
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
            .orElseThrow(() -> new UnauthorizedException("Doctor not found"));
        
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("User is not a doctor");
        }
        
        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
        return appointments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get appointments for a doctor on a specific date
     */
    public List<AppointmentResponse> getAppointmentsByDoctorAndDate(Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
            .orElseThrow(() -> new UnauthorizedException("Doctor not found"));
        
        if (!doctor.getRole().equals(UserRole.DOCTOR)) {
            throw new UnauthorizedException("User is not a doctor");
        }
        
        List<Appointment> appointments = appointmentRepository.findDoctorAppointmentsByDate(doctor, date);
        return appointments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark appointment as completed
     */
    public AppointmentResponse completeAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentConflictException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("Not authorized to complete this appointment");
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment completedAppointment = appointmentRepository.save(appointment);
        return mapToResponse(completedAppointment);
    }
    
    /**
     * Mark appointment as no-show
     */
    public AppointmentResponse markNoShow(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentConflictException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("Not authorized to update this appointment");
        }
        
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        Appointment noShowAppointment = appointmentRepository.save(appointment);
        return mapToResponse(noShowAppointment);
    }
    
    /**
     * Get single appointment details
     */
    public AppointmentResponse getAppointmentDetails(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentConflictException("Appointment not found", "APPOINTMENT_NOT_FOUND", 404));
        
        if (!appointment.getOwner().getId().equals(userId) && !appointment.getDoctor().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to view this appointment");
        }
        
        return mapToResponse(appointment);
    }
    
    private int calculateTotalAvailableSlots(User doctor, List<DoctorSchedule> schedules) {
        int totalSlots = 0;
        for (DoctorSchedule schedule : schedules) {
            long minutesBetween = ChronoUnit.MINUTES.between(schedule.getStartTime(), schedule.getEndTime());
            totalSlots += (int) (minutesBetween / SLOT_INTERVAL_MINUTES);
        }
        return totalSlots;
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
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
    
    private void triggerBookingNotification(Appointment appointment) {
        // TODO: Trigger notification event for FCM notification
        // This will be implemented with notification service
    }
}
