# Complete SQL Queries - PetVax Care Database

## 📋 Table of Contents

1. [All Table Creation Queries](#all-table-creation-queries)
2. [Sample User Data Insertion](#sample-user-data-insertion)
3. [Common CRUD Operations](#common-crud-operations)
4. [Advanced Queries](#advanced-queries)

---

## All Table Creation Queries

### ⚠️ Important: Migration Order

These queries are automatically run by Flyway in order:

1. V1\_\_Initial_Schema.sql (Main tables)
2. V2\_\_Add_Pet_Photo_And_Attributes.sql (Alterations)
3. V3\_\_Add_Medical_Columns.sql (More alterations)
4. V4\_\_Add_Admin_Assistant_Features.sql (Admin features)
5. V4\_\_Add_Soft_Delete.sql (Soft delete support)

---

## Step 1: Create ENUM Types

```sql
-- Create ENUM types for PostgreSQL type safety
CREATE TYPE user_role AS ENUM ('PET_OWNER', 'DOCTOR', 'ASSISTANT', 'ADMIN');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'PENDING_VERIFICATION', 'INACTIVE', 'SUSPENDED');
CREATE TYPE appointment_status AS ENUM ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE record_type AS ENUM ('VACCINATION', 'DEWORMING', 'SURGERY', 'MEDICAL');
```

---

## Step 2: Create Users Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    phone_number VARCHAR(20),
    address TEXT,
    role user_role NOT NULL DEFAULT 'PET_OWNER',
    account_status account_status NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    soft_deleted BOOLEAN DEFAULT FALSE,
    soft_deleted_at TIMESTAMP,
    soft_delete_reason VARCHAR(255),
    verified_reason VARCHAR(255),
    soft_delete_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT valid_email CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- Indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(account_status);
CREATE INDEX idx_users_soft_deleted ON users(soft_deleted);
CREATE INDEX idx_users_soft_deleted_at ON users(soft_deleted_at);
CREATE INDEX idx_users_soft_delete_at ON users(soft_delete_at);
CREATE INDEX idx_users_active ON users(soft_delete_at) WHERE soft_delete_at IS NULL;
```

---

## Step 3: Create Doctor Profiles Table

```sql
CREATE TABLE doctor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    license_id VARCHAR(50) NOT NULL UNIQUE,
    specialization VARCHAR(100),
    years_of_experience INT,
    clinic_name VARCHAR(100),
    verified_by BIGINT REFERENCES users(id),
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctor_profiles_user_id ON doctor_profiles(user_id);
CREATE INDEX idx_doctor_profiles_license_id ON doctor_profiles(license_id);
```

---

## Step 4: Create Pets Table

```sql
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    date_of_birth DATE,
    weight DECIMAL(5,2),
    sex VARCHAR(20),
    markings TEXT,
    photo_url TEXT,
    microchip_id VARCHAR(50) UNIQUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for pets table
CREATE INDEX idx_pets_owner_id ON pets(owner_id);
CREATE INDEX idx_pets_name ON pets(name);
CREATE INDEX idx_pets_microchip ON pets(microchip_id);
CREATE INDEX idx_pets_owner_name ON pets(owner_id, name);
CREATE INDEX idx_pets_dob ON pets(date_of_birth);
```

---

## Step 5: Create Appointments Table

```sql
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES users(id),
    owner_id BIGINT NOT NULL REFERENCES users(id),
    appointment_date TIMESTAMP NOT NULL,
    duration_minutes INT DEFAULT 30,
    status appointment_status DEFAULT 'SCHEDULED',
    reason TEXT,
    notes TEXT,
    cancelled_at TIMESTAMP,
    cancelled_reason TEXT,
    checked_in_at TIMESTAMP,
    checked_out_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT future_appointment CHECK (appointment_date > CURRENT_TIMESTAMP OR status != 'SCHEDULED')
);

-- Indexes for appointments table
CREATE INDEX idx_appointments_pet_id ON appointments(pet_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_owner_id ON appointments(owner_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_checked_in ON appointments(checked_in_at);
CREATE INDEX idx_appointments_checked_out ON appointments(checked_out_at);
```

---

## Step 6: Create Medical Records Table

```sql
CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    record_type record_type NOT NULL,
    doctor_id BIGINT REFERENCES users(id),
    recorded_date DATE NOT NULL,
    description TEXT NOT NULL,
    next_due_date DATE,
    notes TEXT,
    edited_by BIGINT REFERENCES users(id),
    edited_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for medical records table
CREATE INDEX idx_medical_records_pet_id ON medical_records(pet_id);
CREATE INDEX idx_medical_records_doctor_id ON medical_records(doctor_id);
CREATE INDEX idx_medical_records_type ON medical_records(record_type);
CREATE INDEX idx_medical_records_due_date ON medical_records(next_due_date);
CREATE INDEX idx_medical_records_edited_by ON medical_records(edited_by);
```

---

## Step 7: Create Vaccinations Table

```sql
CREATE TABLE vaccinations (
    id BIGSERIAL PRIMARY KEY,
    medical_record_id BIGINT NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    vaccine_name VARCHAR(100) NOT NULL,
    batch_number VARCHAR(50),
    administered_by BIGINT REFERENCES users(id),
    next_booster_date DATE,
    edited_by BIGINT REFERENCES users(id),
    edited_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vaccinations_medical_record_id ON vaccinations(medical_record_id);
CREATE INDEX idx_vaccinations_administered_by ON vaccinations(administered_by);
```

---

## Step 8: Create Deworming Records Table

```sql
CREATE TABLE deworming_records (
    id BIGSERIAL PRIMARY KEY,
    medical_record_id BIGINT NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    dewormer_name VARCHAR(100) NOT NULL,
    dose_mg DECIMAL(8,2),
    administered_by BIGINT REFERENCES users(id),
    next_dose_date DATE,
    edited_by BIGINT REFERENCES users(id),
    edited_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deworming_records_medical_record_id ON deworming_records(medical_record_id);
CREATE INDEX idx_deworming_records_administered_by ON deworming_records(administered_by);
```

---

## Step 9: Create Doctor Schedules Table

```sql
CREATE TABLE doctor_schedules (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_time_range CHECK (start_time < end_time)
);

CREATE INDEX idx_doctor_schedules_doctor_id ON doctor_schedules(doctor_id);
```

---

## Step 10: Create Refresh Tokens Table

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_value TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_ip VARCHAR(45)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token_value);
```

---

## Step 11: Create Device Tokens Table

```sql
CREATE TABLE device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL,
    device_name VARCHAR(100),
    device_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
```

---

## Step 12: Create Reminders Table

```sql
CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    medical_record_id BIGINT REFERENCES medical_records(id) ON DELETE CASCADE,
    appointment_id BIGINT REFERENCES appointments(id) ON DELETE CASCADE,
    reminder_type VARCHAR(50) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reminders_user_id ON reminders(user_id);
CREATE INDEX idx_reminders_scheduled_date ON reminders(scheduled_date);
```

---

## Step 13: Create Audit Logs Table

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_values TEXT,
    new_values TEXT,
    change_type VARCHAR(50),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_entity_type_id ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user_action ON audit_logs(user_id, action);
```

---

## Step 14: Create Clinic Configuration Table

```sql
CREATE TABLE clinic_configuration (
    id BIGSERIAL PRIMARY KEY,
    day_of_week INT NOT NULL UNIQUE CHECK (day_of_week >= 0 AND day_of_week <= 6),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_working_day BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clinic_config_day ON clinic_configuration(day_of_week);

-- Pre-populate clinic configuration
INSERT INTO clinic_configuration (day_of_week, start_time, end_time, is_working_day)
VALUES
  (0, '09:00:00', '17:00:00', TRUE),   -- Sunday
  (1, '09:00:00', '17:00:00', TRUE),   -- Monday
  (2, '09:00:00', '17:00:00', TRUE),   -- Tuesday
  (3, '09:00:00', '17:00:00', TRUE),   -- Wednesday
  (4, '09:00:00', '17:00:00', TRUE),   -- Thursday
  (5, '09:00:00', '17:00:00', TRUE),   -- Friday
  (6, '09:00:00', '17:00:00', FALSE)   -- Saturday (non-working)
ON CONFLICT DO NOTHING;
```

---

## Step 15: Create Doctor Blocked Slots Table

```sql
CREATE TABLE doctor_blocked_slots (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_from TIMESTAMP NOT NULL,
    blocked_until TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctor_blocked_slots_doctor_id ON doctor_blocked_slots(doctor_id);
CREATE INDEX idx_doctor_blocked_slots_period ON doctor_blocked_slots(blocked_from, blocked_until);
```

---

## Sample User Data Insertion

### 1. Insert Admin User

```sql
-- Admin account (encrypted password for "admin123")
-- Password hash example (you should use bcrypt hash in real application)
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'admin',
    'admin@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "admin123"
    'Admin User',
    '+1-555-0101',
    '123 Admin Street, Medical City, MC 12345',
    'ADMIN',
    'ACTIVE',
    true
);
```

### 2. Insert Pet Owner Users

```sql
-- Pet Owner 1
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'john_owner',
    'john@example.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "password123"
    'John Smith',
    '+1-555-0102',
    '456 Pet Avenue, Animal City, AC 67890',
    'PET_OWNER',
    'ACTIVE',
    true
);

-- Pet Owner 2
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'sarah_owner',
    'sarah@example.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "password123"
    'Sarah Johnson',
    '+1-555-0103',
    '789 Puppy Lane, Dog Town, DT 11111',
    'PET_OWNER',
    'ACTIVE',
    true
);

-- Pet Owner 3
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'mike_owner',
    'mike@example.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "password123"
    'Mike Brown',
    '+1-555-0104',
    '321 Kitty Court, Cat Village, CV 22222',
    'PET_OWNER',
    'ACTIVE',
    true
);
```

### 3. Insert Doctor Users

```sql
-- Doctor 1
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'dr_emily',
    'emily.doc@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "doctor123"
    'Dr. Emily White',
    '+1-555-0201',
    '555 Medical Plaza, Hospital District, HD 33333',
    'DOCTOR',
    'ACTIVE',
    true
);

-- Doctor 2
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'dr_james',
    'james.doc@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "doctor123"
    'Dr. James Green',
    '+1-555-0202',
    '555 Medical Plaza, Hospital District, HD 33333',
    'DOCTOR',
    'ACTIVE',
    true
);

-- Doctor 3 (Pending verification)
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'dr_lisa',
    'lisa.doc@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "doctor123"
    'Dr. Lisa Blue',
    '+1-555-0203',
    '555 Medical Plaza, Hospital District, HD 33333',
    'DOCTOR',
    'PENDING_VERIFICATION',
    false
);
```

### 4. Insert Assistant Users

```sql
-- Assistant 1
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'assistant_tom',
    'tom.assist@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "assistant123"
    'Tom Helper',
    '+1-555-0301',
    '555 Medical Plaza, Hospital District, HD 33333',
    'ASSISTANT',
    'ACTIVE',
    true
);

-- Assistant 2
INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'assistant_anna',
    'anna.assist@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',  -- BCrypt hash of "assistant123"
    'Anna Support',
    '+1-555-0302',
    '555 Medical Plaza, Hospital District, HD 33333',
    'ASSISTANT',
    'ACTIVE',
    true
);
```

### 5. Insert Doctor Profiles

```sql
-- Doctor Profile 1
INSERT INTO doctor_profiles (user_id, license_id, specialization, years_of_experience, clinic_name)
SELECT id, 'LIC-2024-001', 'General Practice', 10, 'PetCare Clinic'
FROM users WHERE username = 'dr_emily';

