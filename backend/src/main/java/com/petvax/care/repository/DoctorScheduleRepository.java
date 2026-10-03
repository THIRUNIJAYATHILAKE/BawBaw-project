package com.petvax.care.repository;

import com.petvax.care.entity.DoctorSchedule;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    
    /**
     * Find schedule for a doctor on a specific day of week
     */
    List<DoctorSchedule> findByDoctorAndDayOfWeekAndIsAvailableTrue(User doctor, Integer dayOfWeek);
    
    /**
     * Find all schedules for a doctor
     */
    List<DoctorSchedule> findByDoctorAndIsAvailableTrue(User doctor);
    
    /**
     * Find schedule by doctor, day of week with specific time range
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor AND ds.dayOfWeek = :dayOfWeek AND ds.isAvailable = true AND ds.startTime <= :time AND ds.endTime > :time")
    Optional<DoctorSchedule> findScheduleForTime(@Param("doctor") User doctor, @Param("dayOfWeek") Integer dayOfWeek, @Param("time") LocalTime time);
    
    /**
     * Find all available schedules for a doctor ordered by day of week
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor AND ds.isAvailable = true ORDER BY ds.dayOfWeek ASC, ds.startTime ASC")
    List<DoctorSchedule> findAvailableSchedules(@Param("doctor") User doctor);
    
    /**
     * Find doctor schedule for a specific date (day of week)
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor AND ds.dayOfWeek = :dayOfWeek AND ds.isAvailable = true")
    List<DoctorSchedule> findDoctorScheduleByDate(@Param("doctor") User doctor, @Param("dayOfWeek") Integer dayOfWeek);
}
