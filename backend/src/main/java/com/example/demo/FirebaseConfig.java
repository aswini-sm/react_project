package com.example.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Check to prevent re-initialization if the context is reloaded
            if (FirebaseApp.getApps().isEmpty()) {
                String firebaseEnv = System.getenv("FIREBASE_CREDENTIAL_JSON");
                
                if (firebaseEnv == null || firebaseEnv.trim().isEmpty()) {
                    throw new IllegalStateException("Missing environment variable: FIREBASE_CREDENTIAL_JSON. " +
                            "Please configure this in your Render environment variables with the complete service account JSON.");
                }

                // Load from Render Environment Variable
                InputStream serviceAccount = new ByteArrayInputStream(firebaseEnv.getBytes(StandardCharsets.UTF_8));
                System.out.println("Loading Firebase credentials strictly from FIREBASE_CREDENTIAL_JSON environment variable.");

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(credentials);

                // 🔥 CRITICAL FIX: Explicitly set Project ID to prevent 502 Bad Gateway timeout hangs on Render
                if (credentials instanceof ServiceAccountCredentials) {
                    String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
                    if (projectId != null) {
                        optionsBuilder.setProjectId(projectId);
                        System.out.println("✅ Successfully extracted and set Firebase Project ID: " + projectId);
                    }
                }

                FirebaseApp.initializeApp(optionsBuilder.build());
                System.out.println("✅ Firebase Application has been initialized successfully!");
            }
        } catch (Exception e) {
            System.err.println("🔥 Error initializing Firebase: " + e.getMessage());
            // Optionally, we can rethrow as RuntimeException to guarantee the server stops 
            // and fails health checks immediately instead of returning 500s later.
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
