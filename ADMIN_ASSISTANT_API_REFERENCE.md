# Admin and Assistant APIs - Quick Reference Guide

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer {jwt_token}
```

---

## ADMIN ENDPOINTS

### User Management

#### 1. Get All Users
```
GET /admin/users
Query Parameters:
  - role: (optional) PET_OWNER, DOCTOR, ASSISTANT, ADMIN
  - status: (optional) ACTIVE, PENDING_VERIFICATION, INACTIVE, SUSPENDED
  - pageNumber: (optional, default: 0)
  - pageSize: (optional, default: 20)

Example:
GET /admin/users?role=DOCTOR&status=PENDING_VERIFICATION
```

#### 2. Get Pending Verifications
```
GET /admin/users/pending-verification

Response includes all pending doctors and assistants awaiting verification.
```

#### 3. Verify Doctor
```
POST /admin/users/{userId}/verify-doctor
```

#### 4. Verify Assistant
```
POST /admin/users/{userId}/verify-assistant
```

#### 5. Reject User Application
```
POST /admin/users/{userId}/reject
Query Parameters:
  - reason: (optional) Rejection reason message

Example:
POST /admin/users/5/reject?reason=Incomplete%20documentation
```

#### 6. Suspend User
```
POST /admin/users/{userId}/suspend
Query Parameters:
  - reason: (optional) Suspension reason

Example:
POST /admin/users/5/suspend?reason=Policy%20violation
```

#### 7. Enable Suspended User
```
POST /admin/users/{userId}/enable
```

#### 8. Delete User (Soft Delete)
```
DELETE /admin/users/{userId}

Data is retained for 90 days before permanent deletion.
```

#### 9. Add New Doctor
```
POST /admin/users/add-doctor

Request Body:
{
  "username": "dr_john",
  "email": "john@example.com",
  "fullName": "Dr. John Smith",
  "password": "SecurePassword123!",
  "phoneNumber": "1234567890",
  "address": "123 Medical St",
  "licenseId": "LIC123456",
  "specialization": "Veterinary Surgery",
  "yearsOfExperience": 10,
  "clinicName": "Pet Clinic",
  "dateOfBirth": "1980-05-15"
}

Response: 201 Created
```

#### 10. Add New Assistant
```
POST /admin/users/add-assistant

Request Body:
{
  "username": "assist_jane",
  "email": "jane@example.com",
  "fullName": "Jane Doe",
  "password": "SecurePassword123!",
  "phoneNumber": "0987654321",
  "address": "456 Helper Ave",
  "dateOfBirth": "1995-08-20",
  "qualifications": "Veterinary Technician"
}

Response: 201 Created
```

### Appointment Management

#### 11. Get All Appointments
```
GET /admin/appointments
Query Parameters:
  - status: (optional) SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
  - startDate: (optional) yyyy-MM-dd
  - endDate: (optional) yyyy-MM-dd

Example:
GET /admin/appointments?status=SCHEDULED&startDate=2026-05-20&endDate=2026-05-30
```

#### 12. Advanced Appointment Filtering
```
POST /admin/appointments/filter

Request Body:
{
  "status": "SCHEDULED",
  "startDate": "2026-05-20",
  "endDate": "2026-05-30",
  "pageNumber": 0,
  "pageSize": 20,
  "sortBy": "appointmentDate",
  "sortDirection": "ASC"
}
```

### Schedule Configuration

#### 13. Configure Clinic Time Slots
```
POST /admin/schedule/configure

Query Parameters:
  - dayOfWeek: (required) MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  - openingTime: (required) HH:mm (24-hour format)
  - closingTime: (required) HH:mm (24-hour format)

Example:
POST /admin/schedule/configure?dayOfWeek=MONDAY&openingTime=09:00&closingTime=17:00
```

---

## ASSISTANT ENDPOINTS

### Schedule Management

#### 1. Get Clinic Schedule
```
GET /assistant/schedule
Query Parameters:
  - date: (optional) yyyy-MM-dd, defaults to today

Example:
GET /assistant/schedule?date=2026-05-25
```

#### 2. Get Today's Appointments
```
GET /assistant/appointments/today
```

#### 3. Get Today's Schedule with Check-in Status
```
GET /assistant/schedule/today-with-status

