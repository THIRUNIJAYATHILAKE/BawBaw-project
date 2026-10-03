# PetVax Care - How to Run

## Prerequisites

### System Requirements

- **Java 17+** (for Spring Boot)
- **Node.js 18+** (for React Native/Expo)
- **PostgreSQL 14+** (database)
- **npm 9+** (package manager)

### Optional but Recommended

- **Android Studio** (for Android emulation)
- **Xcode** (for iOS emulation on macOS)
- **Postman** (for API testing)

---

## 1️⃣ Backend Setup (Spring Boot)

### Step 1: Install PostgreSQL

#### Windows

```bash
# Download from https://www.postgresql.org/download/windows/
# Run installer, remember password for postgres user
# Default port: 5432
```

#### macOS

```bash
brew install postgresql@14
brew services start postgresql@14
```

#### Linux

```bash
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start
```

### Step 2: Create Database

```bash
# Login to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE petvax_care;
CREATE USER petvax_user WITH PASSWORD 'petvax_password';
GRANT ALL PRIVILEGES ON DATABASE petvax_care TO petvax_user;
\q
```

### Step 3: Navigate to Backend Directory

```bash
cd d:\Projects\Petcare\backend
```

### Step 4: Configure Application (Optional)

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/petvax_care
    username: petvax_user
    password: petvax_password # Change if different

  jpa:
    hibernate:
      ddl-auto: validate # Keep as validate, Flyway handles migrations

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-min-32-chars}
  access-token-expiry: 86400000 # 24 hours in ms
  refresh-token-expiry: 2592000000 # 30 days in ms

firebase:
  credentials-path: ${FIREBASE_CREDENTIALS_PATH:path/to/firebase-key.json}

mail:
  smtp:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    from: ${MAIL_FROM:your-email@gmail.com}
```

### Step 5: Build Backend

```bash
# From backend directory
mvn clean package -DskipTests

# Output: target/petvax-care-1.0.0.jar
```

### Step 6: Run Backend

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using JAR
java -jar target/petvax-care-1.0.0.jar

# Expected output:
# Started PetVaxCareApplication in X.XXX seconds
# Server running on http://localhost:8080
```

### ✅ Verify Backend is Running

```bash
# In another terminal, test the API
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## 2️⃣ Frontend Setup (React Native)

### Step 1: Navigate to Frontend Directory

```bash
cd d:\Projects\Petcare\frontend
```

### Step 2: Install Dependencies

```bash
npm install

# This installs 1,334+ packages
# Takes 2-5 minutes depending on internet speed
```

### Step 3: Configure API URL (Optional)

Create `.env` file in frontend root (or skip if using default localhost):

```env
REACT_APP_API_URL=http://localhost:8080/api
```

### Step 4: Start Development Server

```bash
# Option 1: Expo development server (recommended for testing)
npm start

# You'll see:
# ┌─────────────────────────────────────────────────────┐
# │ Starting Expo CLI...                                │
# │ Use 'w' to open web client                          │
# │ Use 'a' to open Android client                      │
# │ Use 'i' to open iOS client                          │
# └─────────────────────────────────────────────────────┘
```

---

## 3️⃣ Run Frontend Options

### Option A: Web Browser (Easiest for Testing)

```bash
# Press 'w' in terminal running npm start
# Opens http://localhost:19006 in browser

# Or manually:
npm run web
```

**Note**: React Native on web has some limitations. Use for quick testing only.

---

### Option B: Android Emulator

#### Prerequisites

- Android Studio installed
- Android SDK configured

```bash
# Start Android emulator first
# In Android Studio: Tools → Device Manager → Start Emulator

# Then run:
npm run android

# Or:
npx react-native run-android
```

---

### Option C: iOS Simulator (macOS only)

```bash
# Requires Xcode installed
npm run ios

# Or:
npx react-native run-ios
```

---

### Option D: Physical Device

#### Android

```bash
# Enable USB Debugging on device
# Connect via USB
npm run android
```

#### iOS

```bash
# Trust your computer on device
npm run ios
```

---

## 4️⃣ Test the Full Stack

### Step 1: Check Backend Health

```bash
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```

### Step 2: Register a User (Backend API)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testowner",
    "email": "test@example.com",
    "password": "TestPass123!",
    "fullName": "Test Owner",
    "phoneNumber": "1234567890",
    "address": "123 Test St",
    "role": "PET_OWNER"
  }'
```