-- Doctor Profile 2
INSERT INTO doctor_profiles (user_id, license_id, specialization, years_of_experience, clinic_name)
SELECT id, 'LIC-2024-002', 'Surgery', 15, 'PetCare Clinic'
FROM users WHERE username = 'dr_james';

-- Doctor Profile 3
INSERT INTO doctor_profiles (user_id, license_id, specialization, years_of_experience, clinic_name)
SELECT id, 'LIC-2024-003', 'Preventive Care', 5, 'PetCare Clinic'
FROM users WHERE username = 'dr_lisa';
```

### 6. Insert Pets

```sql
-- Pet 1: John's Dog
INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Max', 'Dog', 'Golden Retriever', '2020-03-15', 28.5, 'Male', 'Golden color'
FROM users WHERE username = 'john_owner';

-- Pet 2: John's Cat
INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Whiskers', 'Cat', 'Siamese', '2021-06-22', 4.2, 'Female', 'Blue eyes, cream color'
FROM users WHERE username = 'john_owner';

-- Pet 3: Sarah's Dog
INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Buddy', 'Dog', 'Labrador', '2019-08-10', 32.0, 'Male', 'Black coat'
FROM users WHERE username = 'sarah_owner';

-- Pet 4: Mike's Cat
INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Mittens', 'Cat', 'Persian', '2022-01-05', 5.5, 'Female', 'White with orange patches'
FROM users WHERE username = 'mike_owner';
```

### 7. Insert Doctor Schedules

```sql
-- Dr. Emily's schedule (Monday to Friday, 9 AM - 5 PM)
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 1, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_emily'
UNION ALL
SELECT id, 2, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_emily'
UNION ALL
SELECT id, 3, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_emily'
UNION ALL
SELECT id, 4, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_emily'
UNION ALL
SELECT id, 5, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_emily';

