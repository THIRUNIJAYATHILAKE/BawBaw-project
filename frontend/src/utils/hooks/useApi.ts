import { useState, useCallback } from 'react';
import { logger } from '@utils/logger';



export const useApi = <T,>() => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = useCallback(
    async (apiCall: () => Promise<T>) => {
      setLoading(true);
      setError(null);
      try {
        const result = await apiCall();
        setData(result);
        return result;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'An error occurred';
        setError(errorMessage);
        logger.error('API call failed', err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const reset = useCallback(() => {
    setData(null);
    setLoading(false);
    setError(null);
  }, []);

  return {
    data,
    loading,
    error,
    execute,
    reset,
  } as const;
};

export const useApiWithRetry = <T,>(maxRetries = 3) => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  const execute = useCallback(
    async (apiCall: () => Promise<T>) => {
      setLoading(true);
      setError(null);
      let lastError: Error | null = null;

      for (let attempt = 0; attempt < maxRetries; attempt++) {
        try {
          const result = await apiCall();
          setData(result);
          setRetryCount(0);
          setLoading(false);
          return result;
        } catch (err) {
          lastError = err instanceof Error ? err : new Error(String(err));
          if (attempt < maxRetries - 1) {
            setRetryCount(attempt + 1);
            const delay = Math.pow(2, attempt) * 1000;
            await new Promise((resolve) => setTimeout(resolve, delay));
          }
        }
      }

      const errorMessage = lastError?.message || 'API call failed after retries';
      setError(errorMessage);
      logger.error('API call failed after retries', lastError);
      setLoading(false);
      throw lastError;
    },
    [maxRetries]
  );

  const reset = useCallback(() => {
    setData(null);
    setLoading(false);
    setError(null);
    setRetryCount(0);
  }, []);

  return {
    data,
    loading,
    error,
    retryCount,
    execute,
    reset,
  } as const;
};
