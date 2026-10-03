package com.petvax.care.service;

import com.petvax.care.dto.DoctorListResponse;
import com.petvax.care.dto.DoctorScheduleDto;
import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.AppointmentStatus;
import com.petvax.care.entity.DoctorProfile;
import com.petvax.care.entity.User;
import com.petvax.care.repository.AppointmentRepository;
import com.petvax.care.repository.DoctorProfileRepository;
import com.petvax.care.repository.DoctorScheduleRepository;
import com.petvax.care.repository.UserRepository;
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
public class DoctorListService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DoctorProfileRepository doctorProfileRepository;
    
    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorScheduleService doctorScheduleService;
    
    /**
     * Get list of all active, verified doctors
     */
    @Transactional(readOnly = true)
    public List<DoctorListResponse> getAvailableDoctors() {
        log.info("Fetching list of all available doctors");
        
        List<DoctorProfile> doctorProfiles = doctorProfileRepository.findAll();
        
        return doctorProfiles.stream()
                .filter(profile -> profile.getVerifiedAt() != null && 
                        profile.getUser().getAccountStatus().toString().equals("ACTIVE"))
                .map(profile -> mapDoctorProfileToResponse(profile))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter doctors by specialty
     */
    @Transactional(readOnly = true)
    public List<DoctorListResponse> filterBySpecialty(String specialty) {
        log.info("Filtering doctors by specialty: {}", specialty);
        
        List<DoctorProfile> doctorProfiles = doctorProfileRepository.findAll();
        
        return doctorProfiles.stream()
                .filter(profile -> profile.getVerifiedAt() != null && 
                        profile.getUser().getAccountStatus().toString().equals("ACTIVE") &&
                        profile.getSpecialization() != null &&
                        profile.getSpecialization().equalsIgnoreCase(specialty))
                .map(profile -> mapDoctorProfileToResponse(profile))
                .collect(Collectors.toList());
    }
    
    /**
     * Get detailed doctor profile with schedule information
     */
    @Transactional(readOnly = true)
    public DoctorListResponse getDoctorDetails(Long doctorId) {
        log.info("Fetching details for doctor ID: {}", doctorId);
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> {
                    log.warn("Doctor not found: {}", doctorId);
                    return new IllegalArgumentException("Doctor not found");
                });
        
        DoctorProfile profile = doctorProfileRepository.findByUser(doctor)
                .orElseThrow(() -> {
                    log.warn("Doctor profile not found for user ID: {}", doctorId);
                    return new IllegalArgumentException("Doctor profile not found");
                });
        
        return mapDoctorProfileToResponse(profile);
    }
    
    /**
     * Get count of available appointment slots for a doctor this week
     */
    @Transactional(readOnly = true)
    public Integer getAvailableSlotCount(Long doctorId) {
        log.info("Counting available slots for doctor ID: {} this week", doctorId);
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> {
                    log.warn("Doctor not found: {}", doctorId);
                    return new IllegalArgumentException("Doctor not found");
                });
        
        int totalSlots = 0;
        
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        
        for (LocalDate date = today; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            List<Appointment> bookedAppointments = appointmentRepository.findDoctorAppointmentsByDate(doctor, date);
            
            int bookedCount = (int) bookedAppointments.stream()
                    .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
                    .count();
            
            // Assuming 10 slots per day (configurable)
            int availableSlotsForDay = Math.max(0, 10 - bookedCount);
            totalSlots += availableSlotsForDay;
        }
        
        return totalSlots;
    }
    
    /**
     * Map DoctorProfile entity to DoctorListResponse DTO
     */
    private DoctorListResponse mapDoctorProfileToResponse(DoctorProfile profile) {
        User doctor = profile.getUser();
        Integer availableSlots = getAvailableSlotCount(doctor.getId());
        
        return DoctorListResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .specialization(profile.getSpecialization())
                .yearsOfExperience(profile.getYearsOfExperience())
                .clinicName(profile.getClinicName())
                .licenseId(profile.getLicenseId())
                .availableSlots(availableSlots)
                .isVerified(profile.getVerifiedAt() != null)
                .verifiedAt(profile.getVerifiedAt())
                .schedule(doctorScheduleService.getDoctorScheduleList(doctor))
                .build();
    }
}