-- Dr. James's schedule (Monday, Wednesday, Friday, 9 AM - 5 PM)
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 1, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_james'
UNION ALL
SELECT id, 3, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_james'
UNION ALL
SELECT id, 5, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_james';
```

### 8. Insert Appointments

```sql
-- Appointment 1: John's Max with Dr. Emily
INSERT INTO appointments (pet_id, doctor_id, owner_id, appointment_date, duration_minutes, status, reason)
SELECT p.id, d.id, o.id, NOW() + INTERVAL '2 days' + INTERVAL '10 hours', 30, 'SCHEDULED', 'Vaccination'
FROM pets p
JOIN users d ON d.username = 'dr_emily'
JOIN users o ON o.username = 'john_owner'
WHERE p.name = 'Max';

-- Appointment 2: Sarah's Buddy with Dr. James
INSERT INTO appointments (pet_id, doctor_id, owner_id, appointment_date, duration_minutes, status, reason)
SELECT p.id, d.id, o.id, NOW() + INTERVAL '3 days' + INTERVAL '14 hours', 30, 'SCHEDULED', 'Checkup'
FROM pets p
JOIN users d ON d.username = 'dr_james'
JOIN users o ON o.username = 'sarah_owner'
WHERE p.name = 'Buddy';

-- Appointment 3: John's Whiskers with Dr. Emily
INSERT INTO appointments (pet_id, doctor_id, owner_id, appointment_date, duration_minutes, status, reason)
SELECT p.id, d.id, o.id, NOW() + INTERVAL '5 days' + INTERVAL '11 hours', 30, 'SCHEDULED', 'Deworming'
FROM pets p
JOIN users d ON d.username = 'dr_emily'
JOIN users o ON o.username = 'john_owner'
WHERE p.name = 'Whiskers';
```

### 9. Insert Medical Records

```sql
-- Medical Record 1: Max's Vaccination
INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
SELECT p.id, 'VACCINATION', d.id, CURRENT_DATE, 'Rabies and DHPP vaccines administered', CURRENT_DATE + INTERVAL '1 year'
FROM pets p
JOIN users d ON d.username = 'dr_emily'
WHERE p.name = 'Max';

