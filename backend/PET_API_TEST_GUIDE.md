# Pet Management API - Testing Guide

## Prerequisites
- Spring Boot application running on `http://localhost:8080`
- Valid JWT token for authentication
- At least one registered pet owner user

## Test Cases

### 1. Create a New Pet
**Endpoint:** `POST /api/owner/pets`
**Headers:** 
- `Authorization: Bearer {JWT_TOKEN}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Max",
  "species": "Cat",
  "breed": "Persian",
  "dateOfBirth": "2021-03-20",
  "weight": 5.5,
  "sex": "Male",
  "markings": "White with black spots",
  "microchipId": "985120050012346",
  "notes": "Very fluffy and friendly"
}
```

**Expected Response (201 Created):**
```json
{
  "message": "Pet created successfully",
  "data": {
    "id": 2,
    "name": "Max",
    "species": "Cat",
    "breed": "Persian",
    "dateOfBirth": "2021-03-20",
    "weight": 5.5,
    "sex": "Male",
    "markings": "White with black spots",
    "microchipId": "985120050012346",
    "photoUrl": null,
    "notes": "Very fluffy and friendly",
    "createdAt": "2026-05-23T21:30:00",
    "updatedAt": "2026-05-23T21:30:00",
    "upcomingAppointments": [],
    "vaccinationsDue": []
  }
}
```

---

### 2. Get All User's Pets
**Endpoint:** `GET /api/owner/pets`
**Headers:** 
- `Authorization: Bearer {JWT_TOKEN}`

**Expected Response (200 OK):**
```json
{
  "message": "Pets retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Buddy",
      "species": "Dog",
      "breed": "Golden Retriever",
      "weight": 30.5,
      "sex": "Male",
      "upcomingAppointments": [
        {
          "id": 5,
          "appointmentDate": "2026-05-30T14:00:00",
          "reason": "Vaccination",
          "doctorName": "Dr. Smith",
          "status": "SCHEDULED"
        }
      ],
      "vaccinationsDue": []
    },
    {
      "id": 2,
      "name": "Max",
      "species": "Cat",
      "breed": "Persian",
      "weight": 5.5,
      "upcomingAppointments": [],
      "vaccinationsDue": []
    }
  ],
  "count": 2
}
```

---

### 3. Get Specific Pet with Details
**Endpoint:** `GET /api/owner/pets/1`
**Headers:** 
- `Authorization: Bearer {JWT_TOKEN}`

**Expected Response (200 OK):**
```json
{
  "message": "Pet retrieved successfully",
  "data": {
    "id": 1,
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "dateOfBirth": "2020-05-15",
    "weight": 30.5,
    "sex": "Male",
    "markings": "Brown with white chest",
    "microchipId": "985120050012345",
    "photoUrl": "uploads/pet-photos/pet_1_1716530625000.jpg",
    "notes": "Friendly and energetic",
    "createdAt": "2026-05-20T10:00:00",
    "updatedAt": "2026-05-23T15:45:00",
    "upcomingAppointments": [
      {
        "id": 5,
        "appointmentDate": "2026-05-30T14:00:00",
        "reason": "Regular checkup",
        "doctorName": "Dr. Smith",
        "status": "SCHEDULED"
      },
      {
        "id": 6,
        "appointmentDate": "2026-06-15T10:30:00",
        "reason": "Vaccination",
        "doctorName": "Dr. Johnson",
        "status": "SCHEDULED"
      }
    ],
    "vaccinationsDue": [
      {
        "id": 3,
        "vaccineName": "Rabies Booster",
        "nextBoosterDate": "2026-05-25",
        "recordType": "VACCINATION",
        "isDue": true
      },
      {
        "id": 4,
        "vaccineName": "DHPP Booster",
        "nextBoosterDate": "2026-06-10",
        "recordType": "VACCINATION",
        "isDue": false
      }
    ]
  }
}
```

---

### 4. Update Pet Details
**Endpoint:** `PUT /api/owner/pets/1`
**Headers:** 
- `Authorization: Bearer {JWT_TOKEN}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Buddy Updated",
  "species": "Dog",
  "breed": "Golden Retriever",
  "dateOfBirth": "2020-05-15",
  "weight": 31.0,
  "sex": "Male",
  "markings": "Brown with white chest and paws",
  "microchipId": "985120050012345",
  "notes": "Very friendly and energetic"
}
```

**Expected Response (200 OK):**
- Returns updated PetResponse with new values

---

### 5. Upload Pet Photo
**Endpoint:** `POST /api/owner/pets/1/upload-photo`
**Headers:** 
- `Authorization: Bearer {JWT_TOKEN}`
- `Content-Type: multipart/form-data`

**Request Form Data:**
- `file`: [Select image file - JPEG or PNG, max 5MB]

**Expected Response (200 OK):**
```json
{
  "message": "Photo uploaded successfully",
  "data": {
    "id": 1,
    "name": "Buddy",
    "photoUrl": "uploads/pet-photos/pet_1_1716530889234.jpg",
    "upcomingAppointments": [...],
    "vaccinationsDue": [...]
  }
}
```

---

### 6. Delete Pet
**Endpoint:** `DELETE /api/owner/pets/1`
**Headers:** 
- `Authorization: Bearer {JWT_TOKEN}`

**Expected Response (200 OK):**
```json
{
  "message": "Pet deleted successfully"
}
```

---

## Error Test Cases

### Test Unauthorized Access
**Attempt:** Get another user's pet with your token
```bash
GET /api/owner/pets/999  # Pet belonging to different owner
```
**Expected Response (404 Not Found):**
```json
{
  "error": "Pet not found or you don't have access to this pet",
  "errorCode": "PET_NOT_FOUND"
}
```

### Test Invalid Photo Upload
**Attempt:** Upload file > 5MB or wrong format
```
POST /api/owner/pets/1/upload-photo
file: large_file.zip (6MB)
```
**Expected Response (400 Bad Request):**
```json
{
  "error": "File size exceeds maximum allowed size of 5MB"
}
```

### Test Missing Required Fields
**Attempt:** Create pet without name
```json
{
  "species": "Dog"
}
```
**Expected Response (400 Bad Request):**
```json
{
  "error": "Pet name is required"
}
```

### Test Invalid Date
**Attempt:** Set date of birth in future
```json
{
  "name": "Puppy",
  "species": "Dog",
  "dateOfBirth": "2027-12-25"
}
```
**Expected Response (400 Bad Request):**
```json
{
  "error": "Date of birth cannot be in the future"
}
```

---

## cURL Examples

### Create Pet
```bash
curl -X POST http://localhost:8080/api/owner/pets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "weight": 30.5,
    "sex": "Male"
  }'
```

### Get All Pets
```bash
curl -X GET http://localhost:8080/api/owner/pets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Pet by ID
```bash
curl -X GET http://localhost:8080/api/owner/pets/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Update Pet
```bash
curl -X PUT http://localhost:8080/api/owner/pets/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy Updated",
    "species": "Dog",
    "weight": 31.0
  }'
```

### Delete Pet
```bash
curl -X DELETE http://localhost:8080/api/owner/pets/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Upload Photo
```bash
curl -X POST http://localhost:8080/api/owner/pets/1/upload-photo \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/photo.jpg"
```

---

## Notes
- Always include valid JWT token in Authorization header
- Photo directory `uploads/pet-photos/` will be created automatically
- Microchip ID is unique across the system (database constraint)
- All timestamps are in ISO 8601 format
- Vaccination due dates comparison is done with current date (time-agnostic)
