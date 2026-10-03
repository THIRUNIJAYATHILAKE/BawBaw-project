# Appointment Booking API - Quick Reference Guide

## Authentication
All endpoints requiring authentication use JWT tokens in the Authorization header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Owner Endpoints (Pet Owner Role)

### 1. Get Available Doctors
```
GET /api/owner/doctors
Authentication: Optional
Response: List<AvailableDoctorDto>
```
Returns doctors with available appointment slots.

**Response Example:**
```json
[
  {
    "doctorId": 1,
    "doctorName": "Dr. Smith",
    "specialization": "Vaccination Specialist",
    "yearsOfExperience": 10,
    "clinicName": "Happy Paws Clinic",
    "availableSlots": 24
  }
]
```

### 2. Get Available Time Slots
```
GET /api/owner/doctors/{doctorId}/available-slots?date=2026-05-25
Authentication: Optional
Response: List<AvailableSlotDto>
```
Returns all available 30-minute slots for a doctor on a specific date.

**Parameters:**
- `doctorId`: ID of the doctor (Path parameter)
- `date`: Date in YYYY-MM-DD format (Query parameter)

**Response Example:**
```json
[
  {
    "slotTime": "2026-05-25T09:00:00",
    "durationMinutes": 30,
    "isAvailable": true
  },
  {
    "slotTime": "2026-05-25T09:30:00",
    "durationMinutes": 30,
    "isAvailable": true
  }
]
```

### 3. Book Appointment
```
POST /api/owner/appointments
Authentication: Required (Owner)
Content-Type: application/json
Response: AppointmentResponse
```
Books a new appointment with double-booking prevention.

**Request Body:**
```json
{
  "petId": 5,
  "doctorId": 1,
  "appointmentDate": "2026-05-25T09:00:00",
  "purpose": "VACCINATION",
  "reason": "Annual rabies vaccination"
}
```

**Purpose Options:**
- `VACCINATION` - Routine or required vaccination
- `DIAGNOSIS_CHECKUP` - General health checkup
- `DEWORMING` - Deworming treatment
- `OTHER` - Other medical purposes

**Response Example:**
```json
{
  "message": "Appointment booked successfully",
  "appointment": {
    "id": 101,
    "petId": 5,
    "petName": "Buddy",
    "doctorId": 1,
    "doctorName": "Dr. Smith",
    "ownerId": 10,
    "ownerName": "John Doe",
    "appointmentDate": "2026-05-25T09:00:00",
    "durationMinutes": 30,
    "status": "SCHEDULED",
    "purpose": "VACCINATION",
    "reason": "Annual rabies vaccination",
    "notes": null,
    "cancelledAt": null,
    "cancelledReason": null,
    "createdAt": "2026-05-23T21:30:00",
    "updatedAt": "2026-05-23T21:30:00"
  }
}
```

### 4. Get Owner's Appointments
```
GET /api/owner/appointments
Authentication: Required (Owner)
Response: List<AppointmentResponse>
```
Returns all non-cancelled appointments for the authenticated owner.

### 5. Get Appointment Details
```
GET /api/owner/appointments/{appointmentId}
Authentication: Required (Owner or Doctor)
Response: AppointmentResponse
```
Returns detailed information for a specific appointment. Owner can only view their own, doctor can only view theirs.

### 6. Cancel Appointment
```
POST /api/owner/appointments/{appointmentId}/cancel
Authentication: Required (Owner or Doctor)
Content-Type: application/json
Response: AppointmentResponse
```
Cancels an appointment. Must be at least 2 hours before appointment time.

**Request Body:**
```json
{
  "cancelReason": "Rescheduling to another date"
}
```

**Error Response (Cancellation Window Expired):**
```json
{
  "error": "Cannot cancel appointment within 2 hours of scheduled time"
}
```

---

## Doctor Endpoints (Doctor Role)

### 1. Get Doctor's Appointments
```
GET /api/doctor/appointments
Authentication: Required (Doctor)
Response: List<AppointmentResponse>
```
Returns all appointments for the authenticated doctor.

### 2. Get Doctor's Appointments by Date
```
GET /api/doctor/appointments/by-date?date=2026-05-25
Authentication: Required (Doctor)
Response: List<AppointmentResponse>
```
Returns appointments for the doctor on a specific date.

**Parameters:**
- `date`: Date in YYYY-MM-DD format

### 3. Mark Appointment as Completed/No-Show
```
POST /api/doctor/appointments/{appointmentId}/complete
Authentication: Required (Doctor)
Content-Type: application/json
Response: AppointmentResponse
```
Marks appointment as completed or no-show.

**Request Body:**
```json
{
  "status": "COMPLETED"
}
```

**Status Options:**
- `COMPLETED` - Appointment was completed
- `NO_SHOW` - Pet owner did not show up

---

## Error Handling

### Double-Booking Conflict
```
Status: 409 Conflict
{
  "error": "Doctor already has an appointment at this time"
}
```

### Cancellation Window Expired
```
Status: 409 Conflict
{
  "error": "Cannot cancel appointment within 2 hours of scheduled time"
}
```

### Doctor Not Available
```
Status: 409 Conflict
{
  "error": "Doctor is not available at the requested time"
}
```

### Pet Not Found
```
Status: 404 Not Found
{
  "error": "Pet not found"
}
```

### Authorization Failed
```
Status: 401 Unauthorized
{
  "error": "Not authorized to cancel this appointment"
}
```

---

## Appointment Statuses

- **SCHEDULED** - Appointment is booked and upcoming
- **COMPLETED** - Appointment was completed by doctor
- **CANCELLED** - Appointment was cancelled by owner or doctor
- **NO_SHOW** - Pet owner did not show up for appointment

---

## Business Rules

1. **Double-Booking Prevention**: Doctor cannot have overlapping appointments
2. **Cancellation Window**: Can only cancel 2+ hours before appointment time
3. **Pet Ownership**: Pet must belong to the owner booking the appointment
4. **Doctor Role**: Only users with DOCTOR role can be assigned appointments
5. **Schedule Validation**: Appointment must fall within doctor's available schedule
6. **30-Minute Slots**: All appointments are 30 minutes long
7. **Slot Generation**: Slots are generated at 30-minute intervals

---

## Common Workflows

### Booking an Appointment
1. GET `/api/owner/doctors` - Browse available doctors
2. GET `/api/owner/doctors/{doctorId}/available-slots?date=...` - View available times
3. POST `/api/owner/appointments` - Book appointment
4. Response includes confirmation details

### Cancelling an Appointment
1. GET `/api/owner/appointments` - Find appointment to cancel
2. POST `/api/owner/appointments/{appointmentId}/cancel` - Cancel (if within 2-hour window)
3. Response includes cancellation confirmation

### Doctor Completing Appointment
1. GET `/api/doctor/appointments` - View today's appointments
2. POST `/api/doctor/appointments/{appointmentId}/complete` - Mark as completed
3. Response includes updated appointment status

---

## Implementation Notes

- **Timezone**: All times are handled as LocalDateTime (server timezone)
- **Slot Duration**: Fixed at 30 minutes per appointment
- **Slot Intervals**: Generated at 30-minute intervals
- **Notifications**: Booking confirmation trigger available (FCM integration pending)
- **Authentication**: Uses JWT tokens with SecurityContextHolder
- **Transaction Management**: All operations are transactional (@Transactional)

---

**Last Updated**: May 23, 2026
**API Version**: 1.0
**Status**: Production Ready