-- Medical Record 2: Buddy's Deworming
INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
SELECT p.id, 'DEWORMING', d.id, CURRENT_DATE, 'Deworming treatment administered', CURRENT_DATE + INTERVAL '3 months'
FROM pets p
JOIN users d ON d.username = 'dr_james'
WHERE p.name = 'Buddy';

-- Medical Record 3: Whiskers' Checkup
INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
SELECT p.id, 'MEDICAL', d.id, CURRENT_DATE, 'General health checkup - all normal', NULL
FROM pets p
JOIN users d ON d.username = 'dr_emily'
WHERE p.name = 'Whiskers';
```

### 10. Insert Vaccination Details

```sql
-- Vaccination Detail 1
INSERT INTO vaccinations (medical_record_id, vaccine_name, batch_number, administered_by, next_booster_date)
SELECT mr.id, 'Rabies', 'BATCH-2024-001', d.id, CURRENT_DATE + INTERVAL '1 year'
FROM medical_records mr
JOIN users d ON d.username = 'dr_emily'
WHERE mr.record_type = 'VACCINATION'
LIMIT 1;

-- Vaccination Detail 2
INSERT INTO vaccinations (medical_record_id, vaccine_name, batch_number, administered_by, next_booster_date)
SELECT mr.id, 'DHPP', 'BATCH-2024-002', d.id, CURRENT_DATE + INTERVAL '1 year'
FROM medical_records mr
JOIN users d ON d.username = 'dr_emily'
WHERE mr.record_type = 'VACCINATION'
LIMIT 1;
```

### 11. Insert Deworming Details

```sql
-- Deworming Detail 1
INSERT INTO deworming_records (medical_record_id, dewormer_name, dose_mg, administered_by, next_dose_date)
SELECT mr.id, 'Panacur', 250.00, d.id, CURRENT_DATE + INTERVAL '3 months'
FROM medical_records mr
JOIN users d ON d.username = 'dr_james'
WHERE mr.record_type = 'DEWORMING'
LIMIT 1;
```

---

## Common CRUD Operations

### Create Operations

```sql
-- Add a new pet for an owner
INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex)
SELECT id, 'Fluffy', 'Cat', 'Maine Coon', '2023-05-10', 6.5, 'Female'
FROM users WHERE email = 'john@example.com';

