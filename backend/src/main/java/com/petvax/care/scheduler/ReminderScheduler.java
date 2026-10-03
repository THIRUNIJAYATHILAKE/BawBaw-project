package com.petvax.care.scheduler;

import com.petvax.care.entity.Reminder;
import com.petvax.care.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ReminderScheduler {

    @Autowired
    private ReminderService reminderService;

    /**
     * Run every hour to send due reminders
     */
    @Scheduled(fixedDelay = 3600000)
    public void sendDueReminders() {
        try {
            log.info("Starting reminder scheduler job");

            List<Reminder> dueReminders = reminderService.getUnsentDueReminders();
            log.info("Found {} due reminders to send", dueReminders.size());

            for (Reminder reminder : dueReminders) {
                reminderService.sendReminderNotification(reminder);
            }

            log.info("Completed reminder scheduler job - sent {} reminders", dueReminders.size());
        } catch (Exception e) {
            log.error("Error in reminder scheduler", e);
        }
    }

    /**
     * Alternative: Run at a fixed rate (every hour)
     * Use this instead of fixedDelay if you want reminders to run on a schedule
     * rather than with a delay between completion
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void sendDueRemindersFixed() {
        // This would be an alternative to the fixedDelay method
        // Uncomment to use, but disable sendDueReminders() method
    }
}
