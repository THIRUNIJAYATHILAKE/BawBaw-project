import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';

interface MedicalRecordCardProps {
  date: string;
  type: string;
  doctorName?: string;
  description: string;
  diagnosis?: string;
  treatment?: string;
  medications?: string[];
  isExpanded?: boolean;
  onPress?: () => void;
}

const getTypeIcon = (type: string): string => {
  switch (type?.toUpperCase()) {
    case 'VACCINATION':
    case 'VACCINE':
      return '💉';
    case 'SURGERY':
      return '🏥';
    case 'DEWORMING':
      return '🐛';
    default:
      return '📋';
  }
};

const getTypeColor = (type: string): string => {
  switch (type?.toUpperCase()) {
    case 'VACCINATION':
    case 'VACCINE':
      return '#34C759';
    case 'SURGERY':
      return '#FF3B30';
    case 'DEWORMING':
      return '#FF9500';
    default:
      return '#007AFF';
  }
};

const MedicalRecordCard: React.FC<MedicalRecordCardProps> = ({
  date,
  type,
  doctorName,
  description,
  diagnosis,
  treatment,
  medications,
  isExpanded = false,
  onPress,
}) => {
  const typeColor = getTypeColor(type);

  return (
    <TouchableOpacity
      style={styles.container}
      onPress={onPress}
      activeOpacity={0.7}
    >
      <View style={styles.timelineMarker}>
        <View style={[styles.markerCircle, { backgroundColor: typeColor }]} />
        <View style={[styles.markerLine, { backgroundColor: typeColor }]} />
      </View>

      <View style={styles.content}>
        <View style={styles.headerRow}>
          <View style={styles.typeContainer}>
            <Text style={styles.typeIcon}>{getTypeIcon(type)}</Text>
            <Text style={[styles.type, { color: typeColor }]}>{type}</Text>
          </View>
          <Text style={styles.date}>{date}</Text>
        </View>

        <Text style={styles.description}>{description}</Text>

        {doctorName && (
          <Text style={styles.doctorName}>👨‍⚕️ {doctorName}</Text>
        )}

        {isExpanded && (
          <>
            {diagnosis && (
              <View style={styles.expandedSection}>
                <Text style={styles.sectionLabel}>Diagnosis:</Text>
                <Text style={styles.sectionValue}>{diagnosis}</Text>
              </View>
            )}
            {treatment && (
              <View style={styles.expandedSection}>
                <Text style={styles.sectionLabel}>Treatment:</Text>
                <Text style={styles.sectionValue}>{treatment}</Text>
              </View>
            )}
            {medications && medications.length > 0 && (
              <View style={styles.expandedSection}>
                <Text style={styles.sectionLabel}>Medications:</Text>
                {medications.map((med, idx) => (
                  <Text key={idx} style={styles.medicationItem}>
                    • {med}
                  </Text>
                ))}
              </View>
            )}
          </>
        )}

        {isExpanded && <Text style={styles.expandIcon}>▲</Text>}
        {!isExpanded && <Text style={styles.expandIcon}>▼</Text>}
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    marginVertical: 8,
    marginHorizontal: 20,
  },
  timelineMarker: {
    width: 30,
    alignItems: 'center',
    paddingVertical: 8,
  },
  markerCircle: {
    width: 12,
    height: 12,
    borderRadius: 6,
    borderWidth: 2,
    borderColor: '#fff',
  },
  markerLine: {
    width: 2,
    flex: 1,
    marginTop: 4,
  },
  content: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 12,
    marginLeft: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  typeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  typeIcon: {
    fontSize: 18,
  },
  type: {
    fontSize: 14,
    fontWeight: '600',
  },
  date: {
    fontSize: 12,
    color: '#999',
  },
  description: {
    fontSize: 13,
    color: '#666',
    marginBottom: 8,
    lineHeight: 18,
  },
  doctorName: {
    fontSize: 12,
    color: '#666',
    marginBottom: 8,
  },
  expandedSection: {
    marginTop: 8,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#f0f0f0',
  },
  sectionLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  sectionValue: {
    fontSize: 12,
    color: '#666',
    lineHeight: 18,
  },
  medicationItem: {
    fontSize: 12,
    color: '#666',
    marginVertical: 2,
  },
  expandIcon: {
    position: 'absolute',
    right: 12,
    top: 12,
    fontSize: 12,
    color: '#999',
  },
});

export default MedicalRecordCard;
