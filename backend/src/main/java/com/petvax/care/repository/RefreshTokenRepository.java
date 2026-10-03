package com.petvax.care.repository;

import com.petvax.care.entity.RefreshToken;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find refresh token by token value
     */
    Optional<RefreshToken> findByTokenValue(String tokenValue);
    
    /**
     * Find refresh token by user
     */
    Optional<RefreshToken> findByUser(User user);
    
    /**
     * Delete all expired tokens
     */
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
    
    /**
     * Check if token exists and is not revoked
     */
    Optional<RefreshToken> findByTokenValueAndRevokedFalse(String tokenValue);
    
    /**
     * Revoke token by value
     */
    void deleteByUser(User user);
}
