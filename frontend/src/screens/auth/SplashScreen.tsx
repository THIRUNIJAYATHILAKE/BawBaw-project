import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Animated,
} from 'react-native';

interface Props {
  navigation: any;
}

const SplashScreen: React.FC<Props> = ({ navigation }) => {
  const [fadeAnim] = useState(new Animated.Value(0));

  useEffect(() => {
    Animated.sequence([
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 800,
        useNativeDriver: true,
      }),
      Animated.delay(500),
    ]).start();
  }, [fadeAnim]);

  const handleGetStarted = () => {
    navigation.navigate('RoleSelection');
  };

  return (
    <Animated.View
      style={[
        styles.container,
        {
          opacity: fadeAnim,
        },
      ]}
    >
      <View style={styles.content}>
        <View style={styles.logoContainer}>
          <Text style={styles.logo}>🐾</Text>
          <Text style={styles.brandName}>PetCare</Text>
        </View>

        <View style={styles.heroImageContainer}>
          <Text style={styles.heroEmoji}>🐕🐈🦜</Text>
        </View>

        <View style={styles.textContainer}>
          <Text style={styles.title}>Welcome to PetVax Care</Text>
          <Text style={styles.tagline}>
            Treat your pets with the very best care!
          </Text>
          <Text style={styles.subtitle}>
            Manage vaccinations, health records, and appointments all in one place.
          </Text>
        </View>

        <TouchableOpacity
          style={styles.getStartedButton}
          onPress={handleGetStarted}
          activeOpacity={0.8}
        >
          <Text style={styles.buttonText}>Get Started</Text>
        </TouchableOpacity>

        <View style={styles.footerContainer}>
          <Text style={styles.footerText}>
            Already have an account?{' '}
            <Text
              style={styles.loginLink}
              onPress={() => navigation.navigate('Login')}
            >
              Login
            </Text>
          </Text>
        </View>
      </View>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  content: {
    flex: 1,
    justifyContent: 'space-between',
    paddingVertical: 40,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 20,
  },
  logo: {
    fontSize: 60,
    marginBottom: 12,
  },
  brandName: {
    fontSize: 28,
    fontWeight: '700',
    color: '#6B4C9A',
    letterSpacing: 1,
  },
  heroImageContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f0e6ff',
    borderRadius: 20,
    paddingVertical: 60,
    marginVertical: 40,
  },
  heroEmoji: {
    fontSize: 100,
  },
  textContainer: {
    alignItems: 'center',
    marginBottom: 40,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    color: '#333',
    textAlign: 'center',
    marginBottom: 16,
  },
  tagline: {
    fontSize: 18,
    fontWeight: '600',
    color: '#6B4C9A',
    textAlign: 'center',
    marginBottom: 12,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    lineHeight: 22,
  },
  getStartedButton: {
    backgroundColor: '#6B4C9A',
    paddingVertical: 16,
    paddingHorizontal: 40,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#6B4C9A',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '700',
  },
  footerContainer: {
    alignItems: 'center',
  },
  footerText: {
    fontSize: 14,
    color: '#666',
  },
  loginLink: {
    color: '#6B4C9A',
    fontWeight: '700',
  },
});

export default SplashScreen;
