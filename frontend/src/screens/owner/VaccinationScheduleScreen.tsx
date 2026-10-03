import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import apiClient from '@services/api';
import LoadingSpinner from '@components/LoadingSpinner';
import ErrorAlert from '@components/ErrorAlert';
import { calculateAge } from '@utils/dateFormatter';

interface Vaccine {
  name: string;
  frequency: string;
  ageRecommendation: string;
  status: 'DUE' | 'SCHEDULED' | 'COMPLETED';
}

interface VaccinationRecord {
  id: number;
  vaccineName: string;
  date: string;
  nextDueDate: string;
  doctorName?: string;
  notes?: string;
}

interface Pet {
  id: number;
  name: string;
  dateOfBirth: string;
  species: string;
}

interface Props {
  navigation: any;
  route: any;
}

const wsavaVaccines: Vaccine[] = [
  {
    name: 'DHPP (Distemper, Hepatitis, Parvovirus, Parainfluenza)',
    frequency: 'Every 3 years',
    ageRecommendation: '6-16 weeks, then annually for 1 year',
    status: 'DUE',
  },
  {
    name: 'Rabies',
    frequency: 'Every 1-3 years',
    ageRecommendation: '12-16 weeks, then annually',
    status: 'SCHEDULED',
  },
  {
    name: 'Bordetella (Kennel Cough)',
    frequency: 'Annually',
    ageRecommendation: '6 weeks or older',
    status: 'COMPLETED',
  },
  {
    name: 'Leptospirosis',
    frequency: 'Every 1-2 years',
    ageRecommendation: '8-12 weeks',
    status: 'DUE',
  },
  {
    name: 'Lyme Disease',
    frequency: 'Every 1-2 years',
    ageRecommendation: '12 weeks or older',
    status: 'COMPLETED',
  },
];

