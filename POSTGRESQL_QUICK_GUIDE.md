# PostgreSQL Quick Setup (Visual Guide)

## 🚀 Fastest Way to Setup (5 Minutes)

### Step 1: Download & Install (2 mins)

**Windows:**

```
Visit: https://www.postgresql.org/download/windows/
↓
Click "Download the installer"
↓
Choose PostgreSQL 14 or 15
↓
Run .exe → Keep clicking "Next"
↓
When asked for password, enter: postgres
↓
Accept port 5432 (default)
↓
Finish ✅
```

**macOS:**

```
brew install postgresql@14
brew services start postgresql@14
```

**Linux:**

```
sudo apt install postgresql
sudo systemctl start postgresql
```

---

### Step 2: Create Database (2 mins)

Open PowerShell (Windows) or Terminal (Mac/Linux):

```bash
# Connect to PostgreSQL
psql -U postgres

# You'll be asked for password (enter: postgres)
```

Then paste these commands:

```sql
CREATE DATABASE petvax_care;

CREATE USER petvax_user WITH PASSWORD 'petvax_password';

GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO petvax_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO petvax_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO petvax_user;

\q
```

### Step 3: Verify Connection (1 min)

```bash
# Test connection as petvax_user
psql -U petvax_user -d petvax_care -h localhost

# You should see: petvax_care=>

# Type \q to exit
\q
```

✅ **Done! Database is ready.**

---

## 📊 Database Structure (What Gets Created)

### 15 Tables

```
USERS
 ├─ Stores all user accounts
 ├─ Fields: email, username, password, role, verification_status
 └─ Indexes: email, username (for fast lookup)

PETS
 ├─ Pet information
 ├─ Linked to: users (owner)
 └─ Fields: name, species, breed, age, weight

APPOINTMENTS
 ├─ Booking information
 ├─ Linked to: users (doctor), users (assistant), pets
 └─ Fields: time, status, purpose, check_in_time, check_out_time

VACCINATION_RECORD
 ├─ Vaccine history
 ├─ Linked to: pets
 └─ Fields: vaccine_name, date, due_date, administered_by

DEWORMING_RECORD
 ├─ Deworming history
 ├─ Linked to: pets
 └─ Fields: medicine, dose, date, next_due_date

MEDICAL_RECORD
 ├─ Medical/surgical history
 ├─ Linked to: pets
 └─ Fields: diagnosis, treatment, date, attending_doctor

DOCTOR_PROFILE
 ├─ Doctor details
 ├─ Linked to: users
 └─ Fields: license_id, specialization, experience

NOTIFICATION
 ├─ Sent notifications
 ├─ Linked to: users
 └─ Fields: title, message, type, status

... and 7 more support tables

```

---

## ⚡ Key Commands (Copy-Paste)

### Check If PostgreSQL Is Running

```bash
# Windows
Get-Service postgresql-x64-14 | Select Status

# macOS
brew services list | grep postgresql

# Linux
sudo systemctl status postgresql
```

### Start PostgreSQL

```bash
# Windows
Start-Service postgresql-x64-14

# macOS
brew services start postgresql@14

# Linux
sudo systemctl start postgresql
```

### Connect to Database

```bash
# As admin (postgres)
psql -U postgres

# As app user
psql -U petvax_user -d petvax_care

# From specific host/port
psql -U petvax_user -h localhost -p 5432 -d petvax_care
```

### List All Databases

```bash
psql -U postgres

# Then in psql:
\l

# Output:
#     Name     | Owner | Encoding
# ─────────────┼───────┼──────────
#  postgres    | admin | UTF8
#  petvax_care | admin | UTF8
```

### List All Tables

```bash
# Connect to database first
psql -U petvax_user -d petvax_care

# Then:
\dt

# Output shows all 15 tables
```

### Check Table Rows

```sql
SELECT table_name,
       (SELECT count(*) FROM information_schema.tables t2
        WHERE t2.table_name = t1.table_name) as row_count
FROM information_schema.tables t1
WHERE table_schema = 'public'
ORDER BY table_name;
```

---

## 🔧 Common Tasks

