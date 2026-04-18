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
                String json = System.getenv("FIREBASE_CREDENTIAL_JSON");
                System.out.println("Firebase JSON loaded: " + (json != null));
                
                if (json == null || json.trim().isEmpty()) {
                    throw new IllegalArgumentException("FIREBASE_CREDENTIAL_JSON environment variable is missing or empty.");
                }

                System.out.println("Firebase JSON length: " + json.length());

                // Fix common env issues:
                // Remove extra quotes wrapping entire JSON
                json = json.trim();
                if (json.startsWith("\"") && json.endsWith("\"")) {
                    json = json.substring(1, json.length() - 1);
                }
                
                // Replace all line breaks in private_key with real newlines
                json = json.replace("\\n", "\n");

                InputStream serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setDatabaseUrl("https://twelvefirebase-default-rtdb.asia-southeast1.firebasedatabase.app/");

                // Explicitly set Project ID to prevent 502 Bad Gateway timeout hangs on Render
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
            System.err.println("🔥 Error initializing Firebase:");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
}
