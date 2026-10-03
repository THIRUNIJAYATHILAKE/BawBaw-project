# Doctor Features API Quick Reference

## Authentication
All endpoints require doctor authentication. Include JWT token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Search API Endpoints

### Search for Pets
**Endpoint:** `GET /api/doctor/search?query=<search_term>`

**Parameters:**
- `query` (required): Pet ID, pet name, owner name, or microchip ID
- `type` (optional): Search type (not used in current implementation)

**Response:**
```json
{
  "message": "Search completed successfully",
  "count": 1,
  "results": [
    {
      "id": 1,
      "name": "Buddy",
      "species": "Dog",
      "breed": "Golden Retriever",
      "weight": 30.5,
      "microchipId": "ABC123456",
      "photoUrl": "uploads/pet-photos/pet_1.jpg"
    }
  ]
}
```

### Get Pet Medical View
**Endpoint:** `GET /api/doctor/pets/{petId}/medical-view`

**Response Includes:**
- Complete pet information
- Owner contact details
- All medical records with doctor names
- Appointment history

```json
{
  "message": "Medical view retrieved successfully",
  "data": {
    "id": 1,
    "name": "Buddy",
    "species": "Dog",
    "ownerId": 5,
    "ownerName": "John Doe",
    "ownerEmail": "john@example.com",
    "ownerPhoneNumber": "+1-555-0100",
    "medicalRecords": [
      {
        "id": 10,
        "recordType": "VACCINATION",
        "doctorName": "Dr. Smith",
        "recordedDate": "2024-01-15",
        "description": "Rabies vaccination",
        "nextDueDate": "2025-01-15"
      }
    ],
    "appointments": []
  }
}
```

### Get Owner Details
**Endpoint:** `GET /api/doctor/pets/{petId}/owner-details`

**Response:**
```json
{
  "message": "Owner details retrieved successfully",
  "data": {
    "id": 5,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+1-555-0100",
    "address": "123 Main St, City, State",
    "dateOfBirth": "1990-05-15"
  }
}
```

### Record Diagnosis
**Endpoint:** `POST /api/doctor/pets/{petId}/diagnosis`

**Request Body:**
```json
{
  "recordType": "DIAGNOSIS",
  "description": "Ear infection",
  "recordedDate": "2024-01-20",
  "nextDueDate": "2024-02-03",
  "notes": "Follow-up in 2 weeks. Patient responsive to medication."
}
```

**Record Types:**
- VACCINATION
- DIAGNOSIS
- DEWORMING
- CHECKUP
- SURGERY
- PRESCRIPTION

**Response:**
```json
{
  "message": "Diagnosis recorded successfully",
  "recordId": 15,
  "timestamp": "2024-01-20T14:30:00"
}
```

### Edit Medical Record
**Endpoint:** `PUT /api/doctor/medical-records/{recordId}`

**Request Body:** Same as Record Diagnosis

**Response:**
```json
{
  "message": "Medical record updated successfully",
  "recordId": 15,
  "timestamp": "2024-01-20T15:45:00"
}
```

---

## Schedule API Endpoints

### Get Weekly Schedule
**Endpoint:** `GET /api/doctor/schedule`

**Response:**
```json
{
  "message": "Weekly schedule retrieved successfully",
  "data": {
    "doctorId": 2,
    "doctorName": "Dr. Smith",
    "schedule": [
      {
        "dayOfWeek": 1,
        "dayName": "MONDAY",
        "startTime": "09:00:00",
        "endTime": "17:00:00",
        "isAvailable": true,
        "appointments": [
          {
            "appointmentId": 5,
            "petId": 1,
            "petName": "Buddy",
            "petSpecies": "Dog",
            "ownerId": 3,
            "ownerName": "Alice Brown",
            "appointmentDate": "2024-01-22T10:00:00",
            "status": "SCHEDULED"
          }
        ]
      }
    ]
  }
}
```

