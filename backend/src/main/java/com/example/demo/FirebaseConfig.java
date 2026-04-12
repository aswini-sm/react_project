package com.example.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    // Spring will automatically inject the file from classpath: or file: based on the prefix in application.properties
    @Value("${firebase.credential.path}")
    private Resource firebaseCredential;

    @PostConstruct
    public void initialize() {
        try {
            // Check to prevent re-initialization if the context is reloaded
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;
                String firebaseEnv = System.getenv("FIREBASE_CREDENTIALS");
                
                if (firebaseEnv != null && !firebaseEnv.trim().isEmpty()) {
                    // Load from Render Environment Variable
                    serviceAccount = new ByteArrayInputStream(firebaseEnv.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Loading Firebase credentials from FIREBASE_CREDENTIALS environment variable.");
                } else {
                    // Fallback to local file (e.g. classpath:firebase/serviceAccountKey.json)
                    serviceAccount = firebaseCredential.getInputStream();
                    System.out.println("Loading Firebase credentials from local file path.");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Application has been initialized successfully!");
            }
        } catch (Exception e) {
            System.err.println("🔥 Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