const VaccinationScheduleScreen: React.FC<Props> = ({ navigation, route }) => {
  const { petId } = route.params;
  const [pet, setPet] = useState<Pet | null>(null);
  const [vaccinations, setVaccinations] = useState<VaccinationRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [showAllVaccinations, setShowAllVaccinations] = useState(false);

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [])
  );

  const loadData = async () => {
    try {
      setErrorMessage('');
      const [petRes, vaccRes] = await Promise.all([
        apiClient.getPetById(petId),
        apiClient.getVaccinations(petId),
      ]);

      setPet(petRes.data);
      setVaccinations(vaccRes.data || []);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load vaccination data';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setIsRefreshing(true);
    loadData();
  };

  const handleBookAppointment = () => {
    navigation.navigate('Appointments', {
      screen: 'BookAppointment',
      params: { petId },
    });
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  const petAge = pet ? calculateAge(new Date(pet.dateOfBirth)) : 0;

  const getVaccineStatus = (vaccine: Vaccine): string => {
    const completed = vaccinations.find(
      v => v.vaccineName.toUpperCase().includes(vaccine.name.split('(')[0].trim())
    );
    return completed ? 'COMPLETED' : vaccine.status;
  };

  const getStatusColor = (status: string): string => {
    switch (status) {
      case 'DUE':
        return '#FF9500';
      case 'SCHEDULED':
        return '#34C759';
      case 'COMPLETED':
        return '#34C759';
      default:
        return '#007AFF';
    }
  };

  const getStatusIcon = (status: string): string => {
    switch (status) {
      case 'DUE':
        return '⚠️';
      case 'SCHEDULED':
        return '✓';
      case 'COMPLETED':
        return '✓';
      default:
        return '•';
    }
  };

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

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        refreshControl={
          <RefreshControl
            refreshing={isRefreshing}
            onRefresh={handleRefresh}
            colors={['#6B4C9A']}
          />
        }
      >
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Vaccination Schedule</Text>
          <Text style={styles.headerSubtitle}>{pet?.name} • {petAge} years old</Text>
        </View>

        <TouchableOpacity
          style={styles.scheduleButton}
          onPress={handleBookAppointment}
        >
          <Text style={styles.scheduleButtonText}>📅 Schedule Vaccination</Text>
        </TouchableOpacity>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Recommended Vaccines (WSAVA Guidelines)</Text>
          <Text style={styles.sectionSubtitle}>Age-appropriate vaccines for {pet?.species}</Text>

          {wsavaVaccines.map((vaccine, idx) => {
            const status = getVaccineStatus(vaccine);
            const statusColor = getStatusColor(status);
            const statusIcon = getStatusIcon(status);

            return (
              <View key={idx} style={styles.vaccineCard}>
                <View style={styles.vaccineHeader}>
                  <View style={styles.vaccineInfo}>
                    <Text style={styles.vaccineName}>{vaccine.name}</Text>
                    <Text style={styles.vaccineDetails}>
                      Frequency: {vaccine.frequency}
                    </Text>
                    <Text style={styles.vaccineDetails}>
                      Recommendation: {vaccine.ageRecommendation}
                    </Text>
                  </View>

                  <View
                    style={[
                      styles.statusBadge,
                      { backgroundColor: statusColor },
                    ]}
                  >
                    <Text style={styles.statusIcon}>{statusIcon}</Text>
                    <Text style={styles.statusText}>{status}</Text>
                  </View>
                </View>
              </View>
            );
          })}
        </View>

        <View style={styles.section}>
          <View style={styles.sectionHeaderRow}>
            <Text style={styles.sectionTitle}>Vaccination History</Text>
            <TouchableOpacity
              onPress={() => setShowAllVaccinations(!showAllVaccinations)}
            >
              <Text style={styles.seeAllLink}>
                {showAllVaccinations ? 'Show Less' : 'Show All'}
              </Text>
            </TouchableOpacity>
          </View>

          {vaccinations.length === 0 ? (
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyText}>No vaccination records yet</Text>
            </View>
          ) : (
            (showAllVaccinations ? vaccinations : vaccinations.slice(0, 3)).map(
              (vacc) => (
                <View key={vacc.id} style={styles.historyCard}>
                  <View style={styles.historyDate}>
                    <Text style={styles.historyDateDay}>
                      {new Date(vacc.date).getDate()}
                    </Text>
                    <Text style={styles.historyDateMonth}>
                      {new Date(vacc.date).toLocaleString('default', { month: 'short' })}
                    </Text>
                  </View>

                  <View style={styles.historyContent}>
                    <Text style={styles.historyVaccineName}>
                      {vacc.vaccineName}
                    </Text>
                    {vacc.doctorName && (
                      <Text style={styles.historyDoctor}>
                        👨‍⚕️ {vacc.doctorName}
                      </Text>
                    )}
                    {vacc.nextDueDate && (
                      <Text style={styles.historyNextDue}>
                        Next Due: {new Date(vacc.nextDueDate).toLocaleDateString()}
                      </Text>
                    )}
                    {vacc.notes && (
                      <Text style={styles.historyNotes}>{vacc.notes}</Text>
                    )}
                  </View>

                  <View style={styles.historyBadge}>
                    <Text style={styles.completedIcon}>✓</Text>
                  </View>
                </View>
              )
            )
          )}
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
    paddingVertical: 12,
    paddingBottom: 20,
  },
  header: {
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingVertical: 16,
    marginBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#333',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 13,
    color: '#999',
  },
  scheduleButton: {
    marginHorizontal: 20,
    marginBottom: 16,
    backgroundColor: '#6B4C9A',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  scheduleButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
  section: {
    backgroundColor: '#fff',
    borderRadius: 12,
    marginHorizontal: 20,
    marginBottom: 16,
    overflow: 'hidden',
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 16,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#333',
    paddingHorizontal: 16,
    paddingTop: 16,
    marginBottom: 4,
  },
  sectionSubtitle: {
    fontSize: 12,
    color: '#999',
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  seeAllLink: {
    fontSize: 13,
    color: '#6B4C9A',
    fontWeight: '600',
    paddingRight: 16,
  },
  vaccineCard: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  vaccineHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  vaccineInfo: {
    flex: 1,
    marginRight: 12,
  },
  vaccineName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  vaccineDetails: {
    fontSize: 12,
    color: '#666',
    marginBottom: 2,
  },
  statusBadge: {
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 6,
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 80,
  },
  statusIcon: {
    fontSize: 16,
    marginBottom: 2,
  },
  statusText: {
    color: '#fff',
    fontSize: 11,
    fontWeight: '600',
  },
  historyCard: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
    alignItems: 'center',
  },
  historyDate: {
    width: 50,
    alignItems: 'center',
    marginRight: 12,
  },
  historyDateDay: {
    fontSize: 16,
    fontWeight: '700',
    color: '#6B4C9A',
  },
  historyDateMonth: {
    fontSize: 10,
    color: '#999',
  },
  historyContent: {
    flex: 1,
  },
  historyVaccineName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 2,
  },
  historyDoctor: {
    fontSize: 12,
    color: '#666',
    marginBottom: 2,
  },
  historyNextDue: {
    fontSize: 11,
    color: '#999',
  },
  historyNotes: {
    fontSize: 11,
    color: '#666',
    fontStyle: 'italic',
    marginTop: 2,
  },
  historyBadge: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#34C759',
    justifyContent: 'center',
    alignItems: 'center',
  },
  completedIcon: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '700',
  },
  emptyContainer: {
    paddingVertical: 20,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
  },
});

export default VaccinationScheduleScreen;
