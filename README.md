# PetVax Care - Pet Vaccination Management System

A comprehensive mobile and web-based platform for managing pet vaccinations, appointments, and medical records.

## Project Structure

```
PetVax Care/
├── backend/                 # Spring Boot REST API
│   ├── src/main/java/com/petvax/care/
│   │   ├── config/         # Spring configuration
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Database access
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   ├── exception/      # Custom exceptions
│   │   ├── security/       # JWT & security
│   │   └── util/           # Utilities
│   ├── src/main/resources/
│   │   ├── db/migration/   # Flyway SQL migrations
│   │   └── application.yml # Spring config
│   └── pom.xml             # Maven dependencies
│
├── frontend/                # React Native mobile app
│   ├── src/
│   │   ├── screens/        # Screen components by role
│   │   ├── navigation/     # Navigation setup
│   │   ├── components/     # Reusable components
│   │   ├── services/       # API calls
│   │   ├── context/        # State management
│   │   ├── utils/          # Utilities
│   │   ├── assets/         # Images & fonts
│   │   ├── App.tsx         # Root component
│   │   └── index.tsx       # Entry point
│   ├── package.json        # NPM dependencies
│   └── tsconfig.json       # TypeScript config
│
└── README.md
```

## Technology Stack

### Backend

- **Java 17** with Spring Boot 3.2.0
- **PostgreSQL 14+** for data persistence
- **Flyway** for database migrations
- **JWT (jjwt)** for authentication
- **Spring Security** for authorization
- **Firebase Admin SDK** for push notifications
- **Maven** for dependency management

### Frontend

- **React Native** with TypeScript
- **Expo** for easier development
- **Zustand** for state management
- **Axios** for API calls
- **React Navigation** for routing
- **Native Base** for UI components

## Prerequisites

### Backend

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Frontend

- Node.js 18+
- npm 9+ or yarn
- Expo CLI

## Setup Instructions

### Backend Setup

1. **Navigate to backend directory:**

   ```bash
   cd backend
   ```

2. **Configure database:**
   - Create PostgreSQL database:
     ```sql
     CREATE DATABASE petvax_care;
     ```
   - Update `application.yml` with your database credentials

3. **Set environment variables:**

   ```bash
   export JWT_SECRET="your-long-secret-key-at-least-256-bits"
   export MAIL_HOST="smtp.gmail.com"
   export MAIL_USERNAME="your-email@gmail.com"
   export MAIL_PASSWORD="your-app-password"
   export FIREBASE_CREDENTIALS_PATH="/path/to/firebase-key.json"
   ```

4. **Build the project:**

   ```bash
   mvn clean install
   ```

5. **Run the application:**

   ```bash
   mvn spring-boot:run
   ```

   Backend will start on `http://localhost:8080/api`

### Frontend Setup

1. **Navigate to frontend directory:**

   ```bash
   cd frontend
   ```

2. **Install dependencies:**

   ```bash
   npm install
   ```

3. **Create .env file:**

   ```
   REACT_APP_API_URL=http://localhost:8080/api
   ```

4. **Start the development server:**

   ```bash
   npm start
   ```

   For Android:

   ```bash
   npm run android
   ```

   For iOS:

   ```bash
   npm run ios
   ```

## API Endpoints (Phase 2)

### Authentication

- `POST /auth/register` - Register new user
- `POST /auth/login` - Login user
- `POST /auth/refresh-token` - Refresh access token
- `POST /auth/forgot-password` - Request password reset
- `POST /auth/reset-password` - Reset password with OTP

### User Management

- `GET /users/profile` - Get current user profile
- `PUT /users/profile` - Update user profile
- `POST /users/change-password` - Change password

### Pet Owner Endpoints

- `GET /owner/pets` - List user's pets
- `POST /owner/pets` - Create new pet
- `GET /owner/pets/{id}` - Get pet details
- `PUT /owner/pets/{id}` - Update pet
- `DELETE /owner/pets/{id}` - Delete pet
- `GET /owner/appointments` - List appointments
- `POST /owner/appointments` - Book appointment
- `POST /owner/appointments/{id}/cancel` - Cancel appointment

### Doctor Endpoints

- `GET /doctor/schedule` - Get doctor's schedule
- `GET /doctor/appointments` - List appointments
- `POST /doctor/patients/{id}/diagnose` - Record diagnosis

### Admin Endpoints

- `GET /admin/users` - List all users
- `POST /admin/users/{id}/verify-doctor` - Verify doctor
- `POST /admin/users/{id}/suspend` - Suspend user

## Database Schema

Key tables created by Flyway migrations:

- `users` - User accounts (PetOwner, Doctor, Assistant, Admin)
- `doctor_profiles` - Doctor-specific information
- `pets` - Pet records linked to owners
- `appointments` - Appointment bookings
- `medical_records` - Vaccination, deworming, surgery records
- `vaccinations` - Detailed vaccination records
- `deworming_records` - Detailed deworming records
- `refresh_tokens` - JWT refresh token storage
- `device_tokens` - FCM push notification tokens
- `reminders` - Scheduled reminders

## Implementation Phases

✅ **Phase 1: Project Setup & Database Layer** (Complete)

- Spring Boot project initialization
- PostgreSQL schema with Flyway
- JPA entity models
- JWT authentication utilities
- React Native project setup
- API client service

🔄 **Phase 2: Authentication & User Management** (In Progress)

- User registration APIs
- Login with rate limiting
- Password reset with OTP
- Token refresh mechanism
- Role-based authorization

📋 **Phase 3: Onboarding & Frontend Auth UI**

- Welcome/splash screen
- Registration forms
- Login screen
- Forgot password flow

📋 **Phase 4: Core Features**

- Pet management APIs
- Appointment booking system
- Vaccination/deworming tracking
- Medical history documentation

📋 **Phase 5-8: Advanced Features & Testing**

- Push notifications
- Admin dashboard
- Testing & security hardening

## Security Measures

- ✅ JWT tokens (24h access, 30d refresh)
- ✅ BCrypt password hashing (cost 12)
- ✅ HTTPS/TLS required
- ✅ CORS configuration
- ✅ Role-based access control (RBAC)
- ✅ Account lockout after 5 failed attempts
- ✅ Audit logging
- ✅ Input validation

## Next Steps

1. Implement user registration endpoint
2. Implement login endpoint with lockout logic
3. Create password reset functionality
4. Build React Native auth screens
5. Implement pet management APIs

## Contributing

Follow these conventions:

- Use meaningful commit messages with Co-authored-by trailer
- Keep commits atomic and focused
- Write tests for new features
- Run linters before committing

## License

Proprietary - PetVax Care 2026
