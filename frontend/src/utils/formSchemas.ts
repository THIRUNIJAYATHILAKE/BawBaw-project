import * as Yup from 'yup';

// Login Schema
export const loginSchema = Yup.object().shape({
  usernameOrEmail: Yup.string()
    .required('Username or email is required')
    .min(3, 'Must be at least 3 characters'),
  password: Yup.string()
    .required('Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

// Register Schema
export const registerSchema = Yup.object().shape({
  role: Yup.string()
    .required('Please select a role')
    .oneOf(['PET_OWNER', 'DOCTOR', 'ASSISTANT'], 'Invalid role'),
  fullName: Yup.string()
    .required('Full name is required')
    .min(3, 'Full name must be at least 3 characters')
    .max(100, 'Full name must be at most 100 characters'),
  email: Yup.string()
    .required('Email is required')
    .email('Invalid email format'),
  phoneNumber: Yup.string()
    .required('Phone number is required')
    .matches(/^[\d\s\-+()]{7,}$/, 'Invalid phone number'),
  address: Yup.string()
    .required('Address is required')
    .min(10, 'Address must be at least 10 characters'),
  dateOfBirth: Yup.date()
    .required('Date of birth is required')
    .max(new Date(), 'Date of birth cannot be in the future'),
  username: Yup.string()
    .required('Username is required')
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must be at most 50 characters')
    .matches(/^[a-zA-Z0-9_]+$/, 'Username can only contain letters, numbers, and underscores'),
  password: Yup.string()
    .required('Password is required')
    .min(8, 'Password must be at least 8 characters')
    .matches(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .matches(/[a-z]/, 'Password must contain at least one lowercase letter')
    .matches(/[0-9]/, 'Password must contain at least one number')
    .matches(/[@$!%*?&]/, 'Password must contain at least one special character (@$!%*?&)'),
  confirmPassword: Yup.string()
    .required('Please confirm your password')
    .oneOf([Yup.ref('password')], 'Passwords must match'),
  // Doctor-specific fields
  licenseId: Yup.string().when('role', {
    is: 'DOCTOR',
    then: (schema) =>
      schema
        .required('License ID is required')
        .min(5, 'License ID must be at least 5 characters'),
    otherwise: (schema) => schema.notRequired(),
  }),
  specialization: Yup.string().when('role', {
    is: 'DOCTOR',
    then: (schema) =>
      schema
        .required('Specialization is required')
        .min(3, 'Specialization must be at least 3 characters'),
    otherwise: (schema) => schema.notRequired(),
  }),
  yearsOfExperience: Yup.number().when('role', {
    is: 'DOCTOR',
    then: (schema) =>
      schema
        .required('Years of experience is required')
        .min(0, 'Years of experience cannot be negative')
        .max(70, 'Years of experience seems invalid'),
    otherwise: (schema) => schema.notRequired(),
  }),
});

// Forgot Password Schema
export const forgotPasswordSchema = Yup.object().shape({
  email: Yup.string()
    .required('Email is required')
    .email('Invalid email format'),
});

// Reset Password Schema
export const resetPasswordSchema = Yup.object().shape({
  otp: Yup.string()
    .required('OTP is required')
    .matches(/^\d{6}$/, 'OTP must be 6 digits'),
  newPassword: Yup.string()
    .required('New password is required')
    .min(8, 'Password must be at least 8 characters')
    .matches(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .matches(/[a-z]/, 'Password must contain at least one lowercase letter')
    .matches(/[0-9]/, 'Password must contain at least one number')
    .matches(/[@$!%*?&]/, 'Password must contain at least one special character (@$!%*?&)'),
  confirmPassword: Yup.string()
    .required('Please confirm your password')
    .oneOf([Yup.ref('newPassword')], 'Passwords must match'),
});

// Pet Schema
export const petSchema = Yup.object().shape({
  name: Yup.string()
    .required('Pet name is required')
    .min(2, 'Pet name must be at least 2 characters')
    .max(50, 'Pet name must be at most 50 characters'),
  species: Yup.string()
    .required('Species is required')
    .oneOf(['DOG', 'CAT', 'OTHER'], 'Invalid species'),
  breed: Yup.string()
    .required('Breed is required')
    .min(2, 'Breed must be at least 2 characters'),
  dateOfBirth: Yup.date()
    .required('Date of birth is required')
    .max(new Date(), 'Date of birth cannot be in the future'),
  weight: Yup.number()
    .required('Weight is required')
    .positive('Weight must be a positive number')
    .max(200, 'Weight seems invalid'),
  sex: Yup.string()
    .required('Sex is required')
    .oneOf(['MALE', 'FEMALE'], 'Invalid sex'),
  microchipId: Yup.string()
    .notRequired()
    .min(5, 'Microchip ID must be at least 5 characters'),
  photoUrl: Yup.string()
    .notRequired()
    .url('Invalid photo URL'),
});

// Password Strength Validator
export const validatePasswordStrength = (password: string): {
  score: number;
  message: string;
  isStrong: boolean;
} => {
  let score = 0;
  let message = '';

  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[@$!%*?&]/.test(password)) score++;

  if (score <= 2) {
    message = 'Weak';
  } else if (score <= 4) {
    message = 'Fair';
  } else if (score <= 5) {
    message = 'Good';
  } else {
    message = 'Strong';
  }

  return {
    score,
    message,
    isStrong: score >= 4,
  };
};