### Create a Table

```sql
CREATE TABLE example (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Insert Data

```sql
INSERT INTO example (name) VALUES ('Test');
```

### Query Data

```sql
SELECT * FROM example;
```

### Update Data

```sql
UPDATE example SET name = 'Updated' WHERE id = 1;
```

### Delete Data

```sql
DELETE FROM example WHERE id = 1;
```

### Drop Table

```sql
DROP TABLE example;
```

---

## 💾 Backup & Restore

### Quick Backup

```bash
# Full backup
pg_dump -U petvax_user -d petvax_care -F c -f backup.dump

# Or SQL format (readable)
pg_dump -U petvax_user -d petvax_care > backup.sql
```

### Quick Restore

```bash
# From dump file
pg_restore -U petvax_user -d petvax_care backup.dump

# From SQL file
psql -U petvax_user -d petvax_care < backup.sql
```

---

## 🐛 Troubleshooting

| Problem                      | Solution                                                              |
| ---------------------------- | --------------------------------------------------------------------- |
| **"Could not connect"**      | `Start-Service postgresql-x64-14` (Windows)                           |
| **"Port 5432 in use"**       | Another PostgreSQL instance running. Kill it or use different port.   |
| **"User doesn't exist"**     | Create user with: `CREATE USER petvax_user WITH PASSWORD 'password';` |
| **"Database doesn't exist"** | Create database with: `CREATE DATABASE petvax_care;`                  |
| **"Permission denied"**      | Run: `GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;`   |
| **"psql command not found"** | Add PostgreSQL bin folder to PATH                                     |

---

## 📈 Check Database Health

```sql
-- Database size
SELECT pg_size_pretty(pg_database_size('petvax_care'));

-- Table count
SELECT count(*) FROM information_schema.tables
WHERE table_schema = 'public';

-- Active connections
SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;

-- Recently created tables
SELECT tablename, pg_size_pretty(pg_total_relation_size('public.'||tablename))
FROM pg_tables WHERE schemaname = 'public'
ORDER BY pg_total_relation_size('public.'||tablename) DESC;
```

---

## 🎯 Next Steps

1. **Install PostgreSQL** (see Step 1 above)
2. **Create Database** (see Step 2 above)
3. **Verify Connection** (see Step 3 above)
4. **Start Spring Boot** - migrations run automatically:
   ```bash
   cd d:\Projects\Petcare\backend
   mvn spring-boot:run
   ```
5. **Check Tables Created**:
   ```bash
   psql -U petvax_user -d petvax_care -c "\dt"
   ```

---

## 📚 Learn More

### PostgreSQL Commands Reference

| Command            | What It Does         |
| ------------------ | -------------------- |
| `\l`               | List all databases   |
| `\c database_name` | Connect to database  |
| `\dt`              | List all tables      |
| `\d table_name`    | Show table structure |
| `\du`              | List all users       |
| `\h`               | PostgreSQL help      |
| `\?`               | psql help            |
| `\q`               | Exit psql            |

### Useful Queries

```sql
-- Count rows in all tables
SELECT schemaname, tablename, n_live_tup
FROM pg_stat_user_tables
WHERE schemaname = 'public';

-- Find database size
SELECT
    schemaname,
    COUNT(*) as table_count,
    pg_size_pretty(SUM(pg_total_relation_size(schemaname||'.'||tablename))) as total_size
FROM pg_tables
WHERE schemaname = 'public'
GROUP BY schemaname;

-- List all indexes
SELECT tablename, indexname FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename;
```

---

## ✨ You're Ready!

**Your PostgreSQL database is set up and ready for:**

- ✅ Spring Boot backend to connect
- ✅ Automatic migrations to run
- ✅ PetVax Care application to work
- ✅ Storing user, pet, and appointment data

**Time taken:** ~15 minutes (including download time)

**What happened:**

- PostgreSQL server installed and running
- Database `petvax_care` created
- User `petvax_user` created with password
- All permissions set up
- Ready for application to use

---

For detailed information, see: **POSTGRESQL_SETUP_GUIDE.md**

**Happy database building! 🎉**