### Step 3: Login (Get Tokens)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testowner",
    "password": "TestPass123!"
  }'

# Response includes: accessToken, refreshToken, userId, role
```

### Step 4: Test Authentication in Frontend

- Start the app (web, Android, or iOS)
- Click "Get Started" → "Pet Owner"
- Register with same credentials
- You should see the Owner Dashboard

---

## 5️⃣ Troubleshooting

### Backend Won't Start

**Error: "Connection refused" on port 5432**

```bash
# PostgreSQL isn't running. Start it:
# Windows: Services → postgresql → Start
# macOS: brew services start postgresql@14
# Linux: sudo service postgresql start
```

**Error: "Database does not exist"**

```bash
# Create the database (see Step 2 above)
psql -U postgres -c "CREATE DATABASE petvax_care;"
```

**Error: "JWT secret not configured"**

```bash
# Set environment variable before running:
# Windows: set JWT_SECRET=your-secret-key-min-32-chars
# Linux/Mac: export JWT_SECRET=your-secret-key-min-32-chars
```

---

### Frontend Won't Start

**Error: "Port 19006 already in use"**

```bash
# Kill the process using port 19006:
# Windows: netstat -ano | findstr :19006, then taskkill /PID <PID>
# Mac/Linux: lsof -i :19006, then kill <PID>

# Or use different port:
npm start -- --port 19007
```

**Error: "Cannot find module..."**

```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

**Error: "API calls fail with 401 Unauthorized"**

```bash
# Tokens might be expired. Clear storage and re-login:
# In browser: DevTools → Application → Storage → Clear All
# In Expo: Settings → Clear all data → Re-login
```

---

## 6️⃣ Quick Start Script (All-in-One)

### Windows PowerShell

```powershell
# Start PostgreSQL (if not already running)
# Windows: Start-Service postgresql-x64-14

# Terminal 1: Backend
cd d:\Projects\Petcare\backend
mvn spring-boot:run

# Terminal 2: Frontend
cd d:\Projects\Petcare\frontend
npm install
npm start
# Press 'w' for web browser
```

### macOS/Linux

```bash
#!/bin/bash

# Start PostgreSQL
brew services start postgresql@14

# Terminal 1: Backend
cd ~/Projects/Petcare/backend
mvn spring-boot:run &

# Wait for backend to start
sleep 10

# Terminal 2: Frontend
cd ~/Projects/Petcare/frontend
npm install
npm start
```

---

## 7️⃣ Environment Variables

Create `.env` file in backend root for production:

```bash
# Backend
JWT_SECRET=your-256-bit-secret-key-here
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-key.json

# Frontend (.env in frontend root)
REACT_APP_API_URL=http://localhost:8080/api
```

---

## 8️⃣ Default Credentials (For Testing)

After running the setup above, test with:

```
Username: testowner
Email: test@example.com
Password: TestPass123!
Role: Pet Owner
```

---

## 9️⃣ Useful Commands

### Backend

```bash
# Build
mvn clean package -DskipTests

# Run with specific port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9000"

# View logs
tail -f target/application.log

# Format code
mvn com.spotify.fmt:fmt-maven-plugin:format
```

### Frontend

```bash
# Type check
npm run type-check

# Lint
npm run lint

# Run tests
npm test

# Clear cache
npm start -- --reset-cache
```

---

## 🔟 Next Steps

1. ✅ Backend running on `http://localhost:8080`
2. ✅ Frontend running on `http://localhost:19006` (web)
3. ✅ Database migrations auto-applied on startup
4. ✅ Ready for development/testing

### To Add Sample Data

```bash
# Use the APIs or SQL script (if available)
# Or register multiple users via auth endpoint
```

### To Deploy

```bash
# See DEPLOYMENT_READY_SUMMARY.md for production setup
```

---

## 📞 Support

- **Backend not running?** Check PostgreSQL and ports
- **Frontend not building?** Clear node_modules and reinstall
- **API calls failing?** Check network tab and backend logs
- **Still stuck?** Check logs in `target/` (backend) or console (frontend)

---

**Happy coding! 🚀**
