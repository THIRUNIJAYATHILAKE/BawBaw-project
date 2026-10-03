// package com.petvax.care.config;

// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.firebase.FirebaseApp;
// import com.google.firebase.FirebaseOptions;
// import com.google.firebase.messaging.FirebaseMessaging;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import java.io.FileInputStream;
// import java.io.IOException;

// @Configuration
// @Slf4j
// public class FirebaseConfig {

//     @Value("${firebase.credentials.path:}")
//     private String firebaseCredentialsPath;

//     @Bean
//     public FirebaseMessaging firebaseMessaging() throws IOException {
//         if (firebaseCredentialsPath == null || firebaseCredentialsPath.isEmpty()) {
//             log.warn("Firebase credentials path not configured. Using default credentials.");
//             if (FirebaseApp.getApps().isEmpty()) {
//                 FirebaseApp.initializeApp();
//             }
//         } else {
//             if (FirebaseApp.getApps().isEmpty()) {
//                 GoogleCredentials credentials = GoogleCredentials.fromStream(
//                         new FileInputStream(firebaseCredentialsPath));

//                 FirebaseOptions options = FirebaseOptions.builder()
//                         .setCredentials(credentials)
//                         .build();

//                 FirebaseApp.initializeApp(options);
//                 log.info("Firebase initialized with credentials from: {}", firebaseCredentialsPath);
//             }
//         }

//         return FirebaseMessaging.getInstance();
//     }
// }
