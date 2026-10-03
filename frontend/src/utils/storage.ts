import AsyncStorage from '@react-native-async-storage/async-storage';
import { logger } from './logger';

export const storage = {
  /**
   * Store a value
   */
  async setItem<T>(key: string, value: T): Promise<void> {
    try {
      const serialized = JSON.stringify(value);
      await AsyncStorage.setItem(key, serialized);
      logger.debug(`Storage: Set ${key}`);
    } catch (error) {
      logger.error(`Storage: Failed to set ${key}`, error);
      throw error;
    }
  },

  /**
   * Retrieve a value
   */
  async getItem<T>(key: string): Promise<T | null> {
    try {
      const value = await AsyncStorage.getItem(key);
      if (value === null) {
        return null;
      }
      const deserialized = JSON.parse(value) as T;
      logger.debug(`Storage: Got ${key}`);
      return deserialized;
    } catch (error) {
      logger.error(`Storage: Failed to get ${key}`, error);
      return null;
    }
  },

  /**
   * Remove a value
   */
  async removeItem(key: string): Promise<void> {
    try {
      await AsyncStorage.removeItem(key);
      logger.debug(`Storage: Removed ${key}`);
    } catch (error) {
      logger.error(`Storage: Failed to remove ${key}`, error);
      throw error;
    }
  },

  /**
   * Clear all storage
   */
  async clear(): Promise<void> {
    try {
      await AsyncStorage.clear();
      logger.debug('Storage: Cleared all');
    } catch (error) {
      logger.error('Storage: Failed to clear', error);
      throw error;
    }
  },

  /**
   * Get all keys
   */
  async getAllKeys(): Promise<string[]> {
    try {
      const keys = await AsyncStorage.getAllKeys();
      return Array.from(keys);
    } catch (error) {
      logger.error('Storage: Failed to get keys', error);
      return [];
    }
  },

  /**
   * Multi get operation
   */
  async multiGet<T>(keys: string[]): Promise<Record<string, T | null>> {
    try {
      const pairs = await AsyncStorage.multiGet(keys);
      const result: Record<string, T | null> = {};
      pairs.forEach(([key, value]) => {
        result[key] = value ? (JSON.parse(value) as T) : null;
      });
      return result;
    } catch (error) {
      logger.error('Storage: Failed to multiGet', error);
      return {};
    }
  },

  /**
   * Multi set operation
   */
  async multiSet(data: Record<string, any>): Promise<void> {
    try {
      const pairs = Object.entries(data).map(([key, value]) => [
        key,
        JSON.stringify(value),
      ]);
      await AsyncStorage.multiSet(pairs as [string, string][]);
      logger.debug('Storage: Multi set completed');
    } catch (error) {
      logger.error('Storage: Failed to multiSet', error);
      throw error;
    }
  },
};
