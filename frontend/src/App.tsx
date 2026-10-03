import { useEffect } from "react";
import * as SplashScreen from "expo-splash-screen";
import { AuthProvider } from "@context/AuthContext";

SplashScreen.preventAutoHideAsync();

function AppContent() {
  useEffect(() => {
    async function prepare() {
      try {
        await SplashScreen.hideAsync();
      } catch (e) {
        console.warn(e);
      }
    }

    prepare();
  }, []);

  // Navigator will be added here
  return null;
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
