import { useState, useCallback } from 'react';
import { storage } from '@utils/storage';
import { logger } from '@utils/logger';

interface Pet {
  id: string;
  name: string;
  type: string;
  breed?: string;
  dateOfBirth?: string;
  microchipId?: string;
}

export const usePets = () => {
  const [pets, setPets] = useState<Pet[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPets = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const storedPets = await storage.getItem<Pet[]>('pets');
      if (storedPets) {
        setPets(storedPets);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load pets';
      setError(errorMessage);
      logger.error('Failed to load pets', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const addPet = useCallback(
    async (pet: Pet) => {
      try {
        const updatedPets = [...pets, pet];
        setPets(updatedPets);
        await storage.setItem('pets', updatedPets);
        logger.info('Pet added', { petId: pet.id });
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to add pet';
        setError(errorMessage);
        logger.error('Failed to add pet', err);
      }
    },
    [pets]
  );

  const removePet = useCallback(
    async (petId: string) => {
      try {
        const updatedPets = pets.filter((p) => p.id !== petId);
        setPets(updatedPets);
        await storage.setItem('pets', updatedPets);
        logger.info('Pet removed', { petId });
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to remove pet';
        setError(errorMessage);
        logger.error('Failed to remove pet', err);
      }
    },
    [pets]
  );

  const updatePet = useCallback(
    async (petId: string, updates: Partial<Pet>) => {
      try {
        const updatedPets = pets.map((p) =>
          p.id === petId ? { ...p, ...updates } : p
        );
        setPets(updatedPets);
        await storage.setItem('pets', updatedPets);
        logger.info('Pet updated', { petId });
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to update pet';
        setError(errorMessage);
        logger.error('Failed to update pet', err);
      }
    },
    [pets]
  );

  return {
    pets,
    isLoading,
    error,
    loadPets,
    addPet,
    removePet,
    updatePet,
  };
};
