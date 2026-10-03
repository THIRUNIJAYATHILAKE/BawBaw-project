# PetVax Care - Owner Profile and Doctor List APIs - Quick Reference

## Owner Profile API Endpoints

### 1. Get Current User's Profile
**Endpoint:** `GET /api/owner/profile`
**Authentication:** Required (JWT)
**Response:**
```json
{
  "message": "Profile retrieved successfully",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St, City, State",
    "dateOfBirth": "1990-01-15",
    "profilePhotoUrl": null,
    "petCount": 2,
    "accountStatus": "ACTIVE",
    "emailVerified": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-20T14:22:00"
  }
}
```

### 2. Update User's Profile
**Endpoint:** `PUT /api/owner/profile`
**Authentication:** Required (JWT)
**Request Body:**
```json
{
  "fullName": "John Doe Updated",
  "email": "newemail@example.com",
  "phoneNumber": "+1234567890",
  "address": "456 Oak Ave, New City, State"
}
```
**Response:** Same as Get Profile

### 3. Upload Profile Photo
**Endpoint:** `POST /api/owner/profile/photo`
**Authentication:** Required (JWT)
**Content-Type:** multipart/form-data
**Request Parameters:**
- `photo` (file) - JPEG or PNG file, max 5MB
**Response:**
```json
{
  "message": "Profile photo uploaded successfully",
  "photoUrl": "uploads/profile-photos/profile_1_1705766400000.jpg"
}
```

### 4. Delete Account (Soft Delete)
**Endpoint:** `DELETE /api/owner/profile`
**Authentication:** Required (JWT)
**Response:**
```json
{
  "message": "Account deleted successfully. It will be permanently removed in 90 days."
}
```

### 5. Get All Owner's Pets
**Endpoint:** `GET /api/owner/profile/pets`
**Authentication:** Required (JWT)
**Response:**
```json
{
  "message": "Pets retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Max",
      "species": "Dog",
      "breed": "Golden Retriever",
      "dateOfBirth": "2020-05-10",
      "weight": 28.5,
      "sex": "Male",
      "markings": "None",
      "microchipId": "CHIP123456",
      "photoUrl": "uploads/pet-photos/pet_1_1705766400000.jpg",
      "notes": "Very friendly",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-20T14:22:00",
      "upcomingAppointments": [],
      "vaccinationsDue": []
    }
  ],
  "count": 1
}
```

## Doctor List API Endpoints

### 1. List All Available Doctors
**Endpoint:** `GET /api/owner/doctors`
**Authentication:** Required (JWT)
**Query Parameters:**
- `specialty` (optional) - Filter by specialty (e.g., "Small Animals")
**Response:**
```json
{
  "message": "Doctors retrieved successfully",
  "data": [
    {
      "doctorId": 10,
      "doctorName": "Dr. Sarah Johnson",
      "specialization": "Small Animals",
      "yearsOfExperience": 8,
      "clinicName": "Happy Paws Clinic",
      "licenseId": "LIC-SA-12345",
      "availableSlots": 7,
      "isVerified": true,
      "verifiedAt": "2023-06-15T10:00:00",
      "schedule": [
        {
          "dayOfWeek": 1,
          "dayName": "MONDAY",
          "startTime": "09:00:00",
          "endTime": "17:00:00",
          "isAvailable": true,
          "appointments": []
        }
      ]
    }
  ],
  "count": 5
}
```

### 2. Get Doctor Details
**Endpoint:** `GET /api/owner/doctors/{doctorId}`
**Authentication:** Required (JWT)
**Path Parameters:**
- `doctorId` - Doctor's user ID
**Response:** Same as doctor object from List endpoint

### 3. Get Doctor's Weekly Availability
**Endpoint:** `GET /api/owner/doctors/{doctorId}/availability`
**Authentication:** Required (JWT)
**Response:**
```json
{
  "message": "Doctor availability retrieved successfully",
  "doctorId": 10,
  "availableSlotsThisWeek": 35,
  "isAvailable": true
}
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "Validation error message"
}
```

### 401 Unauthorized
```json
{
  "error": "User not authenticated"
}
```

### 404 Not Found
```json
{
  "error": "Doctor not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Failed to perform operation"
}
```

## Profile Update Validation Rules
- **fullName**: 2-100 characters required
- **email**: Valid email format, must be unique
- **phoneNumber**: Max 20 characters
- **address**: Max 500 characters

## File Upload Validation
- **Formats**: JPEG, JPG, PNG only
- **Size**: Max 5MB
- **Stored at**: `uploads/profile-photos/`

## Doctor Availability Calculation
- Default: 10 appointment slots per day per doctor
- Calculated for current week (Monday-Sunday)
- Excludes cancelled appointments
- Real-time availability check

## Account Deletion Process
1. User calls DELETE /api/owner/profile
2. Account status changed to INACTIVE
3. soft_delete_at timestamp set to current time
4. Hard deletion scheduled for 90 days later
5. Automatic cleanup via scheduled task

## Cross-Origin Settings
All endpoints configured for:
- http://localhost:3000 (Frontend dev)
- http://localhost:8081 (Alternative frontend)
