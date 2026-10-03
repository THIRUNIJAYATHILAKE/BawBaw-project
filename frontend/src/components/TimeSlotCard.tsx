import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';

interface TimeSlotCardProps {
  startTime: string;
  endTime: string;
  isSelected?: boolean;
  isAvailable?: boolean;
  onPress?: () => void;
}

const TimeSlotCard: React.FC<TimeSlotCardProps> = ({
  startTime,
  endTime,
  isSelected = false,
  isAvailable = true,
  onPress,
}) => {
  return (
    <TouchableOpacity
      style={[
        styles.container,
        isSelected && styles.selected,
        !isAvailable && styles.unavailable,
      ]}
      onPress={onPress}
      disabled={!isAvailable}
      activeOpacity={0.7}
    >
      <View style={styles.timeContainer}>
        <Text style={[styles.time, isSelected && styles.selectedText]}>
          {startTime}
        </Text>
        <Text style={[styles.dash, isSelected && styles.selectedText]}>-</Text>
        <Text style={[styles.time, isSelected && styles.selectedText]}>
          {endTime}
        </Text>
      </View>
      {isSelected && <Text style={styles.checkmark}>✓</Text>}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    backgroundColor: '#f5f5f5',
    marginVertical: 8,
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  selected: {
    backgroundColor: '#6B4C9A',
    borderColor: '#6B4C9A',
  },
  unavailable: {
    opacity: 0.5,
  },
  timeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  time: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
  },
  dash: {
    fontSize: 14,
    color: '#999',
  },
  selectedText: {
    color: '#fff',
  },
  checkmark: {
    fontSize: 18,
    color: '#fff',
    fontWeight: '700',
  },
});

export default TimeSlotCard;
