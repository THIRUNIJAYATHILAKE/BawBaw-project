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
import { Picker } from '@react-native-picker/picker';
import { Formik } from 'formik';
import DateTimePicker from '@react-native-community/datetimepicker';
import { useAuth } from '@context/AuthContext';
import { registerSchema, validatePasswordStrength } from '@utils/formSchemas';
import ErrorAlert from '@components/ErrorAlert';
import PasswordStrengthIndicator from '@components/PasswordStrengthIndicator';
import { formatDate } from '@utils/dateFormatter';

interface Props {
  navigation: any;
  route?: any;
}

const RegisterScreen: React.FC<Props> = ({ navigation, route }) => {
  const roleFromParams = route?.params?.role || 'PET_OWNER';
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [passwordStrength, setPasswordStrength] = useState({ score: 0, message: '' });
  const { register, isLoading } = useAuth();

  const handleRegister = async (values: any) => {
    try {
      setErrorMessage('');
      const payload = {
        role: values.role,
        fullName: values.fullName,
        email: values.email,
        phoneNumber: values.phoneNumber,
        address: values.address,
        dateOfBirth: selectedDate.toISOString(),
        password: values.password,
        username: values.username,
        ...(values.role === 'DOCTOR' && {
          licenseId: values.licenseId,
          specialization: values.specialization,
          yearsOfExperience: parseInt(values.yearsOfExperience, 10),
        }),
      };
      await register(payload);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Registration failed. Please try again.';
      setErrorMessage(message);
    }
  };

  const handleDateChange = (_event: any, date?: Date) => {
    if (date) {
      setSelectedDate(date);
    }
    setShowDatePicker(false);
  };

  const handlePasswordChange = (text: string, setFieldValue: any) => {
    setFieldValue('password', text);
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
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Text style={styles.backButton}>← Back</Text>
          </TouchableOpacity>
          <Text style={styles.title}>Register to get started</Text>
          <Text style={styles.subtitle}>Join PetVax Care</Text>
        </View>

        <Formik
          initialValues={{
            role: roleFromParams,
            fullName: '',
            email: '',
            phoneNumber: '',
            address: '',
            username: '',
            password: '',
            confirmPassword: '',
            licenseId: '',
            specialization: '',
            yearsOfExperience: '',
          }}
          validationSchema={registerSchema}
          onSubmit={handleRegister}
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
                <Text style={styles.label}>Role</Text>
                <View style={styles.pickerContainer}>
                  <Picker
                    selectedValue={values.role}
                    onValueChange={(value: string) => setFieldValue('role', value)}
                    enabled={!isLoading}
                  >
                    <Picker.Item label="Pet Owner" value="PET_OWNER" />
                    <Picker.Item label="Veterinary Doctor" value="DOCTOR" />
                    <Picker.Item label="Assistant" value="ASSISTANT" />
                  </Picker>
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Full Name</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.fullName && errors.fullName ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter full name"
                  placeholderTextColor="#999"
                  value={values.fullName}
                  onChangeText={handleChange('fullName')}
                  editable={!isLoading}
                />
                {touched.fullName && errors.fullName && (
                  <Text style={styles.errorText}>{errors.fullName}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Username</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.username && errors.username ? styles.inputError : undefined,
                  ]}
                  placeholder="Choose a username"
                  placeholderTextColor="#999"
                  value={values.username}
                  onChangeText={handleChange('username')}
                  autoCapitalize="none"
                  editable={!isLoading}
                />
                {touched.username && errors.username && (
                  <Text style={styles.errorText}>{errors.username}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Email</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.email && errors.email ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter email"
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

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Phone Number</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.phoneNumber && errors.phoneNumber ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter phone number"
                  placeholderTextColor="#999"
                  value={values.phoneNumber}
                  onChangeText={handleChange('phoneNumber')}
                  keyboardType="phone-pad"
                  editable={!isLoading}
                />
                {touched.phoneNumber && errors.phoneNumber && (
                  <Text style={styles.errorText}>{errors.phoneNumber}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Address</Text>
                <TextInput
                  style={[
                    styles.input,
                    styles.multilineInput,
                    touched.address && errors.address ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter address"
                  placeholderTextColor="#999"
                  value={values.address}
                  onChangeText={handleChange('address')}
                  multiline
                  numberOfLines={3}
                  editable={!isLoading}
                />
                {touched.address && errors.address && (
                  <Text style={styles.errorText}>{errors.address}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Date of Birth</Text>
                <TouchableOpacity
                  style={[
                    styles.input,
                    styles.dateInput,
                    touched.address && styles.inputError,
                  ]}
                  onPress={() => setShowDatePicker(true)}
                  disabled={isLoading}
                >
                  <Text style={styles.dateInputText}>
                    {formatDate(selectedDate)}
                  </Text>
                </TouchableOpacity>
              </View>

              {showDatePicker && (
                <DateTimePicker
                  value={selectedDate}
                  mode="date"
                  display={Platform.OS === 'ios' ? 'spinner' : 'default'}
                  onChange={handleDateChange}
                  maximumDate={new Date()}
                />
              )}

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
                {values.password && (
                  <PasswordStrengthIndicator
                    score={passwordStrength.score}
                    message={passwordStrength.message}
                  />
                )}
                {touched.password && errors.password && (
                  <Text style={styles.errorText}>{errors.password}</Text>
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

              {values.role === 'DOCTOR' && (
                <>
                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>License ID</Text>
                    <TextInput
                      style={[
                        styles.input,
                        touched.licenseId && errors.licenseId ? styles.inputError : undefined,
                      ]}
                      placeholder="Enter license ID"
                      placeholderTextColor="#999"
                      value={values.licenseId}
                      onChangeText={handleChange('licenseId')}
                      editable={!isLoading}
                    />
                    {touched.licenseId && errors.licenseId && (
                      <Text style={styles.errorText}>{errors.licenseId}</Text>
                    )}
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Specialization</Text>
                    <TextInput
                      style={[
                        styles.input,
                        touched.specialization &&
                          errors.specialization
                          ? styles.inputError
                          : undefined,
                      ]}
                      placeholder="Enter specialization"
                      placeholderTextColor="#999"
                      value={values.specialization}
                      onChangeText={handleChange('specialization')}
                      editable={!isLoading}
                    />
                    {touched.specialization && errors.specialization && (
                      <Text style={styles.errorText}>{errors.specialization}</Text>
                    )}
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Years of Experience</Text>
                    <TextInput
                      style={[
                        styles.input,
                        touched.yearsOfExperience &&
                          errors.yearsOfExperience
                          ? styles.inputError
                          : undefined,
                      ]}
                      placeholder="Enter years of experience"
                      placeholderTextColor="#999"
                      value={values.yearsOfExperience}
                      onChangeText={handleChange('yearsOfExperience')}
                      keyboardType="number-pad"
                      editable={!isLoading}
                    />
                    {touched.yearsOfExperience && errors.yearsOfExperience && (
                      <Text style={styles.errorText}>
                        {errors.yearsOfExperience}
                      </Text>
                    )}
                  </View>
                </>
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

              <TouchableOpacity
                style={[
                  styles.registerButton,
                  isLoading && styles.registerButtonDisabled,
                ]}
                onPress={handleSubmit as any}
                disabled={isLoading}
              >
                {isLoading ? (
                  <ActivityIndicator color="#fff" size="small" />
                ) : (
                  <Text style={styles.registerButtonText}>Create Account</Text>
                )}
              </TouchableOpacity>

              <View style={styles.loginContainer}>
                <Text style={styles.loginText}>Already have an account? </Text>
                <TouchableOpacity
                  onPress={() => navigation.navigate('Login')}
                  disabled={isLoading}
                >
                  <Text style={styles.loginLink}>Login</Text>
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
    paddingVertical: 20,
  },
  headerContainer: {
    alignItems: 'center',
    marginBottom: 30,
    paddingHorizontal: 20,
  },
  backButton: {
    fontSize: 14,
    fontWeight: '600',
    color: '#6B4C9A',
    marginBottom: 16,
    alignSelf: 'flex-start',
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
  },
  formContainer: {
    paddingHorizontal: 20,
  },
  inputGroup: {
    marginBottom: 18,
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
  multilineInput: {
    textAlignVertical: 'top',
    paddingVertical: 12,
    minHeight: 80,
  },
  errorText: {
    color: '#FF3B30',
    fontSize: 12,
    marginTop: 6,
    fontWeight: '500',
  },
  pickerContainer: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    overflow: 'hidden',
  },
  dateInput: {
    justifyContent: 'center',
  },
  dateInputText: {
    fontSize: 16,
    color: '#333',
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
  registerButton: {
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 20,
  },
  registerButtonDisabled: {
    opacity: 0.6,
  },
  registerButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  loginContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 20,
    marginBottom: 30,
  },
  loginText: {
    fontSize: 14,
    color: '#666',
  },
  loginLink: {
    fontSize: 14,
    color: '#007AFF',
    fontWeight: '700',
  },
});

export default RegisterScreen;
