# PetVax Care - Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USER DEVICES                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   Android    │  │     iOS      │  │   Web/PC     │              │
│  │   (Expo)     │  │   (Expo)     │  │  (Browser)   │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
│         │                 │                  │                      │
└─────────┼─────────────────┼──────────────────┼──────────────────────┘
          │ HTTP/S          │                  │
          └────────┬────────┴──────────────────┘
                   │
          ┌────────▼────────┐
          │   Expo Server   │
          │  :19006 (web)   │
          │  :8081 (native) │
          └────────┬────────┘
                   │
     ┌─────────────┴──────────────┐
     │ React Native Application   │
     │ - AuthContext              │
     │ - Axios API Client         │
     │ - 12+ Screens              │
     │ - Role-Based Navigation    │
     └─────────────┬──────────────┘
                   │
                   │ REST API Calls (JWT Authenticated)
                   │
        ┌──────────▼───────────────────────────────────┐
        │     Spring Boot 3.2 Application              │
        │     :8080                                     │
        │  ┌────────────────────────────────────────┐  │
        │  │  Controllers (8)                       │  │
        │  │  - AuthController                      │  │
        │  │  - PetController                       │  │
        │  │  - AppointmentController               │  │
        │  │  - DoctorController                    │  │
        │  │  - AdminController (NEW)               │  │
        │  │  - AssistantController (NEW)           │  │
        │  └────────────────────────────────────────┘  │
        │                   │                            │
        │  ┌────────────────▼────────────────────────┐  │
        │  │  Services (8)                          │  │
        │  │  - AuthService                         │  │
        │  │  - PetService                          │  │
        │  │  - AppointmentService                  │  │
        │  │  - VaccinationService                  │  │
        │  │  - DoctorService                       │  │
        │  │  - AdminUserManagementService (NEW)    │  │
        │  │  - AdminScheduleManagementService (NEW)│  │
        │  │  - AssistantScheduleService (NEW)      │  │
        │  │  - EmailService                        │  │
        │  │  - NotificationService                 │  │
        │  └────────────────────────────────────────┘  │
        │                   │                            │
        │  ┌────────────────▼────────────────────────┐  │
        │  │  Spring Data JPA (11 Repositories)     │  │
        │  │  - UserRepository                      │  │
        │  │  - PetRepository                       │  │
        │  │  - AppointmentRepository               │  │
        │  │  - DoctorBlockedSlotRepository (NEW)   │  │
        │  │  - ... (7 more)                        │  │
        │  └────────────────────────────────────────┘  │
        └──────────────┬───────────────────────────────┘
                       │
        ┌──────────────┴───────────────────────────────┐
        │                                               │
   ┌────▼──────┐                              ┌────────▼────┐
   │PostgreSQL │                              │Firebase FCM │
   │ :5432     │                              │ (Cloud)     │
   │           │                              │             │
   │ Tables:   │                              │ Push        │
   │ - users   │  Flyway Migrations (V1-V4)   │ Notifs      │
   │ - pets    │◄──────────────────────────┐  │             │
   │ - appts   │                            │  └─────────────┘
   │ - medics  │                    ┌───────┴──┐
   │ - vaccs   │                    │ Scheduler│
   │ - doctors │                    │ (Hourly) │
   │ - ...     │                    └──────────┘
   └───────────┘

                    ┌──────────────┐
                    │ Email Service│
                    │  (SMTP)      │
                    │  Gmail/etc   │
                    └──────────────┘
