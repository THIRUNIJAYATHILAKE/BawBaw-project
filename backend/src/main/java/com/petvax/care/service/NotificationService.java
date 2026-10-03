// package com.petvax.care.service;

// // import com.google.firebase.messaging.FirebaseMessaging;
// // import com.google.firebase.messaging.FirebaseMessagingException;
// // import com.google.firebase.messaging.Message;
// // import com.google.firebase.messaging.Notification;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @Service
// @Slf4j
// public class NotificationService {

//     @Autowired
//     private FirebaseMessaging firebaseMessaging;

//     @Autowired
//     private DeviceTokenService deviceTokenService;

//     /**
//      * Send a push notification via FCM
//      */
//     public boolean sendPushNotification(Long userId, String title, String body, Map<String, String> data) {
//         try {
//             List<String> tokens = deviceTokenService.getActiveTokensForUser(userId);

//             if (tokens.isEmpty()) {
//                 log.warn("No active device tokens found for user: {}", userId);
//                 return false;
//             }

//             for (String token : tokens) {
//                 try {
//                     sendNotificationToToken(token, title, body, data);
//                 } catch (FirebaseMessagingException e) {
//                     log.error("Failed to send notification to token: {}", token, e);
//                     if (isInvalidTokenError(e)) {
//                         deviceTokenService.invalidateDeviceToken(userId, token);
//                         log.info("Invalidated token for user: {}", userId);
//                     }
//                 }
//             }

//             return true;
//         } catch (Exception e) {
//             log.error("Error sending push notification to user: {}", userId, e);
//             return false;
//         }
//     }

//     /**
//      * Send notification to a specific token
//      */
//     private String sendNotificationToToken(String token, String title, String body, Map<String, String> data)
//             throws FirebaseMessagingException {
//         Notification notification = Notification.builder()
//                 .setTitle(title)
//                 .setBody(body)
//                 .build();

//         Message.Builder messageBuilder = Message.builder()
//                 .setToken(token)
//                 .setNotification(notification);

//         if (data != null && !data.isEmpty()) {
//             messageBuilder.putAllData(data);
//         }

//         Message message = messageBuilder.build();
//         String response = firebaseMessaging.send(message);
//         log.debug("Notification sent successfully: {}", response);
//         return response;
//     }

//     /**
//      * Send notifications to multiple users
//      */
//     public Map<Long, Boolean> sendMultipleNotifications(Map<Long, NotificationData> notifications) {
//         Map<Long, Boolean> results = new HashMap<>();

//         for (Map.Entry<Long, NotificationData> entry : notifications.entrySet()) {
//             Long userId = entry.getKey();
//             NotificationData notificationData = entry.getValue();

//             boolean sent = sendPushNotification(
//                     userId,
//                     notificationData.getTitle(),
//                     notificationData.getBody(),
//                     notificationData.getData()
//             );

//             results.put(userId, sent);
//         }

//         return results;
//     }

//     /**
//      * Build FCM notification payload
//      */
//     public Map<String, String> buildNotificationPayload(String type, String petName, String details) {
//         Map<String, String> data = new HashMap<>();
//         data.put("type", type);
//         data.put("petName", petName);
//         data.put("details", details);
//         data.put("timestamp", String.valueOf(System.currentTimeMillis()));
//         return data;
//     }

//     /**
//      * Check if error is due to invalid token
//      */
//     private boolean isInvalidTokenError(FirebaseMessagingException e) {
//         String message = e.getMessage();
//         return message != null && (message.contains("invalid-argument") ||
//                 message.contains("unregistered") ||
//                 message.contains("not-registered") ||
//                 message.contains("invalid registration") ||
//                 message.contains("third party auth error"));
//     }

//     /**
//      * Data class for notification
//      */
//     public static class NotificationData {
//         private String title;
//         private String body;
//         private Map<String, String> data;

//         public NotificationData(String title, String body, Map<String, String> data) {
//             this.title = title;
//             this.body = body;
//             this.data = data;
//         }

//         public String getTitle() {
//             return title;
//         }

//         public String getBody() {
//             return body;
//         }

//         public Map<String, String> getData() {
//             return data;
//         }
//     }
// }


package com.petvax.care.service;

