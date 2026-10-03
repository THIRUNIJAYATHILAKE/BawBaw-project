import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface Props {
  navigation?: any;
  route?: any;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  text: {
    fontSize: 18,
    color: '#333',
  },
});

const AppointmentBookingScreen: React.FC<Props> = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Appointment Booking Screen</Text>
    </View>
  );
};

export default AppointmentBookingScreen;
