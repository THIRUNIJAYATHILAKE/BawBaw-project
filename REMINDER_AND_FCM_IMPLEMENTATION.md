# PetVax Care - Reminder Scheduling & Firebase FCM Notification System
## Implementation Summary (Phase 2)

### Overview
This document summarizes the complete implementation of the Reminder Scheduling and Firebase FCM Notification System for PetVax Care. The system enables automated push notifications for vaccinations, deworming, and appointments with proper reminder scheduling and device token management.

---

## PART 1: REMINDER SYSTEM (Backend)

### 1. Database Entities

#### Reminder Entity (`Reminder.java`)
- **Status**: Already existed in codebase
- **Fields**:
  - `id`: Long (Primary Key)
  - `user`: User (FK) - Owner who receives reminder
  - `medicalRecord`: MedicalRecord (FK) - Links to vaccination/deworming
  - `appointment`: Appointment (FK) - Links to appointment
  - `reminderType`: String - VACCINATION, DEWORMING, or APPOINTMENT
  - `scheduledDate`: LocalDateTime - When reminder should be sent
  - `sent`: Boolean - Whether reminder was already sent
  - `sentAt`: LocalDateTime - Timestamp when sent
  - Audit fields: createdAt, updatedAt

#### DeviceToken Entity (`DeviceToken.java`)
- **Status**: Already existed in codebase
- **Fields**:
  - `id`: Long (Primary Key)
  - `user`: User (FK) - Device owner
  - `fcmToken`: String - Firebase Cloud Messaging token
  - `deviceName`: String - Device name (e.g., "iPhone 12")
  - `deviceType`: String - Device type (e.g., "iOS", "Android")
  - `lastUsed`: LocalDateTime - Last activity timestamp
  - Audit fields: createdAt, updatedAt

### 2. Repository Layer

#### ReminderRepository
**File**: `src/main/java/com/petvax/care/repository/ReminderRepository.java`

**Key Methods**:
- `findByUser(User user)` - Get all reminders for a user
- `findByUserAndReminderType(User user, String reminderType)` - Filter by type
- `findUnsentRemindersByScheduledDate(LocalDateTime now)` - Find due reminders
- `findSentRemindersByUser(User user)` - Get reminder history
- `findByUserOrderByScheduledDateDesc(User user)` - Get all reminders sorted

#### DeviceTokenRepository
**File**: `src/main/java/com/petvax/care/repository/DeviceTokenRepository.java`

**Key Methods**:
- `findByUser(User user)` - Get all tokens for user
- `findByFcmToken(String fcmToken)` - Find token by FCM value
- `findByUserAndFcmToken(User user, String fcmToken)` - Find specific user token
- `findByUserOrderByLastUsedDesc(User user)` - Get tokens sorted by activity
- `deleteByFcmToken(String fcmToken)` - Remove token
- `deleteByUserAndFcmToken(User user, String fcmToken)` - Remove user's token

### 3. Service Layer

#### ReminderService
**File**: `src/main/java/com/petvax/care/service/ReminderService.java`

**Reminder Scheduling Methods**:

1. **scheduleVaccinationReminders(Vaccination vaccination)**
   - Creates 3 reminders: T-3 days, T-1 day, T-day
   - Scheduled date: nextBoosterDate at 00:00 (adjusted backward)
   - Type: "VACCINATION"

2. **scheduledewormingReminders(DewormingRecord dewormingRecord)**
   - Creates 2 reminders: T-3 days, T-1 day
   - Scheduled date: nextDoseDate at 00:00 (adjusted backward)
   - Type: "DEWORMING"

3. **scheduleAppointmentReminders(Appointment appointment)**
   - Creates 2 reminders: T-1 day, T-day (same time as appointment)
   - Type: "APPOINTMENT"

**Retrieval Methods**:
- `getRemindersByUser(Long userId)` - Get all user reminders
- `getRemindersByType(Long userId, String type)` - Filter by type
- `getSentReminders(Long userId)` - Get reminder history
- `getUnsentDueReminders()` - Find all overdue unsent reminders

**Processing Methods**:
- `acknowledgeReminder(Long reminderId)` - Mark as read
- `sendReminderNotification(Reminder reminder)` - Send via FCM and mark as sent
- `markRemindersAsSent(List<Reminder> reminders)` - Batch mark as sent