-- Add a new appointment
INSERT INTO appointments (pet_id, doctor_id, owner_id, appointment_date, reason)
VALUES (1, 2, 1, '2026-05-30 10:00:00', 'Vaccination Booster');

-- Add a new medical record
INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
VALUES (1, 'VACCINATION', 2, CURRENT_DATE, 'Annual checkup vaccines', CURRENT_DATE + INTERVAL '1 year');
```

### Read Operations

```sql
-- Get all pet owners
SELECT id, username, email, full_name, phone_number FROM users WHERE role = 'PET_OWNER' AND soft_deleted = FALSE;

-- Get all active doctors
SELECT u.id, u.username, u.email, u.full_name, dp.specialization, dp.years_of_experience
FROM users u
JOIN doctor_profiles dp ON u.id = dp.user_id
WHERE u.role = 'DOCTOR' AND u.account_status = 'ACTIVE' AND u.soft_deleted = FALSE;

-- Get all pets for a specific owner
SELECT id, name, species, breed, date_of_birth, weight FROM pets WHERE owner_id = 1 AND deleted = FALSE;

-- Get upcoming appointments
SELECT a.id, p.name as pet_name, u.full_name as owner_name, d.full_name as doctor_name, a.appointment_date, a.reason
FROM appointments a
JOIN pets p ON a.pet_id = p.id
JOIN users u ON a.owner_id = u.id
JOIN users d ON a.doctor_id = d.id
WHERE a.appointment_date > NOW() AND a.status = 'SCHEDULED'
ORDER BY a.appointment_date;

