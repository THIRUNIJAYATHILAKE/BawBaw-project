-- V3__Add_Medical_Columns.sql
-- Add audit logging columns and medical record enhancements

-- Add audit columns to medical_records table if they don't exist
ALTER TABLE medical_records
ADD COLUMN IF NOT EXISTS edited_by BIGINT REFERENCES users(id),
ADD COLUMN IF NOT EXISTS edited_at TIMESTAMP;

-- Add audit columns to vaccinations table
ALTER TABLE vaccinations
ADD COLUMN IF NOT EXISTS edited_by BIGINT REFERENCES users(id),
ADD COLUMN IF NOT EXISTS edited_at TIMESTAMP;

-- Add audit columns to deworming_records table
ALTER TABLE deworming_records
ADD COLUMN IF NOT EXISTS edited_by BIGINT REFERENCES users(id),
ADD COLUMN IF NOT EXISTS edited_at TIMESTAMP;

-- Create indexes for audit queries
CREATE INDEX IF NOT EXISTS idx_medical_records_edited_by ON medical_records(edited_by);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type_id ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_action ON audit_logs(user_id, action);

-- Add pet_age related indexes for vaccination scheduling
CREATE INDEX IF NOT EXISTS idx_pets_dob ON pets(date_of_birth);