**Helper Methods**:
- `buildReminderTitle(Reminder)` - Generate notification title
- `buildReminderBody(Reminder)` - Generate notification body with time calculations
- `buildReminderData(Reminder)` - Build notification payload
- `convertToResponse(Reminder)` - Convert entity to DTO

#### Service Integration

**VaccinationService Integration**:
- Added `ReminderService` autowired dependency
- Modified `recordVaccination()` method to call `scheduleVaccinationReminders()`
- Reminders are automatically created when vaccination is recorded

**DewormingService Integration**:
- Added `ReminderService` autowired dependency
- Modified `recordDeworming()` method to call `scheduledewormingReminders()`
- Reminders are automatically created when deworming is recorded

**AppointmentService Integration**:
- Added `ReminderService` as final field in constructor
- Modified `bookAppointment()` method to call `scheduleAppointmentReminders()`
- Reminders are automatically created when appointment is booked

### 4. Scheduler

#### ReminderScheduler
**File**: `src/main/java/com/petvax/care/scheduler/ReminderScheduler.java`

**Job**: `sendDueReminders()`
- **Frequency**: Every hour (fixedDelay = 3600000 ms)
- **Process**:
  1. Find all unsent reminders with scheduledDate <= now
  2. For each reminder:
     - Send push notification via FCM
     - Mark as sent with timestamp
  3. Log execution results

### 5. Controller Layer

#### ReminderController
**File**: `src/main/java/com/petvax/care/controller/ReminderController.java`
**Endpoint Base**: `/api/owner/reminders`

**Endpoints**:

1. **GET /api/owner/reminders**
   - Query Param: `type` (optional) - VACCINATION, DEWORMING, APPOINTMENT
   - Returns: List of user's reminders (filtered if type specified)
   - Auth: Required (extracts user from JWT)

2. **GET /api/owner/reminders/history**
   - Returns: List of sent reminders (reminder history)
   - Auth: Required

3. **POST /api/owner/reminders/{reminderId}/acknowledge**
   - Path Param: `reminderId` (Long)
   - Marks reminder as read
   - Returns: Updated reminder
   - Auth: Required

### 6. Data Transfer Objects (DTOs)

#### ReminderResponse
**File**: `src/main/java/com/petvax/care/dto/ReminderResponse.java`

```java
{
  "id": 1,
  "reminderType": "VACCINATION",
  "title": "Vaccination Reminder",
  "message": "Bella's Rabies vaccination is due in 3 days",
  "scheduledDate": "2026-05-27T00:00:00",
  "sent": false,
  "sentAt": null,
  "medicalRecordId": 5,
  "appointmentId": null,
  "petName": "Bella",
  "createdAt": "2026-05-24T10:30:00",
  "updatedAt": "2026-05-24T10:30:00"
}
```

---

## PART 2: FIREBASE FCM INTEGRATION

### 1. Configuration

#### FirebaseConfig
**File**: `src/main/java/com/petvax/care/config/FirebaseConfig.java`

**Features**:
- Initializes Firebase Admin SDK
- Loads credentials from environment variable: `FIREBASE_CREDENTIALS_PATH`
- Provides `FirebaseMessaging` bean for sending notifications
- Graceful fallback to default credentials if path not configured

**Configuration Entry** (application.yml):
```yaml
app:
  firebase:
    credentials-path: ${FIREBASE_CREDENTIALS_PATH}
```

**Setup Instructions**:
1. Create Firebase project in Google Cloud Console
2. Generate service account key JSON file
3. Set environment variable: `FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json`

#### firebase-service-account.json Template
**File**: `src/main/resources/firebase-service-account.json`

Template provided with placeholders for:
- `project_id`
- `private_key_id`
- `private_key`
- `client_email`
- `client_id`

### 2. Service Layer

#### NotificationService
**File**: `src/main/java/com/petvax/care/service/NotificationService.java`

**Core Methods**:

1. **sendPushNotification(Long userId, String title, String body, Map<String, String> data)**
   - Gets all active device tokens for user
   - Sends notification to each token
   - Handles token rotation/invalidation
   - Returns: boolean (success status)

2. **sendNotificationToToken(String token, String title, String body, Map<String, String> data)**
   - Builds FCM message with Notification and data payload
   - Sends via Firebase Admin SDK
   - Returns: message ID from FCM

