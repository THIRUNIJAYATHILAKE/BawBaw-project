import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
} from 'react-native';

interface RoleOption {
  id: string;
  name: string;
  description: string;
  icon: string;
  color: string;
  lightColor: string;
}

const ROLE_OPTIONS: RoleOption[] = [
  {
    id: 'PET_OWNER',
    name: 'Pet Owner',
    description: 'Manage your pets and their health records',
    icon: '🐾',
    color: '#FF6B9D',
    lightColor: '#FFE8F0',
  },
  {
    id: 'DOCTOR',
    name: 'Veterinarian',
    description: 'Provide healthcare services to pets',
    icon: '⚕️',
    color: '#4ECDC4',
    lightColor: '#E0F9F7',
  },
  {
    id: 'ASSISTANT',
    name: 'Assistant',
    description: 'Support veterinary operations',
    icon: '👥',
    color: '#FFD93D',
    lightColor: '#FFF9E3',
  },
];

interface Props {
  navigation: any;
  route?: any;
}

interface RoleCardProps {
  option: RoleOption;
  onSelect: (roleId: string) => void;
}

const RoleCard: React.FC<RoleCardProps> = ({ option, onSelect }) => (
  <TouchableOpacity
    style={[styles.roleCard, { borderColor: option.color }]}
    onPress={() => onSelect(option.id)}
    activeOpacity={0.8}
  >
    <View
      style={[styles.iconContainer, { backgroundColor: option.lightColor }]}
    >
      <Text style={styles.icon}>{option.icon}</Text>
    </View>
    <Text style={[styles.roleName, { color: option.color }]}>
      {option.name}
    </Text>
    <Text style={styles.roleDescription}>{option.description}</Text>
  </TouchableOpacity>
);

const RoleSelectionScreen: React.FC<Props> = ({ navigation }) => {
  const handleRoleSelect = (roleId: string) => {
    navigation.navigate('Register', { role: roleId });
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.headerContainer}>
          <Text style={styles.backButton} onPress={() => navigation.goBack()}>
            ← Back
          </Text>
        </View>

        <View style={styles.titleContainer}>
          <Text style={styles.title}>Who u are</Text>
          <Text style={styles.subtitle}>
            Select your role to get started with PetVax Care
          </Text>
        </View>

        <View style={styles.cardsContainer}>
          {ROLE_OPTIONS.map((option) => (
            <RoleCard
              key={option.id}
              option={option}
              onSelect={handleRoleSelect}
            />
          ))}
        </View>

        <View style={styles.infoContainer}>
          <Text style={styles.infoText}>
            You can change your role later in your account settings.
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollContent: {
    paddingVertical: 20,
  },
  headerContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  backButton: {
    fontSize: 16,
    fontWeight: '600',
    color: '#6B4C9A',
  },
  titleContainer: {
    paddingHorizontal: 20,
    marginBottom: 40,
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    color: '#333',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    lineHeight: 20,
  },
  cardsContainer: {
    paddingHorizontal: 20,
    marginBottom: 40,
  },
  roleCard: {
    backgroundColor: '#fff',
    borderRadius: 16,
    borderWidth: 2,
    padding: 20,
    marginBottom: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  iconContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  icon: {
    fontSize: 40,
  },
  roleName: {
    fontSize: 20,
    fontWeight: '700',
    marginBottom: 8,
  },
  roleDescription: {
    fontSize: 13,
    color: '#666',
    textAlign: 'center',
    lineHeight: 18,
  },
  infoContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  infoText: {
    fontSize: 12,
    color: '#999',
    textAlign: 'center',
    fontStyle: 'italic',
  },
});

export default RoleSelectionScreen;
