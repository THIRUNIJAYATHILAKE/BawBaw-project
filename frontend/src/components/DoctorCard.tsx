import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';

interface DoctorCardProps {
  id: number;
  name: string;
  specialization: string;
  rating: number;
  availableSlots: number;
  photoUrl?: string;
  isSelected?: boolean;
  onPress?: () => void;
}

const DoctorCard: React.FC<DoctorCardProps> = ({
  name,
  specialization,
  rating,
  availableSlots,
  photoUrl,
  isSelected = false,
  onPress,
}) => {
  return (
    <TouchableOpacity
      style={[
        styles.container,
        isSelected && styles.selected,
      ]}
      onPress={onPress}
      activeOpacity={0.7}
    >
      <View style={styles.photoContainer}>
        {photoUrl ? (
          <Image
            source={{ uri: photoUrl }}
            style={styles.photo}
          />
        ) : (
          <View style={styles.photoPlaceholder}>
            <Text style={styles.placeholderText}>👨‍⚕️</Text>
          </View>
        )}
      </View>

      <View style={styles.content}>
        <Text style={styles.name}>{name}</Text>
        <Text style={styles.specialization}>{specialization}</Text>

        <View style={styles.bottomRow}>
          <View style={styles.ratingContainer}>
            <Text style={styles.ratingIcon}>⭐</Text>
            <Text style={styles.rating}>{rating.toFixed(1)}</Text>
          </View>
          <Text style={styles.slots}>
            {availableSlots} slots available
          </Text>
        </View>
      </View>

      {isSelected && (
        <View style={styles.selectionIndicator}>
          <Text style={styles.checkmark}>✓</Text>
        </View>
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
    marginHorizontal: 20,
    marginVertical: 8,
    borderWidth: 2,
    borderColor: '#f0f0f0',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  selected: {
    borderColor: '#6B4C9A',
    backgroundColor: '#f8f5ff',
  },
  photoContainer: {
    marginRight: 12,
  },
  photo: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#f0f0f0',
  },
  photoPlaceholder: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#e8e0f5',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    fontSize: 28,
  },
  content: {
    flex: 1,
  },
  name: {
    fontSize: 15,
    fontWeight: '600',
    color: '#333',
    marginBottom: 2,
  },
  specialization: {
    fontSize: 12,
    color: '#666',
    marginBottom: 8,
  },
  bottomRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  ratingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  ratingIcon: {
    fontSize: 12,
  },
  rating: {
    fontSize: 12,
    fontWeight: '600',
    color: '#333',
  },
  slots: {
    fontSize: 11,
    color: '#999',
  },
  selectionIndicator: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#6B4C9A',
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: 8,
  },
  checkmark: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
});

export default DoctorCard;
