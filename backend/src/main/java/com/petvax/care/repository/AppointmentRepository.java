package com.petvax.care.repository;

import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.Pet;
import com.petvax.care.entity.AppointmentStatus;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * Find upcoming appointments for a pet after a given date
     */
    @Query("SELECT a FROM Appointment a WHERE a.pet = :pet AND a.appointmentDate > :date AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByPet(@Param("pet") Pet pet, @Param("date") LocalDateTime date);
    
    /**
     * Find all appointments for a pet
     */
    List<Appointment> findByPet(Pet pet);
    
    /**
     * Find appointments by pet and status
     */
    List<Appointment> findByPetAndStatus(Pet pet, AppointmentStatus status);
    
    /**
     * Find appointments for a doctor on a specific date (for double-booking prevention)
     * Checks if doctor already has an appointment during the requested time slot
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.status != 'CANCELLED' " +
            "AND a.appointmentDate >= :startDateTime AND a.appointmentDate < :endDateTime")
    List<Appointment> findConflictingAppointments(@Param("doctor") User doctor, 
                                                   @Param("startDateTime") LocalDateTime startDateTime,
                                                   @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Find all appointments for an owner
     */
    List<Appointment> findByOwner(User owner);
    
    /**
     * Find appointments for owner with non-cancelled status
     */
    @Query("SELECT a FROM Appointment a WHERE a.owner = :owner AND a.status != 'CANCELLED' ORDER BY a.appointmentDate DESC")
    List<Appointment> findActiveAppointmentsByOwner(@Param("owner") User owner);
    
    /**
     * Find all appointments for a doctor
     */
    List<Appointment> findByDoctor(User doctor);
    
    /**
     * Find appointments for a doctor on a specific date
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND DATE(a.appointmentDate) = :date AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findDoctorAppointmentsByDate(@Param("doctor") User doctor, @Param("date") LocalDate date);
    
    /**
     * Find appointments by owner and date
     */
    @Query("SELECT a FROM Appointment a WHERE a.owner = :owner AND DATE(a.appointmentDate) = :date ORDER BY a.appointmentDate ASC")
    List<Appointment> findOwnerAppointmentsByDate(@Param("owner") User owner, @Param("date") LocalDate date);
    
    /**
     * Find appointments for doctor within a date range
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDate >= :startDate AND a.appointmentDate < :endDate AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findDoctorAppointmentsByDateRange(@Param("doctor") User doctor, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find appointment by ID and doctor (authorization check)
     */
    Optional<Appointment> findByIdAndDoctor(Long id, User doctor);
}

