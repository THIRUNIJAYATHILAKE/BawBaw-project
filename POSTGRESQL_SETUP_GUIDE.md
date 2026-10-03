# PostgreSQL Setup Guide for PetVax Care

## 📋 Table of Contents

1. [Installation](#installation)
2. [Database Creation](#database-creation)
3. [Schema Overview](#schema-overview)
4. [Running Migrations](#running-migrations)
5. [Verification](#verification)
6. [Backup & Restore](#backup--restore)
7. [Troubleshooting](#troubleshooting)

---

## Installation

### Windows

#### Option 1: Download Installer (Easiest)

1. **Download PostgreSQL**
   - Visit: https://www.postgresql.org/download/windows/
   - Click on "Download the installer"
   - Choose PostgreSQL 14 or 15

2. **Run Installer**
   - Open the downloaded `.exe` file
   - Click "Next" → "Next" → "Next"
   - When asked for password, enter: `postgres` (you can change later)
   - Accept port 5432 (default)
   - Click "Finish"

3. **Verify Installation**
   ```powershell
   psql --version
   # Output: psql (PostgreSQL) 14.x or 15.x
   ```

#### Option 2: Using Chocolatey (If Installed)

```powershell
# Install PostgreSQL
choco install postgresql

# Or specific version
choco install postgresql --version=14.0
```

#### Option 3: Windows Subsystem for Linux (WSL2)

```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start service
sudo service postgresql start

# Verify
psql --version
```

### macOS

#### Using Homebrew

```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install PostgreSQL
brew install postgresql@14

# Start PostgreSQL
brew services start postgresql@14

# Add to PATH
echo 'export PATH="/usr/local/opt/postgresql@14/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verify
psql --version
```

#### Download DMG Installer

1. Visit: https://www.postgresql.org/download/macosx/
2. Download the macOS installer
3. Run the `.dmg` file
4. Follow installation wizard

### Linux (Ubuntu/Debian)

```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start service
sudo systemctl start postgresql

# Enable on startup
sudo systemctl enable postgresql

# Verify
psql --version
```

---

## Database Creation

### Step 1: Connect to PostgreSQL

#### Windows (Command Line)

```powershell
# Open command prompt or PowerShell
psql -U postgres

# You'll be prompted for password (default: postgres)
```

#### macOS/Linux

```bash
sudo -u postgres psql

# Or if no sudo needed:
psql -U postgres
```

### Step 2: Create Database

Run these SQL commands in the `psql` prompt:

```sql
-- Create database
CREATE DATABASE petvax_care;

-- Create user
CREATE USER petvax_user WITH PASSWORD 'petvax_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;

-- Grant schema privileges
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO petvax_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO petvax_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO petvax_user;

-- Connect to database
\c petvax_care

-- Grant privileges for existing tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO petvax_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO petvax_user;

-- Exit psql
\q
```

### Step 3: Verify Connection

```powershell
# Connect as petvax_user
psql -U petvax_user -d petvax_care -h localhost

# Password: petvax_password

# You should see:
# petvax_care=>
```

---

## Schema Overview

### PetVax Care Database Structure

The PetVax Care database contains **15 tables**:

#### Core Tables

| Table                  | Purpose                         | Records                                 |
| ---------------------- | ------------------------------- | --------------------------------------- |
| `users`                | User accounts (all roles)       | Pet owners, Doctors, Assistants, Admins |
| `user_roles`           | Role assignments                | OWNER, DOCTOR, ASSISTANT, ADMIN         |
| `doctor_profile`       | Doctor specialization & details | Licensed veterinarians                  |
| `pet`                  | Pet information                 | Dog, cat, other species                 |
| `appointment`          | Appointment bookings            | Scheduled/Completed/Cancelled           |
| `medical_record`       | Health & surgical history       | Diagnoses, treatments                   |
| `vaccination_record`   | Vaccination tracking            | Vaccine history                         |
| `deworming_record`     | Deworming tracking              | Deworming history                       |
| `notification`         | Notification tracking           | Email, SMS, FCM                         |
| `reminder`             | Appointment & health reminders  | Automated reminders                     |
| `clinic_configuration` | Clinic settings                 | Operating hours, slots                  |
| `doctor_blocked_slot`  | Doctor availability blocks      | Holidays, training                      |
| `audit_log`            | System audit trail              | User actions, changes                   |
| `email_template`       | Email notification templates    | Registration, reset, etc                |
| `password_reset_token` | Password reset tokens           | OTP management                          |

### Data Relationships

```
┌─────────────────────────────────────────┐
│             USERS TABLE                 │
│  ├─ id (PK)                             │
│  ├─ email, username, password           │
│  ├─ role_id (FK → user_roles)           │
│  ├─ verification_status                 │
│  ├─ soft_deleted, soft_deleted_at       │
│  └─ created_at, updated_at              │
└─────────────────────────────────────────┘
         │               │
         ↓               ↓
    ┌──────────┐  ┌──────────────────┐
    │   PETS   │  │ DOCTOR_PROFILE   │
    ├─ id (PK)│  ├─ id (PK)         │
    ├─ user_id│  ├─ user_id (FK)    │
    ├─ name   │  ├─ license_id      │
    └──────────┘  └──────────────────┘
         │
         ↓
    ┌─────────────────────┐
    │ APPOINTMENT         │
    ├─ id (PK)           │
    ├─ pet_id (FK)       │
    ├─ doctor_id (FK)    │
    ├─ assistant_id (FK) │
    ├─ status            │
    └─────────────────────┘
         │         │         │
         ↓         ↓         ↓
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │MEDICAL   │ │VACCINE   │ │DEWORMING │
   │RECORD    │ │RECORD    │ │RECORD    │
   └──────────┘ └──────────┘ └──────────┘
```

---

## Running Migrations

### Automated (Recommended - Spring Boot)

When you start the Spring Boot backend, Flyway automatically runs migrations:

```powershell
# Navigate to backend
cd d:\Projects\Petcare\backend

# Start Spring Boot (migrations run automatically)
mvn spring-boot:run

# Watch for:
# Flyway: Successfully validated 4 migrations
# Flyway: Successfully applied 4 migrations
```

### Manual Migration (Using psql)

If you need to run migrations manually:

```powershell
# Navigate to migration folder
cd d:\Projects\Petcare\backend\src\main\resources\db\migration

# Connect to database
psql -U petvax_user -d petvax_care

# Run each migration file
\i V1__Initial_Schema.sql
\i V2__Add_Email_Support.sql
\i V3__Add_FCM_Notifications.sql
\i V4__Add_Admin_Assistant_Features.sql

# Verify
\dt              # List all tables
\d users         # Describe users table
```

### Migration Files Explained

**V1\_\_Initial_Schema.sql**

- Creates base tables: users, user_roles, pets, appointments
- Sets up indexes and relationships

**V2\_\_Add_Email_Support.sql**

- Adds email_template, password_reset_token tables
- Adds email_sent column to notifications

**V3\_\_Add_FCM_Notifications.sql**

- Adds FCM token tracking
- Adds reminder scheduling tables

**V4\_\_Add_Admin_Assistant_Features.sql**

- Adds soft delete columns
- Adds clinic_configuration, doctor_blocked_slots
- Adds check-in/check-out timestamps

---

## Verification

### Check Database Created

```powershell
# Connect to PostgreSQL
psql -U postgres

# List databases
\l

# Output should show:
#  petvax_care | petvax_user | UTF8     | en_US.UTF-8
```

### Check Tables Created

```powershell
# Connect to petvax_care database
psql -U petvax_user -d petvax_care

# List all tables
\dt

# Output should show ~15 tables:
# users, pets, appointments, vaccination_record, etc.
```

### Check Table Structure

```sql
-- View users table structure
\d users

-- Output:
--                    Table "public.users"
--   Column    |           Type           | Modifiers
-- ─────────────┼──────────────────────────┼───────────
--  id          | bigint                   | not null
--  email       | character varying(255)   | not null
--  username    | character varying(100)   | not null
--  password    | character varying(255)   | not null
--  full_name   | character varying(255)   |
--  phone       | character varying(20)    |
--  address     | character varying(500)   |
--  role_id     | integer                  |
--  verified    | boolean                  | default false
--  created_at  | timestamp                |
--  updated_at  | timestamp                |
```

### Count Records

```sql
-- Check how many users
SELECT COUNT(*) FROM users;

-- Check how many pets
SELECT COUNT(*) FROM pet;

-- Check all tables and row counts
SELECT
    schemaname,
    tablename,
    (SELECT COUNT(*) FROM information_schema.tables
     WHERE table_name=tablename) as rows
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;
```

### Run Test Queries

```sql
-- List all users
SELECT id, username, email, full_name FROM users LIMIT 10;

-- List all pets with owners
SELECT p.id, p.name, u.full_name
FROM pet p
JOIN users u ON p.user_id = u.id;

-- List all appointments with details
SELECT
    a.id,
    p.name as pet_name,
    u.full_name as owner_name,
    a.appointment_status,
    a.appointment_time
FROM appointment a
JOIN pet p ON a.pet_id = p.id
JOIN users u ON p.user_id = u.id
LIMIT 10;
```

---

## Backup & Restore

### Backup Database

#### Full Database Backup

```powershell
# Backup entire database
pg_dump -U petvax_user -d petvax_care -F c -b -v -f petvax_care_backup.dump

# Output file: petvax_care_backup.dump (~5-10 MB)
```

#### SQL Format Backup

```powershell
# Backup as SQL script (human-readable)
pg_dump -U petvax_user -d petvax_care > petvax_care_backup.sql

# Can be version controlled
```

#### Scheduled Backup (Daily)

**Windows - Task Scheduler:**

```batch
# Create batch file: backup.bat
@echo off
set PGUSER=petvax_user
set PGPASSWORD=petvax_password
cd C:\Users\YourUsername\Documents
pg_dump -d petvax_care -F c -b -v -f backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%.dump
```

Then schedule via Task Scheduler to run daily.

**Linux - Cron:**

```bash
# Add to crontab
crontab -e

# Add line (daily at 2 AM):
0 2 * * * pg_dump -U petvax_user -d petvax_care -F c -f /home/user/backups/petvax_care_$(date +\%Y\%m\%d).dump
```

### Restore Database

#### From Dump File

```powershell
# Restore from backup
pg_restore -U petvax_user -d petvax_care -v petvax_care_backup.dump

# If database already has data, use:
pg_restore -U petvax_user -d petvax_care -v --clean petvax_care_backup.dump
```

#### From SQL File

```powershell
# Restore from SQL script
psql -U petvax_user -d petvax_care < petvax_care_backup.sql
```

#### Drop and Recreate

```powershell
# If restore fails, start fresh:
psql -U postgres

# Drop database
DROP DATABASE petvax_care;

# Recreate
CREATE DATABASE petvax_care;
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;

# Exit and restore
\q
pg_restore -U petvax_user -d petvax_care petvax_care_backup.dump
```

---

## Troubleshooting

### Issue: "Cannot connect to database"

**Cause:** PostgreSQL service not running

**Solution:**

```powershell
# Windows - Start service
Start-Service postgresql-x64-14

# Or check if running
Get-Service postgresql-x64-14 | Select Status

# macOS - Start service
brew services start postgresql@14

# Linux - Start service
sudo systemctl start postgresql
```

### Issue: "Role 'petvax_user' does not exist"

**Cause:** User not created

**Solution:**

```powershell
# Connect as postgres
psql -U postgres

# Create user
CREATE USER petvax_user WITH PASSWORD 'petvax_password';

# Exit
\q
```

### Issue: "Database 'petvax_care' does not exist"

**Cause:** Database not created

**Solution:**

```powershell
# Connect as postgres
psql -U postgres

# Create database
CREATE DATABASE petvax_care;

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;

# Exit
\q
```

### Issue: "Permission denied"

**Cause:** User doesn't have privileges

**Solution:**

```powershell
# Connect as postgres
psql -U postgres

# Connect to database
\c petvax_care

# Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO petvax_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO petvax_user;

# Exit
\q
```

### Issue: "Port 5432 already in use"

**Cause:** Another PostgreSQL instance or service using port

**Solution Windows:**

```powershell
# Find process using port 5432
netstat -ano | findstr :5432

# Kill process (replace PID)
taskkill /PID <PID> /F

# Or use different port in application.yml
```

### Issue: Migrations not running

**Cause:** Flyway configuration issue

**Solution:**

```powershell
# Check if migrations folder exists
cd d:\Projects\Petcare\backend
ls src/main/resources/db/migration/

# Should show V1, V2, V3, V4 SQL files

# Check backend logs for Flyway messages
# mvn spring-boot:run | findstr Flyway
```

### Issue: "psql: command not found"

**Cause:** PostgreSQL not in PATH

**Solution Windows:**

```powershell
# Add PostgreSQL to PATH temporarily
$env:PATH += ";C:\Program Files\PostgreSQL\14\bin"

# Verify
psql --version

# Make permanent (Admin PowerShell):
[Environment]::SetEnvironmentVariable("PATH", "$env:PATH;C:\Program Files\PostgreSQL\14\bin", "User")
```

---

## Common PostgreSQL Commands

### Connection Commands

```sql
-- Connect to database
psql -U username -d database_name

-- Connect to specific host
psql -U username -h localhost -p 5432 -d database_name

-- List all databases
\l

-- Connect to database
\c database_name

-- Exit psql
\q or \quit
```

### Table Commands

```sql
-- List all tables
\dt

-- Describe table structure
\d table_name

-- Describe specific column
\d table_name.column_name

-- Show table size
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### User/Permission Commands

```sql
-- Create user
CREATE USER username WITH PASSWORD 'password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE database_name TO username;

-- Remove privileges
REVOKE ALL PRIVILEGES ON DATABASE database_name FROM username;

-- List users
SELECT * FROM pg_user;

-- Change password
ALTER USER username WITH PASSWORD 'new_password';
```

### Backup/Restore Commands

```bash
# Full backup
pg_dump -U username -d database_name -F c -f backup.dump

# SQL format backup
pg_dump -U username -d database_name > backup.sql

# Restore from backup
pg_restore -U username -d database_name backup.dump

# Restore from SQL
psql -U username -d database_name < backup.sql
```

---

## Performance Tuning

### Check Query Performance

```sql
-- Enable query timing
\timing

-- Run query (will show execution time)
SELECT * FROM users;

-- Analyze query plan
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'test@example.com';
```

### Create Indexes (Already Done)

Existing indexes in schema:

```sql
-- Already created for performance:
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_pet_user_id ON pet(user_id);
CREATE INDEX idx_appointment_pet_id ON appointment(pet_id);
CREATE INDEX idx_appointment_doctor_id ON appointment(doctor_id);
CREATE INDEX idx_vaccination_pet_id ON vaccination_record(pet_id);
CREATE INDEX idx_deworming_pet_id ON deworming_record(pet_id);
```

### Monitor Database

```sql
-- Check active connections
SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;

-- Check database size
SELECT datname, pg_size_pretty(pg_database_size(datname))
FROM pg_database
ORDER BY pg_database_size(datname) DESC;

-- Check slow queries (if logging enabled)
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC LIMIT 10;
```

---

## Security Best Practices

### 1. Strong Passwords

```sql
-- Change petvax_user password
ALTER USER petvax_user WITH PASSWORD 'StrongPassword123!@#';
```

### 2. Limit Connections

```sql
-- Only allow localhost connections
-- Edit postgresql.conf:
# listen_addresses = 'localhost'

-- Or edit pg_hba.conf for connection rules
```

### 3. Regular Backups

```powershell
# Automated daily backups
# See "Backup Database" section above
```

### 4. Update PostgreSQL

```powershell
# Windows - Check for updates via installer
# Or use: choco upgrade postgresql

# macOS - Update via Homebrew
brew upgrade postgresql@14

# Linux - Update via package manager
sudo apt update && sudo apt upgrade postgresql
```

### 5. Monitor Access

```sql
-- Enable query logging
-- Edit postgresql.conf:
log_statement = 'all'
log_duration = on

-- Then check logs:
-- Windows: C:\Program Files\PostgreSQL\14\data\log\
-- Linux: /var/log/postgresql/
```

---

## Next Steps

1. ✅ Install PostgreSQL (see Installation section)
2. ✅ Create database and user (see Database Creation)
3. ✅ Verify schema created (see Verification)
4. ✅ Start Spring Boot backend (migrations run automatically)
5. ✅ Test with queries (see Verification)

Once database is running:

```powershell
# Backend will auto-connect and run migrations
cd d:\Projects\Petcare\backend
mvn spring-boot:run

# Then start frontend
cd d:\Projects\Petcare\frontend
npm start
```

---

## Additional Resources

- **Official PostgreSQL Docs:** https://www.postgresql.org/docs/
- **Flyway Documentation:** https://flywaydb.org/documentation/
- **pgAdmin (GUI Tool):** https://www.pgadmin.org/
- **DBeaver (Database IDE):** https://dbeaver.io/

---

**Created:** January 25, 2025  
**For:** PetVax Care Project  
**Version:** PostgreSQL 14+