```

---

## Data Flow Diagrams

### 1. User Registration & Login Flow

```
┌─────────────────┐
│  User Opens App │
└────────┬────────┘
         │
         ▼
    ┌─────────────────────────────────┐
    │ SplashScreen → RoleSelection    │
    │ (Get Started)                   │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │   RegisterScreen                │
    │   - Enter details               │
    │   - Password strength check     │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  POST /api/auth/register        │
    │  (AuthController)               │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  AuthService.registerUser()     │
    │  - Validate input               │
    │  - Hash password (BCrypt)       │
    │  - Check username uniqueness    │
    │  - Create User entity           │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  UserRepository.save(user)      │
    │  (Database Insert)              │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  EmailService.sendVerification()│
    │  (Async, OTP sent)              │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  Response: User created,        │
    │  email verification sent        │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │   LoginScreen                   │
    │   - Enter credentials           │
    │   - Account lockout check       │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  POST /api/auth/login           │
    │  (AuthController)               │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  AuthService.login()            │
    │  - Verify credentials           │
    │  - Check account lockout        │
    │  - Verify password (BCrypt)     │
    │  - Generate JWT tokens          │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  Response: accessToken,         │
    │  refreshToken, userId, role     │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  AuthContext.login()            │
    │  - Store tokens (AsyncStorage)  │
    │  - Store user info              │
    │  - Set isSignedIn = true        │
    └──────────────┬──────────────────┘
                   │
    ┌──────────────▼──────────────────┐
    │  RootNavigator redirects to     │
    │  role-based dashboard           │
    │  (OwnerDashboard, etc)          │
    └──────────────────────────────────┘
```

---

### 2. Appointment Booking Flow (Owner)

```
┌──────────────────────────┐
│  OwnerDashboard          │
│  "Book Appointment" btn  │
└──────────────┬───────────┘
               │
┌──────────────▼──────────────────┐
│  BookAppointmentScreen          │
│  Step 1: Select Doctor          │
└──────────────┬──────────────────┘
               │
    ┌──────────▼──────────────┐
    │ GET /api/doctor/list    │
    │ (DoctorController)      │
    │ with filtering          │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │ Show doctor list        │
    │ with availability       │
    └──────────┬──────────────┘
               │
┌──────────────▼──────────────────┐
│  Step 2: Select Date & Time     │
│  - CalendarPicker               │
│  - Available slots              │
└──────────────┬──────────────────┘
               │
    ┌──────────▼──────────────┐
    │ GET /api/doctor/        │
    │ {doctorId}/availability │
    │ (DoctorController)      │
    └──────────┬──────────────┘
               │
┌──────────────▼──────────────────┐
│  Step 3: Select Pet             │
│  - Dropdown of user's pets      │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│  Step 4: Confirmation           │
│  - Review all details           │
│  - "Book Appointment" btn       │
└──────────────┬──────────────────┘
               │
    ┌──────────▼──────────────┐
    │ POST /api/appointments  │
    │ (AppointmentController) │
    │ with validation         │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │AppointmentService.book()│
    │ - Check no conflicts    │
    │ - Prevent double-book   │
    │ - Create appointment    │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │AppointmentRepository    │
    │.save(appointment)       │
    │(Database Insert)        │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │NotificationService      │
    │.notify(doctor, app)     │
    │(FCM + Email)            │
    └──────────┬──────────────┘
               │
┌──────────────▼──────────────────┐
│  Show Success Message           │
│  Appointment booked!            │
│  Confirmation email sent        │
└──────────────────────────────────┘
```

---

### 3. Admin Verification Flow

```
┌──────────────────────────────┐
│  AdminDashboard              │
│  "Pending Verifications" (5) │
└──────────────┬───────────────┘
               │
    ┌──────────▼────────────────┐
    │ GET /api/admin/pending-   │
    │ verifications             │
    │ (AdminController)         │
    └──────────┬────────────────┘
               │
    ┌──────────▼────────────────┐
    │ Returns list of doctors   │
    │ & assistants awaiting OK  │
    └──────────┬────────────────┘
               │
