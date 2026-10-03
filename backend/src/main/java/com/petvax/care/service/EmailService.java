package com.petvax.care.service;

import com.petvax.care.entity.Appointment;
import com.petvax.care.entity.DoctorProfile;
import com.petvax.care.entity.Pet;
import com.petvax.care.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm a");
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@petvaxcare.com}")
    private String fromEmail;
    
    @Value("${app.email.display-name:PetVax Care}")
    private String displayName;
    
    /**
     * Send email verification OTP asynchronously
     */
    @Async
    public void sendVerificationEmail(String email, String otp) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", email);
                return;
            }
            
            String subject = "PetVax Care - Email Verification";
            String body = buildVerificationEmailBody(otp);
            
            sendHtmlEmailWithRetry(email, subject, body, 0);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
        }
    }
    
    /**
     * Send password reset OTP asynchronously
     */
    @Async
    public void sendPasswordResetEmail(String email, String otp) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", email);
                return;
            }
            
            String subject = "PetVax Care - Password Reset";
            String body = buildPasswordResetEmailBody(otp);
            
            sendHtmlEmailWithRetry(email, subject, body, 0);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }
    
    /**
     * Send appointment confirmation email asynchronously
     */
    @Async
    public void sendAppointmentConfirmationEmail(Appointment appointment, User owner, User doctor) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", owner.getEmail());
                return;
            }
            
            String subject = "Appointment Confirmation - " + appointment.getPet().getName();
            String body = buildAppointmentConfirmationEmailBody(appointment, owner, doctor);
            
            sendHtmlEmailWithRetry(owner.getEmail(), subject, body, 0);
            log.info("Appointment confirmation email sent to: {}", owner.getEmail());
        } catch (Exception e) {
            log.error("Failed to send appointment confirmation email: {}", e.getMessage());
        }
    }
    
    /**
     * Send appointment cancellation email asynchronously
     */
    @Async
    public void sendAppointmentCancellationEmail(Appointment appointment, User owner) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", owner.getEmail());
                return;
            }
            
            String subject = "Appointment Cancelled - " + appointment.getPet().getName();
            String body = buildAppointmentCancellationEmailBody(appointment, owner);
            
            sendHtmlEmailWithRetry(owner.getEmail(), subject, body, 0);
            log.info("Appointment cancellation email sent to: {}", owner.getEmail());
        } catch (Exception e) {
            log.error("Failed to send appointment cancellation email: {}", e.getMessage());
        }
    }
    
    /**
     * Send doctor verification email asynchronously
     */
    @Async
    public void sendDoctorVerificationEmail(User doctor, boolean approved, String message) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", doctor.getEmail());
                return;
            }
            
            String subject = approved ? "Doctor Account Approved" : "Doctor Account Verification Status";
            String body = buildDoctorVerificationEmailBody(doctor, approved, message);
            
            sendHtmlEmailWithRetry(doctor.getEmail(), subject, body, 0);
            log.info("Doctor verification email sent to: {}", doctor.getEmail());
        } catch (Exception e) {
            log.error("Failed to send doctor verification email: {}", e.getMessage());
        }
    }
    
    /**
     * Send vaccination reminder email asynchronously
     */
    @Async
    public void sendVaccinationReminderEmail(User owner, Pet pet, String vaccineName) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", owner.getEmail());
                return;
            }
            
            String subject = "Vaccination Reminder - " + pet.getName();
            String body = buildVaccinationReminderEmailBody(owner, pet, vaccineName);
            
            sendHtmlEmailWithRetry(owner.getEmail(), subject, body, 0);
            log.info("Vaccination reminder email sent to: {}", owner.getEmail());
        } catch (Exception e) {
            log.error("Failed to send vaccination reminder email: {}", e.getMessage());
        }
    }
    
    /**
     * Send deworming reminder email asynchronously
     */
    @Async
    public void sendDewormingReminderEmail(User owner, Pet pet) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", owner.getEmail());
                return;
            }
            
            String subject = "Deworming Reminder - " + pet.getName();
            String body = buildDewormingReminderEmailBody(owner, pet);
            
            sendHtmlEmailWithRetry(owner.getEmail(), subject, body, 0);
            log.info("Deworming reminder email sent to: {}", owner.getEmail());
        } catch (Exception e) {
            log.error("Failed to send deworming reminder email: {}", e.getMessage());
        }
    }
    
    /**
     * Send account created notification asynchronously
     */
    @Async
    public void sendAccountCreatedEmail(String email, String username) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", email);
                return;
            }
            
            String subject = "Welcome to PetVax Care";
            String body = buildAccountCreatedEmailBody(username);
            
            sendHtmlEmailWithRetry(email, subject, body, 0);
            log.info("Account created email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send account created email to {}: {}", email, e.getMessage());
        }
    }
    
    /**
     * Send HTML email with retry logic
     */
    private void sendHtmlEmailWithRetry(String to, String subject, String htmlContent, int attempt) {
        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                log.warn("Email send failed for {}. Retrying attempt {}/{}", to, attempt + 1, MAX_RETRY_ATTEMPTS);
                try {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                    sendHtmlEmailWithRetry(to, subject, htmlContent, attempt + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Email retry interrupted for: {}", to);
                }
            } else {
                log.error("Failed to send email to {} after {} attempts: {}", to, MAX_RETRY_ATTEMPTS, e.getMessage());
            }
        }
    }
    
    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, displayName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Failed to encode email", e);
        }
    }
    
    /**
     * Send plain text email
     */
    private void sendTextEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        mailSender.send(message);
    }
    
    /**
     * Build verification email body
     */
    private String buildVerificationEmailBody(String otp) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #333;'>Email Verification</h2>" +
                "<p>Your OTP for email verification is:</p>" +
                "<div style='background-color: #007bff; color: white; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 2px;'>" +
                otp +
                "</div>" +
                "<p style='margin-top: 20px; color: #666;'>This OTP is valid for 10 minutes.</p>" +
                "<p style='color: #999;'>If you didn't request this, please ignore this email.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build password reset email body
     */
    private String buildPasswordResetEmailBody(String otp) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #333;'>Password Reset Request</h2>" +
                "<p>Your OTP for password reset is:</p>" +
                "<div style='background-color: #007bff; color: white; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 2px;'>" +
                otp +
                "</div>" +
                "<p style='margin-top: 20px; color: #666;'>This OTP is valid for 10 minutes.</p>" +
                "<p style='color: #999;'>If you didn't request this, please ignore this email and your account remains secure.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build account created email body
     */
    private String buildAccountCreatedEmailBody(String username) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #333;'>Welcome to PetVax Care!</h2>" +
                "<p>Hello " + username + ",</p>" +
                "<p>Your account has been successfully created. You can now log in to access all features.</p>" +
                "<p style='margin-top: 20px; color: #999;'>Best regards,<br>PetVax Care Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build appointment confirmation email body
     */
    private String buildAppointmentConfirmationEmailBody(Appointment appointment, User owner, User doctor) {
        String petName = appointment.getPet().getName();
        String doctorName = doctor.getFullName();
        String appointmentDate = appointment.getAppointmentDate().format(DATE_FORMATTER);
        String appointmentTime = appointment.getAppointmentDate().format(TIME_FORMATTER);
        String purpose = appointment.getPurpose().toString().replace("_", " ");
        
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #333;'>Appointment Confirmation</h2>" +
                "<p>Hello " + owner.getFullName() + ",</p>" +
                "<p>Your appointment has been confirmed. Here are the details:</p>" +
                "<div style='background-color: white; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;'>" +
                "<p><strong>Pet:</strong> " + petName + "</p>" +
                "<p><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "<p><strong>Date:</strong> " + appointmentDate + "</p>" +
                "<p><strong>Time:</strong> " + appointmentTime + "</p>" +
                "<p><strong>Purpose:</strong> " + purpose + "</p>" +
                (appointment.getDurationMinutes() != null ? 
                   "<p><strong>Duration:</strong> " + appointment.getDurationMinutes() + " minutes</p>" : "") +
                "</div>" +
                "<p style='color: #666;'>Please arrive 10 minutes early. If you need to reschedule, please contact us as soon as possible.</p>" +
                "<p style='margin-top: 20px; color: #999;'>Best regards,<br>PetVax Care Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build appointment cancellation email body
     */
    private String buildAppointmentCancellationEmailBody(Appointment appointment, User owner) {
        String petName = appointment.getPet().getName();
        String appointmentDate = appointment.getAppointmentDate().format(DATE_FORMATTER);
        String appointmentTime = appointment.getAppointmentDate().format(TIME_FORMATTER);
        
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #d32f2f;'>Appointment Cancelled</h2>" +
                "<p>Hello " + owner.getFullName() + ",</p>" +
                "<p>Your appointment has been cancelled.</p>" +
                "<div style='background-color: white; padding: 15px; border-left: 4px solid #d32f2f; margin: 20px 0;'>" +
                "<p><strong>Pet:</strong> " + petName + "</p>" +
                "<p><strong>Date:</strong> " + appointmentDate + "</p>" +
                "<p><strong>Time:</strong> " + appointmentTime + "</p>" +
                (appointment.getCancelledReason() != null && !appointment.getCancelledReason().isEmpty() ? 
                   "<p><strong>Reason:</strong> " + appointment.getCancelledReason() + "</p>" : "") +
                "</div>" +
                "<p style='color: #666;'>If you would like to schedule another appointment, please visit our website or contact us.</p>" +
                "<p style='margin-top: 20px; color: #999;'>Best regards,<br>PetVax Care Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build doctor verification email body
     */
    private String buildDoctorVerificationEmailBody(User doctor, boolean approved, String message) {
        String subject = approved ? "Your doctor account has been approved!" : "Doctor Account Verification";
        String statusColor = approved ? "#28a745" : "#ffc107";
        
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: " + statusColor + ";'>" + subject + "</h2>" +
                "<p>Hello Dr. " + doctor.getFullName() + ",</p>" +
                (approved ? 
                   "<p>Congratulations! Your doctor profile has been verified and approved. You can now start accepting appointments from pet owners.</p>" :
                   "<p>Thank you for your submission. Our team is reviewing your doctor profile.</p>") +
                (message != null && !message.isEmpty() ? 
                   "<div style='background-color: white; padding: 15px; border-left: 4px solid " + statusColor + "; margin: 20px 0;'>" +
                   "<p><strong>Message:</strong> " + message + "</p>" +
                   "</div>" : "") +
                "<p style='margin-top: 20px; color: #999;'>Best regards,<br>PetVax Care Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build vaccination reminder email body
     */
    private String buildVaccinationReminderEmailBody(User owner, Pet pet, String vaccineName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #333;'>Vaccination Reminder</h2>" +
                "<p>Hello " + owner.getFullName() + ",</p>" +
                "<p>This is a reminder that " + pet.getName() + " is due for a vaccination.</p>" +
                "<div style='background-color: white; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;'>" +
                "<p><strong>Pet:</strong> " + pet.getName() + "</p>" +
                "<p><strong>Species:</strong> " + pet.getSpecies() + "</p>" +
                "<p><strong>Vaccine:</strong> " + vaccineName + "</p>" +
                "</div>" +
                "<p style='color: #666;'>Please schedule an appointment with a veterinarian to keep your pet's vaccinations up to date.</p>" +
                "<p style='margin-top: 20px; color: #999;'>Best regards,<br>PetVax Care Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Build deworming reminder email body
     */
    private String buildDewormingReminderEmailBody(User owner, Pet pet) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                "<h2 style='color: #333;'>Deworming Reminder</h2>" +
                "<p>Hello " + owner.getFullName() + ",</p>" +
                "<p>This is a reminder that " + pet.getName() + " is due for deworming.</p>" +
                "<div style='background-color: white; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;'>" +
                "<p><strong>Pet:</strong> " + pet.getName() + "</p>" +
                "<p><strong>Species:</strong> " + pet.getSpecies() + "</p>" +
                "</div>" +
                "<p style='color: #666;'>Regular deworming is essential for your pet's health. Please schedule an appointment with a veterinarian.</p>" +
                "<p style='margin-top: 20px; color: #999;'>Best regards,<br>PetVax Care Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
