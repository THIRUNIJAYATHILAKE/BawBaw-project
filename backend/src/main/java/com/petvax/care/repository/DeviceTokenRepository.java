package com.petvax.care.repository;

import com.petvax.care.entity.DeviceToken;
import com.petvax.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUser(User user);

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    @Query("SELECT dt FROM DeviceToken dt WHERE dt.user = :user AND dt.fcmToken = :fcmToken")
    Optional<DeviceToken> findByUserAndFcmToken(@Param("user") User user, @Param("fcmToken") String fcmToken);

    @Query("SELECT dt FROM DeviceToken dt WHERE dt.user = :user ORDER BY dt.lastUsed DESC")
    List<DeviceToken> findByUserOrderByLastUsedDesc(@Param("user") User user);

    void deleteByFcmToken(String fcmToken);

    void deleteByUserAndFcmToken(User user, String fcmToken);
}
