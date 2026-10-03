# PostgreSQL Documentation Index

## 📖 Two Complete Guides Created

### 1. **POSTGRESQL_QUICK_GUIDE.md** (7 KB) ⭐ START HERE

- **5-minute quick setup**
- Copy-paste commands for each OS (Windows, macOS, Linux)
- Visual database structure diagram
- Common tasks (create table, insert, query, backup)
- Troubleshooting quick reference
- Health check commands
- **Best for:** Getting started fast

### 2. **POSTGRESQL_SETUP_GUIDE.md** (19 KB) 📚 COMPREHENSIVE

- Installation options for all operating systems
- Step-by-step database creation
- Complete schema overview (15 tables with relationships)
- Automated migration explanation
- Manual migration instructions
- Verification procedures
- Backup & restore strategies (daily backups, recovery)
- Performance tuning
- Security best practices
- Extensive troubleshooting with solutions
- Common PostgreSQL commands reference
- **Best for:** Understanding PostgreSQL deeply

---

## 🚀 Quick Navigation

### If You Want to...

| Goal                     | Read This                                           | Time   |
| ------------------------ | --------------------------------------------------- | ------ |
| **Get started now**      | POSTGRESQL_QUICK_GUIDE.md                           | 5 min  |
| **Install PostgreSQL**   | POSTGRESQL_SETUP_GUIDE.md → Installation            | 15 min |
| **Create database**      | POSTGRESQL_QUICK_GUIDE.md → Step 2                  | 2 min  |
| **Understand schema**    | POSTGRESQL_SETUP_GUIDE.md → Schema Overview         | 10 min |
| **Set up backups**       | POSTGRESQL_SETUP_GUIDE.md → Backup & Restore        | 15 min |
| **Fix errors**           | POSTGRESQL_SETUP_GUIDE.md → Troubleshooting         | 5 min  |
| **Learn SQL commands**   | POSTGRESQL_SETUP_GUIDE.md → Common Commands         | 10 min |
| **Optimize performance** | POSTGRESQL_SETUP_GUIDE.md → Performance Tuning      | 15 min |
| **Security hardening**   | POSTGRESQL_SETUP_GUIDE.md → Security Best Practices | 10 min |

---

## 📋 What Each Guide Covers

### POSTGRESQL_QUICK_GUIDE.md

**Sections:**

1. Fastest Way to Setup (5 Minutes)
   - Download & Install (2 mins)
   - Create Database (2 mins)
   - Verify Connection (1 min)

2. Database Structure (What Gets Created)
   - Visual table layout
   - 15 tables explained

3. Key Commands (Copy-Paste)
   - Check if running
   - Start PostgreSQL
   - Connect to database
   - List databases
   - List tables
   - Check table rows

4. Common Tasks
   - Create table
   - Insert data
   - Query data
   - Update data
   - Delete data
   - Drop table

5. Backup & Restore
   - Quick backup
   - Quick restore

6. Troubleshooting
   - Common issues table
   - Quick solutions

7. Check Database Health
   - Database size
   - Table count
   - Active connections
   - Table sizes

8. Next Steps
   - Sequential tasks

### POSTGRESQL_SETUP_GUIDE.md

**Sections:**

1. **Installation** (Choose your OS)
   - Windows Installer (easiest)
   - Windows Chocolatey
   - Windows WSL2
   - macOS Homebrew
   - macOS DMG installer
   - Linux Ubuntu/Debian

2. **Database Creation**
   - Step 1: Connect to PostgreSQL
   - Step 2: Create database with SQL
   - Step 3: Verify connection
   - Detailed SQL commands explained

3. **Schema Overview**
   - 15 tables listed with descriptions
   - Data relationships diagram
   - Each table's purpose

4. **Running Migrations**
   - Automated (Spring Boot Flyway)
   - Manual (using psql)
   - Migration files explained (V1-V4)

5. **Verification**
   - Check database created
   - Check tables created
   - Check table structure
   - Count records
   - Run test queries

6. **Backup & Restore**
   - Full database backup
   - SQL format backup
   - Scheduled backups (Windows Task Scheduler)
   - Scheduled backups (Linux Cron)
   - Restore from dump file
   - Restore from SQL file
   - Drop and recreate recovery

7. **Troubleshooting** (Detailed)
   - Cannot connect to database
   - Role does not exist
   - Database does not exist
   - Permission denied
   - Port already in use
   - Migrations not running
   - Command not found
   - Each issue has clear solution

