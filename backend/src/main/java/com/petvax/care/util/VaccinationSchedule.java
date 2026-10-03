package com.petvax.care.util;

import java.time.LocalDate;
import java.util.*;

/**
 * WSAVA (World Small Animal Veterinary Association) vaccination guidelines
 * for puppies, kittens, and adult animals.
 */
public class VaccinationSchedule {
    
    public enum VaccineType {
        RABIES("Rabies", 365),
        DHPP("DHPP (Distemper, Hepatitis, Parvovirus, Parainfluenza)", 365),
        DHLPP("DHLPP (Distemper, Hepatitis, Leptospirosis, Parvovirus, Parainfluenza)", 365),
        FVRCP("FVRCP (Feline Viral Rhinotracheitis, Calicivirus, Panleukopenia)", 365),
        FELV("Feline Leukemia", 365),
        BORDETELLA("Bordetella", 365),
        LEPTO("Leptospirosis", 365),
        FECV("Feline Enteric Coronavirus", 365);
        
        private final String displayName;
        private final int boosterIntervalDays;
        
        VaccineType(String displayName, int boosterIntervalDays) {
            this.displayName = displayName;
            this.boosterIntervalDays = boosterIntervalDays;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getBoosterIntervalDays() {
            return boosterIntervalDays;
        }
    }
    
    /**
     * WSAVA core vaccines for puppies (6-16 weeks age)
     */
    public static List<VaccinationScheduleItem> getPuppyVaccinationSchedule() {
        return Arrays.asList(
            new VaccinationScheduleItem("6 weeks", VaccineType.DHPP),
            new VaccinationScheduleItem("9 weeks", VaccineType.DHPP),
            new VaccinationScheduleItem("12 weeks", VaccineType.DHPP),
            new VaccinationScheduleItem("16 weeks", VaccineType.DHPP),
            new VaccinationScheduleItem("16 weeks", VaccineType.RABIES)
        );
    }
    
    /**
     * WSAVA core vaccines for kittens (8-16 weeks age)
     */
    public static List<VaccinationScheduleItem> getKittenVaccinationSchedule() {
        return Arrays.asList(
            new VaccinationScheduleItem("8 weeks", VaccineType.FVRCP),
            new VaccinationScheduleItem("12 weeks", VaccineType.FVRCP),
            new VaccinationScheduleItem("16 weeks", VaccineType.FVRCP),
            new VaccinationScheduleItem("16 weeks", VaccineType.RABIES),
            new VaccinationScheduleItem("16 weeks", VaccineType.FELV)
        );
    }
    
    /**
     * Calculate next booster date based on vaccine type
     * Standard WSAVA recommendation: annual boosters for most vaccines
     */
    public static LocalDate calculateNextBoosterDate(VaccineType vaccineType, LocalDate administrationDate) {
        return administrationDate.plusDays(vaccineType.getBoosterIntervalDays());
    }
    
    /**
     * Calculate next booster date with custom interval
     */
    public static LocalDate calculateNextBoosterDate(LocalDate administrationDate, int intervalDays) {
        return administrationDate.plusDays(intervalDays);
    }
    
    /**
     * Determine if a pet is a puppy (up to 1 year old)
     */
    public static boolean isPuppy(LocalDate dateOfBirth) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return dateOfBirth.isAfter(oneYearAgo);
    }
    
    /**
     * Determine if a pet is a kitten (up to 1 year old)
     */
    public static boolean isKitten(LocalDate dateOfBirth) {
        return isPuppy(dateOfBirth);
    }
    
    /**
     * Get age in weeks
     */
    public static int getAgeInWeeks(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        return (int) ((today.toEpochDay() - dateOfBirth.toEpochDay()) / 7);
    }
    
    /**
     * Get age in months
     */
    public static int getAgeInMonths(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        return (int) ((today.toEpochDay() - dateOfBirth.toEpochDay()) / 30);
    }
    
    public static class VaccinationScheduleItem {
        private final String ageDescription;
        private final VaccineType vaccineType;
        
        public VaccinationScheduleItem(String ageDescription, VaccineType vaccineType) {
            this.ageDescription = ageDescription;
            this.vaccineType = vaccineType;
        }
        
        public String getAgeDescription() {
            return ageDescription;
        }
        
        public VaccineType getVaccineType() {
            return vaccineType;
        }
    }
}
