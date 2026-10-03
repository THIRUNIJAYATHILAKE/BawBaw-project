# SQL Queries - Quick Access Guide

## 📚 Files Created (66 KB Total)

| File                           | Size  | Purpose                                        |
| ------------------------------ | ----- | ---------------------------------------------- |
| **SQL_QUERIES_COMPLETE.md**    | 32 KB | Detailed guide with all queries & explanations |
| **SQL_QUERIES_EXECUTABLE.sql** | 24 KB | Copy-paste ready SQL script                    |
| **SQL_QUERIES_REFERENCE.md**   | 10 KB | Quick lookup & common queries                  |

---

## ⚡ Get Started in 30 Seconds

```bash
# Run this one command:
psql -U petvax_user -d petvax_care -f SQL_QUERIES_EXECUTABLE.sql

# Done! All 15 tables created with sample data ✅
```

---

## 📊 What You Get

### 15 Tables

- users, pets, appointments, medical_records
- vaccinations, deworming_records, doctor_profiles, doctor_schedules
- refresh_tokens, device_tokens, reminders, audit_logs
- clinic_configuration, doctor_blocked_slots
- (Plus 30+ performance indexes)

### 10+ Sample Users

- 1 Admin
- 3 Pet Owners
- 3 Doctors
- 2 Assistants

### 4 Sample Pets with Full History

- Max (Dog)
- Whiskers (Cat)
- Buddy (Dog)
- Mittens (Cat)

### Complete CRUD Examples

- Create, Read, Update, Delete operations
- Advanced queries included
- Troubleshooting tips

---

## 📖 Which File to Read?

| Need                 | Read                                                   |
| -------------------- | ------------------------------------------------------ |
| **Just run it**      | SQL_QUERIES_EXECUTABLE.sql                             |
| **Quick reference**  | SQL_QUERIES_REFERENCE.md                               |
| **Learn everything** | SQL_QUERIES_COMPLETE.md                                |
| **All 3**            | Start with Reference → then Complete → then Executable |

---

## 🎯 3-Step Setup

1. **Copy-paste SQL_QUERIES_EXECUTABLE.sql into psql** (or run via command)
2. **Verify**: `SELECT COUNT(*) FROM users;` (should show: 10)
3. **Done!** All tables, data, and indexes created

---

## 🔑 Test Credentials

```
Admin Username: admin
Admin Email: admin@petvaxcare.com
Database User: petvax_user
Database Password: petvax_password
```

---

**Everything is ready! All 3 files are in:** `d:\Projects\Petcare\`
