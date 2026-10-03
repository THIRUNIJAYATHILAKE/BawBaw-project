export const APP_CONSTANTS = {
  APP_NAME: 'PetVax Care',
  APP_VERSION: '1.0.0',
  TIMEOUT: {
    API: 30000,
    NOTIFICATION: 5000,
    CACHE: 3600000, // 1 hour
  },
  LIMITS: {
    MAX_PETS: 10,
    MAX_APPOINTMENTS: 100,
    MAX_UPLOAD_SIZE: 10 * 1024 * 1024, // 10MB
    MAX_RETRIES: 3,
  },
  API: {
    BASE_URL: process.env.API_BASE_URL || 'http://localhost:3000/api',
    TIMEOUT: parseInt(process.env.API_TIMEOUT || '30000', 10),
  },
  STORAGE_KEYS: {
    ACCESS_TOKEN: 'accessToken',
    REFRESH_TOKEN: 'refreshToken',
    USER: 'user',
    PETS: 'pets',
    APPOINTMENTS: 'appointments',
  },
  ROLES: {
    ADMIN: 'admin',
    DOCTOR: 'doctor',
    OWNER: 'owner',
  },
  APPOINTMENT_STATUS: {
    SCHEDULED: 'scheduled',
    COMPLETED: 'completed',
    CANCELLED: 'cancelled',
    PENDING: 'pending',
  },
  VACCINATION_STATUS: {
    PENDING: 'pending',
    COMPLETED: 'completed',
    OVERDUE: 'overdue',
  },
};