8. **Common PostgreSQL Commands**
   - Connection commands
   - Table commands
   - User/permission commands
   - Backup/restore commands

9. **Performance Tuning**
   - Check query performance
   - Create indexes (already done)
   - Monitor database

10. **Security Best Practices**
    - Strong passwords
    - Limit connections
    - Regular backups
    - Update PostgreSQL
    - Monitor access

11. **Next Steps**
    - Sequential tasks to complete

12. **Additional Resources**
    - External links
    - Tools (pgAdmin, DBeaver)

---

## 🎯 Database Structure Reference

### 15 Tables Created

**Core Tables (8):**

1. **users** - All user accounts (Pet owners, doctors, assistants, admins)
2. **pet** - Pet information (dogs, cats, other)
3. **appointment** - Appointment bookings and scheduling
4. **vaccination_record** - Vaccine history tracking
5. **deworming_record** - Deworming schedule tracking
6. **medical_record** - Medical and surgical history
7. **doctor_profile** - Doctor specialization and credentials
8. **notification** - Sent notifications tracking

**Support Tables (7):** 9. **user_roles** - Role definitions (OWNER, DOCTOR, ASSISTANT, ADMIN) 10. **reminder** - Automated reminders for appointments/vaccinations 11. **clinic_configuration** - Clinic settings and operating hours 12. **doctor_blocked_slot** - Doctor availability blocks (holidays, training) 13. **audit_log** - System audit trail of all actions 14. **email_template** - Email notification templates 15. **password_reset_token** - OTP tokens for password reset

### Key Fields Explained

**Users Table:**

- Authentication: email, username, password (BCrypt)
- Profile: full_name, phone, address, profile_photo
- Status: verified, soft_deleted, soft_deleted_at
- Timestamps: created_at, updated_at

**Pets Table:**

- Info: name, species, breed, date_of_birth, weight, sex
- Medical: microchip_id, special_markings, photo
- Reference: user_id (owner), created_at, updated_at

**Appointments Table:**

- Booking: pet_id, doctor_id, assistant_id, appointment_time
- Status: appointment_status (SCHEDULED, COMPLETED, CANCELLED)
- Purpose: purpose (VACCINATION, CHECKUP, DEWORMING, OTHER)
- Tracking: checked_in_at, checked_out_at
- Metadata: notes, reason_for_cancellation

---

## 🔐 Default Configuration

### Connection Details

```
Hostname: localhost
Port: 5432
Database: petvax_care
User: petvax_user
Password: petvax_password
```

### Spring Boot Configuration (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/petvax_care
    username: petvax_user
    password: petvax_password
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

## 📊 Performance Indexes

Automatically created for performance:

```sql
-- User lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Pet lookups
CREATE INDEX idx_pet_user_id ON pet(user_id);

-- Appointment queries
CREATE INDEX idx_appointment_pet_id ON appointment(pet_id);
CREATE INDEX idx_appointment_doctor_id ON appointment(doctor_id);
CREATE INDEX idx_appointment_assistant_id ON appointment(assistant_id);

-- Medical history
CREATE INDEX idx_vaccination_pet_id ON vaccination_record(pet_id);
CREATE INDEX idx_deworming_pet_id ON deworming_record(pet_id);
CREATE INDEX idx_medical_record_pet_id ON medical_record(pet_id);

-- Notifications
CREATE INDEX idx_notification_user_id ON notification(user_id);
```

---

## 🔄 Migration Flow

```
Start Spring Boot
        ↓
Flyway checks database
        ↓
Does Flyway metadata table exist?
    ↓ NO → Create it
    ↓ YES → Continue
        ↓
Read all V*.sql files in order
        ↓
For each migration:
    - Check if already applied
    - If not → Execute SQL
    - Record in flyway_schema_history
        ↓
All 4 migrations run:
    - V1__Initial_Schema.sql
    - V2__Add_Email_Support.sql
    - V3__Add_FCM_Notifications.sql
    - V4__Add_Admin_Assistant_Features.sql
        ↓
Database ready with 15 tables ✅
```

---

## ✨ Key Features

- ✅ **Automated migrations** - Flyway handles schema creation
- ✅ **Foreign keys** - Relationship integrity enforced
- ✅ **Indexes** - Performance optimized for common queries
- ✅ **Soft deletes** - Data retention for 90 days
- ✅ **Audit logging** - Track all changes
- ✅ **Multi-user** - Support for 4 roles
- ✅ **Timestamps** - created_at, updated_at on all tables
- ✅ **Password security** - BCrypt hashing

