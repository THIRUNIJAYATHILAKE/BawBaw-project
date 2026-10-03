import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { useFocusEffect } from '@react-navigation/native';
import apiClient from '@services/api';
import LoadingSpinner from '@components/LoadingSpinner';
import ErrorAlert from '@components/ErrorAlert';
import DoctorCard from '@components/DoctorCard';
import CalendarPicker from '@components/CalendarPicker';
import TimeSlotCard from '@components/TimeSlotCard';

interface Doctor {
  id: number;
  name: string;
  specialization: string;
  rating: number;
  availableSlots: number;
  photoUrl?: string;
}

interface Pet {
  id: number;
  name: string;
}

interface TimeSlot {
  id: string;
  startTime: string;
  endTime: string;
  isAvailable: boolean;
}

interface Props {
  navigation: any;
  route: any;
}

type Step = 'doctor' | 'date' | 'time' | 'confirm';

const BookAppointmentScreen: React.FC<Props> = ({ navigation, route }) => {
  const { petId } = route.params || {};
  const [currentStep, setCurrentStep] = useState<Step>('doctor');
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [pets, setPets] = useState<Pet[]>([]);
  const [selectedDoctor, setSelectedDoctor] = useState<Doctor | null>(null);
  const [selectedPet, setSelectedPet] = useState<Pet | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [specialty, setSpecialty] = useState<string>('');
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);
  const [availableDates, setAvailableDates] = useState<Date[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isBooking, setIsBooking] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [purpose, setPurpose] = useState<string>('OTHER');

  useFocusEffect(
    useCallback(() => {
      loadInitialData();
    }, [])
  );

  const loadInitialData = async () => {
    try {
      setErrorMessage('');
      const [doctorsRes, petsRes] = await Promise.all([
        apiClient.getClient().get('/owner/doctors'),
        apiClient.getPets(),
      ]);

      setDoctors(doctorsRes.data || []);
      setPets(petsRes.data || []);

      if (petId) {
        const pet = petsRes.data?.find((p: Pet) => p.id === petId);
        if (pet) setSelectedPet(pet);
      } else if (petsRes.data?.length > 0) {
        setSelectedPet(petsRes.data[0]);
      }
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load data';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  const loadTimeSlots = async () => {
    if (!selectedDoctor || !selectedDate) return;

    try {
      setIsLoadingSlots(true);
      const response = await apiClient.getClient().get(
        `/owner/doctors/${selectedDoctor.id}/available-slots`,
        {
          params: {
            date: selectedDate.toISOString().split('T')[0],
          },
        }
      );

      setTimeSlots(response.data || []);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load time slots';
      setErrorMessage(message);
    } finally {
      setIsLoadingSlots(false);
    }
  };

  const handleDoctorSelect = (doctor: Doctor) => {
    setSelectedDoctor(doctor);
    loadAvailableDates(doctor.id);
  };

  const loadAvailableDates = async (doctorId: number) => {
    try {
      const response = await apiClient.getClient().get(
        `/owner/doctors/${doctorId}/available-dates`
      );

      const dates = (response.data || []).map((dateStr: string) => new Date(dateStr));
      setAvailableDates(dates);
    } catch (error: any) {
      console.warn('Failed to load available dates');
    }
  };

  const handleDateSelect = async (date: Date) => {
    setSelectedDate(date);
  };

  React.useEffect(() => {
    if (selectedDate && selectedDoctor) {
      loadTimeSlots();
    }
  }, [selectedDate, selectedDoctor]);

  const handleConfirmBooking = async () => {
    if (!selectedPet || !selectedDoctor || !selectedDate || !selectedSlot) {
      Alert.alert('Error', 'Please complete all steps');
      return;
    }

    try {
      setIsBooking(true);
      setErrorMessage('');

      const appointmentData = {
        petId: selectedPet.id,
        doctorId: selectedDoctor.id,
        appointmentDate: new Date(
          `${selectedDate.toISOString().split('T')[0]}T${selectedSlot.startTime}`
        ).toISOString(),
        purpose,
      };

      await apiClient.bookAppointment(appointmentData);

      Alert.alert(
        'Success',
        'Appointment booked successfully!',
        [
          {
            text: 'OK',
            onPress: () => {
              navigation.navigate('Appointments', {
                screen: 'AppointmentsList',
              });
            },
          },
        ]
      );
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to book appointment';
      setErrorMessage(message);
    } finally {
      setIsBooking(false);
    }
  };

  const specialties = Array.from(
    new Set(doctors.map(d => d.specialization))
  );

  const filteredDoctors = specialty
    ? doctors.filter(d => d.specialization === specialty)
    : doctors;

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <View style={styles.container}>
      {errorMessage && (
        <ErrorAlert
          message={errorMessage}
          visible={!!errorMessage}
          type="error"
          duration={0}
          onDismiss={() => setErrorMessage('')}
        />
      )}

      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.stepsContainer}>
          {(['doctor', 'date', 'time', 'confirm'] as const).map((step, idx) => (
            <View key={step} style={styles.stepIndicator}>
              <View
                style={[
                  styles.stepCircle,
                  currentStep === step && styles.stepCircleActive,
                  ['doctor', 'date', 'time', 'confirm'].indexOf(currentStep) > idx &&
                    styles.stepCircleCompleted,
                ]}
              >
                <Text
                  style={[
                    styles.stepNumber,
                    (currentStep === step ||
                      ['doctor', 'date', 'time', 'confirm'].indexOf(currentStep) >
                        idx) &&
                      styles.stepNumberActive,
                  ]}
                >
                  {idx + 1}
                </Text>
              </View>
              <Text style={styles.stepLabel}>
                {step.charAt(0).toUpperCase() + step.slice(1)}
              </Text>
            </View>
          ))}
        </View>

        {currentStep === 'doctor' && (
          <View style={styles.stepContent}>
            <Text style={styles.stepTitle}>Select a Doctor</Text>

            {specialties.length > 0 && (
              <View style={styles.filterContainer}>
                <Text style={styles.filterLabel}>Filter by Specialty:</Text>
                <View style={styles.pickerContainer}>
                  <Picker
                    selectedValue={specialty}
                    onValueChange={(value: string) => setSpecialty(value)}
                  >
                    <Picker.Item label="All Specialties" value="" />
                    {specialties.map((spec) => (
                      <Picker.Item key={spec} label={spec} value={spec} />
                    ))}
                  </Picker>
                </View>
              </View>
            )}

            {filteredDoctors.map((doctor) => (
              <DoctorCard
                key={doctor.id}
                {...doctor}
                isSelected={selectedDoctor?.id === doctor.id}
                onPress={() => handleDoctorSelect(doctor)}
              />
            ))}
          </View>
        )}

        {currentStep === 'date' && selectedDoctor && (
          <View style={styles.stepContent}>
            <Text style={styles.stepTitle}>Select a Date</Text>
            <CalendarPicker
              selectedDate={selectedDate || undefined}
              onDateSelect={handleDateSelect}
              availableDates={availableDates}
              minDate={new Date()}
            />
          </View>
        )}

        {currentStep === 'time' && selectedDoctor && selectedDate && (
          <View style={styles.stepContent}>
            <Text style={styles.stepTitle}>Select a Time Slot</Text>

            {isLoadingSlots ? (
              <View style={styles.loadingContainer}>
                <ActivityIndicator size="large" color="#6B4C9A" />
                <Text style={styles.loadingText}>Loading available slots...</Text>
              </View>
            ) : timeSlots.length === 0 ? (
              <View style={styles.emptyContainer}>
                <Text style={styles.emptyText}>No slots available for this date</Text>
              </View>
            ) : (
              timeSlots.map((slot) => (
                <TimeSlotCard
                  key={slot.id}
                  startTime={slot.startTime}
                  endTime={slot.endTime}
                  isSelected={selectedSlot?.id === slot.id}
                  isAvailable={slot.isAvailable}
                  onPress={() => setSelectedSlot(slot)}
                />
              ))
            )}
          </View>
        )}

        {currentStep === 'confirm' && (
          <View style={styles.stepContent}>
            <Text style={styles.stepTitle}>Confirm Appointment</Text>

            <View style={styles.confirmSection}>
              <Text style={styles.sectionTitle}>Pet</Text>
              <View style={styles.pickerContainer}>
                <Picker
                  selectedValue={selectedPet?.id}
                  onValueChange={(value: number) => {
                    const pet = pets.find(p => p.id === value);
                    setSelectedPet(pet || null);
                  }}
                >
                  {pets.map((pet) => (
                    <Picker.Item key={pet.id} label={pet.name} value={pet.id} />
                  ))}
                </Picker>
              </View>
            </View>

            <View style={styles.confirmSection}>
              <Text style={styles.sectionTitle}>Doctor</Text>
              <Text style={styles.confirmText}>{selectedDoctor?.name}</Text>
              <Text style={styles.confirmSubText}>
                {selectedDoctor?.specialization}
              </Text>
            </View>

            <View style={styles.confirmSection}>
              <Text style={styles.sectionTitle}>Date & Time</Text>
              <Text style={styles.confirmText}>
                {selectedDate?.toLocaleDateString()}
              </Text>
              <Text style={styles.confirmSubText}>
                {selectedSlot?.startTime} - {selectedSlot?.endTime}
              </Text>
            </View>

            <View style={styles.confirmSection}>
              <Text style={styles.sectionTitle}>Purpose of Visit</Text>
              <View style={styles.pickerContainer}>
                <Picker
                  selectedValue={purpose}
                  onValueChange={(value: string) => setPurpose(value)}
                >
                  <Picker.Item label="Vaccination" value="VACCINATION" />
                  <Picker.Item label="Diagnosis" value="DIAGNOSIS" />
                  <Picker.Item label="Deworming" value="DEWORMING" />
                  <Picker.Item label="Other" value="OTHER" />
                </Picker>
              </View>
            </View>
          </View>
        )}
      </ScrollView>

      <View style={styles.buttonFooter}>
        {currentStep !== 'doctor' && (
          <TouchableOpacity
            style={[styles.button, styles.previousButton]}
            onPress={() => {
              const steps: Step[] = ['doctor', 'date', 'time', 'confirm'];
              const currentIdx = steps.indexOf(currentStep);
              if (currentIdx > 0) {
                setCurrentStep(steps[currentIdx - 1]);
              }
            }}
            disabled={isBooking}
          >
            <Text style={styles.previousButtonText}>Previous</Text>
          </TouchableOpacity>
        )}

        <TouchableOpacity
          style={[
            styles.button,
            styles.nextButton,
            (currentStep === 'confirm' && isBooking) && styles.buttonDisabled,
          ]}
          onPress={() => {
            if (currentStep === 'doctor' && !selectedDoctor) {
              Alert.alert('Error', 'Please select a doctor');
              return;
            }
            if (currentStep === 'date' && !selectedDate) {
              Alert.alert('Error', 'Please select a date');
              return;
            }
            if (currentStep === 'time' && !selectedSlot) {
              Alert.alert('Error', 'Please select a time slot');
              return;
            }

            if (currentStep === 'confirm') {
              handleConfirmBooking();
            } else {
              const steps: Step[] = ['doctor', 'date', 'time', 'confirm'];
              const currentIdx = steps.indexOf(currentStep);
              if (currentIdx < steps.length - 1) {
                setCurrentStep(steps[currentIdx + 1]);
              }
            }
          }}
          disabled={isBooking || (currentStep === 'time' && isLoadingSlots)}
        >
          {isBooking ? (
            <ActivityIndicator color="#fff" size="small" />
          ) : (
            <Text style={styles.nextButtonText}>
              {currentStep === 'confirm' ? 'Confirm Booking' : 'Next'}
            </Text>
          )}
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollContent: {
    paddingVertical: 20,
    paddingBottom: 100,
  },
  stepsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  stepIndicator: {
    alignItems: 'center',
    flex: 1,
  },
  stepCircle: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#e0e0e0',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  stepCircleActive: {
    backgroundColor: '#6B4C9A',
  },
  stepCircleCompleted: {
    backgroundColor: '#34C759',
  },
  stepNumber: {
    fontSize: 16,
    fontWeight: '700',
    color: '#999',
  },
  stepNumberActive: {
    color: '#fff',
  },
  stepLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: '#666',
    textAlign: 'center',
  },
  stepContent: {
    paddingHorizontal: 0,
    marginBottom: 20,
  },
  stepTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
    marginBottom: 16,
    paddingHorizontal: 20,
  },
  filterContainer: {
    marginHorizontal: 20,
    marginBottom: 16,
  },
  filterLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#666',
    marginBottom: 8,
  },
  pickerContainer: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    overflow: 'hidden',
  },
  loadingContainer: {
    paddingVertical: 40,
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    color: '#666',
  },
  emptyContainer: {
    paddingVertical: 40,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
  },
  confirmSection: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginHorizontal: 20,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 12,
    fontWeight: '600',
    color: '#999',
    marginBottom: 8,
  },
  confirmText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  confirmSubText: {
    fontSize: 13,
    color: '#666',
  },
  buttonFooter: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    flexDirection: 'row',
    gap: 12,
    padding: 16,
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#f0f0f0',
  },
  button: {
    flex: 1,
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 48,
  },
  previousButton: {
    backgroundColor: '#e0e0e0',
  },
  previousButtonText: {
    color: '#333',
    fontSize: 14,
    fontWeight: '700',
  },
  nextButton: {
    backgroundColor: '#6B4C9A',
  },
  nextButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
});

export default BookAppointmentScreen;
