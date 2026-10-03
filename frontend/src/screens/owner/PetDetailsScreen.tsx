import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Image,
  ActivityIndicator,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import apiClient from '@services/api';
import LoadingSpinner from '@components/LoadingSpinner';
import ErrorAlert, { showAlert } from '@components/ErrorAlert';
import { formatDate, calculateAge } from '@utils/dateFormatter';

interface Pet {
  id: number;
  name: string;
  species: string;
  breed: string;
  dateOfBirth: string;
  photoUrl?: string;
  weight?: number;
  sex?: string;
  microchipId?: string;
}

interface MedicalRecord {
  id: number;
  type: string;
  date: string;
  description: string;
  notes?: string;
}

interface Props {
  navigation: any;
  route: any;
}

const PetDetailsScreen: React.FC<Props> = ({ navigation, route }) => {
  const { petId } = route.params;
  const [pet, setPet] = useState<Pet | null>(null);
  const [vaccinations, setVaccinations] = useState<MedicalRecord[]>([]);
  const [deworming, setDeworming] = useState<MedicalRecord[]>([]);
  const [medicalRecords, setMedicalRecords] = useState<MedicalRecord[]>([]);
  const [activeTab, setActiveTab] = useState<'info' | 'vaccinations' | 'deworming' | 'medical'>('info');
  const [isLoading, setIsLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');

  useFocusEffect(
    useCallback(() => {
      loadPetDetails();
    }, [])
  );

  const loadPetDetails = async () => {
    try {
      setErrorMessage('');
      const [petRes, vaccRes, dewRes, medRes] = await Promise.all([
        apiClient.getPetById(petId),
        apiClient.getVaccinations(petId),
        apiClient.getDewormingRecords(petId),
        apiClient.getMedicalRecords(petId),
      ]);

      setPet(petRes.data);
      setVaccinations(vaccRes.data || []);
      setDeworming(dewRes.data || []);
      setMedicalRecords(medRes.data || []);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load pet details';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeletePet = () => {
    showAlert(
      'Delete Pet',
      `Are you sure you want to delete ${pet?.name}? This action cannot be undone.`,
      {
        confirmText: 'Delete',
        cancelText: 'Cancel',
        onConfirm: async () => {
          try {
            setIsDeleting(true);
            await apiClient.deletePet(petId);
            navigation.goBack();
          } catch (error: any) {
            const message =
              error.response?.data?.message ||
              error.message ||
              'Failed to delete pet';
            setErrorMessage(message);
          } finally {
            setIsDeleting(false);
          }
        },
      }
    );
  };

  const handleEditPet = () => {
    navigation.navigate('EditPet', { petId, pet });
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!pet) {
    return (
      <View style={styles.container}>
        <Text style={styles.errorTitle}>Pet not found</Text>
      </View>
    );
  }

  const petAge = calculateAge(new Date(pet.dateOfBirth));

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
        <View style={styles.photoSection}>
          {pet.photoUrl ? (
            <Image
              source={{ uri: pet.photoUrl }}
              style={styles.petPhoto}
            />
          ) : (
            <View style={styles.photoPlaceholder}>
              <Text style={styles.placeholderText}>🐾</Text>
            </View>
          )}
        </View>

        <View style={styles.headerSection}>
          <Text style={styles.petName}>{pet.name}</Text>
          <Text style={styles.petInfo}>
            {pet.species} • {pet.breed} • {petAge} years old
          </Text>
        </View>

        <View style={styles.tabContainer}>
          <TouchableOpacity
            style={[
              styles.tab,
              activeTab === 'info' && styles.activeTab,
            ]}
            onPress={() => setActiveTab('info')}
          >
            <Text
              style={[
                styles.tabText,
                activeTab === 'info' && styles.activeTabText,
              ]}
            >
              Info
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.tab,
              activeTab === 'vaccinations' && styles.activeTab,
            ]}
            onPress={() => setActiveTab('vaccinations')}
          >
            <Text
              style={[
                styles.tabText,
                activeTab === 'vaccinations' && styles.activeTabText,
              ]}
            >
              Vaccinations
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.tab,
              activeTab === 'deworming' && styles.activeTab,
            ]}
            onPress={() => setActiveTab('deworming')}
          >
            <Text
              style={[
                styles.tabText,
                activeTab === 'deworming' && styles.activeTabText,
              ]}
            >
              Deworming
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.tab,
              activeTab === 'medical' && styles.activeTab,
            ]}
            onPress={() => setActiveTab('medical')}
          >
            <Text
              style={[
                styles.tabText,
                activeTab === 'medical' && styles.activeTabText,
              ]}
            >
              Medical
            </Text>
          </TouchableOpacity>
        </View>

        {activeTab === 'info' && (
          <View style={styles.tabContent}>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Species</Text>
              <Text style={styles.infoValue}>{pet.species}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Breed</Text>
              <Text style={styles.infoValue}>{pet.breed}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Date of Birth</Text>
              <Text style={styles.infoValue}>
                {formatDate(new Date(pet.dateOfBirth))}
              </Text>
            </View>
            {pet.weight && (
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>Weight</Text>
                <Text style={styles.infoValue}>{pet.weight} kg</Text>
              </View>
            )}
            {pet.sex && (
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>Sex</Text>
                <Text style={styles.infoValue}>
                  {pet.sex === 'MALE' ? 'Male' : 'Female'}
                </Text>
              </View>
            )}
            {pet.microchipId && (
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>Microchip ID</Text>
                <Text style={styles.infoValue}>{pet.microchipId}</Text>
              </View>
            )}
          </View>
        )}

        {activeTab === 'vaccinations' && (
          <View style={styles.tabContent}>
            {vaccinations.length === 0 ? (
              <View style={styles.emptyMessage}>
                <Text style={styles.emptyText}>No vaccination records yet</Text>
              </View>
            ) : (
              <>
                {vaccinations.slice(0, 3).map((vac) => (
                  <View key={vac.id} style={styles.recordCard}>
                    <Text style={styles.recordType}>{vac.type}</Text>
                    <Text style={styles.recordDate}>
                      {formatDate(new Date(vac.date))}
                    </Text>
                    {vac.notes && (
                      <Text style={styles.recordNotes}>{vac.notes}</Text>
                    )}
                  </View>
                ))}
                {vaccinations.length > 0 && (
                  <TouchableOpacity
                    style={styles.viewMoreButton}
                    onPress={() =>
                      navigation.navigate('VaccinationSchedule', { petId, petName: pet.name })
                    }
                  >
                    <Text style={styles.viewMoreButtonText}>
                      View Vaccination Schedule →
                    </Text>
                  </TouchableOpacity>
                )}
              </>
            )}
          </View>
        )}

        {activeTab === 'deworming' && (
          <View style={styles.tabContent}>
            {deworming.length === 0 ? (
              <View style={styles.emptyMessage}>
                <Text style={styles.emptyText}>No deworming records yet</Text>
              </View>
            ) : (
              <>
                {deworming.slice(0, 3).map((dew) => (
                  <View key={dew.id} style={styles.recordCard}>
                    <Text style={styles.recordType}>{dew.type}</Text>
                    <Text style={styles.recordDate}>
                      {formatDate(new Date(dew.date))}
                    </Text>
                    {dew.notes && (
                      <Text style={styles.recordNotes}>{dew.notes}</Text>
                    )}
                  </View>
                ))}
                {deworming.length > 0 && (
                  <TouchableOpacity
                    style={styles.viewMoreButton}
                    onPress={() =>
                      navigation.navigate('MedicalHistory', { petId, petName: pet.name })
                    }
                  >
                    <Text style={styles.viewMoreButtonText}>
                      View Medical History →
                    </Text>
                  </TouchableOpacity>
                )}
              </>
            )}
          </View>
        )}

        {activeTab === 'medical' && (
          <View style={styles.tabContent}>
            {medicalRecords.length === 0 ? (
              <View style={styles.emptyMessage}>
                <Text style={styles.emptyText}>No medical records yet</Text>
              </View>
            ) : (
              <>
                {medicalRecords.slice(0, 3).map((record) => (
                  <View key={record.id} style={styles.recordCard}>
                    <Text style={styles.recordType}>{record.type}</Text>
                    <Text style={styles.recordDate}>
                      {formatDate(new Date(record.date))}
                    </Text>
                    <Text style={styles.recordDescription}>{record.description}</Text>
                    {record.notes && (
                      <Text style={styles.recordNotes}>{record.notes}</Text>
                    )}
                  </View>
                ))}
                {medicalRecords.length > 0 && (
                  <TouchableOpacity
                    style={styles.viewMoreButton}
                    onPress={() =>
                      navigation.navigate('MedicalHistory', { petId, petName: pet.name })
                    }
                  >
                    <Text style={styles.viewMoreButtonText}>
                      View Full Medical History →
                    </Text>
                  </TouchableOpacity>
                )}
              </>
            )}
          </View>
        )}

        <TouchableOpacity
          style={styles.bookButton}
          onPress={() =>
            navigation.navigate('Appointments', {
              screen: 'BookAppointment',
              params: { petId },
            })
          }
        >
          <Text style={styles.bookButtonText}>📅 Book Appointment</Text>
        </TouchableOpacity>

        <View style={styles.buttonContainer}>
          <TouchableOpacity
            style={styles.editButton}
            onPress={handleEditPet}
            disabled={isDeleting}
          >
            <Text style={styles.editButtonText}>Edit Pet</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[
              styles.deleteButton,
              isDeleting && styles.buttonDisabled,
            ]}
            onPress={handleDeletePet}
            disabled={isDeleting}
          >
            {isDeleting ? (
              <ActivityIndicator color="#FF3B30" size="small" />
            ) : (
              <Text style={styles.deleteButtonText}>Delete Pet</Text>
            )}
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollContent: {
    paddingBottom: 30,
  },
  photoSection: {
    alignItems: 'center',
    paddingVertical: 20,
  },
  petPhoto: {
    width: 150,
    height: 150,
    borderRadius: 12,
  },
  photoPlaceholder: {
    width: 150,
    height: 150,
    borderRadius: 12,
    backgroundColor: '#e0e0e0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    fontSize: 48,
  },
  headerSection: {
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 24,
  },
  petName: {
    fontSize: 24,
    fontWeight: '700',
    color: '#333',
    marginBottom: 4,
  },
  petInfo: {
    fontSize: 14,
    color: '#666',
  },
  tabContainer: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
    marginHorizontal: 20,
    marginBottom: 20,
  },
  tab: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
  },
  activeTab: {
    borderBottomWidth: 3,
    borderBottomColor: '#007AFF',
  },
  tabText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#999',
  },
  activeTabText: {
    color: '#007AFF',
  },
  tabContent: {
    paddingHorizontal: 20,
  },
  infoRow: {
    backgroundColor: '#fff',
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 8,
    borderRadius: 8,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  infoLabel: {
    fontSize: 14,
    color: '#666',
    fontWeight: '500',
  },
  infoValue: {
    fontSize: 14,
    color: '#333',
    fontWeight: '600',
  },
  emptyMessage: {
    paddingVertical: 40,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
  },
  recordCard: {
    backgroundColor: '#fff',
    padding: 16,
    marginBottom: 12,
    borderRadius: 8,
  },
  recordType: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  recordDate: {
    fontSize: 13,
    color: '#999',
    marginBottom: 8,
  },
  recordDescription: {
    fontSize: 13,
    color: '#666',
    marginBottom: 8,
  },
  recordNotes: {
    fontSize: 13,
    color: '#666',
    fontStyle: 'italic',
  },
  viewMoreButton: {
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
    marginTop: 12,
    marginHorizontal: 20,
  },
  viewMoreButtonText: {
    color: '#6B4C9A',
    fontSize: 14,
    fontWeight: '600',
  },
  errorTitle: {
    fontSize: 16,
    color: '#FF3B30',
    textAlign: 'center',
    marginTop: 20,
  },
  bookButton: {
    marginHorizontal: 20,
    marginTop: 20,
    marginBottom: 12,
    backgroundColor: '#6B4C9A',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  bookButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 12,
    paddingHorizontal: 20,
    marginTop: 12,
    marginBottom: 30,
  },
  editButton: {
    flex: 1,
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  editButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
  deleteButton: {
    flex: 1,
    backgroundColor: '#FF3B30',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  deleteButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
});

export default PetDetailsScreen;