---

## 🎓 Learning Path

### For Beginners

1. Start: POSTGRESQL_QUICK_GUIDE.md
2. Read: Installation section
3. Do: Create database (copy-paste commands)
4. Verify: Check tables created
5. Learn: Common tasks section

### For Experienced Users

1. Review: POSTGRESQL_SETUP_GUIDE.md → Schema Overview
2. Understand: Database structure and relationships
3. Configure: Performance tuning section
4. Secure: Security best practices section
5. Setup: Backup and restore procedures

### For System Administrators

1. Review: Installation for your OS
2. Configure: Security best practices
3. Setup: Scheduled backups
4. Monitor: Performance monitoring queries
5. Document: Custom settings for your environment

---

## 🛠️ Common Operations

### Backup Schedule (Recommended)

- **Daily backups** - Keep last 7 days
- **Weekly full backups** - Keep last 4 weeks
- **Monthly full backups** - Keep last 12 months

### Monitoring Schedule

- **Hourly** - Check database size
- **Daily** - Check active connections
- **Weekly** - Review slow query logs
- **Monthly** - Review backup integrity

### Maintenance Schedule

- **Weekly** - Analyze query performance
- **Monthly** - Run VACUUM (cleanup)
- **Quarterly** - Update PostgreSQL security patches
- **Annually** - Complete disaster recovery test

---

## 📞 Quick Help

| Issue                  | Quick Fix                                                           |
| ---------------------- | ------------------------------------------------------------------- |
| PostgreSQL won't start | `Start-Service postgresql-x64-14` (Windows)                         |
| Can't connect          | Check hostname (localhost), port (5432), credentials                |
| Tables not created     | Ensure Flyway migrations ran (check Spring Boot logs)               |
| Permission errors      | Run: `GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;` |
| Backup failed          | Check directory permissions and disk space                          |
| Restore failed         | Ensure database is empty before restore                             |

---

## 🚀 Next Steps

1. **Choose a guide based on your needs:**
   - Beginner? → POSTGRESQL_QUICK_GUIDE.md
   - Detailed? → POSTGRESQL_SETUP_GUIDE.md

2. **Install PostgreSQL** (15 minutes)

3. **Create database** (2 minutes using guide)

4. **Verify connection** (1 minute)

5. **Start Spring Boot** (migrations run automatically)

6. **Verify tables created** (1 minute)

7. **Start developing!**

---

## 📁 File Locations

```
d:\Projects\Petcare\
├── POSTGRESQL_QUICK_GUIDE.md ........... 7 KB (Quick setup)
├── POSTGRESQL_SETUP_GUIDE.md .......... 19 KB (Comprehensive)
├── DOCS_INDEX.md
├── START_HERE.md
├── QUICK_START.md
└── backend/
    └── src/main/resources/
        └── db/migration/
            ├── V1__Initial_Schema.sql
            ├── V2__Add_Email_Support.sql
            ├── V3__Add_FCM_Notifications.sql
            └── V4__Add_Admin_Assistant_Features.sql
```

---

## 📚 External Resources

- **PostgreSQL Official Docs:** https://www.postgresql.org/docs/
- **Flyway Documentation:** https://flywaydb.org/documentation/
- **pgAdmin (GUI):** https://www.pgadmin.org/
- **DBeaver (IDE):** https://dbeaver.io/
- **PostgreSQL Tutorials:** https://www.postgresqltutorial.com/

---

## ✅ Verification Checklist

After following the guides:

- [ ] PostgreSQL installed and running
- [ ] Database `petvax_care` created
- [ ] User `petvax_user` created
- [ ] Permissions granted
- [ ] Can connect to database
- [ ] Spring Boot starts without errors
- [ ] Flyway migrations run successfully
- [ ] All 15 tables visible in database
- [ ] Indexes created
- [ ] Database ready for application

---

## 🎉 You're Ready!

Both guides have everything you need to:

- ✅ Install PostgreSQL
- ✅ Create database
- ✅ Understand schema
- ✅ Run migrations
- ✅ Backup data
- ✅ Troubleshoot issues
- ✅ Optimize performance
- ✅ Secure database

**Start with:** POSTGRESQL_QUICK_GUIDE.md

**For questions:** Check POSTGRESQL_SETUP_GUIDE.md

---

**Created:** January 25, 2026  
**For:** PetVax Care Project  
**Status:** Complete & Ready to Use  
**Version:** 1.0
