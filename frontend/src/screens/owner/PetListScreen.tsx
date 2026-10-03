import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  RefreshControl,
  ActivityIndicator,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import apiClient from '@services/api';
import PetCard from '@components/PetCard';
import LoadingSpinner from '@components/LoadingSpinner';
import ErrorAlert from '@components/ErrorAlert';

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

interface Props {
  navigation: any;
  route?: any;
}

const PetListScreen: React.FC<Props> = ({ navigation }) => {
  const [pets, setPets] = useState<Pet[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');

  useFocusEffect(
    useCallback(() => {
      loadPets();
    }, [])
  );

  const loadPets = async () => {
    try {
      setErrorMessage('');
      const response = await apiClient.getPets();
      setPets(response.data || []);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load pets';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setIsRefreshing(true);
    loadPets();
  };

  const handleAddPet = () => {
    navigation.navigate('AddPet');
  };

  const handlePetPress = (pet: any) => {
    navigation.navigate('PetDetails', { petId: pet.id, pet });
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

      {pets.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>No Pets Yet</Text>
          <Text style={styles.emptySubtitle}>
            Add your first pet to get started with vaccinations and health tracking
          </Text>
          <TouchableOpacity
            style={styles.addButton}
            onPress={handleAddPet}
          >
            <Text style={styles.addButtonText}>+ Add Your First Pet</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={pets}
          keyExtractor={(item) => item.id.toString()}
          renderItem={({ item }) => (
            <PetCard
              pet={item}
              onPress={handlePetPress}
              onEdit={handlePetPress}
            />
          )}
          refreshControl={
            <RefreshControl
              refreshing={isRefreshing}
              onRefresh={handleRefresh}
              colors={['#007AFF']}
            />
          }
          contentContainerStyle={styles.listContent}
          ListFooterComponent={
            isRefreshing ? (
              <View style={styles.refreshingContainer}>
                <ActivityIndicator color="#007AFF" size="small" />
              </View>
            ) : null
          }
        />
      )}

      <TouchableOpacity
        style={styles.floatingButton}
        onPress={handleAddPet}
        activeOpacity={0.7}
      >
        <Text style={styles.floatingButtonText}>+</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  listContent: {
    paddingVertical: 8,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#333',
    marginBottom: 12,
  },
  emptySubtitle: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    marginBottom: 24,
    lineHeight: 20,
  },
  addButton: {
    backgroundColor: '#007AFF',
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  addButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  refreshingContainer: {
    paddingVertical: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  floatingButton: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: '#007AFF',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  floatingButtonText: {
    color: '#fff',
    fontSize: 28,
    fontWeight: '300',
    lineHeight: 32,
  },
});

export default PetListScreen;
