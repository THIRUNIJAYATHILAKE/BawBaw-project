package com.petvax.care.service;

import com.petvax.care.dto.RegisterDeviceTokenRequest;
import com.petvax.care.entity.DeviceToken;
import com.petvax.care.entity.User;
import com.petvax.care.exception.UserNotFoundException;
import com.petvax.care.repository.DeviceTokenRepository;
import com.petvax.care.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DeviceTokenService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Register a new device token for a user
     */
    public DeviceToken registerDeviceToken(Long userId, RegisterDeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        DeviceToken existing = deviceTokenRepository.findByUserAndFcmToken(user, request.getFcmToken())
                .orElse(null);

        if (existing != null) {
            existing.setLastUsed(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());
            log.debug("Updated existing device token for user: {}", userId);
            return deviceTokenRepository.save(existing);
        }

        DeviceToken deviceToken = DeviceToken.builder()
                .user(user)
                .fcmToken(request.getFcmToken())
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .lastUsed(LocalDateTime.now())
                .build();

        log.info("Registered new device token for user: {} device: {}", userId, request.getDeviceName());
        return deviceTokenRepository.save(deviceToken);
    }

    /**
     * Get all active tokens for a user (updated in last 30 days)
     */
    public List<String> getActiveTokensForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return deviceTokenRepository.findByUserOrderByLastUsedDesc(user)
                .stream()
                .filter(token -> token.getLastUsed() != null && token.getLastUsed().isAfter(thirtyDaysAgo))
                .map(DeviceToken::getFcmToken)
                .collect(Collectors.toList());
    }

    /**
     * Get all tokens for a user (regardless of activity)
     */
    public List<String> getAllTokensForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return deviceTokenRepository.findByUser(user)
                .stream()
                .map(DeviceToken::getFcmToken)
                .collect(Collectors.toList());
    }

    /**
     * Get all device tokens for a user with details
     */
    public List<DeviceToken> getUserDeviceTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return deviceTokenRepository.findByUserOrderByLastUsedDesc(user);
    }

    /**
     * Invalidate a device token (mark as deleted)
     */
    public void invalidateDeviceToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        deviceTokenRepository.deleteByUserAndFcmToken(user, fcmToken);
        log.info("Invalidated device token for user: {}", userId);
    }

    /**
     * Remove a device token by FCM token alone
     */
    public void removeDeviceToken(String fcmToken) {
        deviceTokenRepository.deleteByFcmToken(fcmToken);
        log.debug("Removed device token: {}", fcmToken);
    }

    /**
     * Update last used timestamp for a token
     */
    public void updateLastUsed(String fcmToken) {
        deviceTokenRepository.findByFcmToken(fcmToken).ifPresent(token -> {
            token.setLastUsed(LocalDateTime.now());
            deviceTokenRepository.save(token);
        });
    }
}
