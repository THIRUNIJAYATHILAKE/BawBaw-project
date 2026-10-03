package com.petvax.care.repository;

import com.petvax.care.entity.Pet;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    /**
     * Find all pets belonging to a specific owner
     */
    List<Pet> findByOwner(User owner);
    
    /**
     * Find pets by owner and name (case-insensitive partial match)
     */
    @Query("SELECT p FROM Pet p WHERE p.owner = :owner AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Pet> findByOwnerAndNameContainingIgnoreCase(@Param("owner") User owner, @Param("name") String name);
    
    /**
     * Find pet by microchip ID (unique identifier)
     */
    Optional<Pet> findByMicrochipId(String microchipId);
    
    /**
     * Find pet by owner and microchip ID
     */
    Optional<Pet> findByOwnerAndMicrochipId(User owner, String microchipId);
    
    /**
     * Find pet by ID and owner (security check)
     */
    Optional<Pet> findByIdAndOwner(Long petId, User owner);
    
    /**
     * Search pets by owner and various criteria (name or microchip)
     */
    @Query("SELECT p FROM Pet p WHERE p.owner = :owner AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR p.microchipId = :searchTerm)")
    List<Pet> searchByOwnerAndNameOrMicrochip(@Param("owner") User owner, @Param("searchTerm") String searchTerm);
    
    /**
     * Multi-field search for doctor - search by pet name, owner name, or microchip ID
     */
    @Query("SELECT DISTINCT p FROM Pet p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.owner.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.microchipId = :searchTerm")
    List<Pet> searchByNameOrOwnerNameOrMicrochipId(@Param("searchTerm") String searchTerm);
    
    /**
     * Find all pets ordered for doctor view
     */
    @Query("SELECT p FROM Pet p ORDER BY p.name ASC")
    List<Pet> findAllOrderByName();
}