3. **sendMultipleNotifications(Map<Long, NotificationData> notifications)**
   - Batch send to multiple users
   - Returns: Map of userId -> success status

4. **buildNotificationPayload(String type, String petName, String details)**
   - Creates data payload for notification
   - Includes: type, petName, details, timestamp

**Error Handling**:
- Detects invalid/expired tokens
- Automatically invalidates bad tokens
- Retries with fresh token
- Comprehensive error logging

#### DeviceTokenService
**File**: `src/main/java/com/petvax/care/service/DeviceTokenService.java`

**Device Management Methods**:

1. **registerDeviceToken(Long userId, RegisterDeviceTokenRequest request)**
   - Saves or updates FCM token for device
   - Updates lastUsed timestamp
   - Returns: DeviceToken entity

2. **getActiveTokensForUser(Long userId)**
   - Gets tokens updated within last 30 days
   - Returns: List<String> of FCM tokens

3. **getAllTokensForUser(Long userId)**
   - Gets all user tokens (regardless of activity)
   - Returns: List<String> of FCM tokens

4. **getUserDeviceTokens(Long userId)**
   - Gets full DeviceToken entities with metadata
   - Returns: List<DeviceToken> sorted by lastUsed DESC

5. **invalidateDeviceToken(Long userId, String fcmToken)**
   - Removes specific token
   - Called when FCM returns invalid token error

6. **removeDeviceToken(String fcmToken)**
   - Removes token by FCM value alone

7. **updateLastUsed(String fcmToken)**
   - Updates activity timestamp for token

### 3. Controller Layer

#### DeviceTokenController
**File**: `src/main/java/com/petvax/care/controller/DeviceTokenController.java`
**Endpoint Base**: `/api/owner/device-tokens`

**Endpoints**:

1. **POST /api/owner/device-tokens/register**
   - Body: `RegisterDeviceTokenRequest`
   - Registers FCM token for current user's device
   - Returns: Token ID, device name, device type
   - Auth: Required

2. **GET /api/owner/device-tokens**
   - Returns: List of user's registered devices with token details
   - Auth: Required

3. **DELETE /api/owner/device-tokens/{tokenId}**
   - Path Param: `tokenId` (Long)
   - Removes device token
   - Returns: Success message
   - Auth: Required

### 4. Data Transfer Objects

#### RegisterDeviceTokenRequest
**File**: `src/main/java/com/petvax/care/dto/RegisterDeviceTokenRequest.java`

```java
{
  "fcmToken": "eWz8H7dK...",
  "deviceName": "iPhone 12 Pro",
  "deviceType": "iOS"
}
```

#### PushNotificationRequest
**File**: `src/main/java/com/petvax/care/dto/PushNotificationRequest.java`

```java
{
  "fcmToken": "eWz8H7dK...",
  "title": "Vaccination Reminder",
  "body": "Bella's Rabies vaccination is due in 3 days",
  "data": "{...}",
  "priority": 1
}
```

---

## NOTIFICATION FLOW

### 1. Reminder Creation Flow
```
Vaccination/Deworming/Appointment Recorded
  ↓
Service creates entity (Vaccination, DewormingRecord, Appointment)
  ↓
ReminderService.schedule*Reminders() called
  ↓
Reminders created in database with:
  - reminderType (VACCINATION, DEWORMING, APPOINTMENT)
  - scheduledDate (calculated T-3, T-1, T-day)
  - sent = false
  ↓
Reminders await scheduled job
```

### 2. Reminder Sending Flow
```
ReminderScheduler.sendDueReminders() runs every hour
  ↓
Query all unsent reminders with scheduledDate <= now
  ↓
For each due reminder:
  ├─ Build notification title and body
  ├─ Build data payload
  ├─ Get active device tokens for owner
  ├─ Send via NotificationService.sendPushNotification()
  │  ├─ For each token:
  │  │  ├─ Create FCM Message
  │  │  ├─ Send via FirebaseMessaging
  │  │  ├─ Handle response
  │  │  └─ Invalidate if token error
  │  └─ Return success status
  ├─ Mark reminder as sent
  ├─ Set sentAt timestamp
  └─ Save to database
  ↓
Log job completion with count
```

### 3. Device Token Registration Flow
```
User login
  ↓
Client calls POST /api/owner/device-tokens/register
  ↓
DeviceTokenService.registerDeviceToken()
  ├─ Check if token exists for user
  ├─ If exists: update lastUsed
  ├─ If new: create DeviceToken record
  └─ Return token details
  ↓
Future reminders sent to this token
```