┌──────────────▼────────────────────┐
│  Show PendingUserCard             │
│  - Name, Email, Role, License     │
│  [Approve] [Reject] buttons       │
└──────────────┬────────────────────┘
               │
               ├─ Admin clicks Approve
               │
    ┌──────────▼────────────────┐
    │ POST /api/admin/verify-   │
    │ doctor/{doctorId}         │
    │ ?status=APPROVED          │
    │ (AdminController)         │
    └──────────┬────────────────┘
               │
    ┌──────────▼────────────────┐
    │AdminUserManagementService │
    │.verifyDoctor()            │
    │ - Set status = ACTIVE     │
    │ - Create DoctorProfile    │
    │ - Log audit trail         │
    └──────────┬────────────────┘
               │
    ┌──────────▼────────────────┐
    │ UserRepository.save()     │
    │ (Database Update)         │
    └──────────┬────────────────┘
               │
    ┌──────────▼────────────────┐
    │NotificationService        │
    │.sendPushNotification()    │
    │ "Your account approved"   │
    └──────────┬────────────────┘
               │
┌──────────────▼────────────────────┐
│  Success! Doctor can now login    │
│  and access doctor features       │
└────────────────────────────────────┘
```

---

### 4. Assistant Check-in/Check-out Flow

```
┌──────────────────────────┐
│  AssistantDashboard      │
│  Today's Schedule (8)    │
└──────────────┬───────────┘
               │
    ┌──────────▼──────────────┐
    │ GET /api/assistant/     │
    │ schedule/today          │
    │ (AssistantController)   │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │ Shows list:             │
    │ - Pet: Fluffy           │
    │ - Owner: John           │
    │ - Time: 2:00 PM         │
    │ - Status: Pending       │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │ [Check In] button       │
    └──────────┬──────────────┘
               │
    ┌──────────▼────────────────────┐
    │ POST /api/assistant/          │
    │ appointment/{id}/check-in     │
    │ (AssistantController)         │
    └──────────┬────────────────────┘
               │
    ┌──────────▼────────────────────┐
    │AssistantScheduleService       │
    │.markCheckIn()                 │
    │ - Set checkedInAt = now()     │
    │ - Validate not already in     │
    └──────────┬────────────────────┘
               │
    ┌──────────▼────────────────────┐
    │ AppointmentRepository.        │
    │ save(appointment)             │
    │ (Database Update)             │
    └──────────┬────────────────────┘
               │
    ┌──────────▼────────────────────┐
    │ Status changes:               │
    │ ✓ Checked In                  │
    │ [Check Out] button now shows  │
    └──────────┬────────────────────┘
               │
    ┌──────────▼────────────────────┐
    │ POST /api/assistant/          │
    │ appointment/{id}/check-out    │
    │ (AssistantController)         │
    └──────────┬────────────────────┘
               │
    ┌──────────▼────────────────────┐
    │AssistantScheduleService       │
    │.markCheckOut()                │
    │ - Set checkedOutAt = now()    │
    │ - Set status = COMPLETED      │
    └──────────┬────────────────────┘
               │
    ┌──────────▼────────────────────┐
    │ Status changes:               │
    │ ✓ Checked Out                 │
    │ Appointment completed!        │
    └────────────────────────────────┘
```

---

## Database Schema (Key Tables)

```
┌──────────────┐          ┌──────────────┐
│    USERS     │          │  DOCTOR_     │
├──────────────┤          │ PROFILES     │
│ id (PK)      │◄────────►├──────────────┤
│ username     │   1:1    │ id (PK)      │
│ email        │          │ user_id (FK) │
│ password     │          │ license_id   │
│ full_name    │          │ specialty    │
│ role         │          │ verified_at  │
│ account_      │          │ verified_by  │
│  status      │          │ verified_at  │
│ soft_deleted │          └──────────────┘
│ locked_until │
└──────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐
│    PETS      │
├──────────────┤
│ id (PK)      │
│ owner_id(FK) │
│ name         │
│ species      │
│ breed        │
│ dob          │
│ weight       │
│ photo_url    │
└──────────────┘
       │
       │ 1:N
       ▼
