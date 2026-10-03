package com.petvax.care.repository;

import com.petvax.care.entity.User;
import com.petvax.care.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find users by username or email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
}
