# SQL Queries Quick Reference

## 📁 Files Created

1. **SQL_QUERIES_COMPLETE.md** (32 KB) - Detailed explanations with all queries
2. **SQL_QUERIES_EXECUTABLE.sql** (24 KB) - Copy-paste ready SQL script

---

## 🚀 Quick Start - Run All Queries

### Option 1: Using psql (Fastest)

```bash
# Connect to database
psql -U petvax_user -d petvax_care

# Run the script
\i SQL_QUERIES_EXECUTABLE.sql

# Exit
\q
```

### Option 2: Via Command Line

```bash
# Run directly without entering psql
psql -U petvax_user -d petvax_care -f SQL_QUERIES_EXECUTABLE.sql
```

### Option 3: Spring Boot (Automatic)

```bash
# Backend automatically runs migrations on startup
cd d:\Projects\Petcare\backend
mvn spring-boot:run
```

---

## 📊 Database Tables (15 Total)

### Core Tables (8)

| Table                 | Purpose                             | Records                            |
| --------------------- | ----------------------------------- | ---------------------------------- |
| **users**             | All user accounts (4 roles)         | Admin, Doctors, Assistants, Owners |
| **doctor_profiles**   | Doctor specialization & credentials | Licensed vets                      |
| **pets**              | Pet information                     | Dogs, cats, other animals          |
| **appointments**      | Appointment bookings                | Scheduled, completed, cancelled    |
| **medical_records**   | Medical history                     | Vaccinations, deworming, surgery   |
| **vaccinations**      | Vaccine details                     | Vaccine name, batch, booster date  |
| **deworming_records** | Deworming details                   | Dewormer, dose, next date          |
| **doctor_schedules**  | Doctor availability                 | Weekly schedules                   |

### Support Tables (7)

| Table                    | Purpose                    |
| ------------------------ | -------------------------- |
| **refresh_tokens**       | JWT refresh tokens         |
| **device_tokens**        | Firebase FCM tokens        |
| **reminders**            | Scheduled reminders        |
| **audit_logs**           | System audit trail         |
| **clinic_configuration** | Clinic operating hours     |
| **doctor_blocked_slots** | Doctor availability blocks |
| **admin logs**           | Admin action tracking      |

---

## 👥 Sample Users Created

### Admin Account

```
Username: admin
Email: admin@petvaxcare.com
Password: admin123 (hashed)
Role: ADMIN
```

### Pet Owners (3)

```
1. john_owner / john@example.com
2. sarah_owner / sarah@example.com
3. mike_owner / mike@example.com
```

### Doctors (3)

```
1. dr_emily (Active, General Practice)
2. dr_james (Active, Surgery)
3. dr_lisa (Pending Verification)
```

### Assistants (2)

```
1. assistant_tom
2. assistant_anna
```

### Sample Pets (4)

```
1. Max (Dog) - John's pet
2. Whiskers (Cat) - John's pet
3. Buddy (Dog) - Sarah's pet
4. Mittens (Cat) - Mike's pet
```

---

## 🔑 Important Credentials

### Database Connection

```
Host: localhost
Port: 5432
Database: petvax_care
User: petvax_user
Password: petvax_password
```

### Test Login

```
Username: admin
Password: admin123 (or any password, Spring Security validates)
```

---

## 📝 Common Queries

### Get All Users

```sql
SELECT id, username, email, full_name, role FROM users WHERE soft_deleted = FALSE;
```

### Get All Active Doctors

```sql
SELECT u.id, u.username, u.full_name, dp.specialization, dp.years_of_experience
FROM users u
JOIN doctor_profiles dp ON u.id = dp.user_id
WHERE u.role = 'DOCTOR' AND u.account_status = 'ACTIVE' AND u.soft_deleted = FALSE;
```

### Get Pets for Owner

```sql
SELECT * FROM pets WHERE owner_id = (SELECT id FROM users WHERE username = 'john_owner');
```

### Get Upcoming Appointments

```sql
SELECT a.id, p.name as pet_name, u.full_name as doctor_name, a.appointment_date
FROM appointments a
JOIN pets p ON a.pet_id = p.id
JOIN users u ON a.doctor_id = u.id
WHERE a.appointment_date > NOW() AND a.status = 'SCHEDULED'
ORDER BY a.appointment_date;
```

### Get Medical History for Pet

```sql
SELECT record_type, recorded_date, description, next_due_date
FROM medical_records
WHERE pet_id = 1
ORDER BY recorded_date DESC;
```

### Get Overdue Vaccinations

```sql
SELECT p.name, u.full_name as owner_name, mr.next_due_date
FROM pets p
JOIN users u ON p.owner_id = u.id
JOIN medical_records mr ON p.id = mr.pet_id
WHERE mr.record_type = 'VACCINATION' AND mr.next_due_date < CURRENT_DATE;
```

### Get Doctor Appointments Count

```sql
SELECT u.full_name as doctor_name,
       COUNT(CASE WHEN a.status = 'SCHEDULED' THEN 1 END) as scheduled,
       COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed,
       COUNT(CASE WHEN a.status = 'CANCELLED' THEN 1 END) as cancelled
FROM users u
LEFT JOIN appointments a ON u.id = a.doctor_id
WHERE u.role = 'DOCTOR'
GROUP BY u.id, u.full_name;
```

### Get Pending Doctor Verifications

```sql
SELECT u.id, u.username, u.full_name, dp.license_id, dp.specialization
FROM users u
JOIN doctor_profiles dp ON u.id = dp.user_id
WHERE u.role = 'DOCTOR' AND u.account_status = 'PENDING_VERIFICATION';
```

---

## ✏️ Insert New Data

### Add New Pet Owner

