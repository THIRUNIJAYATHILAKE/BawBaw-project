# Medical Records API - Quick Reference Guide

## 📋 15 API Endpoints at a Glance

### Medical Records Management
| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/owner/pets/{petId}/medical-records` | GET | Owner | View medical history (read-only) |
| `/api/doctor/pets/{petId}/medical-records` | GET | Doctor | View full history |
| `/api/owner/pets/{petId}/medical-records/{type}` | GET | Owner | Filter by type |
| `/api/doctor/pets/{petId}/medical-records` | POST | Doctor | Create record |
| `/api/doctor/pets/{petId}/medical-records/{recordId}` | PUT | Doctor | Edit record |

### Vaccination Management
| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/owner/pets/{petId}/vaccinations` | GET | Owner | List vaccinations |
| `/api/owner/pets/{petId}/vaccination-schedule` | GET | Owner | Get schedule |
| `/api/owner/pets/{petId}/vaccinations/upcoming` | GET | Owner | 30-day window |
| `/api/doctor/pets/{petId}/vaccinations` | POST | Doctor | Record vaccine |
| `/api/owner/vaccinations-due` | GET | Owner | Due across pets |

### Deworming Management
| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/owner/pets/{petId}/deworming` | GET | Owner | History |
| `/api/owner/pets/{petId}/deworming/upcoming` | GET | Owner | 30-day window |
| `/api/doctor/pets/{petId}/deworming` | POST | Doctor | Record dose |
| `/api/owner/deworming-due` | GET | Owner | Due across pets |

---

## 🔧 Quick Code Examples

### Record a Vaccination (as Doctor)
```bash
curl -X POST http://localhost:8080/api/doctor/pets/123/vaccinations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "vaccineName": "DHPP",
    "recordedDate": "2024-05-23",
    "batchNumber": "BATCH123",
    "notes": "Puppy first dose"
  }'
```

**Response:**
```json
{
  "message": "Vaccination recorded successfully",
  "data": {
    "id": 1,
    "vaccineName": "DHPP",
    "nextBoosterDate": "2025-05-23",
    "administeredBy": "Dr. Smith"
  }
}
```

### Record Deworming (as Doctor)
```bash
curl -X POST http://localhost:8080/api/doctor/pets/123/deworming \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "dewormerName": "Panacur",
    "recordedDate": "2024-05-23",
    "doseMg": 100.50,
    "notes": "8-week-old puppy"
  }'
```

**Response:**
```json
{
  "message": "Deworming recorded successfully",
  "data": {
    "id": 1,
    "dewormerName": "Panacur",
    "nextDoseDate": "2024-06-06",
    "administeredBy": "Dr. Smith"
  }
}
```

### Get Medical History (as Owner)
```bash
curl http://localhost:8080/api/owner/pets/123/medical-records \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "message": "Medical history retrieved successfully",
  "data": [
    {
      "id": 2,
      "recordType": "DEWORMING",
      "recordedDate": "2024-05-23",
      "nextDueDate": "2024-06-06",
      "doctorName": "Dr. Smith"
    },
    {
      "id": 1,
      "recordType": "VACCINATION",
      "recordedDate": "2024-05-20",
      "nextDueDate": "2025-05-20",
      "doctorName": "Dr. Smith"
    }
  ],
  "count": 2
}
```

### Get Vaccination Schedule
```bash
curl http://localhost:8080/api/owner/pets/123/vaccination-schedule \
  -H "Authorization: Bearer <token>"
