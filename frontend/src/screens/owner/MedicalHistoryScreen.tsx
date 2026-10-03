import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { useFocusEffect } from '@react-navigation/native';
import apiClient from '@services/api';
import LoadingSpinner from '@components/LoadingSpinner';
import ErrorAlert from '@components/ErrorAlert';
import MedicalRecordCard from '@components/MedicalRecordCard';

interface MedicalRecord {
  id: number;
  petId: number;
  type: string;
  date: string;
  description: string;
  diagnosis?: string;
  treatment?: string;
  medications?: string[];
  doctorName?: string;
}

interface Props {
  navigation: any;
  route: any;
}

const MedicalHistoryScreen: React.FC<Props> = ({ route }) => {
  const { petId, petName } = route.params;
  const [records, setRecords] = useState<MedicalRecord[]>([]);
  const [filteredRecords, setFilteredRecords] = useState<MedicalRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [filterType, setFilterType] = useState<string>('ALL');
  const [sortOrder, setSortOrder] = useState<'recent' | 'oldest'>('recent');
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useFocusEffect(
    useCallback(() => {
      loadMedicalRecords();
    }, [])
  );

  const loadMedicalRecords = async () => {
    try {
      setErrorMessage('');
      const response = await apiClient.getMedicalRecords(petId);
      setRecords(response.data || []);
    } catch (error: any) {
      const message =
        error.response?.data?.message ||
        error.message ||
        'Failed to load medical records';
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setIsRefreshing(true);
    loadMedicalRecords();
  };

  React.useEffect(() => {
    let filtered = records;

    if (filterType !== 'ALL') {
      filtered = filtered.filter(r => r.type.toUpperCase() === filterType);
    }

    filtered.sort((a, b) => {
      const dateA = new Date(a.date).getTime();
      const dateB = new Date(b.date).getTime();
      return sortOrder === 'recent' ? dateB - dateA : dateA - dateB;
    });

    setFilteredRecords(filtered);
  }, [records, filterType, sortOrder]);

  if (isLoading) {
    return <LoadingSpinner />;
  }

  // recordTypes not used, but kept for reference
  Array.from(new Set(records.map(r => r.type.toUpperCase())));

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

      <View style={styles.header}>
        <Text style={styles.headerTitle}>Medical History</Text>
        <Text style={styles.headerSubtitle}>{petName}</Text>
      </View>

      <View style={styles.controlsContainer}>
        <View style={styles.controlRow}>
          <View style={styles.filterGroup}>
            <Text style={styles.label}>Filter:</Text>
            <View style={[styles.pickerContainer, styles.smallPicker]}>
              <Picker
                selectedValue={filterType}
                onValueChange={(value: string) => setFilterType(value)}
              >
                <Picker.Item label="All Records" value="ALL" />
                <Picker.Item label="Vaccination" value="VACCINATION" />
                <Picker.Item label="Surgery" value="SURGERY" />
                <Picker.Item label="Medical" value="MEDICAL" />
                <Picker.Item label="Deworming" value="DEWORMING" />
              </Picker>
            </View>
          </View>

          <View style={styles.filterGroup}>
            <Text style={styles.label}>Sort:</Text>
            <View style={[styles.pickerContainer, styles.smallPicker]}>
              <Picker
                selectedValue={sortOrder}
                onValueChange={(value: any) => setSortOrder(value)}
              >
                <Picker.Item label="Recent First" value="recent" />
                <Picker.Item label="Oldest First" value="oldest" />
              </Picker>
            </View>
          </View>
        </View>
      </View>

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
        {filteredRecords.length === 0 ? (
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyIcon}>📋</Text>
            <Text style={styles.emptyTitle}>No Medical Records</Text>
            <Text style={styles.emptySubtitle}>
              {filterType !== 'ALL'
                ? 'No records found for this type'
                : 'No medical history available yet'}
            </Text>
          </View>
        ) : (
          <View style={styles.timelineContainer}>
            {filteredRecords.map((record) => (
              <TouchableOpacity
                key={record.id}
                onPress={() =>
                  setExpandedId(expandedId === record.id ? null : record.id)
                }
                activeOpacity={0.7}
              >
                <MedicalRecordCard
                  date={new Date(record.date).toLocaleDateString()}
                  type={record.type}
                  doctorName={record.doctorName}
                  description={record.description}
                  diagnosis={record.diagnosis}
                  treatment={record.treatment}
                  medications={record.medications}
                  isExpanded={expandedId === record.id}
                />
              </TouchableOpacity>
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
  header: {
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingVertical: 16,
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
    fontSize: 14,
    color: '#999',
  },
  controlsContainer: {
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  controlRow: {
    flexDirection: 'row',
    gap: 12,
  },
  filterGroup: {
    flex: 1,
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
    color: '#666',
    marginBottom: 6,
  },
  pickerContainer: {
    backgroundColor: '#f8f9fa',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 6,
    overflow: 'hidden',
  },
  smallPicker: {
    minHeight: 32,
  },
  scrollContent: {
    paddingVertical: 12,
    paddingBottom: 20,
  },
  timelineContainer: {
    paddingVertical: 8,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 60,
  },
  emptyIcon: {
    fontSize: 48,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
  },
});

export default MedicalHistoryScreen;
