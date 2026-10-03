import React, { useEffect, useState } from 'react';
import { View, ScrollView, RefreshControl, ActivityIndicator, TouchableOpacity, FlatList } from 'react-native';
import { Text, Button } from 'react-native';
import { useAuth } from '@context/AuthContext';
import apiClient from '@services/api';
import { colors } from '@theme/colors';
import { typography } from '@theme/typography';
import { formatTime, formatDate } from '@utils/helpers';

interface Appointment {
  id: number;
  petName: string;
  ownerName: string;
  appointmentDate: string;
  purpose: string;
  status: string;
  notes?: string;
}

interface DoctorStats {
  todayAppointments: number;
  completedAppointments: number;
  pendingAppointments: number;
  thisWeekAppointments: number;
}

export const DoctorDashboardScreen = ({ navigation }: any) => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [stats, setStats] = useState<DoctorStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Get today's appointments
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);

      const appointmentsRes = await apiClient.getClient().get(`/doctor/appointments`, {
        params: {
          dateFrom: today.toISOString(),
          dateTo: tomorrow.toISOString(),
        },
      });

      setAppointments(appointmentsRes.data.data || []);

      // Calculate stats
      const completed = (appointmentsRes.data.data || []).filter((a: Appointment) => a.status === 'COMPLETED').length;
      const pending = (appointmentsRes.data.data || []).filter((a: Appointment) => a.status === 'SCHEDULED').length;

      setStats({
        todayAppointments: appointmentsRes.data.data?.length || 0,
        completedAppointments: completed,
        pendingAppointments: pending,
        thisWeekAppointments: 0,
      });
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await fetchDashboardData();
    setRefreshing(false);
  };

  const handleCompleteAppointment = async (appointmentId: number) => {
    try {
      const response = await apiClient.getClient().post(`/doctor/appointments/${appointmentId}/complete`);
      if (response.data.success) {
        onRefresh();
      }
    } catch (error) {
      console.error('Failed to complete appointment:', error);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading && !stats) {
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
        <Text style={[typography.h2, { color: colors.surface }]}>Welcome, Dr. {user?.fullName}</Text>
        <Text style={[typography.caption, { color: colors.surface, opacity: 0.8 }]}>
          Today's Schedule • {formatDate(new Date().toISOString())}
        </Text>
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        {/* Quick Stats */}
        {stats && (
          <View style={{ padding: 16, gap: 8 }}>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              <StatCard label="Today" value={stats.todayAppointments} color={colors.info} />
              <StatCard label="Pending" value={stats.pendingAppointments} color={colors.warning} />
              <StatCard label="Completed" value={stats.completedAppointments} color={colors.success} />
            </View>
          </View>
        )}

        {/* Appointments List */}
        <View style={{ padding: 16 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <Text style={[typography.h3]}>Today's Appointments</Text>
            <Button title="View Schedule" onPress={() => navigation.navigate('Schedule')} color={colors.primary} />
          </View>

          {appointments.length === 0 ? (
            <Text style={[typography.body, { color: colors.textLight, textAlign: 'center', paddingVertical: 32 }]}>
              No appointments scheduled for today
            </Text>
          ) : (
            <FlatList
              scrollEnabled={false}
              data={appointments}
              renderItem={({ item }) => (
                <AppointmentCard
                  appointment={item}
                  onPress={() => navigation.navigate('PatientDetails', { appointmentId: item.id })}
                  onComplete={() => handleCompleteAppointment(item.id)}
                />
              )}
              keyExtractor={(item: Appointment) => item.id.toString()}
              ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
            />
          )}
        </View>

        {/* Action Buttons */}
        <View style={{ padding: 16, gap: 8 }}>
          <Button
            title="Search Patient"
            onPress={() => navigation.navigate('SearchPet')}
            color={colors.primary}
          />
          <Button
            title="View Medical Records"
            onPress={() => navigation.navigate('Records')}
            color={colors.success}
          />
        </View>
      </ScrollView>
    </View>
  );
};

interface StatCardProps {
  label: string;
  value: number;
  color: string;
}

const StatCard: React.FC<StatCardProps> = ({ label, value, color }) => (
  <View style={{ flex: 1, backgroundColor: colors.surface, borderRadius: 8, padding: 12, borderLeftWidth: 4, borderLeftColor: color }}>
    <Text style={[typography.h2, { color }]}>{value}</Text>
    <Text style={[typography.caption, { color: colors.textLight }]}>{label}</Text>
  </View>
);

interface AppointmentCardProps {
  appointment: Appointment;
  onPress: () => void;
  onComplete: () => void;
}

const AppointmentCard: React.FC<AppointmentCardProps> = ({ appointment, onPress, onComplete }) => (
  <TouchableOpacity onPress={onPress}>
    <View style={{ backgroundColor: colors.surface, borderRadius: 8, padding: 12, borderLeftWidth: 4, borderLeftColor: colors.primary }}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
        <View style={{ flex: 1 }}>
          <Text style={[typography.label, { color: colors.text }]}>{appointment.petName}</Text>
          <Text style={[typography.caption, { color: colors.textLight }]}>{appointment.ownerName}</Text>
        </View>
        <View style={{ alignItems: 'flex-end' }}>
          <Text style={[typography.caption, { color: colors.textLight }]}>
            {formatTime(appointment.appointmentDate)}
          </Text>
          <Text style={[typography.caption, { color: colors.primary, fontWeight: 'bold' }]}>
            {appointment.status}
          </Text>
        </View>
      </View>

      <View style={{ marginBottom: 8 }}>
        <Text style={[typography.caption, { color: colors.textLight }]}>
          Purpose: {appointment.purpose}
        </Text>
      </View>

      {appointment.status === 'SCHEDULED' && (
        <Button title="Complete" onPress={onComplete} color={colors.success} />
      )}
    </View>
  </TouchableOpacity>
);

export default DoctorDashboardScreen;
