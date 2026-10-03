import React, { useEffect, useState } from 'react';
import { View, ScrollView, RefreshControl, ActivityIndicator, FlatList } from 'react-native';
import { Text, Button } from 'react-native';
import apiClient from '@services/api';
import { colors } from '@theme/colors';
import { typography } from '@theme/typography';
import { formatTime } from '@utils/helpers';

interface Appointment {
  id: number;
  petName: string;
  ownerName: string;
  doctorName: string;
  appointmentTime: string;
  purpose: string;
  status: string;
  checkedInAt?: string;
  checkedOutAt?: string;
}

interface ScheduleSummary {
  totalAppointments: number;
  checkedInCount: number;
  checkedOutCount: number;
  pendingCount: number;
}

export const AssistantDashboardScreen = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [summary, setSummary] = useState<ScheduleSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const fetchTodaySchedule = async () => {
    try {
      setLoading(true);
      const scheduleRes = await apiClient.getClient().get('/assistant/schedule/today');
      const summaryRes = await apiClient.getClient().get('/assistant/schedule/summary');
      
      setAppointments(scheduleRes.data.data || []);
      setSummary(summaryRes.data.data);
    } catch (error) {
      console.error('Failed to load schedule:', error);
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await fetchTodaySchedule();
    setRefreshing(false);
  };

  const handleCheckIn = async (appointmentId: number) => {
    try {
      const response = await apiClient.getClient().post(`/assistant/appointment/${appointmentId}/check-in`);
      if (response.data.success) {
        onRefresh();
      }
    } catch (error) {
      console.error('Failed to check in:', error);
    }
  };

  const handleCheckOut = async (appointmentId: number) => {
    try {
      const response = await apiClient.getClient().post(`/assistant/appointment/${appointmentId}/check-out`);
      if (response.data.success) {
        onRefresh();
      }
    } catch (error) {
      console.error('Failed to check out:', error);
    }
  };

  useEffect(() => {
    fetchTodaySchedule();
  }, []);

  if (loading && !summary) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.background }}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      {/* Header */}
      <View style={{ backgroundColor: colors.primary, padding: 16, paddingTop: 32 }}>
        <Text style={[typography.h2, { color: colors.surface, marginBottom: 8 }]}>Today's Schedule</Text>
        <Text style={[typography.caption, { color: colors.surface, opacity: 0.8 }]}>
          {new Date().toLocaleDateString()}
        </Text>
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        {/* Summary Cards */}
        {summary && (
          <View style={{ padding: 16, gap: 12 }}>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              <SummaryCard label="Total" value={summary.totalAppointments} color={colors.info} />
              <SummaryCard label="Checked In" value={summary.checkedInCount} color={colors.success} />
              <SummaryCard label="Pending" value={summary.pendingCount} color={colors.warning} />
            </View>
          </View>
        )}

        {/* Appointments List */}
        <View style={{ padding: 16 }}>
          <Text style={[typography.h3, { marginBottom: 12 }]}>Appointments</Text>
          {appointments.length === 0 ? (
            <Text style={[typography.body, { color: colors.textLight, textAlign: 'center', paddingVertical: 32 }]}>
              No appointments for today
            </Text>
          ) : (
            <FlatList
              scrollEnabled={false}
              data={appointments}
              renderItem={({ item }) => (
                <AppointmentCard
                  appointment={item}
                  onCheckIn={() => handleCheckIn(item.id)}
                  onCheckOut={() => handleCheckOut(item.id)}
                />
              )}
              keyExtractor={(item: Appointment) => item.id.toString()}
              ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
            />
          )}
        </View>
      </ScrollView>
    </View>
  );
};

interface SummaryCardProps {
  label: string;
  value: number;
  color: string;
}

const SummaryCard: React.FC<SummaryCardProps> = ({ label, value, color }) => (
  <View style={{ flex: 1, backgroundColor: colors.surface, borderRadius: 8, padding: 12, alignItems: 'center' }}>
    <Text style={[typography.h2, { color }]}>{value}</Text>
    <Text style={[typography.caption, { color: colors.textLight }]}>{label}</Text>
  </View>
);

interface AppointmentCardProps {
  appointment: Appointment;
  onCheckIn: () => void;
  onCheckOut: () => void;
}

const AppointmentCard: React.FC<AppointmentCardProps> = ({ appointment, onCheckIn, onCheckOut }) => {
  const isCheckedIn = !!appointment.checkedInAt;
  const isCheckedOut = !!appointment.checkedOutAt;

  return (
    <View style={{ backgroundColor: colors.surface, borderRadius: 8, padding: 12, borderLeftWidth: 4, borderLeftColor: colors.primary }}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
        <View style={{ flex: 1 }}>
          <Text style={[typography.label, { color: colors.text }]}>{appointment.petName}</Text>
          <Text style={[typography.caption, { color: colors.textLight }]}>{appointment.ownerName}</Text>
        </View>
        <Text style={[typography.caption, { color: colors.textLight }]}>
          {formatTime(appointment.appointmentTime)}
        </Text>
      </View>

      <View style={{ marginBottom: 8 }}>
        <Text style={[typography.caption, { color: colors.textLight }]}>
          Doctor: {appointment.doctorName}
        </Text>
        <Text style={[typography.caption, { color: colors.textLight }]}>
          Purpose: {appointment.purpose}
        </Text>
      </View>

      <View style={{ flexDirection: 'row', gap: 8 }}>
        {!isCheckedIn ? (
          <Button title="Check In" onPress={onCheckIn} color={colors.success} />
        ) : (
          <View style={{ flex: 1, backgroundColor: colors.success, opacity: 0.3, borderRadius: 4, paddingVertical: 8, justifyContent: 'center', alignItems: 'center' }}>
            <Text style={{ color: colors.success, fontWeight: 'bold' }}>✓ Checked In</Text>
          </View>
        )}

        {isCheckedIn && !isCheckedOut && (
          <Button title="Check Out" onPress={onCheckOut} color={colors.warning} />
        )}

        {isCheckedOut && (
          <View style={{ flex: 1, backgroundColor: colors.info, opacity: 0.3, borderRadius: 4, paddingVertical: 8, justifyContent: 'center', alignItems: 'center' }}>
            <Text style={{ color: colors.info, fontWeight: 'bold' }}>✓ Checked Out</Text>
          </View>
        )}
      </View>
    </View>
  );
};