┌──────────────────┐      ┌──────────────────┐
│  APPOINTMENTS    │      │  MEDICAL_        │
├──────────────────┤      │  RECORDS         │
│ id (PK)          │      ├──────────────────┤
│ pet_id (FK)      │◄────►│ id (PK)          │
│ doctor_id (FK)   │      │ pet_id (FK)      │
│ owner_id (FK)    │      │ doctor_id (FK)   │
│ appointment_     │      │ diagnosis        │
│  date           │      │ treatment        │
│ duration_        │      │ medications      │
│  minutes        │      │ created_at       │
│ purpose         │      │ next_due_date    │
│ status          │      └──────────────────┘
│ checked_in_at   │
│ checked_out_at  │
│ created_at      │
└──────────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐       ┌──────────────┐
│ VACCINATIONS │       │ DEWORMING_   │
├──────────────┤       │ RECORDS      │
│ id (PK)      │       ├──────────────┤
│ pet_id (FK)  │       │ id (PK)      │
│ vaccine_name │       │ pet_id (FK)  │
│ date_given   │       │ date_admin   │
│ next_due     │       │ medicine     │
│ lot_number   │       │ dose         │
│ administered_│       │ next_due     │
│  by (FK)     │       │ created_at   │
│ created_at   │       └──────────────┘
└──────────────┘

┌────────────────────┐    ┌──────────────────┐
│    REMINDERS       │    │  DEVICE_TOKENS   │
├────────────────────┤    ├──────────────────┤
│ id (PK)            │    │ id (PK)          │
│ user_id (FK)       │    │ user_id (FK)     │
│ reminder_type      │    │ device_token     │
│ scheduled_date     │    │ device_type      │
│ sent               │    │ registered_at    │
│ created_at         │    │ last_used_at     │
└────────────────────┘    └──────────────────┘
```

---

## API Endpoints Summary

### Authentication (8 endpoints)

- POST /auth/register
- POST /auth/login
- POST /auth/refresh-token
- POST /auth/verify-email
- POST /auth/forgot-password
- POST /auth/reset-password
- GET /auth/logout

### Pets (6 endpoints)

- GET /pets
- GET /pets/{id}
- POST /pets
- PUT /pets/{id}
- DELETE /pets/{id}
- POST /pets/{id}/photo

### Appointments (9 endpoints)

- GET /appointments
- GET /appointments/{id}
- POST /appointments
- PUT /appointments/{id}
- DELETE /appointments/{id}
- POST /appointments/{id}/complete
- POST /appointments/{id}/no-show
- GET /doctor/appointments

### Doctor Features (10 endpoints)

- GET /doctor/pets/search
- GET /doctor/medical-records/{petId}
- POST /doctor/medical-records
- PUT /doctor/medical-records/{id}
- GET /doctor/schedule
- POST /doctor/schedule

### **Admin Features (12+ endpoints)**

- GET /admin/users
- GET /admin/pending-verifications
- POST /admin/verify-doctor/{id}
- POST /admin/verify-assistant/{id}
- POST /admin/suspend-user/{id}
- POST /admin/enable-user/{id}
- DELETE /admin/users/{id}
- POST /admin/add-doctor
- POST /admin/add-assistant
- POST /admin/change-role/{id}
- GET /admin/appointments
- POST /admin/block-slot

### **Assistant Features (6 endpoints)**

- GET /assistant/schedule/today
- GET /assistant/schedule
- POST /assistant/appointment/{id}/check-in
- POST /assistant/appointment/{id}/check-out
- GET /assistant/appointment/{id}/status
- PUT /assistant/appointment/{id}/time
- GET /assistant/schedule/summary

---

## Security Architecture

```
                    ┌─────────────────────┐
                    │   Client Request    │
                    │  with JWT Token     │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │   JwtAuthFilter     │
                    │ - Extract token     │
                    │ - Validate sig      │
                    │ - Check expiry      │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ SecurityContext     │
                    │ - Set Principal     │
                    │ - Store userId      │
                    │ - Store role        │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ @PreAuthorize       │
                    │ - Check roles       │
                    │ - Verify access     │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Controller Method  │
                    │ - Process request   │
                    │ - Return response   │
                    └────────────────────┘
```

---

This architecture diagram shows how all components work together! 🚀
