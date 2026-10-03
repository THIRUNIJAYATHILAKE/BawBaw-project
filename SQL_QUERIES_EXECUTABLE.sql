-- ============================================================
-- PetVax Care - Complete SQL Script for Quick Execution
-- ============================================================
-- This script creates all tables and inserts sample data
-- You can copy this entire file and run it in psql
-- ============================================================

-- ============================================================
-- Step 1: Create ENUM Types
-- ============================================================

CREATE TYPE user_role AS ENUM ('PET_OWNER', 'DOCTOR', 'ASSISTANT', 'ADMIN');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'PENDING_VERIFICATION', 'INACTIVE', 'SUSPENDED');
CREATE TYPE appointment_status AS ENUM ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE record_type AS ENUM ('VACCINATION', 'DEWORMING', 'SURGERY', 'MEDICAL');

-- ============================================================
-- Step 2: Create Users Table
-- ============================================================

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

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(account_status);
CREATE INDEX idx_users_soft_deleted ON users(soft_deleted);
CREATE INDEX idx_users_soft_deleted_at ON users(soft_deleted_at);
CREATE INDEX idx_users_soft_delete_at ON users(soft_delete_at);
CREATE INDEX idx_users_active ON users(soft_delete_at) WHERE soft_delete_at IS NULL;

-- ============================================================
-- Step 3: Create Doctor Profiles Table
-- ============================================================

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

-- ============================================================
-- Step 4: Create Pets Table
-- ============================================================

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

CREATE INDEX idx_pets_owner_id ON pets(owner_id);
CREATE INDEX idx_pets_name ON pets(name);
CREATE INDEX idx_pets_microchip ON pets(microchip_id);
CREATE INDEX idx_pets_owner_name ON pets(owner_id, name);
CREATE INDEX idx_pets_dob ON pets(date_of_birth);

-- ============================================================
-- Step 5: Create Appointments Table
-- ============================================================

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

CREATE INDEX idx_appointments_pet_id ON appointments(pet_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_owner_id ON appointments(owner_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_checked_in ON appointments(checked_in_at);
CREATE INDEX idx_appointments_checked_out ON appointments(checked_out_at);

-- ============================================================
-- Step 6: Create Medical Records Table
-- ============================================================

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

CREATE INDEX idx_medical_records_pet_id ON medical_records(pet_id);
CREATE INDEX idx_medical_records_doctor_id ON medical_records(doctor_id);
CREATE INDEX idx_medical_records_type ON medical_records(record_type);
CREATE INDEX idx_medical_records_due_date ON medical_records(next_due_date);
CREATE INDEX idx_medical_records_edited_by ON medical_records(edited_by);

-- ============================================================
-- Step 7: Create Vaccinations Table
-- ============================================================

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

-- ============================================================
-- Step 8: Create Deworming Records Table
-- ============================================================

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

-- ============================================================
-- Step 9: Create Doctor Schedules Table
-- ============================================================

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

-- ============================================================
-- Step 10: Create Refresh Tokens Table
-- ============================================================

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

-- ============================================================
-- Step 11: Create Device Tokens Table
-- ============================================================

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

-- ============================================================
-- Step 12: Create Reminders Table
-- ============================================================

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

-- ============================================================
-- Step 13: Create Audit Logs Table
-- ============================================================

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

-- ============================================================
-- Step 14: Create Clinic Configuration Table
-- ============================================================

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

-- ============================================================
-- Step 15: Create Doctor Blocked Slots Table
-- ============================================================

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

-- ============================================================
-- ============================================================
-- INSERT SAMPLE DATA
-- ============================================================
-- ============================================================

-- ============================================================
-- Insert Clinic Configuration (Pre-populate working hours)
-- ============================================================

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

-- ============================================================
-- Insert Admin User
-- ============================================================

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'admin',
    'admin@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Admin User',
    '+1-555-0101',
    '123 Admin Street, Medical City, MC 12345',
    'ADMIN',
    'ACTIVE',
    true
);

-- ============================================================
-- Insert Pet Owner Users
-- ============================================================

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'john_owner',
    'john@example.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'John Smith',
    '+1-555-0102',
    '456 Pet Avenue, Animal City, AC 67890',
    'PET_OWNER',
    'ACTIVE',
    true
);

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'sarah_owner',
    'sarah@example.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Sarah Johnson',
    '+1-555-0103',
    '789 Puppy Lane, Dog Town, DT 11111',
    'PET_OWNER',
    'ACTIVE',
    true
);

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'mike_owner',
    'mike@example.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Mike Brown',
    '+1-555-0104',
    '321 Kitty Court, Cat Village, CV 22222',
    'PET_OWNER',
    'ACTIVE',
    true
);

-- ============================================================
-- Insert Doctor Users
-- ============================================================

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'dr_emily',
    'emily.doc@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Dr. Emily White',
    '+1-555-0201',
    '555 Medical Plaza, Hospital District, HD 33333',
    'DOCTOR',
    'ACTIVE',
    true
);

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'dr_james',
    'james.doc@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Dr. James Green',
    '+1-555-0202',
    '555 Medical Plaza, Hospital District, HD 33333',
    'DOCTOR',
    'ACTIVE',
    true
);

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'dr_lisa',
    'lisa.doc@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Dr. Lisa Blue',
    '+1-555-0203',
    '555 Medical Plaza, Hospital District, HD 33333',
    'DOCTOR',
    'PENDING_VERIFICATION',
    false
);