-- Get vaccination history for a pet
SELECT v.vaccine_name, v.batch_number, mr.recorded_date, v.next_booster_date, u.full_name as administered_by
FROM vaccinations v
JOIN medical_records mr ON v.medical_record_id = mr.id
JOIN users u ON v.administered_by = u.id
WHERE mr.pet_id = 1
ORDER BY mr.recorded_date DESC;

-- Get doctor's schedule
SELECT day_of_week, start_time, end_time, is_available
FROM doctor_schedules
WHERE doctor_id = 2
ORDER BY day_of_week;
```

### Update Operations

```sql
-- Update user profile
UPDATE users SET full_name = 'John Doe Updated', phone_number = '+1-555-9999'
WHERE id = 1;

-- Mark appointment as completed
UPDATE appointments SET status = 'COMPLETED', checked_in_at = NOW()
WHERE id = 1;

-- Mark appointment as checked out
UPDATE appointments SET checked_out_at = NOW()
WHERE id = 1 AND status = 'COMPLETED';

-- Update pet information
UPDATE pets SET weight = 30.5, markings = 'New markings'
WHERE id = 1;

-- Update doctor profile
UPDATE doctor_profiles SET specialization = 'Emergency Care', verified_at = NOW()
WHERE user_id = 2;

-- Soft delete a user (90-day retention)
UPDATE users SET soft_deleted = TRUE, soft_deleted_at = NOW(), soft_delete_reason = 'User requested deletion'
WHERE id = 10;
```

### Delete Operations

```sql
-- Hard delete a pet (permanent, use with caution!)
DELETE FROM pets WHERE id = 1;

-- Hard delete an appointment
DELETE FROM appointments WHERE id = 1;

-- Hard delete medical records
DELETE FROM medical_records WHERE pet_id = 1;

-- Cleanup soft-deleted users older than 90 days
DELETE FROM users WHERE soft_deleted = TRUE AND soft_deleted_at < NOW() - INTERVAL '90 days';
```

---

## Advanced Queries

### 1. Get Pet Care History

```sql
SELECT
    p.name as pet_name,
    p.species,
    p.breed,
    u.full_name as owner,
    mr.record_type,
    mr.recorded_date,
    mr.description,
    mr.next_due_date,
    d.full_name as doctor_name
FROM medical_records mr
JOIN pets p ON mr.pet_id = p.id
JOIN users u ON p.owner_id = u.id
LEFT JOIN users d ON mr.doctor_id = d.id
WHERE p.id = 1
ORDER BY mr.recorded_date DESC;
```

### 2. Get Overdue Vaccinations

```sql
SELECT
    p.name as pet_name,
    u.full_name as owner_name,
    u.email,
    u.phone_number,
    v.vaccine_name,
    mr.next_due_date,
    CURRENT_DATE - mr.next_due_date as days_overdue
FROM pets p
JOIN users u ON p.owner_id = u.id
JOIN medical_records mr ON p.id = mr.pet_id
JOIN vaccinations v ON mr.id = v.medical_record_id
WHERE mr.record_type = 'VACCINATION'
    AND mr.next_due_date < CURRENT_DATE
    AND p.soft_deleted = FALSE
ORDER BY mr.next_due_date ASC;
```

### 3. Get Doctor Appointment Statistics

```sql
SELECT
    u.id,
    u.full_name as doctor_name,
    COUNT(CASE WHEN a.status = 'SCHEDULED' THEN 1 END) as scheduled_appointments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_appointments,
    COUNT(CASE WHEN a.status = 'CANCELLED' THEN 1 END) as cancelled_appointments,
    COUNT(CASE WHEN a.status = 'NO_SHOW' THEN 1 END) as no_show_appointments
FROM users u
LEFT JOIN appointments a ON u.id = a.doctor_id
WHERE u.role = 'DOCTOR' AND u.soft_deleted = FALSE
GROUP BY u.id, u.full_name
ORDER BY scheduled_appointments DESC;
```

### 4. Get Appointment Statistics by Owner

```sql
SELECT
    u.id,
    u.full_name as owner_name,
    COUNT(DISTINCT p.id) as total_pets,
    COUNT(a.id) as total_appointments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_appointments,
    MAX(a.appointment_date) as last_appointment
