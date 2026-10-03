import React, { useEffect, useState } from 'react';
import { View, ScrollView, RefreshControl, ActivityIndicator, FlatList, Alert } from 'react-native';
import { Text, Button } from 'react-native';
import apiClient from '@services/api';
import { colors } from '@theme/colors';
import { typography } from '@theme/typography';

interface PendingUser {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  accountStatus: string;
}

interface AdminStats {
  totalUsers: number;
  totalDoctors: number;
  totalAssistants: number;
  pendingVerifications: number;
  todayAppointments: number;
}

export const AdminDashboardScreen = ({ navigation }: any) => {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [pending, setPending] = useState<PendingUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const pendingRes = await apiClient.getClient().get('/admin/pending-verifications');
      setPending(pendingRes.data.data);
      
      // Calculate stats
      setStats({
        totalUsers: 0,
        totalDoctors: 0,
        totalAssistants: 0,
        pendingVerifications: pendingRes.data.count,
        todayAppointments: 0,
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

  const handleVerifyDoctor = async (doctorId: number, approve: boolean) => {
    try {
      const response = await apiClient.getClient().post(
        `/admin/verify-doctor/${doctorId}`,
        {},
        { params: { status: approve ? 'APPROVED' : 'REJECTED' } }
      );
      if (response.data.success) {
        Alert.alert('Success', `Doctor ${approve ? 'approved' : 'rejected'}`);
        onRefresh();
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to update doctor verification');
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
        <Text style={[typography.h2, { color: colors.surface }]}>Admin Dashboard</Text>
        <Text style={[typography.caption, { color: colors.surface, opacity: 0.8 }]}>
          System Overview
        </Text>
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        {/* Quick Stats */}
        {stats && (
          <View style={{ padding: 16, gap: 12 }}>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              <StatTile label="Users" value={stats.totalUsers} color={colors.info} />
              <StatTile label="Doctors" value={stats.totalDoctors} color={colors.success} />
              <StatTile label="Assistants" value={stats.totalAssistants} color={colors.warning} />
            </View>
            <StatTile 
              label="Pending Verifications" 
              value={stats.pendingVerifications} 
              color={colors.error}
              highlight={stats.pendingVerifications > 0}
            />
          </View>
        )}

        {/* Pending Verifications */}
        <View style={{ padding: 16 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <Text style={[typography.h3]}>Pending Verifications</Text>
            <Button title="View All" onPress={() => navigation.navigate('VerifyUsers')} color={colors.primary} />
          </View>

          {pending.length === 0 ? (
            <Text style={[typography.body, { color: colors.textLight, textAlign: 'center', paddingVertical: 20 }]}>
              No pending verifications
            </Text>
          ) : (
            <FlatList
              scrollEnabled={false}
              data={pending.slice(0, 3)}
              renderItem={({ item }) => (
                <PendingUserCard
                  user={item}
                  onApprove={() => handleVerifyDoctor(item.userId, true)}
                  onReject={() => handleVerifyDoctor(item.userId, false)}
                />
              )}
              keyExtractor={(item) => item.userId.toString()}
              ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
            />
          )}
        </View>

        {/* Action Buttons */}
        <View style={{ padding: 16, gap: 8 }}>
          <Button
            title="Manage Users"
            onPress={() => navigation.navigate('ManageUsers')}
            color={colors.primary}
          />
          <Button
            title="View Appointments"
            onPress={() => navigation.navigate('Appointments')}
            color={colors.success}
          />
          <Button
            title="Add Doctor/Assistant"
            onPress={() => navigation.navigate('AddUser')}
            color={colors.info}
          />
        </View>
      </ScrollView>
    </View>
  );
};

interface StatTileProps {
  label: string;
  value: number;
  color: string;
  highlight?: boolean;
}

const StatTile: React.FC<StatTileProps> = ({ label, value, color, highlight }) => (
  <View 
    style={{
      flex: 1,
      backgroundColor: colors.surface,
      borderRadius: 8,
      padding: 12,
      borderLeftWidth: 4,
      borderLeftColor: color,
      opacity: highlight ? 1 : 0.8,
    }}
  >
    <Text style={[typography.h2, { color }]}>{value}</Text>
    <Text style={[typography.caption, { color: colors.textLight }]}>{label}</Text>
  </View>
);

interface PendingUserCardProps {
  user: PendingUser;
  onApprove: () => void;
  onReject: () => void;
}

const PendingUserCard: React.FC<PendingUserCardProps> = ({ user, onApprove, onReject }) => (
  <View style={{ backgroundColor: colors.surface, borderRadius: 8, padding: 12 }}>
    <View style={{ marginBottom: 8 }}>
      <Text style={[typography.label, { color: colors.text }]}>{user.fullName}</Text>
      <Text style={[typography.caption, { color: colors.textLight }]}>{user.email}</Text>
      <Text style={[typography.caption, { color: colors.primary, marginTop: 4 }]}>
        {user.role} • {user.username}
      </Text>
    </View>
    <View style={{ flexDirection: 'row', gap: 8 }}>
      <Button title="Approve" onPress={onApprove} color={colors.success} />
      <Button title="Reject" onPress={onReject} color={colors.error} />
    </View>
  </View>
);

export default AdminDashboardScreen;