```

**Response (Puppy):**
```json
{
  "message": "Vaccination schedule retrieved successfully",
  "data": {
    "species": "Dog",
    "breed": "Golden Retriever",
    "petAge": "8 weeks",
    "petAgeInMonths": "2 months",
    "recommendedSchedule": [
      {"age": "6 weeks", "vaccine": "DHPP"},
      {"age": "9 weeks", "vaccine": "DHPP"},
      {"age": "12 weeks", "vaccine": "DHPP"},
      {"age": "16 weeks", "vaccine": "DHPP"},
      {"age": "16 weeks", "vaccine": "Rabies"}
    ]
  }
}
```

---

## 📅 Vaccination Schedules

### WSAVA Puppy Schedule (Dogs)
```
6 weeks  → DHPP
9 weeks  → DHPP
12 weeks → DHPP
16 weeks → DHPP + Rabies
Boosters → Annual
```

### WSAVA Kitten Schedule (Cats)
```
8 weeks  → FVRCP
12 weeks → FVRCP
16 weeks → FVRCP + Rabies + FELV
Boosters → Annual
```

---

## 🐕 Deworming Schedules (Age-Based)

### Puppies (0-12 weeks)
**Every 2 weeks**
- Week 0 → Next: Week 2
- Week 2 → Next: Week 4
- Week 4 → Next: Week 6
- (continues every 2 weeks until 12 weeks)

### Juveniles (12 weeks - 6 months)
**Monthly**
- Month 3 → Next: Month 4
- Month 4 → Next: Month 5
- Month 5 → Next: Month 6

### Adults (6+ months)
**Quarterly (3 months)**
- Jan → Apr → Jul → Oct → Jan

---

## 🔐 Authorization Rules

| Endpoint | Owner | Doctor | Notes |
|----------|-------|--------|-------|
| View own medical history | ✅ | ✅ | Read-only for owners |
| View all histories | ❌ | ✅ | Doctor can view any pet |
| Create medical record | ❌ | ✅ | Doctor only |
| Edit medical record | ❌ | ✅ | With audit logging |
| View due/upcoming | ✅ | ✅ | Own pets for owner |

---

## 🗄️ Database Entities

### MedicalRecord
- `id`, `pet_id`, `record_type`, `doctor_id`
- `recorded_date`, `description`, `next_due_date`, `notes`
- `created_at`, `updated_at`

### Vaccination
- `id`, `medical_record_id`, `vaccine_name`, `batch_number`
- `administered_by`, `next_booster_date`
- `created_at`, `updated_at`

### DewormingRecord
- `id`, `medical_record_id`, `dewormer_name`, `dose_mg`
- `administered_by`, `next_dose_date`
- `created_at`, `updated_at`

### AuditLog
- `id`, `user_id`, `action`, `entity_type`, `entity_id`
- `old_values`, `new_values`, `created_at`

---

## 📊 Supported Vaccine Types

```
RABIES              - Annual
DHPP                - Annual
DHLPP               - Annual
FVRCP (Cats)        - Annual
FELV (Cats)         - Annual
BORDETELLA          - Annual
LEPTO               - Annual
FECV (Cats)         - Annual
```

---

## ⚠️ Common Error Responses

### 401 Unauthorized
```json
{"error": "User not authenticated"}
```
→ Missing or invalid JWT token

### 403 Forbidden
```json
{"error": "Only doctors can record vaccinations"}
```
→ Role check failed (Owner trying doctor operation)

### 404 Not Found
```json
{"error": "Pet not found or you don't have access to this pet"}
```
→ Pet doesn't exist or ownership mismatch

### 400 Bad Request
```json
{"error": "Failed to create medical record"}
```
→ Invalid request data or validation failed

---

## 🔄 Data Flow Example

### Recording a Vaccination

```
1. Doctor sends POST to /api/doctor/pets/{petId}/vaccinations
   ↓
2. VaccinationController validates doctor role
   ↓
3. VaccinationService.recordVaccination() executes
   ├─ Creates MedicalRecord (type=VACCINATION)
   ├─ Calculates nextBoosterDate (+365 days)
   ├─ Creates Vaccination record
   └─ Logs audit entry
   ↓
4. Response with vaccination details
   ↓
5. Owner can retrieve via GET /api/owner/pets/{petId}/vaccinations
```

---

## 📈 Performance Considerations

### Indexes Created
- `idx_medical_records_pet_id` - Fast pet lookups
- `idx_audit_logs_entity_type_id` - Audit queries
- `idx_pets_dob` - Age calculations
- `idx_pets_owner_id` - Owner lookups

### Pagination
- Medical history returns all records (sorted by date)
- Consider adding LIMIT/OFFSET for large histories
- Due/upcoming use date filters (auto-limited)

### Caching Opportunities
- Vaccination schedules (rarely change)
- Pet date of birth (immutable)
- Doctor profiles (relatively static)

---

## 🧪 Testing Endpoints

### Valid Test Cases
```
✓ Record vaccine for 6-week puppy
✓ Record deworming for 8-week puppy (expect 2-week next dose)
✓ Record deworming for adult dog (expect 3-month next dose)
✓ Check due vaccinations after booster date passes
✓ Edit medical record and verify audit log
```

### Error Test Cases
```
✓ Owner tries to create medical record (should fail)
✓ Owner tries to edit medical record (should fail)
✓ Doctor tries to access other owner's pet (should fail)
✓ Invalid vaccination date (future date)
✓ Negative dose amount (should fail validation)
```

---

## 📞 Support & Troubleshooting

### Build Issues
```bash
mvn clean compile -DskipTests
```
→ 0 compilation errors expected

### Database Issues
```sql
SELECT * FROM audit_logs WHERE entity_type='MedicalRecord';
```
→ Check audit trail for tracking changes

### JWT Issues
- Verify Authorization header format: `Bearer <token>`
- Check token expiration
- Validate role claims in token

---

## 📚 Related Documentation

- `MEDICAL_RECORDS_IMPLEMENTATION.md` - Feature overview
- `MEDICAL_RECORDS_API_SUMMARY.md` - Complete API reference
- `MEDICAL_RECORDS_TECHNICAL_DETAILS.md` - Implementation details
- `IMPLEMENTATION_COMPLETION_REPORT.md` - Completion summary

---

**Last Updated**: May 23, 2024
**Status**: PRODUCTION READY ✅
