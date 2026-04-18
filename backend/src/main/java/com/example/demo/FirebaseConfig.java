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
            // Prevent multiple initializations
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;
                String json = System.getenv("FIREBASE_CREDENTIAL_JSON");
                
                // IF environment variable FIREBASE_CREDENTIAL_JSON exists
                if (json != null && !json.trim().isEmpty()) {
                    System.out.println("Using ENV Firebase credentials");
                    
                    // Handle JSON string formatting issues (strip quotes, fix newlines)
                    json = json.trim();
                    if (json.startsWith("\"") && json.endsWith("\"")) {
                        json = json.substring(1, json.length() - 1);
                    }
                    json = json.replace("\\n", "\n");
                    
                    serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                } else {
                    // ELSE load from local file for development
                    System.out.println("Using LOCAL Firebase credentials");
                    
                    // Note: serviceAccountKey.json MUST be placed in:
                    // src/main/resources/serviceAccountKey.json
                    serviceAccount = getClass().getResourceAsStream("/serviceAccountKey.json");
                    
                    // Fallback to the old /firebase/ directory path just in case it hasn't been moved yet
                    if (serviceAccount == null) {
                        serviceAccount = getClass().getResourceAsStream("/firebase/serviceAccountKey.json");
                    }
                }
                
                // If BOTH env variable and file are missing -> throw explicit error
                if (serviceAccount == null) {
                    throw new RuntimeException("Firebase credentials not found in ENV or resources");
                }

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setDatabaseUrl("https://twelvefirebase-default-rtdb.asia-southeast1.firebasedatabase.app/");

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