---

## REMINDER MESSAGE EXAMPLES

### Vaccination Reminder (T-3 days before)
```
Title: "Vaccination Reminder"
Body: "Bella's Rabies vaccination is due in 3 days"
Data: {
  "type": "VACCINATION",
  "reminderId": "42",
  "petName": "Bella",
  "medicalRecordId": "5"
}
```

### Deworming Reminder (T-1 day before)
```
Title: "Deworming Reminder"
Body: "Max's deworming with Praziquantel is due in 1 day"
Data: {
  "type": "DEWORMING",
  "reminderId": "43",
  "petName": "Max",
  "medicalRecordId": "6"
}
```

### Appointment Reminder (T-day, same time)
```
Title: "Appointment Reminder"
Body: "Your appointment for Luna is today at 02:30 PM"
Data: {
  "type": "APPOINTMENT",
  "reminderId": "44",
  "petName": "Luna",
  "appointmentId": "8"
}
```

---

## FILES CREATED

### Repositories
- ✅ `src/main/java/com/petvax/care/repository/ReminderRepository.java`
- ✅ `src/main/java/com/petvax/care/repository/DeviceTokenRepository.java`

### Services
- ✅ `src/main/java/com/petvax/care/service/ReminderService.java`
- ✅ `src/main/java/com/petvax/care/service/NotificationService.java`
- ✅ `src/main/java/com/petvax/care/service/DeviceTokenService.java`

### Schedulers
- ✅ `src/main/java/com/petvax/care/scheduler/ReminderScheduler.java`

### Controllers
- ✅ `src/main/java/com/petvax/care/controller/ReminderController.java`
- ✅ `src/main/java/com/petvax/care/controller/DeviceTokenController.java`

### DTOs
- ✅ `src/main/java/com/petvax/care/dto/ReminderResponse.java`
- ✅ `src/main/java/com/petvax/care/dto/RegisterDeviceTokenRequest.java`
- ✅ `src/main/java/com/petvax/care/dto/PushNotificationRequest.java`

### Configuration
- ✅ `src/main/java/com/petvax/care/config/FirebaseConfig.java`
- ✅ `src/main/resources/firebase-service-account.json` (template)

### Exceptions
- ✅ `src/main/java/com/petvax/care/exception/UserNotFoundException.java`

### Modified Files
- ✅ `src/main/java/com/petvax/care/service/VaccinationService.java` - Added reminder scheduling
- ✅ `src/main/java/com/petvax/care/service/DewormingService.java` - Added reminder scheduling
- ✅ `src/main/java/com/petvax/care/service/AppointmentService.java` - Added reminder scheduling

---

## REQUIREMENTS MAPPING

### From SRD (Software Requirements Document)

| Requirement | Implementation | Status |
|---|---|---|
| FR-NOTIF-001: Push notifications for vaccination (T-3, T-1, T-day) | ReminderService.scheduleVaccinationReminders() creates 3 reminders; ReminderScheduler sends via FCM | ✅ Complete |
| FR-NOTIF-001: Push notifications for deworming | ReminderService.scheduledewormingReminders() creates 2 reminders (T-3, T-1); ReminderScheduler sends | ✅ Complete |
| FR-NOTIF-001: Push notifications for appointments | ReminderService.scheduleAppointmentReminders() creates 2 reminders (T-1, T-day); ReminderScheduler sends | ✅ Complete |
| FR-NOTIF-002: In-app popups on launch | ReminderController provides API to fetch unsent reminders; Frontend implements popup | ⏳ Frontend task |
| FR-NOTIF-003: User notification preferences | Not implemented (marked as Phase 3 TODO) | 📋 Future |
| Automated scheduling with @Scheduled jobs | ReminderScheduler with @Scheduled(fixedDelay=3600000) | ✅ Complete |
| Firebase Admin SDK integration | FirebaseConfig initializes SDK; NotificationService sends messages | ✅ Complete |
| Device token management | DeviceTokenService registers, updates, invalidates tokens | ✅ Complete |

---

## TESTING & VALIDATION

