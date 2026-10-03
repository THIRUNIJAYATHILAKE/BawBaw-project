import React from 'react';
import {
  View,
  Text,
  TextInput,
  TextInputProps,
  StyleSheet,
  TouchableOpacity,
} from 'react-native';

interface FormFieldProps extends TextInputProps {
  label: string;
  error?: string;
  touched?: boolean;
  showPasswordToggle?: boolean;
  isPasswordVisible?: boolean;
  onPasswordToggle?: () => void;
  containerStyle?: any;
  inputStyle?: any;
  multiline?: boolean;
  numberOfLines?: number;
}

const FormField: React.FC<FormFieldProps> = ({
  label,
  error,
  touched,
  showPasswordToggle = false,
  isPasswordVisible = false,
  onPasswordToggle,
  containerStyle,
  inputStyle,
  multiline = false,
  numberOfLines = 1,
  ...textInputProps
}) => {
  return (
    <View style={[styles.container, containerStyle]}>
      <Text style={styles.label}>{label}</Text>
      <View
        style={[
          styles.inputWrapper,
          showPasswordToggle && styles.passwordWrapper,
          touched && error ? styles.inputWrapperError : undefined,
        ]}
      >
        <TextInput
          {...textInputProps}
          style={[
            styles.input,
            showPasswordToggle && styles.passwordInput,
            multiline && styles.multilineInput,
            inputStyle,
          ]}
          multiline={multiline}
          numberOfLines={numberOfLines}
          placeholderTextColor="#999"
        />
        {showPasswordToggle && (
          <TouchableOpacity
            onPress={onPasswordToggle}
            hitSlop={{ top: 10, right: 10, bottom: 10, left: 10 }}
            style={styles.passwordToggleButton}
          >
            <Text style={styles.passwordToggleIcon}>
              {isPasswordVisible ? '👁️' : '👁️‍🗨️'}
            </Text>
          </TouchableOpacity>
        )}
      </View>
      {touched && error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: 18,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
  },
  passwordWrapper: {
    paddingRight: 0,
  },
  inputWrapperError: {
    borderColor: '#FF3B30',
  },
  input: {
    flex: 1,
    paddingHorizontal: 12,
    paddingVertical: 12,
    fontSize: 16,
    color: '#333',
  },
  multilineInput: {
    textAlignVertical: 'top',
    minHeight: 80,
  },
  passwordInput: {
    paddingRight: 40,
  },
  passwordToggleButton: {
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  passwordToggleIcon: {
    fontSize: 18,
  },
  errorText: {
    color: '#FF3B30',
    fontSize: 12,
    marginTop: 6,
    fontWeight: '500',
  },
});

export default FormField;
