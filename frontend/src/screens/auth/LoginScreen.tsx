import React, { useState, useEffect } from 'react';
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
import { useAuth } from '@context/AuthContext';
import { loginSchema } from '@utils/formSchemas';
import ErrorAlert from '@components/ErrorAlert';

interface Props {
  navigation: any;
  route?: any;
}

const LoginScreen: React.FC<Props> = ({ navigation }) => {
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [showPassword, setShowPassword] = useState(false);
  const [failedAttempts, setFailedAttempts] = useState(0);
  const [isAccountLocked, setIsAccountLocked] = useState(false);
  const [lockoutTimer, setLockoutTimer] = useState(0);
  const { login, isLoading } = useAuth();

  useEffect(() => {
    if (lockoutTimer > 0) {
      const timer = setTimeout(() => {
        setLockoutTimer(lockoutTimer - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
    if (lockoutTimer === 0 && isAccountLocked) {
      setIsAccountLocked(false);
    }
    return undefined;
  }, [lockoutTimer, isAccountLocked]);

  const handleLogin = async (values: { usernameOrEmail: string; password: string }) => {
    if (isAccountLocked) {
      setErrorMessage(
        `Account locked. Try again in ${lockoutTimer} seconds.`
      );
      return;
    }

    try {
      setErrorMessage('');
      await login(values.usernameOrEmail, values.password);
      setFailedAttempts(0);
    } catch (error: any) {
      const newAttempts = failedAttempts + 1;
      setFailedAttempts(newAttempts);

      if (newAttempts >= 5) {
        setIsAccountLocked(true);
        setLockoutTimer(300); // 5 minutes lockout
        setErrorMessage(
          'Too many failed attempts. Account locked for 5 minutes.'
        );
      } else {
        const message =
          error.response?.data?.message ||
          error.message ||
          'Login failed. Please try again.';
        setErrorMessage(
          `${message} (${5 - newAttempts} attempts remaining)`
        );
      }
    }
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
          <Text style={styles.title}>PetVax Care</Text>
          <Text style={styles.subtitle}>Pet Vaccination Management</Text>
        </View>

        <Formik
          initialValues={{
            usernameOrEmail: '',
            password: '',
          }}
          validationSchema={loginSchema}
          onSubmit={handleLogin}
        >
          {({ values, errors, touched, handleChange, handleSubmit }) => (
            <View style={styles.formContainer}>
              <View style={styles.inputGroup}>
                <Text style={styles.label}>Username or Email</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.usernameOrEmail && errors.usernameOrEmail ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter username or email"
                  placeholderTextColor="#999"
                  value={values.usernameOrEmail}
                  onChangeText={handleChange('usernameOrEmail')}
                  editable={!isLoading}
                  keyboardType="email-address"
                  autoCapitalize="none"
                />
                {touched.usernameOrEmail && errors.usernameOrEmail && (
                  <Text style={styles.errorText}>{errors.usernameOrEmail}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Password</Text>
                <View style={styles.passwordContainer}>
                  <TextInput
                    style={[
                      styles.passwordInput,
                      touched.password && errors.password ? styles.inputError : undefined,
                    ]}
                    placeholder="Enter password"
                    placeholderTextColor="#999"
                    value={values.password}
                    onChangeText={handleChange('password')}
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
                {touched.password && errors.password && (
                  <Text style={styles.errorText}>{errors.password}</Text>
                )}
              </View>

              <TouchableOpacity
                onPress={() => navigation.navigate('ForgotPassword')}
                disabled={isLoading}
              >
                <Text style={styles.forgotPassword}>Forgot Password?</Text>
              </TouchableOpacity>

              {errorMessage && (
                <ErrorAlert
                  message={errorMessage}
                  visible={!!errorMessage}
                  type="error"
                  duration={0}
                  onDismiss={() => setErrorMessage('')}
                />
              )}

              <TouchableOpacity
                style={[
                  styles.loginButton,
                  (isLoading || isAccountLocked) ? styles.loginButtonDisabled : undefined,
                ]}
                onPress={handleSubmit as any}
                disabled={isLoading || isAccountLocked}
              >
                {isLoading ? (
                  <ActivityIndicator color="#fff" size="small" />
                ) : (
                  <Text style={styles.loginButtonText}>
                    {isAccountLocked ? 'Account Locked' : 'Login'}
                  </Text>
                )}
              </TouchableOpacity>

              <View style={styles.signupContainer}>
                <Text style={styles.signupText}>Don't have an account? </Text>
                <TouchableOpacity
                  onPress={() => navigation.navigate('Register')}
                  disabled={isLoading}
                >
                  <Text style={styles.signupLink}>Sign Up</Text>
                </TouchableOpacity>
              </View>
            </View>
          )}
        </Formik>
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
    fontSize: 32,
    fontWeight: '700',
    color: '#007AFF',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
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
  forgotPassword: {
    fontSize: 14,
    color: '#007AFF',
    fontWeight: '600',
    textAlign: 'right',
    marginBottom: 24,
  },
  loginButton: {
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 8,
  },
  loginButtonDisabled: {
    opacity: 0.6,
  },
  loginButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  signupContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 24,
  },
  signupText: {
    fontSize: 14,
    color: '#666',
  },
  signupLink: {
    fontSize: 14,
    color: '#007AFF',
    fontWeight: '700',
  },
});

export default LoginScreen;