### Get Schedule for Specific Date
**Endpoint:** `GET /api/doctor/schedule/by-date?date=YYYY-MM-DD`

**Parameters:**
- `date` (optional): Date in YYYY-MM-DD format. Defaults to today.

**Response:**
```json
{
  "message": "Schedule for date retrieved successfully",
  "date": "2024-01-22",
  "appointmentCount": 3,
  "appointments": [
    {
      "appointmentId": 5,
      "petId": 1,
      "petName": "Buddy",
      "petSpecies": "Dog",
      "ownerId": 3,
      "ownerName": "Alice Brown",
      "appointmentDate": "2024-01-22T10:00:00",
      "durationMinutes": 30,
      "status": "SCHEDULED",
      "appointmentPurpose": "CHECKUP",
      "reason": "Routine checkup"
    }
  ]
}
```

### Get Appointment Details
**Endpoint:** `GET /api/doctor/appointments/{appointmentId}`

**Response:** Returns single appointment object with full details

### Mark Appointment as Completed
**Endpoint:** `POST /api/doctor/appointments/{appointmentId}/complete`

**Response:**
```json
{
  "message": "Appointment marked as completed",
  "appointmentId": 5,
  "status": "COMPLETED",
  "timestamp": "2024-01-22T10:30:00"
}
```

### Mark Appointment as No-Show
**Endpoint:** `POST /api/doctor/appointments/{appointmentId}/no-show`

**Response:**
```json
{
  "message": "Appointment marked as no-show",
  "appointmentId": 5,
  "status": "NO_SHOW",
  "timestamp": "2024-01-22T10:30:00"
}
```

---

## Error Responses

### Unauthorized Access
```json
{
  "error": "Only doctors can access this resource"
}
```
Status Code: 403

### Pet Not Found
```json
{
  "error": "Pet not found with ID: 999"
}
```
Status Code: 400

### Invalid Request
```json
{
  "error": "Search query is required"
}
```
Status Code: 400

### Server Error
```json
{
  "error": "Error searching pets: Connection timeout"
}
```
Status Code: 500

---

## Audit Logging

All operations that modify data are automatically logged:
- **Recording diagnosis** - Logs new record creation
- **Editing medical records** - Logs before/after values
- **Updating appointment status** - Logs status change
- **Marking no-shows** - Logs status change

Audit trail includes:
- User (doctor) who made the change
- Timestamp of the change
- Old and new values
- Entity type and ID

Access audit logs through database queries on `audit_logs` table.

---

## Search Examples

### By Pet ID
```
GET /api/doctor/search?query=123
```

### By Pet Name
```
GET /api/doctor/search?query=Buddy
```

### By Owner Name
```
GET /api/doctor/search?query=John%20Doe
```

### By Microchip ID
```
GET /api/doctor/search?query=ABC123456
```

---

## Date Format

All dates use ISO 8601 format:
- Date only: `YYYY-MM-DD` (2024-01-20)
- DateTime: `YYYY-MM-DDTHH:mm:ss` (2024-01-20T14:30:00)

---

## Rate Limiting & Performance Notes

- Search queries support any text input but return top results
- Schedule queries are optimized for weekly/daily views
- Medical record queries load full history (pagination recommended for large records)
- Audit logs are automatically pruned (retention policy: 1 year)

---

## Integration Examples

### Search for pet and view medical history
1. `GET /api/doctor/search?query=Buddy`
2. From results, note the pet ID (e.g., 1)
3. `GET /api/doctor/pets/1/medical-view`

### Complete an appointment
1. `GET /api/doctor/schedule/by-date?date=2024-01-22`
2. Find appointment ID from results
3. `POST /api/doctor/appointments/{appointmentId}/complete`

### Record and document a visit
1. `GET /api/doctor/pets/{petId}/medical-view` (get current records)
2. `POST /api/doctor/pets/{petId}/diagnosis` (record new findings)
3. `GET /api/doctor/appointments/{appointmentId}/complete` (mark complete)

