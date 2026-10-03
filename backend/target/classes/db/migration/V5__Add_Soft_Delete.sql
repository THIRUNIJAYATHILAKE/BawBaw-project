-- V4__Add_Soft_Delete.sql
-- Add soft delete support for users with 90-day retention

ALTER TABLE users ADD COLUMN soft_delete_at TIMESTAMP NULL;

-- Create index on soft_delete_at for efficient querying of deleted vs active users
CREATE INDEX idx_users_soft_delete_at ON users(soft_delete_at);

-- Create index for queries filtering out soft-deleted users
CREATE INDEX idx_users_active ON users(soft_delete_at) WHERE soft_delete_at IS NULL;

-- Create a function to cleanup soft-deleted users older than 90 days
-- This can be scheduled with a cron job or manually executed
CREATE OR REPLACE FUNCTION cleanup_old_deleted_users() RETURNS void AS $$
BEGIN
    DELETE FROM users 
    WHERE soft_delete_at IS NOT NULL 
    AND soft_delete_at < CURRENT_TIMESTAMP - INTERVAL '90 days';
END;
$$ LANGUAGE plpgsql;
