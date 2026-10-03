export const colors = {
  // Primary colors
  primary: '#6B4C9A',
  primaryLight: '#8B6CB5',
  primaryDark: '#4B2C7A',

  // Secondary colors
  secondary: '#1E90FF',
  secondaryLight: '#4EB3FF',
  secondaryDark: '#0060CC',

  // Accent colors
  accent: '#FF6B9D',
  accentLight: '#FFB3D9',
  accentDark: '#FF3B75',

  // Neutrals
  white: '#FFFFFF',
  black: '#000000',
  
  // Text colors (flat structure)
  text: '#333333',
  textSecondary: '#666666',
  textTertiary: '#999999',
  textLight: '#CCCCCC',
  
  // Background colors (flat structure)
  background: '#F8F9FA',
  backgroundSecondary: '#F0E6FF',
  surface: '#FFFFFF',
  
  // Border colors (flat structure)
  border: '#DDD',
  borderSecondary: '#EEE',
  borderLight: '#F5F5F5',

  // Status colors
  success: '#34C759',
  warning: '#FF9500',
  error: '#FF3B30',
  info: '#007AFF',

  // Role-specific colors
  petOwner: '#FF6B9D',
  doctor: '#4ECDC4',
  assistant: '#FFD93D',
  admin: '#6B4C9A',
};

export const lightTheme = {
  ...colors,
  isDark: false,
};

export const darkTheme = {
  ...colors,
  isDark: true,
  background: '#1A1A1A',
  backgroundSecondary: '#2A2A2A',
  surface: '#333333',
  text: '#FFFFFF',
  textSecondary: '#E0E0E0',
  textTertiary: '#A0A0A0',
  textLight: '#707070',
};