Response includes all appointments with isCheckedIn and isCheckedOut flags.
```

### Check-in/Check-out Management

#### 4. Mark Patient Check-in
```
POST /assistant/appointments/{appointmentId}/check-in

Request Body (optional):
{
  "notes": "Patient arrived 5 minutes early"
}

Response: Includes checkInTime and status
```

#### 5. Mark Patient Check-out
```
POST /assistant/appointments/{appointmentId}/check-out

Request Body (optional):
{
  "notes": "Appointment completed successfully"
}

Response: Includes checkInTime, checkOutTime, durationMinutes, and appointment marked as COMPLETED
```

#### 6. Get Check-in Information
```
GET /assistant/appointments/{appointmentId}/check-in-info

Response includes:
  - appointmentId
  - petName, ownerName, ownerPhone
  - appointmentTime, doctorName
  - isCheckedIn, isCheckedOut
  - checkInTime, checkOutTime (if applicable)
  - checkInNotes, checkOutNotes (if applicable)
```

### Appointment Updates

#### 7. Update Appointment Time Slot
```
PUT /assistant/appointments/{appointmentId}/time-slot
Query Parameters:
  - newDateTime: (required) yyyy-MM-dd'T'HH:mm:ss

Example:
PUT /assistant/appointments/15/time-slot?newDateTime=2026-05-25T14:30:00

Note: System checks for conflicts with other doctor appointments
```

---

## Response Format

### Success Response (200, 201)
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response-specific data
  }
}
```

### Error Response (4xx, 5xx)
```json
{
  "success": false,
  "error": "Error description",
  "errorCode": "ERROR_CODE"
}
```

---

## Common Status Codes

- **200 OK** - Successful request
- **201 Created** - Resource created successfully
- **400 Bad Request** - Invalid request parameters
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource conflict (e.g., duplicate username)
- **500 Internal Server Error** - Server error

---

## Sample Workflows

### Workflow 1: Verify and Activate Doctor
```
1. GET /admin/users/pending-verification
   → Find doctor with id=5

2. POST /admin/users/5/verify-doctor
   → Doctor account set to ACTIVE

3. Doctor receives verification email
```

### Workflow 2: Daily Clinic Operations
```
1. Morning: GET /assistant/schedule/today-with-status
   → View all scheduled appointments

2. Patient arrives: POST /assistant/appointments/{id}/check-in
   → Record check-in time and notes

3. After service: POST /assistant/appointments/{id}/check-out
   → Record check-out time, appointment marked COMPLETED

4. Reschedule if needed: PUT /assistant/appointments/{id}/time-slot?newDateTime=...
   → Update appointment time (checks for conflicts)
```

### Workflow 3: User Suspension
```
1. POST /admin/users/{userId}/suspend?reason=Policy%20violation
   → User account set to SUSPENDED

2. User receives suspension email

3. Later: POST /admin/users/{userId}/enable
   → User account re-activated
```

---

## Error Codes

| Code | Description |
|------|-------------|
| USER_NOT_FOUND | User doesn't exist |
| INVALID_ROLE | User role doesn't match operation |
| APPOINTMENT_NOT_FOUND | Appointment doesn't exist |
| INVALID_STATUS | Invalid status value provided |
| INVALID_DATE | Date format invalid |
| SLOT_NOT_AVAILABLE | Time slot has conflict with existing appointment |
| NOT_CHECKED_IN | Patient hasn't checked in yet |
| USERNAME_EXISTS | Username already taken |
| EMAIL_EXISTS | Email already registered |
| INVALID_PASSWORD | Password doesn't meet requirements |
| INVALID_TIME | Time values are invalid |

---

## Soft Delete Details

Deleted users have:
- accountStatus set to INACTIVE
- softDeleteAt timestamp recorded
- Data retained for 90 days
- Can be permanently deleted via database cleanup function

To query only active users (not soft-deleted):
```
Database query automatically filters where softDeleteAt IS NULL
```

---

## Notes

- All timestamps are in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss)
- Phone numbers must contain only digits (10-20 characters)
- Passwords must be at least 8 characters
- Authentication required for all endpoints
- Cross-origin requests from localhost:3000 and localhost:8081 are supported