// import com.google.firebase.messaging.FirebaseMessaging;
// import com.google.firebase.messaging.FirebaseMessagingException;
// import com.google.firebase.messaging.Message;
// import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    // @Autowired
    // private FirebaseMessaging firebaseMessaging;

    // Comment out DeviceTokenService if it also uses Firebase
    // @Autowired
    // private DeviceTokenService deviceTokenService;

    /**
     * Send a push notification via FCM
     */
    public boolean sendPushNotification(Long userId, String title, String body, Map<String, String> data) {
        log.warn("Firebase notifications are disabled. Would send notification to user: {} - {}: {}", userId, title,
                body);
        return false;

        // Original code commented out
        /*
         * try {
         * List<String> tokens = deviceTokenService.getActiveTokensForUser(userId);
         * 
         * if (tokens.isEmpty()) {
         * log.warn("No active device tokens found for user: {}", userId);
         * return false;
         * }
         * 
         * for (String token : tokens) {
         * try {
         * sendNotificationToToken(token, title, body, data);
         * } catch (FirebaseMessagingException e) {
         * log.error("Failed to send notification to token: {}", token, e);
         * if (isInvalidTokenError(e)) {
         * deviceTokenService.invalidateDeviceToken(userId, token);
         * log.info("Invalidated token for user: {}", userId);
         * }
         * }
         * }
         * 
         * return true;
         * } catch (Exception e) {
         * log.error("Error sending push notification to user: {}", userId, e);
         * return false;
         * }
         */
    }

    /**
     * Send notification to a specific token
     */
    private String sendNotificationToToken(String token, String title, String body, Map<String, String> data) {
        log.warn("Firebase notifications are disabled. Would send to token: {}", token);
        return "disabled";

        // Original code commented out
        /*
         * try {
         * Notification notification = Notification.builder()
         * .setTitle(title)
         * .setBody(body)
         * .build();
         * 
         * Message.Builder messageBuilder = Message.builder()
         * .setToken(token)
         * .setNotification(notification);
         * 
         * if (data != null && !data.isEmpty()) {
         * messageBuilder.putAllData(data);
         * }
         * 
         * Message message = messageBuilder.build();
         * String response = firebaseMessaging.send(message);
         * log.debug("Notification sent successfully: {}", response);
         * return response;
         * } catch (FirebaseMessagingException e) {
         * log.error("Failed to send notification to token: {}", token, e);
         * throw e;
         * }
         */
    }

    /**
     * Send notifications to multiple users
     */
    public Map<Long, Boolean> sendMultipleNotifications(Map<Long, NotificationData> notifications) {
        log.warn("Firebase notifications are disabled. Would send {} notifications", notifications.size());
        Map<Long, Boolean> results = new HashMap<>();
        for (Long userId : notifications.keySet()) {
            results.put(userId, false);
        }
        return results;

        // Original code commented out
        /*
         * Map<Long, Boolean> results = new HashMap<>();
         * 
         * for (Map.Entry<Long, NotificationData> entry : notifications.entrySet()) {
         * Long userId = entry.getKey();
         * NotificationData notificationData = entry.getValue();
         * 
         * boolean sent = sendPushNotification(
         * userId,
         * notificationData.getTitle(),
         * notificationData.getBody(),
         * notificationData.getData()
         * );
         * 
         * results.put(userId, sent);
         * }
         * 
         * return results;
         */
    }

    /**
     * Build FCM notification payload
     */
    public Map<String, String> buildNotificationPayload(String type, String petName, String details) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("petName", petName);
        data.put("details", details);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        log.debug("Built notification payload (disabled Firebase): type={}, petName={}", type, petName);
        return data;
    }

    /**
     * Check if error is due to invalid token
     */
    private boolean isInvalidTokenError(/* FirebaseMessagingException e */ Exception e) {
        String message = e.getMessage();
        return message != null && (message.contains("invalid-argument") ||
                message.contains("unregistered") ||
                message.contains("not-registered") ||
                message.contains("invalid registration") ||
                message.contains("third party auth error"));
    }

    /**
     * Data class for notification
     */
    public static class NotificationData {
        private String title;
        private String body;
        private Map<String, String> data;

        public NotificationData(String title, String body, Map<String, String> data) {
            this.title = title;
            this.body = body;
            this.data = data;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public Map<String, String> getData() {
            return data;
        }
    }
}