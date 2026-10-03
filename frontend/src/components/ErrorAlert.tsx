import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ViewStyle,
  StyleProp,
  Alert,
} from 'react-native';

interface ErrorAlertProps {
  message?: string;
  visible?: boolean;
  onDismiss?: () => void;
  type?: 'error' | 'warning' | 'success';
  duration?: number;
  style?: StyleProp<ViewStyle>;
}

const ErrorAlert: React.FC<ErrorAlertProps> = ({
  message,
  visible = false,
  onDismiss,
  type = 'error',
  duration = 5000,
  style,
}) => {
  const [isVisible, setIsVisible] = useState(visible);

  useEffect(() => {
    setIsVisible(visible);

    if (visible && duration > 0) {
      const timer = setTimeout(() => {
        setIsVisible(false);
        onDismiss?.();
      }, duration);

      return () => clearTimeout(timer);
    }

    return undefined;
  }, [visible, duration, onDismiss]);

  if (!isVisible || !message) {
    return null;
  }

  const backgroundColor =
    type === 'error' ? '#FF3B30' : type === 'warning' ? '#FF9500' : '#34C759';

  return (
    <View style={[styles.container, { backgroundColor }, style]}>
      <Text style={styles.message}>{message}</Text>
      <TouchableOpacity
        onPress={() => {
          setIsVisible(false);
          onDismiss?.();
        }}
      >
        <Text style={styles.closeButton}>✕</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginHorizontal: 16,
    marginVertical: 8,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  message: {
    flex: 1,
    color: '#fff',
    fontSize: 14,
    fontWeight: '500',
    marginRight: 12,
  },
  closeButton: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export const showAlert = (
  title: string,
  message: string,
  options?: {
    onConfirm?: () => void;
    onCancel?: () => void;
    confirmText?: string;
    cancelText?: string;
  }
) => {
  Alert.alert(title, message, [
    {
      text: options?.cancelText || 'Cancel',
      onPress: options?.onCancel,
      style: 'cancel',
    },
    {
      text: options?.confirmText || 'OK',
      onPress: options?.onConfirm,
      style: 'default',
    },
  ]);
};

export default ErrorAlert;
