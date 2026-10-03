# PetVax Care - Reminder & FCM API Quick Reference

## Reminder Endpoints

### 1. Get All Reminders
```
GET /api/owner/reminders
Authorization: Bearer <jwt_token>

Optional Query Parameters:
  - type: VACCINATION | DEWORMING | APPOINTMENT

Response (200 OK):
{
  "message": "Reminders retrieved successfully",
  "total": 5,
  "reminders": [
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
  ]
}
```

### 2. Get Reminders by Type
```
GET /api/owner/reminders?type=VACCINATION
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "message": "Reminders retrieved successfully",
  "total": 2,
  "reminders": [
    { ... vaccination reminders only ... }
  ]
}
```

### 3. Get Reminder History (Sent Reminders)
```
GET /api/owner/reminders/history
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "message": "Sent reminders retrieved successfully",
  "total": 10,
  "reminders": [
    {
      "id": 1,
      "reminderType": "VACCINATION",
      "title": "Vaccination Reminder",
      "message": "...",
      "scheduledDate": "2026-05-20T00:00:00",
      "sent": true,
      "sentAt": "2026-05-20T10:15:32",
      ...
    }
  ]
}
```

### 4. Acknowledge Reminder (Mark as Read)
```
POST /api/owner/reminders/{reminderId}/acknowledge
Authorization: Bearer <jwt_token>
Content-Type: application/json

Path Parameters:
  - reminderId: Long (e.g., 42)

Response (200 OK):
{
  "message": "Reminder marked as read",
  "reminder": {
    "id": 42,
    "reminderType": "VACCINATION",
    "title": "Vaccination Reminder",
    "message": "Bella's Rabies vaccination is due in 3 days",
    "scheduledDate": "2026-05-27T00:00:00",
    "sent": true,
    "sentAt": "2026-05-24T14:32:15",
    ...
  }
}
```

---

## Device Token Endpoints

### 1. Register Device Token
```
POST /api/owner/device-tokens/register
Authorization: Bearer <jwt_token>
Content-Type: application/json

Request Body:
{
  "fcmToken": "eWz8H7dK...[very long FCM token from Firebase]...",
  "deviceName": "iPhone 12 Pro",
  "deviceType": "iOS"
}

Response (200 OK):
{
  "message": "Device token registered successfully",
  "tokenId": 1,
  "deviceName": "iPhone 12 Pro",
  "deviceType": "iOS"
}

Response (500 Internal Server Error):
{
  "error": "User not found"
}
```

### 2. Get All Device Tokens
```
GET /api/owner/device-tokens
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "message": "Device tokens retrieved successfully",
  "total": 2,
  "tokens": [
    {
      "id": 1,
      "user": { "id": 10, "username": "john_doe" },
      "fcmToken": "eWz8H7dK...",
      "deviceName": "iPhone 12 Pro",
      "deviceType": "iOS",
      "createdAt": "2026-05-24T10:15:00",
      "updatedAt": "2026-05-24T10:15:00",
      "lastUsed": "2026-05-24T14:30:00"
    },
    {
      "id": 2,
      "user": { "id": 10, "username": "john_doe" },
      "fcmToken": "aB9cD3fG...",
      "deviceName": "Samsung Galaxy A12",
      "deviceType": "Android",
      "createdAt": "2026-05-23T08:00:00",
      "updatedAt": "2026-05-24T11:00:00",
      "lastUsed": "2026-05-24T11:15:00"
    }
  ]
}
```

### 3. Delete Device Token
```
DELETE /api/owner/device-tokens/{tokenId}
Authorization: Bearer <jwt_token>

Path Parameters:
  - tokenId: Long (e.g., 1)

Response (200 OK):
{
  "message": "Device token removed successfully"
}
```

---

## Error Responses

### 401 Unauthorized
```
{
  "error": "Authentication token is missing or invalid",
  "timestamp": "2026-05-24T14:32:15"
}
```

### 404 Not Found
```
{
  "error": "User not found"
}
```

### 500 Internal Server Error
```
{
  "error": "Error sending push notification to user: 10"
}
```

---

## Integration Examples

### JavaScript/TypeScript (Frontend)

#### Register Device Token on Login
```typescript
async function registerDeviceToken(token: string) {
  const fcmToken = await getFirebaseFCMToken();
  
  const response = await fetch('/api/owner/device-tokens/register', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      fcmToken: fcmToken,
      deviceName: `${navigator.userAgent}`,
      deviceType: getDeviceType() // 'iOS' | 'Android' | 'Web'
    })
  });
  
  return response.json();
}
```

#### Fetch Reminders on App Launch
```typescript
async function fetchReminders(token: string, type?: string) {
  let url = '/api/owner/reminders';
  if (type) {
    url += `?type=${type}`;
  }
  
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const data = await response.json();
  return data.reminders;
}
```

