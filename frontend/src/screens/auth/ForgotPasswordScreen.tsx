import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
} from 'react-native';
import { Formik } from 'formik';
import apiClient from '@services/api';
import {
  forgotPasswordSchema,
  resetPasswordSchema,
  validatePasswordStrength,
} from '@utils/formSchemas';
import ErrorAlert from '@components/ErrorAlert';

interface Props {
  navigation: any;
  route?: any;
}

const ForgotPasswordScreen: React.FC<Props> = ({ navigation }) => {
  const [step, setStep] = useState<'email' | 'otp' | 'reset'>('email');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState({ score: 0, message: '' });

  const handleSendOTP = async (values: { email: string }) => {
    try {
      setIsLoading(true);
      setErrorMessage('');
      await apiClient.forgotPassword(values.email);
      setSuccessMessage('OTP sent to your email');
      setStep('otp');
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to send OTP. Please try again.';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (values: any) => {
    try {
      setIsLoading(true);
      setErrorMessage('');
      await apiClient.resetPassword(values.otp, values.newPassword);
      setSuccessMessage('Password reset successfully! Redirecting to login...');
      setTimeout(() => {
        navigation.navigate('Login');
      }, 2000);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to reset password. Please try again.';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePasswordChange = (text: string, setFieldValue: any) => {
    setFieldValue('newPassword', text);
    setPasswordStrength(validatePasswordStrength(text));
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={styles.container}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.headerContainer}>
          <Text style={styles.title}>Reset Password</Text>
          <Text style={styles.subtitle}>
            {step === 'email'
              ? 'Enter your email address'
              : step === 'otp'
              ? 'Enter the OTP sent to your email'
              : 'Create a new password'}
          </Text>
        </View>

        {successMessage && (
          <ErrorAlert
            message={successMessage}
            visible={!!successMessage}
            type="success"
            duration={0}
            onDismiss={() => setSuccessMessage('')}
          />
        )}

        {errorMessage && (
          <ErrorAlert
            message={errorMessage}
            visible={!!errorMessage}
            type="error"
            duration={0}
            onDismiss={() => setErrorMessage('')}
          />
        )}

        {step === 'email' && (
          <Formik
            initialValues={{ email: '' }}
            validationSchema={forgotPasswordSchema}
            onSubmit={handleSendOTP}
          >
            {({ values, errors, touched, handleChange, handleSubmit }) => (
              <View style={styles.formContainer}>
                <View style={styles.inputGroup}>
                  <Text style={styles.label}>Email Address</Text>
                  <TextInput
                    style={[
                      styles.input,
                      touched.email && errors.email ? styles.inputError : undefined,
                    ]}
                    placeholder="Enter your email"
                    placeholderTextColor="#999"
                    value={values.email}
                    onChangeText={handleChange('email')}
                    keyboardType="email-address"
                    autoCapitalize="none"
                    editable={!isLoading}
                  />
                  {touched.email && errors.email && (
                    <Text style={styles.errorText}>{errors.email}</Text>
                  )}
                </View>

                <TouchableOpacity
                  style={[styles.button, isLoading ? styles.buttonDisabled : undefined]}
                  onPress={handleSubmit as any}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <ActivityIndicator color="#fff" size="small" />
                  ) : (
                    <Text style={styles.buttonText}>Send OTP</Text>
                  )}
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => navigation.navigate('Login')}
                  disabled={isLoading}
                >
                  <Text style={styles.backLink}>Back to Login</Text>
                </TouchableOpacity>
              </View>
            )}
          </Formik>
        )}

        {step === 'otp' && (
          <Formik
            initialValues={{
              otp: '',
              newPassword: '',
              confirmPassword: '',
            }}
            validationSchema={resetPasswordSchema}
            onSubmit={handleResetPassword}
          >
            {({
              values,
              errors,
              touched,
              handleChange,
              handleSubmit,
              setFieldValue,
            }) => (
              <View style={styles.formContainer}>
                <View style={styles.inputGroup}>
                  <Text style={styles.label}>OTP (6 digits)</Text>
                  <TextInput
                    style={[
                      styles.input,
                      touched.otp && errors.otp ? styles.inputError : undefined,
                    ]}
                    placeholder="Enter 6-digit OTP"
                    placeholderTextColor="#999"
                    value={values.otp}
                    onChangeText={handleChange('otp')}
                    keyboardType="number-pad"
                    maxLength={6}
                    editable={!isLoading}
                  />
                  {touched.otp && errors.otp && (
                    <Text style={styles.errorText}>{errors.otp}</Text>
                  )}
                </View>

                <View style={styles.inputGroup}>
                  <Text style={styles.label}>New Password</Text>
                  <View style={styles.passwordContainer}>
                    <TextInput
                      style={[
                        styles.passwordInput,
                        touched.newPassword && errors.newPassword ? styles.inputError : undefined,
                      ]}
                      placeholder="Enter new password"
                      placeholderTextColor="#999"
                      value={values.newPassword}
                      onChangeText={(text) =>
                        handlePasswordChange(text, setFieldValue)
                      }
                      secureTextEntry={!showPassword}
                      editable={!isLoading}
                    />
                    <TouchableOpacity
                      onPress={() => setShowPassword(!showPassword)}
                      hitSlop={{ top: 10, right: 10, bottom: 10, left: 10 }}
                    >
                      <Text style={styles.passwordToggle}>
                        {showPassword ? '👁️' : '👁️‍🗨️'}
                      </Text>
                    </TouchableOpacity>
                  </View>
                  {values.newPassword && (
                    <View style={styles.strengthIndicator}>
                      <View
                        style={[
                          styles.strengthBar,
                          {
                            width: `${(passwordStrength.score / 6) * 100}%`,
                            backgroundColor:
                              passwordStrength.score < 3
                                ? '#FF3B30'
                                : passwordStrength.score < 5
                                ? '#FF9500'
                                : '#34C759',
                          },
                        ]}
                      />
                    </View>
                  )}
                  <Text
                    style={[
                      styles.strengthText,
                      {
                        color:
                          passwordStrength.score < 3
                            ? '#FF3B30'
                            : passwordStrength.score < 5
                            ? '#FF9500'
                            : '#34C759',
                      },
                    ]}
                  >
                    Strength: {passwordStrength.message}
                  </Text>
                  {touched.newPassword && errors.newPassword && (
                    <Text style={styles.errorText}>{errors.newPassword}</Text>
                  )}
                </View>

                <View style={styles.inputGroup}>
                  <Text style={styles.label}>Confirm Password</Text>
                  <View style={styles.passwordContainer}>
                    <TextInput
                      style={[
                        styles.passwordInput,
                        touched.confirmPassword &&
                          errors.confirmPassword
                          ? styles.inputError
                          : undefined,
                      ]}
                      placeholder="Confirm password"
                      placeholderTextColor="#999"
                      value={values.confirmPassword}
                      onChangeText={handleChange('confirmPassword')}
                      secureTextEntry={!showConfirmPassword}
                      editable={!isLoading}
                    />
                    <TouchableOpacity
                      onPress={() => setShowConfirmPassword(!showConfirmPassword)}
                      hitSlop={{ top: 10, right: 10, bottom: 10, left: 10 }}
                    >
                      <Text style={styles.passwordToggle}>
                        {showConfirmPassword ? '👁️' : '👁️‍🗨️'}
                      </Text>
                    </TouchableOpacity>
                  </View>
                  {touched.confirmPassword && errors.confirmPassword && (
                    <Text style={styles.errorText}>{errors.confirmPassword}</Text>
                  )}
                </View>

                <TouchableOpacity
                  style={[styles.button, isLoading ? styles.buttonDisabled : undefined]}
                  onPress={handleSubmit as any}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <ActivityIndicator color="#fff" size="small" />
                  ) : (
                    <Text style={styles.buttonText}>Reset Password</Text>
                  )}
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => {
                    setStep('email');
                    setErrorMessage('');
                    setSuccessMessage('');
                  }}
                  disabled={isLoading}
                >
                  <Text style={styles.backLink}>Back</Text>
                </TouchableOpacity>
              </View>
            )}
          </Formik>
        )}
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingVertical: 20,
  },
  headerContainer: {
    alignItems: 'center',
    marginBottom: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#007AFF',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
  },
  formContainer: {
    paddingHorizontal: 20,
  },
  inputGroup: {
    marginBottom: 20,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  input: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 12,
    fontSize: 16,
    color: '#333',
  },
  inputError: {
    borderColor: '#FF3B30',
  },
  errorText: {
    color: '#FF3B30',
    fontSize: 12,
    marginTop: 6,
    fontWeight: '500',
  },
  passwordContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
  },
  passwordInput: {
    flex: 1,
    paddingHorizontal: 12,
    paddingVertical: 12,
    fontSize: 16,
    color: '#333',
  },
  passwordToggle: {
    paddingRight: 12,
    fontSize: 18,
  },
  strengthIndicator: {
    height: 4,
    backgroundColor: '#f0f0f0',
    borderRadius: 2,
    marginTop: 8,
    overflow: 'hidden',
  },
  strengthBar: {
    height: '100%',
    borderRadius: 2,
  },
  strengthText: {
    fontSize: 12,
    fontWeight: '600',
    marginTop: 6,
  },
  button: {
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  backLink: {
    fontSize: 14,
    color: '#007AFF',
    fontWeight: '600',
    textAlign: 'center',
    marginTop: 20,
  },
});

export default ForgotPasswordScreen;
