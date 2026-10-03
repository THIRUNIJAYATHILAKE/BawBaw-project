package com.petvax.care.controller;

import com.petvax.care.dto.ReminderResponse;
import com.petvax.care.entity.User;
import com.petvax.care.service.ReminderService;
import com.petvax.care.service.UserService;
import com.petvax.care.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/reminders")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserService userService;

    /**
     * Get all reminders for the current user
     */
    @GetMapping
    public ResponseEntity<?> getReminders(
            @RequestParam(required = false) String type) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ReminderResponse> reminders;
            if (type != null && !type.isEmpty()) {
                reminders = reminderService.getRemindersByType(user.getId(), type);
                log.info("Retrieved {} reminders of type {} for user: {}", reminders.size(), type, username);
            } else {
                reminders = reminderService.getRemindersByUser(user.getId());
                log.info("Retrieved {} reminders for user: {}", reminders.size(), username);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reminders retrieved successfully");
            response.put("total", reminders.size());
            response.put("reminders", reminders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving reminders", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get sent reminders (reminder history)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getSentReminders() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ReminderResponse> sentReminders = reminderService.getSentReminders(user.getId());
            log.info("Retrieved {} sent reminders for user: {}", sentReminders.size(), username);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sent reminders retrieved successfully");
            response.put("total", sentReminders.size());
            response.put("reminders", sentReminders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving sent reminders", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Mark a reminder as read/acknowledged
     */
    @PostMapping("/{reminderId}/acknowledge")
    public ResponseEntity<?> acknowledgeReminder(@PathVariable Long reminderId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            ReminderResponse reminder = reminderService.acknowledgeReminder(reminderId);
            log.info("Reminder {} acknowledged by user: {}", reminderId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reminder marked as read");
            response.put("reminder", reminder);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error acknowledging reminder", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    /**
     * Extract user ID from authentication
     */
    private Long extractUserIdFromAuth(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            log.warn("Could not parse username as Long: {}", username);
            return null;
        }
    }
}
