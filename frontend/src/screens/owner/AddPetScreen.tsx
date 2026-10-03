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
  Image,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { Formik } from 'formik';
import DateTimePicker from '@react-native-community/datetimepicker';
import * as ImagePicker from 'expo-image-picker';
import apiClient from '@services/api';
import { petSchema } from '@utils/formSchemas';
import ErrorAlert from '@components/ErrorAlert';
import { formatDate } from '@utils/dateFormatter';

interface Props {
  navigation: any;
  route?: any;
}

const AddPetScreen: React.FC<Props> = ({ navigation }) => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const handleDateChange = (_event: any, date?: Date) => {
    if (date) {
      setSelectedDate(date);
    }
    setShowDatePicker(false);
  };

  const pickImage = async () => {
    try {
      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets[0]) {
        setSelectedImage(result.assets[0].uri);
      }
    } catch (error: any) {
      setErrorMessage('Failed to pick image');
    }
  };

  const takePhoto = async () => {
    try {
      const result = await ImagePicker.launchCameraAsync({
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets[0]) {
        setSelectedImage(result.assets[0].uri);
      }
    } catch (error: any) {
      setErrorMessage('Failed to take photo');
    }
  };

  const handleAddPet = async (values: any) => {
    try {
      setIsLoading(true);
      setErrorMessage('');

      const formData = new FormData();
      formData.append('name', values.name);
      formData.append('species', values.species);
      formData.append('breed', values.breed);
      formData.append('dateOfBirth', selectedDate.toISOString());
      formData.append('weight', values.weight);
      formData.append('sex', values.sex);

      if (values.microchipId) {
        formData.append('microchipId', values.microchipId);
      }

      if (selectedImage) {
        const filename = selectedImage.split('/').pop() || 'pet-photo.jpg';
        formData.append('photo', {
          uri: selectedImage,
          type: 'image/jpeg',
          name: filename,
        } as any);
      }

      await apiClient.createPet(formData);
      navigation.goBack();
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to add pet';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
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
        <Formik
          initialValues={{
            name: '',
            species: 'DOG',
            breed: '',
            weight: '',
            sex: 'MALE',
            microchipId: '',
          }}
          validationSchema={petSchema}
          onSubmit={handleAddPet}
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
              <View style={styles.photoSection}>
                <View style={styles.photoContainer}>
                  {selectedImage ? (
                    <Image
                      source={{ uri: selectedImage }}
                      style={styles.selectedPhoto}
                    />
                  ) : (
                    <View style={styles.photoPlaceholder}>
                      <Text style={styles.placeholderText}>📷</Text>
                      <Text style={styles.placeholderLabel}>Add Photo</Text>
                    </View>
                  )}
                </View>

                <View style={styles.photoButtonsContainer}>
                  <TouchableOpacity
                    style={styles.photoButton}
                    onPress={takePhoto}
                    disabled={isLoading}
                  >
                    <Text style={styles.photoButtonText}>Take Photo</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={styles.photoButton}
                    onPress={pickImage}
                    disabled={isLoading}
                  >
                    <Text style={styles.photoButtonText}>Choose Photo</Text>
                  </TouchableOpacity>
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Pet Name</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.name && errors.name ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter pet name"
                  placeholderTextColor="#999"
                  value={values.name}
                  onChangeText={handleChange('name')}
                  editable={!isLoading}
                />
                {touched.name && errors.name && (
                  <Text style={styles.errorText}>{errors.name}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Species</Text>
                <View style={styles.pickerContainer}>
                  <Picker
                    selectedValue={values.species}
                    onValueChange={(value: string) => setFieldValue('species', value)}
                    enabled={!isLoading}
                  >
                    <Picker.Item label="Dog" value="DOG" />
                    <Picker.Item label="Cat" value="CAT" />
                    <Picker.Item label="Other" value="OTHER" />
                  </Picker>
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Breed</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.breed && errors.breed ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter breed"
                  placeholderTextColor="#999"
                  value={values.breed}
                  onChangeText={handleChange('breed')}
                  editable={!isLoading}
                />
                {touched.breed && errors.breed && (
                  <Text style={styles.errorText}>{errors.breed}</Text>
                )}
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Date of Birth</Text>
                <TouchableOpacity
                  style={[
                    styles.input,
                    styles.dateInput,
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

              <View style={styles.rowContainer}>
                <View style={[styles.inputGroup, styles.halfWidth]}>
                  <Text style={styles.label}>Weight (kg)</Text>
                  <TextInput
                    style={[
                      styles.input,
                      touched.weight && errors.weight ? styles.inputError : undefined,
                    ]}
                    placeholder="0.0"
                    placeholderTextColor="#999"
                    value={values.weight}
                    onChangeText={handleChange('weight')}
                    keyboardType="decimal-pad"
                    editable={!isLoading}
                  />
                  {touched.weight && errors.weight && (
                    <Text style={styles.errorText}>{errors.weight}</Text>
                  )}
                </View>

                <View style={[styles.inputGroup, styles.halfWidth]}>
                  <Text style={styles.label}>Sex</Text>
                  <View style={styles.pickerContainer}>
                    <Picker
                      selectedValue={values.sex}
                      onValueChange={(value: string) => setFieldValue('sex', value)}
                      enabled={!isLoading}
                    >
                      <Picker.Item label="Male" value="MALE" />
                      <Picker.Item label="Female" value="FEMALE" />
                    </Picker>
                  </View>
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.label}>Microchip ID (Optional)</Text>
                <TextInput
                  style={[
                    styles.input,
                    touched.microchipId && errors.microchipId ? styles.inputError : undefined,
                  ]}
                  placeholder="Enter microchip ID"
                  placeholderTextColor="#999"
                  value={values.microchipId}
                  onChangeText={handleChange('microchipId')}
                  editable={!isLoading}
                />
                {touched.microchipId && errors.microchipId && (
                  <Text style={styles.errorText}>{errors.microchipId}</Text>
                )}
              </View>

              {errorMessage && (
                <ErrorAlert
                  message={errorMessage}
                  visible={!!errorMessage}
                  type="error"
                  duration={0}
                  onDismiss={() => setErrorMessage('')}
                />
              )}

              <View style={styles.buttonContainer}>
                <TouchableOpacity
                  style={[styles.button, styles.cancelButton]}
                  onPress={() => navigation.goBack()}
                  disabled={isLoading}
                >
                  <Text style={styles.cancelButtonText}>Cancel</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.button,
                    styles.submitButton,
                    isLoading && styles.buttonDisabled,
                  ]}
                  onPress={handleSubmit as any}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <ActivityIndicator color="#fff" size="small" />
                  ) : (
                    <Text style={styles.submitButtonText}>Add Pet</Text>
                  )}
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
  formContainer: {
    paddingHorizontal: 20,
  },
  photoSection: {
    alignItems: 'center',
    marginBottom: 30,
  },
  photoContainer: {
    marginBottom: 16,
  },
  selectedPhoto: {
    width: 120,
    height: 120,
    borderRadius: 12,
  },
  photoPlaceholder: {
    width: 120,
    height: 120,
    borderRadius: 12,
    backgroundColor: '#e0e0e0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    fontSize: 36,
    marginBottom: 4,
  },
  placeholderLabel: {
    fontSize: 12,
    color: '#999',
  },
  photoButtonsContainer: {
    flexDirection: 'row',
    gap: 12,
  },
  photoButton: {
    flex: 1,
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 10,
    alignItems: 'center',
  },
  photoButtonText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '600',
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
  errorText: {
    color: '#FF3B30',
    fontSize: 12,
    marginTop: 6,
    fontWeight: '500',
  },
  dateInput: {
    justifyContent: 'center',
  },
  dateInputText: {
    fontSize: 16,
    color: '#333',
  },
  pickerContainer: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    overflow: 'hidden',
  },
  rowContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 18,
  },
  halfWidth: {
    width: '48%',
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 20,
    marginBottom: 30,
  },
  button: {
    flex: 1,
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
  },
  cancelButton: {
    backgroundColor: '#e0e0e0',
  },
  cancelButtonText: {
    color: '#333',
    fontSize: 16,
    fontWeight: '700',
  },
  submitButton: {
    backgroundColor: '#007AFF',
  },
  submitButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
});

export default AddPetScreen;
