# Email Service Quick Reference Guide

## Configuration Setup

### Environment Variables Required
```bash
# Required
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password      # Use Gmail App Password

# Optional (defaults provided)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
EMAIL_DISPLAY_NAME=PetVax Care
EMAIL_ASYNC_ENABLED=true
```

## Usage Examples

### 1. Sending Verification Email
```java
@Autowired
private EmailService emailService;

// In registration flow
emailService.sendVerificationEmail(userEmail, otpCode);
// OTP: 6-digit code, 10 minutes validity
```

### 2. Sending Password Reset Email
```java
// In forgot password flow
emailService.sendPasswordResetEmail(userEmail, otpCode);
// OTP: 6-digit code, 10 minutes validity
```

### 3. Sending Appointment Confirmation
```java
// After appointment booking
emailService.sendAppointmentConfirmationEmail(appointment, owner, doctor);
// Parameters:
// - appointment: Appointment entity with date, duration, purpose
// - owner: User (pet owner)
// - doctor: User (veterinarian)
```

### 4. Sending Appointment Cancellation
```java
// After appointment cancellation
emailService.sendAppointmentCancellationEmail(appointment, owner);
// Includes cancellation reason if provided
```

### 5. Sending Doctor Verification
```java
// After doctor profile review
boolean approved = true;
String message = "Your credentials have been verified";
emailService.sendDoctorVerificationEmail(doctor, approved, message);
```

### 6. Sending Vaccination Reminder
```java
// In scheduled reminder task
emailService.sendVaccinationReminderEmail(owner, pet, "Rabies Vaccine");
// Parameters:
// - owner: User (pet owner)
// - pet: Pet entity
// - vaccineName: String (vaccine type)
```

### 7. Sending Deworming Reminder
```java
// In scheduled reminder task
emailService.sendDewormingReminderEmail(owner, pet);
// Parameters:
// - owner: User (pet owner)
// - pet: Pet entity
```

## Email Method Signatures

```java
@Async
public void sendVerificationEmail(String email, String otp)

@Async
public void sendPasswordResetEmail(String email, String otp)

@Async
public void sendAppointmentConfirmationEmail(
    Appointment appointment, 
    User owner, 
    User doctor
)

@Async
public void sendAppointmentCancellationEmail(
    Appointment appointment, 
    User owner
)

@Async
public void sendDoctorVerificationEmail(
    User doctor, 
    boolean approved, 
    String message
)

@Async
public void sendVaccinationReminderEmail(
    User owner, 
    Pet pet, 
    String vaccineName
)

@Async
public void sendDewormingReminderEmail(
    User owner, 
    Pet pet
)

@Async
public void sendAccountCreatedEmail(
    String email, 
    String username
)
```

## Important Notes

1. **Async Behavior**
   - All methods are asynchronous (@Async)
   - Returns immediately, sending happens in background
   - No need to handle boolean returns
   - Failures are logged but don't throw exceptions

2. **Retry Logic**
   - Automatically retries up to 3 times on failure
   - Exponential backoff: 1s, 2s, 3s delays
   - Check logs for retry details

3. **Template Rendering**
   - Templates are built programmatically (not Thymeleaf)
   - HTML is generated in the EmailService methods
   - No external template files are loaded at runtime

4. **Email Content**
   - All emails include branded header with logo
   - Professional styling with responsive design
   - Mobile-friendly layout
   - UTF-8 encoding support

5. **Error Handling**
   - Mail sender checks if not configured (graceful degradation)
   - All exceptions caught and logged
   - Application continues without email failures

## Testing in Development

### Using Gmail Test Account
```properties
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-test-email@gmail.com
MAIL_PASSWORD=your-app-password  # Generate from Google Account Settings
```

### Using MailHog (Local Testing)
```properties
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=test
MAIL_PASSWORD=test
```

Then access MailHog UI at http://localhost:8025

### Using SendGrid
```properties
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## Thread Pool Configuration

Default configuration in EmailConfig:
- **Core Threads:** 5 (minimum threads always active)
- **Max Threads:** 10 (peak capacity)
- **Queue Capacity:** 100 (tasks waiting to be processed)
- **Thread Name:** email-executor-{number}
- **Graceful Shutdown:** 60 seconds wait for pending tasks

To customize, override the EmailConfig bean or use properties:
```yaml
app:
  email:
    async:
      enabled: true
      # Custom executor configuration (not yet implemented)
      # core-pool-size: 5
      # max-pool-size: 10
      # queue-capacity: 100
```

## Logging

Enable debug logging for email service:
```yaml
logging:
  level:
    com.petvax.care.service.EmailService: DEBUG
```

Log output includes:
- Email sent confirmation
- Retry attempts
- Failure reasons
- Configuration warnings

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for sensitive data
3. **Use Gmail App Passwords**, not account password
4. **Enable 2FA** on email accounts
5. **Monitor email delivery** for suspicious patterns
6. **Rotate API keys/passwords** periodically
7. **Audit email logs** for unauthorized access

## Troubleshooting

### Emails Not Being Sent
1. Check `MAIL_USERNAME` and `MAIL_PASSWORD` env vars
2. Verify SMTP host and port are correct
3. Check if mail sender is null (logs will show warning)
4. Look for retry attempts in logs
5. Test connection using telnet/nc to SMTP server

### Wrong Sender Address
1. Check `MAIL_USERNAME` setting
2. Verify `EMAIL_DISPLAY_NAME` (optional)
3. Ensure domain/email is authorized for sending

### Timeout Errors
1. Check network connectivity to SMTP server
2. Verify firewall allows outbound connections
3. Increase timeout values if network is slow:
   - `spring.mail.properties.mail.smtp.connectiontimeout`
   - `spring.mail.properties.mail.smtp.timeout`

### Styling Issues in Email Client
- Most modern email clients support CSS
- Some clients (Yahoo, Outlook) have limitations
- Inline styles are more reliable than CSS blocks
- All PetVax Care templates use inline styles

## Contact & Support

For implementation questions or issues:
1. Check logs for detailed error messages
2. Verify email configuration is correct
3. Test with MailHog in development
4. Contact development team with error details
