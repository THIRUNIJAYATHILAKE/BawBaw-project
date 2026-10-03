import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  RefreshControl,
  FlatList,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import apiClient from '@services/api';
import { useAuth } from '@context/AuthContext';
import LoadingSpinner from '@components/LoadingSpinner';
import ErrorAlert from '@components/ErrorAlert';
import PetCard from '@components/PetCard';

interface Pet {
  id: number;
  name: string;
  species: string;
  breed: string;
  dateOfBirth: string;
  photoUrl?: string;
  weight?: number;
  sex?: string;
}

interface Appointment {
  id: number;
  petId: number;
  doctorName: string;
  appointmentDate: string;
  status: string;
}

interface Props {
  navigation: any;
  route?: any;
}

const OwnerDashboardScreen: React.FC<Props> = ({ navigation }) => {
  const { user } = useAuth();
  const [pets, setPets] = useState<Pet[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');

  useFocusEffect(
    useCallback(() => {
      loadDashboardData();
    }, [])
  );

  const loadDashboardData = async () => {
    try {
      setErrorMessage('');
      const [petsRes, appointmentsRes] = await Promise.all([
        apiClient.getPets(),
        apiClient.getAppointments(),
      ]);

      setPets(petsRes.data || []);
      setAppointments(appointmentsRes.data || []);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load dashboard';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setIsRefreshing(true);
    loadDashboardData();
  };

  const upcomingAppointments = appointments.filter(
    (apt) => new Date(apt.appointmentDate) > new Date()
  ).length;

  const handlePetPress = (pet: any) => {
    navigation.navigate('Pets', {
      screen: 'PetDetails',
      params: { petId: pet.id, pet },
    });
  };

  const handleBookAppointment = () => {
    navigation.navigate('Appointments', {
      screen: 'BookAppointment',
    });
  };

  const handleViewVaccinations = () => {
    navigation.navigate('Pets', {
      screen: 'PetList',
    });
  };

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

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        refreshControl={
          <RefreshControl
            refreshing={isRefreshing}
            onRefresh={handleRefresh}
            colors={['#007AFF']}
          />
        }
      >
        <View style={styles.headerSection}>
          <Text style={styles.greeting}>Hello, {user?.fullName?.split(' ')[0]}! 👋</Text>
          <Text style={styles.subtitle}>Your pet's health at a glance</Text>
        </View>

        <View style={styles.statsContainer}>
          <View style={styles.statCard}>
            <Text style={styles.statValue}>{pets.length}</Text>
            <Text style={styles.statLabel}>Total Pets</Text>
          </View>
          <View style={styles.statCard}>
            <Text style={styles.statValue}>{upcomingAppointments}</Text>
            <Text style={styles.statLabel}>Upcoming Appointments</Text>
          </View>
          <View style={styles.statCard}>
            <Text style={styles.statValue}>{Math.max(0, pets.length - 1)}</Text>
            <Text style={styles.statLabel}>Vaccinations Due</Text>
          </View>
        </View>

        <View style={styles.actionButtonsContainer}>
          <TouchableOpacity
            style={styles.actionButton}
            onPress={handleBookAppointment}
          >
            <Text style={styles.actionButtonText}>📅 Book Appointment</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionButton}
            onPress={handleViewVaccinations}
          >
            <Text style={styles.actionButtonText}>💉 View Vaccinations</Text>
          </TouchableOpacity>
        </View>

        {pets.length > 0 && (
          <View style={styles.petsSection}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Your Pets</Text>
              <TouchableOpacity
                onPress={() =>
                  navigation.navigate('Pets', {
                    screen: 'PetList',
                  })
                }
              >
                <Text style={styles.seeAllLink}>See All</Text>
              </TouchableOpacity>
            </View>

            <FlatList
              data={pets.slice(0, 2)}
              keyExtractor={(item: Pet) => item.id.toString()}
              renderItem={({ item }: { item: Pet }) => (
                <PetCard
                  pet={item}
                  onPress={handlePetPress}
                  onEdit={handlePetPress}
                />
              )}
              scrollEnabled={false}
            />
          </View>
        )}

        <View style={styles.addPetSection}>
          <Text style={styles.sectionTitle}>Add Your Pet</Text>
          <TouchableOpacity
            style={styles.addPetButton}
            onPress={() =>
              navigation.navigate('Pets', {
                screen: 'AddPet',
              })
            }
          >
            <Text style={styles.addPetButtonText}>+ Add New Pet</Text>
          </TouchableOpacity>
        </View>

        {appointments.length > 0 && (
          <View style={styles.appointmentsSection}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Upcoming Appointments</Text>
            </View>

            {appointments
              .filter((apt) => new Date(apt.appointmentDate) > new Date())
              .slice(0, 3)
              .map((apt) => (
                <View key={apt.id} style={styles.appointmentCard}>
                  <View style={styles.appointmentDate}>
                    <Text style={styles.appointmentDateText}>
                      {new Date(apt.appointmentDate).getDate()}
                    </Text>
                    <Text style={styles.appointmentMonth}>
                      {new Date(apt.appointmentDate).toLocaleString('default', {
                        month: 'short',
                      })}
                    </Text>
                  </View>
                  <View style={styles.appointmentInfo}>
                    <Text style={styles.appointmentDoctor}>{apt.doctorName}</Text>
                    <Text style={styles.appointmentTime}>
                      {new Date(apt.appointmentDate).toLocaleTimeString('en-US', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </Text>
                  </View>
                  <View style={[styles.appointmentBadge, 
                    apt.status === 'CONFIRMED' && { backgroundColor: '#34C759' }
                  ]}>
                    <Text style={styles.appointmentBadgeText}>{apt.status}</Text>
                  </View>
                </View>
              ))}
          </View>
        )}
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
    paddingBottom: 20,
  },
  headerSection: {
    paddingHorizontal: 20,
    paddingVertical: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  greeting: {
    fontSize: 24,
    fontWeight: '700',
    color: '#333',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 14,
    color: '#999',
  },
  statsContainer: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    paddingVertical: 16,
    gap: 12,
  },
  statCard: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  statValue: {
    fontSize: 24,
    fontWeight: '700',
    color: '#007AFF',
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 12,
    color: '#999',
    textAlign: 'center',
  },
  actionButtonsContainer: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    gap: 12,
    marginBottom: 20,
  },
  actionButton: {
    flex: 1,
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  actionButtonText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '600',
  },
  petsSection: {
    marginBottom: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#333',
  },
  seeAllLink: {
    fontSize: 13,
    color: '#007AFF',
    fontWeight: '600',
  },
  addPetSection: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  addPetButton: {
    backgroundColor: '#fff',
    borderWidth: 2,
    borderColor: '#007AFF',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
    marginTop: 12,
  },
  addPetButtonText: {
    color: '#007AFF',
    fontSize: 14,
    fontWeight: '600',
  },
  appointmentsSection: {
    paddingHorizontal: 20,
  },
  appointmentCard: {
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
  },
  appointmentDate: {
    width: 50,
    alignItems: 'center',
    paddingRight: 12,
  },
  appointmentDateText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#007AFF',
  },
  appointmentMonth: {
    fontSize: 11,
    color: '#999',
  },
  appointmentInfo: {
    flex: 1,
  },
  appointmentDoctor: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 2,
  },
  appointmentTime: {
    fontSize: 12,
    color: '#999',
  },
  appointmentBadge: {
    backgroundColor: '#FFA500',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  appointmentBadgeText: {
    color: '#fff',
    fontSize: 10,
    fontWeight: '600',
  },
});

export default OwnerDashboardScreen;
