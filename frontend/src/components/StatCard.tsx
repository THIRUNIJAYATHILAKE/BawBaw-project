import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface StatCardProps {
  label: string;
  value: string | number;
  icon?: string;
  color?: string;
}

const StatCard: React.FC<StatCardProps> = ({ label, value, icon, color = '#007AFF' }) => {
  return (
    <View style={[styles.container, { borderLeftColor: color }]}>
      {icon && <Text style={styles.icon}>{icon}</Text>}
      <View style={styles.content}>
        <Text style={styles.label}>{label}</Text>
        <Text style={[styles.value, { color }]}>{value}</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    borderLeftWidth: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
    flexDirection: 'row',
    alignItems: 'center',
  },
  icon: {
    fontSize: 28,
    marginRight: 12,
  },
  content: {
    flex: 1,
  },
  label: {
    fontSize: 13,
    color: '#999',
    marginBottom: 4,
  },
  value: {
    fontSize: 20,
    fontWeight: '700',
  },
});

export default StatCard;
