# PetVax Care - Quick Start (5 Minutes)

## TL;DR - Run Everything NOW

### Prerequisites (Install Once)

- Java 17+ (download from oracle.com or use `choco install openjdk`)
- PostgreSQL 14+ (download from postgresql.org)
- Node.js 18+ (download from nodejs.org)

### 1️⃣ Setup Database (Windows PowerShell)

```powershell
# Start PostgreSQL service
Start-Service postgresql-x64-14

# Connect and create database
psql -U postgres

# In psql prompt, paste:
CREATE DATABASE petvax_care;
CREATE USER petvax_user WITH PASSWORD 'petvax_password';
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;
\q
```

### 2️⃣ Start Backend (Terminal 1)

```bash
cd d:\Projects\Petcare\backend
mvn spring-boot:run
```

✅ **Wait for**: `Started PetVaxCareApplication in X seconds`

### 3️⃣ Start Frontend (Terminal 2)

```bash
cd d:\Projects\Petcare\frontend
npm install
npm start
```

✅ **Wait for**: Interactive Expo prompt

### 4️⃣ Open the App

Press **`w`** in the frontend terminal to open in web browser

Or use:

- **`a`** → Android emulator (requires Android Studio)
- **`i`** → iOS simulator (macOS only)

---

## Test Credentials

After both are running, test login:

```
Username: testuser
Password: Test@1234
```

Or register a new account:

1. Click "Get Started"
2. Select role (Pet Owner)
3. Fill form and register
4. Check console for verification (auto-verified in dev mode)
5. Login

---

## Verify Everything Works

### Backend Test

```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### Frontend Test

- Dashboard should load
- Can see "My Pets" section
- Can navigate between screens

---

## Common Issues & Fixes

| Issue                           | Fix                                               |
| ------------------------------- | ------------------------------------------------- |
| `PostgreSQL connection refused` | `Start-Service postgresql-x64-14`                 |
| `Port 8080 in use`              | `netstat -ano \| findstr :8080` then kill process |
| `Port 19006 in use`             | Kill the process or use different port            |
| `npm install fails`             | `rm -r node_modules package-lock.json` then retry |
| `Build fails`                   | `mvn clean install` (not package)                 |
| `Login doesn't work`            | Check backend logs for errors                     |

---

## What's Running

```
Frontend  → http://localhost:19006  (web browser)
Backend   → http://localhost:8080   (API server)
Database  → localhost:5432          (PostgreSQL)
```

---

## Stop Everything

```bash
# Terminal 1 (Backend): Ctrl+C
# Terminal 2 (Frontend): Ctrl+C
# PostgreSQL: Stop-Service postgresql-x64-14
```

---

## Next Steps

- 📖 Read `HOW_TO_RUN.md` for detailed instructions
- 🏗️ Read `ARCHITECTURE.md` for system design
- 🔍 Check API endpoints in API_REFERENCE.md
- 📱 Explore frontend screens in src/screens/

---

## For macOS/Linux Users

Replace Windows commands:

```bash
# PostgreSQL
brew services start postgresql@14

# Kill process
lsof -i :PORT_NUMBER
kill -9 PID

# Environment variables
export JWT_SECRET=your-secret
```

---

**You're all set! Happy coding 🚀**

Questions? Check the detailed guides in the docs folder.