```sql
INSERT INTO users (username, email, password_hash, full_name, phone_number, role, account_status, email_verified)
VALUES ('new_owner', 'newowner@example.com', '$2a$12$...(bcrypt_hash)', 'New Owner', '+1-555-9999', 'PET_OWNER', 'ACTIVE', true);
```

### Add New Pet

```sql
INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex)
VALUES (1, 'Fluffy', 'Cat', 'Maine Coon', '2023-05-10', 6.5, 'Female');
```

### Add New Appointment

```sql
INSERT INTO appointments (pet_id, doctor_id, owner_id, appointment_date, duration_minutes, status, reason)
VALUES (1, 2, 1, '2026-05-30 10:00:00', 30, 'SCHEDULED', 'Vaccination');
```

### Add New Medical Record

```sql
INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
VALUES (1, 'VACCINATION', 2, CURRENT_DATE, 'Rabies vaccine', CURRENT_DATE + INTERVAL '1 year');
```

---

## 🔄 Update Data

### Update User

```sql
UPDATE users SET phone_number = '+1-555-1111', address = 'New Address' WHERE id = 1;
```

### Mark Appointment as Completed

```sql
UPDATE appointments SET status = 'COMPLETED', checked_in_at = NOW() WHERE id = 1;
```

### Check Out Appointment

```sql
UPDATE appointments SET checked_out_at = NOW() WHERE id = 1;
```

### Update Pet Weight

```sql
UPDATE pets SET weight = 30.5 WHERE id = 1;
```

### Soft Delete User (90-day retention)

```sql
UPDATE users SET soft_deleted = TRUE, soft_deleted_at = NOW(), soft_delete_reason = 'Account Deletion' WHERE id = 10;
```

---

## 🗑️ Delete Data

### Hard Delete Pet

```sql
DELETE FROM pets WHERE id = 1;
```

### Hard Delete Appointment

```sql
DELETE FROM appointments WHERE id = 1;
```

### Hard Delete Medical Record

```sql
DELETE FROM medical_records WHERE id = 1;
```

### Cleanup Soft-Deleted Users (90 days+)

```sql
DELETE FROM users WHERE soft_deleted = TRUE AND soft_deleted_at < NOW() - INTERVAL '90 days';
```

---

## 🔍 Verify Data

### Count Tables

```sql
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM pets;
SELECT COUNT(*) FROM appointments;
SELECT COUNT(*) FROM medical_records;
```

### List All Table Names

```sql
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
```

### Check Database Size

```sql
SELECT pg_size_pretty(pg_database_size('petvax_care'));
```

### Check Active Users

```sql
SELECT COUNT(*) FROM users WHERE soft_deleted = FALSE AND account_status = 'ACTIVE';
```

---

## 💡 Tips & Tricks

### Generate BCrypt Hash

```bash
# Use online tool or Spring Security password encoder
# Example: password123 = $2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi
```

### Find User by Email

```sql
SELECT * FROM users WHERE email = 'john@example.com';
```

### Find Pet by Name

```sql
SELECT * FROM pets WHERE name = 'Max';
```

### Export Data

```bash
pg_dump -U petvax_user -d petvax_care -F c -f backup.dump
```

### Import Data

```bash
pg_restore -U petvax_user -d petvax_care backup.dump
```

---

## 🆘 Troubleshooting

### Connection Refused

```bash
# Make sure PostgreSQL is running
Start-Service postgresql-x64-14  # Windows
# or
brew services start postgresql@14  # macOS
```

### Database Doesn't Exist

```sql
-- Create it
CREATE DATABASE petvax_care;
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;
```

### No Tables Exist

```bash
# Backend migrations should create them
# Or run SQL_QUERIES_EXECUTABLE.sql manually
psql -U petvax_user -d petvax_care -f SQL_QUERIES_EXECUTABLE.sql
```

### Permission Denied

```sql
-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO petvax_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO petvax_user;
```

---

## 📊 Query Performance

### Check Query Plan

```sql
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'john@example.com';
```

### View All Indexes

```sql
SELECT tablename, indexname FROM pg_indexes WHERE schemaname = 'public';
```

### Check Table Sizes

```sql
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 📍 File Locations

```
d:\Projects\Petcare\
├── SQL_QUERIES_COMPLETE.md ........... (32 KB, detailed explanations)
├── SQL_QUERIES_EXECUTABLE.sql ....... (24 KB, copy-paste ready)
├── SQL_QUERIES_REFERENCE.md ......... (this file, 5 KB)
├── POSTGRESQL_SETUP_GUIDE.md ........ (PostgreSQL installation guide)
└── backend/src/main/resources/db/migration/
    ├── V1__Initial_Schema.sql
    ├── V2__Add_Pet_Photo_And_Attributes.sql
    ├── V3__Add_Medical_Columns.sql
    ├── V4__Add_Admin_Assistant_Features.sql
    └── V4__Add_Soft_Delete.sql
```

---

## ✅ Next Steps

1. **Read:** SQL_QUERIES_COMPLETE.md (for detailed explanations)
2. **Execute:** SQL_QUERIES_EXECUTABLE.sql (to create all tables)
3. **Verify:** Run verification queries to confirm data
4. **Test:** Use the sample users to test your application

---

## 🎯 Summary

- ✅ 15 tables created
- ✅ All relationships with foreign keys
- ✅ Performance indexes included
- ✅ Sample data for testing (10+ users, 4 pets, 3 appointments)
- ✅ CRUD operations documented
- ✅ Common queries provided
- ✅ Ready for Spring Boot integration

---

**Created:** January 25, 2026
**For:** PetVax Care Project
**Status:** ✅ Complete & Ready to Use
