import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface PasswordStrengthIndicatorProps {
  score: number;
  message: string;
  showBar?: boolean;
  showLabel?: boolean;
}

const PasswordStrengthIndicator: React.FC<
  PasswordStrengthIndicatorProps
> = ({ score, message, showBar = true, showLabel = true }) => {
  const getColor = () => {
    if (score < 3) return '#FF3B30';
    if (score < 5) return '#FF9500';
    return '#34C759';
  };

  const color = getColor();
  const percentage = (score / 6) * 100;

  return (
    <View style={styles.container}>
      {showBar && (
        <View style={styles.barContainer}>
          <View
            style={[
              styles.bar,
              {
                width: `${percentage}%`,
                backgroundColor: color,
              },
            ]}
          />
        </View>
      )}
      {showLabel && (
        <Text style={[styles.label, { color }]}>Strength: {message}</Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginTop: 8,
  },
  barContainer: {
    height: 4,
    backgroundColor: '#f0f0f0',
    borderRadius: 2,
    overflow: 'hidden',
    marginBottom: 8,
  },
  bar: {
    height: '100%',
    borderRadius: 2,
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
  },
});

export default PasswordStrengthIndicator;