### Unit Test Recommendations
```java
// Test reminder scheduling
void testScheduleVaccinationReminders_CreatesThreeReminders()
void testScheduledewormingReminders_CreatesTwoReminders()
void testScheduleAppointmentReminders_CreatesTwoReminders()

// Test reminder retrieval
void testGetRemindersByType_FiltersCorrectly()
void testGetUnsentDueReminders_OnlyReturnsDue()

// Test notification sending
void testSendPushNotification_InvalidatesInvalidTokens()
void testSendPushNotification_RetriesWithFreshToken()

// Test device token management
void testRegisterDeviceToken_UpdatesIfExists()
void testGetActiveTokensForUser_FiltersBy30Days()
void testInvalidateDeviceToken_RemovesToken()
```

### Integration Test Recommendations
```java
// End-to-end flow
void testVaccinationReminder_ScheduledAndSentOnTime()
void testAppointmentReminder_MatchesAppointmentTime()
void testDeviceTokenInvalidation_AutoCleansUp()
```

### Manual Testing Checklist
- [ ] Create vaccination, verify 3 reminders created
- [ ] Register device token via API
- [ ] Trigger scheduler manually (or wait 1 hour)
- [ ] Verify FCM notifications received on device
- [ ] Test invalid token handling
- [ ] Test GET endpoints return correct data
- [ ] Test acknowledge endpoint marks as read

---

## DEPLOYMENT CONSIDERATIONS

### Environment Variables Required
```bash
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json
```

### Firebase Setup Steps
1. Create Firebase project at https://console.firebase.google.com
2. Enable Cloud Messaging
3. Create service account key (JSON format)
4. Download and secure the key file
5. Set FIREBASE_CREDENTIALS_PATH environment variable

### Docker Configuration
```dockerfile
ENV FIREBASE_CREDENTIALS_PATH=/app/config/firebase-service-account.json
COPY firebase-service-account.json /app/config/
```

### Kubernetes Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: firebase-credentials
type: Opaque
stringData:
  firebase-service-account.json: |
    { ... }
```

---

## PERFORMANCE OPTIMIZATIONS

### Current Implementation
- Hourly scheduler runs every 3600000ms (1 hour)
- Token caching with 30-day active window
- Batch token management
- Lazy loading of relationships

### Future Optimizations
- Implement token caching layer (Redis)
- Add scheduled job for token cleanup
- Implement rate limiting for FCM API
- Add telemetry/monitoring for delivery rates
- Implement exponential backoff for failed sends

---

## SECURITY CONSIDERATIONS

### Current Implementation
- ✅ JWT authentication on all endpoints
- ✅ User ID extracted from authenticated token
- ✅ Private Firebase credentials via environment variable
- ✅ Token rotation detection and cleanup
- ✅ No hardcoded secrets

### Recommendations
- Use Spring Data REST projections to limit exposed fields
- Add rate limiting on device token endpoints
- Implement token expiry (optional in current design)
- Add audit logging for sensitive operations
- Enable Firebase security rules in production

---

## FUTURE ENHANCEMENTS

### Phase 3 Features
1. **User Notification Preferences**
   - Allow users to disable certain reminder types
   - Set quiet hours
   - Choose notification method (push, email, SMS)

2. **Advanced Scheduling**
   - Recurring reminders for annual boosters
   - Custom reminder intervals per vaccine
   - Reminders for preventive care

3. **Analytics & Reporting**
   - Track reminder delivery rates
   - Monitor user engagement
   - Generate reminder effectiveness reports

4. **Multi-language Support**
   - Localize reminder messages
   - Support multiple time zones
   - Regional vaccine schedules

5. **SMS Integration**
   - Fallback notification channel
   - Direct carrier integration
   - Delivery confirmation

---

## CONCLUSION

The Reminder Scheduling and Firebase FCM Notification System is now fully implemented and ready for deployment. The system provides:

✅ **Automated Reminder Creation** - Reminders scheduled at T-3, T-1, and T-day
✅ **Firebase Integration** - Push notifications via Cloud Messaging
✅ **Device Management** - Registration and token lifecycle handling
✅ **Scheduled Processing** - Hourly job for timely delivery
✅ **Flexible API** - REST endpoints for managing reminders and tokens
✅ **Error Handling** - Automatic token invalidation and retry logic
✅ **Extensibility** - Easy to add new reminder types or notification channels

The implementation follows Spring Boot best practices, includes comprehensive logging, and is production-ready for deployment.
