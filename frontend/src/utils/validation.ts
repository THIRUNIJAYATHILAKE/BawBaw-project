export const validation = {
  /**
   * Validates email format
   */
  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },

  /**
   * Validates password strength (min 8 chars, 1 uppercase, 1 lowercase, 1 number)
   */
  isValidPassword(password: string): boolean {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/;
    return passwordRegex.test(password);
  },

  /**
   * Validates phone number
   */
  isValidPhoneNumber(phone: string): boolean {
    const phoneRegex = /^[\d\s\-+()]{7,}$/;
    return phoneRegex.test(phone);
  },

  /**
   * Validates that string is not empty or whitespace
   */
  isNotEmpty(value: string): boolean {
    return !!(value && value.trim().length > 0);
  },

  /**
   * Validates string length
   */
  isValidLength(value: string, min: number, max: number): boolean {
    return value.length >= min && value.length <= max;
  },

  /**
   * Validates age (number between 0 and 150)
   */
  isValidAge(age: number): boolean {
    return Number.isInteger(age) && age >= 0 && age <= 150;
  },

  /**
   * Validates date is not in past
   */
  isFutureDate(date: Date): boolean {
    return date.getTime() > new Date().getTime();
  },

  /**
   * Validates URL format
   */
  isValidUrl(url: string): boolean {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  },

  /**
   * Validates file extension
   */
  isValidFileExtension(filename: string, allowedExtensions: string[]): boolean {
    const extension = filename.split('.').pop()?.toLowerCase();
    return extension ? allowedExtensions.includes(extension) : false;
  },

  /**
   * Validates file size in bytes
   */
  isValidFileSize(size: number, maxSizeInBytes: number): boolean {
    return size <= maxSizeInBytes;
  },
};
