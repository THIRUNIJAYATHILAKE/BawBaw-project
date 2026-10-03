-- V4__Add_Admin_Assistant_Features.sql
-- Add Admin and Assistant features to PetVax Care database

-- Add soft delete and verification fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS soft_deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS soft_delete_reason VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS verified_reason VARCHAR(255);

-- Add check-in/check-out fields to appointments table
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS checked_in_at TIMESTAMP;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS checked_out_at TIMESTAMP;

-- Clinic Configuration table
CREATE TABLE IF NOT EXISTS clinic_configuration (
    id BIGSERIAL PRIMARY KEY,
    day_of_week INT NOT NULL UNIQUE CHECK (day_of_week >= 0 AND day_of_week <= 6),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_working_day BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Blocked time slots for doctors
CREATE TABLE IF NOT EXISTS doctor_blocked_slots (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_from TIMESTAMP NOT NULL,
    blocked_until TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Update audit logs to include soft delete tracking
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS change_type VARCHAR(50);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_soft_deleted ON users(soft_deleted);
CREATE INDEX IF NOT EXISTS idx_users_soft_deleted_at ON users(soft_deleted_at);
CREATE INDEX IF NOT EXISTS idx_appointments_checked_in ON appointments(checked_in_at);
CREATE INDEX IF NOT EXISTS idx_appointments_checked_out ON appointments(checked_out_at);
CREATE INDEX IF NOT EXISTS idx_doctor_blocked_slots_doctor_id ON doctor_blocked_slots(doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_blocked_slots_period ON doctor_blocked_slots(blocked_from, blocked_until);
CREATE INDEX IF NOT EXISTS idx_clinic_config_day ON clinic_configuration(day_of_week);

-- Pre-populate clinic configuration with default hours (9 AM - 5 PM, all days working)
INSERT INTO clinic_configuration (day_of_week, start_time, end_time, is_working_day)
VALUES
  (0, '09:00:00', '17:00:00', TRUE),  -- Sunday
  (1, '09:00:00', '17:00:00', TRUE),  -- Monday
  (2, '09:00:00', '17:00:00', TRUE),  -- Tuesday
  (3, '09:00:00', '17:00:00', TRUE),  -- Wednesday
  (4, '09:00:00', '17:00:00', TRUE),  -- Thursday
  (5, '09:00:00', '17:00:00', TRUE),  -- Friday
  (6, '09:00:00', '17:00:00', FALSE)  -- Saturday (non-working)
ON CONFLICT DO NOTHING;