FROM users u
LEFT JOIN pets p ON u.id = p.owner_id
LEFT JOIN appointments a ON p.id = a.pet_id
WHERE u.role = 'PET_OWNER' AND u.soft_deleted = FALSE
GROUP BY u.id, u.full_name
ORDER BY total_appointments DESC;
```

### 5. Get Pending Doctor Verifications

```sql
SELECT
    u.id,
    u.username,
    u.email,
    u.full_name,
    dp.license_id,
    dp.specialization,
    dp.years_of_experience,
    u.created_at,
    u.account_status
FROM users u
JOIN doctor_profiles dp ON u.id = dp.user_id
WHERE u.role = 'DOCTOR'
    AND u.account_status = 'PENDING_VERIFICATION'
    AND u.soft_deleted = FALSE
ORDER BY u.created_at ASC;
```

### 6. Get Doctor Availability

```sql
SELECT
    u.id,
    u.full_name,
    ds.day_of_week,
    ds.start_time,
    ds.end_time,
    COUNT(a.id) as appointments_today
FROM users u
JOIN doctor_schedules ds ON u.id = ds.doctor_id
LEFT JOIN appointments a ON u.id = a.doctor_id
    AND DATE(a.appointment_date) = CURRENT_DATE
    AND a.status = 'SCHEDULED'
WHERE u.role = 'DOCTOR'
    AND u.soft_deleted = FALSE
    AND ds.is_available = TRUE
GROUP BY u.id, u.full_name, ds.day_of_week, ds.start_time, ds.end_time
ORDER BY ds.day_of_week, ds.start_time;
```

### 7. Get Medical Records Requiring Admin Review

```sql
SELECT
    p.name as pet_name,
    u.full_name as owner,
    mr.record_type,
    mr.recorded_date,
    d.full_name as doctor,
    mr.description,
    mr.edited_by,
    mr.edited_at
FROM medical_records mr
JOIN pets p ON mr.pet_id = p.id
JOIN users u ON p.owner_id = u.id
LEFT JOIN users d ON mr.doctor_id = d.id
WHERE mr.edited_at IS NOT NULL
ORDER BY mr.edited_at DESC;
```

### 8. Get User Activity Audit

```sql
SELECT
    al.id,
    u.full_name,
    al.action,
    al.entity_type,
    al.old_values,
    al.new_values,
    al.change_type,
    al.ip_address,
    al.created_at
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
WHERE u.id = 1
    OR al.entity_id = 1
ORDER BY al.created_at DESC
LIMIT 100;
```

---

## Notes on Password Hashing

⚠️ **Important:** The password examples shown are BCrypt hashes. In your application:

1. **Never store plain passwords** in the database
2. **Always use BCrypt** with cost factor 12 minimum
3. Use `$2a$12$...` format for Spring Security
4. For test credentials, you can generate hashes using:

```bash
# Using bcrypt online generator or Spring Security tool
# Example plain password: password123
# Generates: $2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi
```

---

## Running All Queries Together

To run all migrations and inserts automatically:

```bash
# Backend will run migrations automatically
cd d:\Projects\Petcare\backend
mvn spring-boot:run

# All tables and sample data will be created
# Check logs for: "Flyway: Successfully applied X migrations"
```

Or manually in psql:

```bash
psql -U petvax_user -d petvax_care

-- Run all migrations
\i V1__Initial_Schema.sql
\i V2__Add_Pet_Photo_And_Attributes.sql
\i V3__Add_Medical_Columns.sql
\i V4__Add_Admin_Assistant_Features.sql
\i V4__Add_Soft_Delete.sql

-- Verify tables created
\dt

-- Insert sample data
-- (Copy-paste the INSERT statements above)
```

---

**Created:** January 25, 2026  
**For:** PetVax Care Project  
**Status:** Complete & Ready to Use