-- ============================================================
-- Insert Assistant Users
-- ============================================================

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'assistant_tom',
    'tom.assist@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Tom Helper',
    '+1-555-0301',
    '555 Medical Plaza, Hospital District, HD 33333',
    'ASSISTANT',
    'ACTIVE',
    true
);

INSERT INTO users (username, email, password_hash, full_name, phone_number, address, role, account_status, email_verified)
VALUES (
    'assistant_anna',
    'anna.assist@petvaxcare.com',
    '$2a$12$slYQmyNdGzin7olVefo5be0DxiFoo9ipgsegQQSP.Zml.Jwc7cHAi',
    'Anna Support',
    '+1-555-0302',
    '555 Medical Plaza, Hospital District, HD 33333',
    'ASSISTANT',
    'ACTIVE',
    true
);

-- ============================================================
-- Insert Doctor Profiles
-- ============================================================

INSERT INTO doctor_profiles (user_id, license_id, specialization, years_of_experience, clinic_name)
SELECT id, 'LIC-2024-001', 'General Practice', 10, 'PetCare Clinic'
FROM users WHERE username = 'dr_emily';

INSERT INTO doctor_profiles (user_id, license_id, specialization, years_of_experience, clinic_name)
SELECT id, 'LIC-2024-002', 'Surgery', 15, 'PetCare Clinic'
FROM users WHERE username = 'dr_james';

INSERT INTO doctor_profiles (user_id, license_id, specialization, years_of_experience, clinic_name)
SELECT id, 'LIC-2024-003', 'Preventive Care', 5, 'PetCare Clinic'
FROM users WHERE username = 'dr_lisa';

-- ============================================================
-- Insert Pets
-- ============================================================

INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Max', 'Dog', 'Golden Retriever', '2020-03-15', 28.5, 'Male', 'Golden color'
FROM users WHERE username = 'john_owner';

INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Whiskers', 'Cat', 'Siamese', '2021-06-22', 4.2, 'Female', 'Blue eyes, cream color'
FROM users WHERE username = 'john_owner';

INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Buddy', 'Dog', 'Labrador', '2019-08-10', 32.0, 'Male', 'Black coat'
FROM users WHERE username = 'sarah_owner';

INSERT INTO pets (owner_id, name, species, breed, date_of_birth, weight, sex, markings)
SELECT id, 'Mittens', 'Cat', 'Persian', '2022-01-05', 5.5, 'Female', 'White with orange patches'
FROM users WHERE username = 'mike_owner';

-- ============================================================
-- Insert Doctor Schedules
-- ============================================================

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

INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 1, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_james'
UNION ALL
SELECT id, 3, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_james'
UNION ALL
SELECT id, 5, '09:00:00', '17:00:00', TRUE FROM users WHERE username = 'dr_james';

-- ============================================================
-- Insert Medical Records
-- ============================================================

INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
SELECT p.id, 'VACCINATION', d.id, CURRENT_DATE, 'Rabies and DHPP vaccines administered', CURRENT_DATE + INTERVAL '1 year'
FROM pets p
JOIN users d ON d.username = 'dr_emily'
WHERE p.name = 'Max';

INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
SELECT p.id, 'DEWORMING', d.id, CURRENT_DATE, 'Deworming treatment administered', CURRENT_DATE + INTERVAL '3 months'
FROM pets p
JOIN users d ON d.username = 'dr_james'
WHERE p.name = 'Buddy';

INSERT INTO medical_records (pet_id, record_type, doctor_id, recorded_date, description, next_due_date)
SELECT p.id, 'MEDICAL', d.id, CURRENT_DATE, 'General health checkup - all normal', NULL
FROM pets p
JOIN users d ON d.username = 'dr_emily'
WHERE p.name = 'Whiskers';

-- ============================================================
-- Insert Vaccination Details
-- ============================================================

INSERT INTO vaccinations (medical_record_id, vaccine_name, batch_number, administered_by, next_booster_date)
SELECT mr.id, 'Rabies', 'BATCH-2024-001', d.id, CURRENT_DATE + INTERVAL '1 year'
FROM medical_records mr
JOIN users d ON d.username = 'dr_emily'
WHERE mr.record_type = 'VACCINATION'
LIMIT 1;

INSERT INTO vaccinations (medical_record_id, vaccine_name, batch_number, administered_by, next_booster_date)
SELECT mr.id, 'DHPP', 'BATCH-2024-002', d.id, CURRENT_DATE + INTERVAL '1 year'
FROM medical_records mr
JOIN users d ON d.username = 'dr_emily'
WHERE mr.record_type = 'VACCINATION'
LIMIT 1;

-- ============================================================
-- Insert Deworming Details
-- ============================================================

INSERT INTO deworming_records (medical_record_id, dewormer_name, dose_mg, administered_by, next_dose_date)
SELECT mr.id, 'Panacur', 250.00, d.id, CURRENT_DATE + INTERVAL '3 months'
FROM medical_records mr
JOIN users d ON d.username = 'dr_james'
WHERE mr.record_type = 'DEWORMING'
LIMIT 1;

-- ============================================================
-- Verify Data Insertion
-- ============================================================

-- Display all users
SELECT 'Users:' as section;
SELECT id, username, email, full_name, role, account_status FROM users;

-- Display all pets
SELECT 'Pets:' as section;
SELECT id, name, species, breed, owner_id FROM pets;

-- Display all medical records
SELECT 'Medical Records:' as section;
SELECT id, pet_id, record_type, doctor_id, recorded_date, next_due_date FROM medical_records;

-- ============================================================
-- End of Script
-- ============================================================