#### Acknowledge Reminder
```typescript
async function acknowledgeReminder(token: string, reminderId: number) {
  const response = await fetch(`/api/owner/reminders/${reminderId}/acknowledge`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  return response.json();
}
```

#### Handle FCM Notifications
```typescript
// In your Firebase service worker
messaging.onMessage((payload) => {
  const notification = payload.notification;
  const data = payload.data;
  
  // Show in-app notification/popup
  showNotificationPopup({
    title: notification.title,
    body: notification.body,
    type: data.type,
    petName: data.petName,
    reminderId: data.reminderId
  });
});
```

### cURL Examples

#### Register Device Token
```bash
curl -X POST http://localhost:8080/api/owner/device-tokens/register \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "eWz8H7dK...",
    "deviceName": "iPhone 12",
    "deviceType": "iOS"
  }'
```

#### Get All Reminders
```bash
curl -X GET http://localhost:8080/api/owner/reminders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

#### Get Vaccination Reminders Only
```bash
curl -X GET "http://localhost:8080/api/owner/reminders?type=VACCINATION" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

#### Acknowledge Reminder
```bash
curl -X POST http://localhost:8080/api/owner/reminders/42/acknowledge \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json"
```

---

## Reminder Scheduling Logic

### Vaccination Reminders
When a vaccination is recorded with `nextBoosterDate = 2026-05-30`:
- **Reminder 1 (T-3 days)**: 2026-05-27 00:00 - "Rabies vaccination is due in 3 days"
- **Reminder 2 (T-1 day)**: 2026-05-29 00:00 - "Rabies vaccination is due in 1 day"
- **Reminder 3 (T-day)**: 2026-05-30 00:00 - "Today is Rabies vaccination day"

### Deworming Reminders
When a deworming is recorded with `nextDoseDate = 2026-06-15`:
- **Reminder 1 (T-3 days)**: 2026-06-12 00:00 - "Deworming is due in 3 days"
- **Reminder 2 (T-1 day)**: 2026-06-14 00:00 - "Deworming is due in 1 day"

### Appointment Reminders
When appointment is booked for `2026-05-25 14:30`:
- **Reminder 1 (T-1 day)**: 2026-05-24 14:30 - "Appointment is in 1 day"
- **Reminder 2 (T-day)**: 2026-05-25 14:30 - "Your appointment is today"

---

## Firebase Configuration

### Required Environment Variable
```bash
export FIREBASE_CREDENTIALS_PATH="/path/to/firebase-service-account.json"
```

### Service Account JSON Structure
```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "key-id",
  "private_key": "-----BEGIN RSA PRIVATE KEY-----\n...\n-----END RSA PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@your-project-id.iam.gserviceaccount.com",
  "client_id": "client-id",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "..."
}
```

---

## Scheduler Behavior

The `ReminderScheduler` runs every hour (3600000 ms):

1. **00:05** - Job starts, finds all unsent reminders with `scheduledDate <= now`
2. **00:05-00:15** - Sends FCM notifications to all active device tokens
3. **00:15-00:20** - Marks reminders as sent, updates `sentAt` timestamp
4. **00:20** - Job completes, logs summary (e.g., "Sent 5 reminders")
5. **01:05** - Next run...

To test manually (development):
```java
// In a controller or test
@Autowired
private ReminderScheduler reminderScheduler;

reminderScheduler.sendDueReminders(); // Manually trigger
```

---

## Common Issues & Solutions

### 1. Firebase Credentials Not Found
**Error**: `IOException: File not found: /path/to/firebase-service-account.json`

**Solution**:
```bash
export FIREBASE_CREDENTIALS_PATH="/absolute/path/to/firebase-service-account.json"
# Verify file exists
ls -la $FIREBASE_CREDENTIALS_PATH
```

### 2. Invalid FCM Token
**Error**: `FirebaseMessagingException: Invalid registration token provided`

**Solution**:
- Token is automatically invalidated and removed
- User must re-register device token
- Check that token format is correct (long alphanumeric string)

### 3. No Reminders Sent
**Possible Causes**:
- Check scheduler is running (logs should show "Starting reminder scheduler job")
- Verify reminders exist with `GET /api/owner/reminders`
- Check `sent` field is `false`
- Verify device tokens are registered

**Debug**:
```bash
# Check logs
tail -f app.log | grep "ReminderScheduler"
```

### 4. Device Token Not Saved
**Solution**:
- Ensure FCM token is valid (comes from Firebase client SDK)
- Check user authentication is working
- Verify database is accessible

---

## Performance Metrics

- **Scheduler**: Runs hourly (configurable)
- **Token Lookup**: O(1) by FCM token, O(n) for user's tokens
- **Notification Sending**: ~500ms per device (includes FCM round trip)
- **Batch Processing**: 100+ reminders per hour supported

---

## Security Notes

⚠️ **Important**:
- Keep Firebase private key confidential
- Never commit service account JSON to repository
- Use environment variables for deployment
- Rotate credentials annually
- Enable Firebase security rules in production
