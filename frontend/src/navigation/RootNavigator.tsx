import { NavigationContainer } from "@react-navigation/native";
import { createStackNavigator } from "@react-navigation/stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { Text } from "react-native";
import { useAuth } from "@context/AuthContext";

// Auth screens
import SplashScreen from "@screens/auth/SplashScreen";
import RoleSelectionScreen from "@screens/auth/RoleSelectionScreen";
import LoginScreen from "@screens/auth/LoginScreen";
import RegisterScreen from "@screens/auth/RegisterScreen";
import ForgotPasswordScreen from "@screens/auth/ForgotPasswordScreen";

// Owner screens
import OwnerDashboardScreen from "@screens/owner/OwnerDashboardScreen";
import PetListScreen from "@screens/owner/PetListScreen";
import PetDetailsScreen from "@screens/owner/PetDetailsScreen";
import AddPetScreen from "@screens/owner/AddPetScreen";
import EditPetScreen from "@screens/owner/EditPetScreen";
import BookAppointmentScreen from "@screens/owner/BookAppointmentScreen";
import MedicalHistoryScreen from "@screens/owner/MedicalHistoryScreen";
import VaccinationScheduleScreen from "@screens/owner/VaccinationScheduleScreen";

const Stack = createStackNavigator();
const Tab = createBottomTabNavigator();

// Placeholder screens
const DoctorDashboardScreen = () => null;
const AssistantDashboardScreen = () => null;
const AdminDashboardScreen = () => null;

// Auth Stack Navigator
const AuthStack = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: false,
      animationEnabled: true,
    }}
  >
    <Stack.Screen name="Splash" component={SplashScreen} />
    <Stack.Screen name="RoleSelection" component={RoleSelectionScreen} />
    <Stack.Screen name="Login" component={LoginScreen} />
    <Stack.Screen name="Register" component={RegisterScreen} />
    <Stack.Screen name="ForgotPassword" component={ForgotPasswordScreen} />
  </Stack.Navigator>
);

// Owner Pet Stack
const PetStack = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: true,
      headerBackTitle: "Back",
    }}
  >
    <Stack.Screen
      name="PetList"
      component={PetListScreen}
      options={{ title: "My Pets" }}
    />
    <Stack.Screen
      name="PetDetails"
      component={PetDetailsScreen}
      options={{ title: "Pet Details" }}
    />
    <Stack.Screen
      name="EditPet"
      component={EditPetScreen}
      options={{ title: "Edit Pet" }}
    />
    <Stack.Screen
      name="AddPet"
      component={AddPetScreen}
      options={{ title: "Add New Pet" }}
    />
    <Stack.Screen
      name="MedicalHistory"
      component={MedicalHistoryScreen}
      options={{ title: "Medical History" }}
    />
    <Stack.Screen
      name="VaccinationSchedule"
      component={VaccinationScheduleScreen}
      options={{ title: "Vaccination Schedule" }}
    />
  </Stack.Navigator>
);

// Owner Appointments Stack
const AppointmentsStack = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: true,
    }}
  >
    <Stack.Screen
      name="AppointmentsList"
      component={OwnerDashboardScreen}
      options={{ title: "Appointments" }}
    />
    <Stack.Screen
      name="BookAppointment"
      component={BookAppointmentScreen}
      options={{ title: "Book Appointment" }}
    />
  </Stack.Navigator>
);

// Owner Profile Stack
const ProfileStack = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: true,
    }}
  >
    <Stack.Screen
      name="ProfileScreen"
      component={OwnerDashboardScreen}
      options={{ title: "Profile" }}
    />
  </Stack.Navigator>
);

// Owner Stack Navigator with Tab Navigation
const OwnerStack = () => (
  <Tab.Navigator
    screenOptions={{
      headerShown: false,
      tabBarActiveTintColor: "#007AFF",
      tabBarInactiveTintColor: "#999",
      tabBarLabelStyle: {
        fontSize: 12,
        fontWeight: "500" as any,
      },
      tabBarStyle: {
        height: 60,
        paddingBottom: 8,
        backgroundColor: "#fff",
        borderTopWidth: 1,
        borderTopColor: "#f0f0f0",
      },
    }}
  >
    <Tab.Screen
      name="Home"
      component={OwnerDashboardScreen}
      options={{
        tabBarLabel: "Home",
        tabBarIcon: ({ color }: { color: string }) => (
          <Text style={{ fontSize: 20, color }}>🏠</Text>
        ),
      }}
    />
    <Tab.Screen
      name="Pets"
      component={PetStack}
      options={{
        tabBarLabel: "Pets",
        tabBarIcon: ({ color }: { color: string }) => (
          <Text style={{ fontSize: 20, color }}>🐾</Text>
        ),
      }}
    />
    <Tab.Screen
      name="Appointments"
      component={AppointmentsStack}
      options={{
        tabBarLabel: "Appointments",
        tabBarIcon: ({ color }: { color: string }) => (
          <Text style={{ fontSize: 20, color }}>📅</Text>
        ),
      }}
    />
    <Tab.Screen
      name="Profile"
      component={ProfileStack}
      options={{
        tabBarLabel: "Profile",
        tabBarIcon: ({ color }: { color: string }) => (
          <Text style={{ fontSize: 20, color }}>👤</Text>
        ),
      }}
    />
  </Tab.Navigator>
);

// Doctor Stack Navigator
const DoctorStack = () => (
  <Tab.Navigator
    screenOptions={{
      headerShown: true,
    }}
  >
    <Tab.Screen
      name="Dashboard"
      component={DoctorDashboardScreen}
      options={{ title: "Dashboard" }}
    />
  </Tab.Navigator>
);

// Assistant Stack Navigator
const AssistantStack = () => (
  <Tab.Navigator
    screenOptions={{
      headerShown: true,
    }}
  >
    <Tab.Screen
      name="Dashboard"
      component={AssistantDashboardScreen}
      options={{ title: "Dashboard" }}
    />
  </Tab.Navigator>
);

// Admin Stack Navigator
const AdminStack = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: true,
    }}
  >
    <Stack.Screen
      name="AdminDashboard"
      component={AdminDashboardScreen}
      options={{ title: "Admin Dashboard" }}
    />
  </Stack.Navigator>
);

// Root Navigator
const RootNavigator = () => {
  const { isLoading, isSignedIn, user } = useAuth();

  if (isLoading) {
    return (
      <NavigationContainer>
        <Stack.Navigator
          screenOptions={{
            headerShown: false,
          }}
        >
          <Stack.Screen name="SplashLoading" component={SplashScreen} />
        </Stack.Navigator>
      </NavigationContainer>
    );
  }

  return (
    <NavigationContainer>
      {isSignedIn ? (
        <>
          {user?.role === "PET_OWNER" && <OwnerStack />}
          {user?.role === "DOCTOR" && <DoctorStack />}
          {user?.role === "ASSISTANT" && <AssistantStack />}
          {user?.role === "ADMIN" && <AdminStack />}
        </>
      ) : (
        <AuthStack />
      )}
    </NavigationContainer>
  );
};

export default RootNavigator;
