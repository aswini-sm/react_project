package com.example.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

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
                InputStream serviceAccount = firebaseCredential.getInputStream();

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
