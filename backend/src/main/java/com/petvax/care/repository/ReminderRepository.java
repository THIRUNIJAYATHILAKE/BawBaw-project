package com.petvax.care.repository;

import com.petvax.care.entity.Reminder;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUser(User user);

    List<Reminder> findByUserAndReminderType(User user, String reminderType);

    @Query("SELECT r FROM Reminder r WHERE r.sent = false AND r.scheduledDate <= :now ORDER BY r.scheduledDate ASC")
    List<Reminder> findUnsentRemindersByScheduledDate(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reminder r WHERE r.user = :user AND r.sent = true ORDER BY r.sentAt DESC")
    List<Reminder> findSentRemindersByUser(@Param("user") User user);

    @Query("SELECT r FROM Reminder r WHERE r.user = :user ORDER BY r.scheduledDate DESC")
    List<Reminder> findByUserOrderByScheduledDateDesc(@Param("user") User user);

    @Query("SELECT r FROM Reminder r WHERE r.sent = false AND r.scheduledDate <= :now AND r.user = :user")
    List<Reminder> findUnsentRemindersByUserAndDate(@Param("user") User user, @Param("now") LocalDateTime now);
}
