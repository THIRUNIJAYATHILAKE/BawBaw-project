import React from 'react';
import {
  View,
  Image,
  Text,
  StyleSheet,
  TouchableOpacity,
  ImageSourcePropType,
} from 'react-native';
import { formatDate } from '@utils/dateFormatter';

export interface PetCardData {
  id: number;
  name: string;
  species: string;
  breed: string;
  dateOfBirth: string;
  photoUrl?: string | ImageSourcePropType;
  weight?: number;
  sex?: string;
}

interface PetCardProps {
  pet: PetCardData;
  onPress?: (pet: PetCardData) => void;
  onEdit?: (pet: PetCardData) => void;
}

const PetCard: React.FC<PetCardProps> = ({ pet, onPress, onEdit }) => {
  const handlePress = () => {
    onPress?.(pet);
  };

  return (
    <TouchableOpacity
      style={styles.container}
      onPress={handlePress}
      activeOpacity={0.7}
    >
      <View style={styles.photoContainer}>
        {pet.photoUrl ? (
          typeof pet.photoUrl === 'string' ? (
            <Image
              source={{ uri: pet.photoUrl }}
              style={styles.photo}
              defaultSource={require('@assets/placeholder-pet.png')}
            />
          ) : (
            <Image
              source={pet.photoUrl as ImageSourcePropType}
              style={styles.photo}
            />
          )
        ) : (
          <View style={styles.placeholderPhoto}>
            <Text style={styles.placeholderText}>No Photo</Text>
          </View>
        )}
      </View>

      <View style={styles.contentContainer}>
        <Text style={styles.name}>{pet.name}</Text>
        <Text style={styles.details}>
          {pet.species} • {pet.breed}
        </Text>
        <Text style={styles.dob}>
          DOB: {formatDate(new Date(pet.dateOfBirth))}
        </Text>
        {pet.weight && (
          <Text style={styles.weight}>Weight: {pet.weight} kg</Text>
        )}
      </View>

      {onEdit && (
        <TouchableOpacity
          style={styles.editButton}
          onPress={() => onEdit(pet)}
          hitSlop={{ top: 10, right: 10, bottom: 10, left: 10 }}
        >
          <Text style={styles.editButtonText}>Edit</Text>
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 12,
    marginHorizontal: 16,
    marginVertical: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 3,
    alignItems: 'center',
  },
  photoContainer: {
    marginRight: 12,
  },
  photo: {
    width: 80,
    height: 80,
    borderRadius: 8,
    backgroundColor: '#f0f0f0',
  },
  placeholderPhoto: {
    width: 80,
    height: 80,
    borderRadius: 8,
    backgroundColor: '#e0e0e0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    color: '#999',
    fontSize: 12,
    fontWeight: '500',
  },
  contentContainer: {
    flex: 1,
  },
  name: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  details: {
    fontSize: 13,
    color: '#666',
    marginBottom: 4,
  },
  dob: {
    fontSize: 12,
    color: '#999',
    marginBottom: 4,
  },
  weight: {
    fontSize: 12,
    color: '#999',
  },
  editButton: {
    marginLeft: 8,
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: '#007AFF',
    borderRadius: 6,
  },
  editButtonText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
});

export default PetCard;
