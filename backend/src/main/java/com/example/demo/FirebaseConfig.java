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
                String type = System.getenv("FIREBASE_TYPE");
                String projectIdEnv = System.getenv("FIREBASE_PROJECT_ID");
                String privateKey = System.getenv("FIREBASE_PRIVATE_KEY");
                String clientEmail = System.getenv("FIREBASE_CLIENT_EMAIL");

                // IF environment variables exist (Render Deployment)
                if (type != null && projectIdEnv != null && privateKey != null && clientEmail != null) {
                    System.out.println("[DEPLOYMENT DEBUG] Using individual ENV Firebase credentials");

                    // Render might literalize the \n characters, so we enforce real newlines
                    privateKey = privateKey.replace("\\n", "\n");

                    // Manually build the JSON required by Firebase Admin SDK
                    String json = String.format(
                        "{\"type\": \"%s\", \"project_id\": \"%s\", \"private_key\": \"%s\", \"client_email\": \"%s\"}",
                        type, projectIdEnv, privateKey.replace("\n", "\\n"), clientEmail
                    );

                    serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                } else {
                    // ELSE load from local file for development
                    System.out.println("Using LOCAL Firebase credentials");

                    // Note: serviceAccountKey.json MUST be placed in:
                    // src/main/resources/serviceAccountKey.json
                    serviceAccount = getClass().getResourceAsStream("/serviceAccountKey.json");

                    // Fallback to the old /firebase/ directory path just in case it hasn't been
                    // moved yet
                    if (serviceAccount == null) {
                        serviceAccount = getClass().getResourceAsStream("/firebase/serviceAccountKey.json");
                    }
                }

                // If BOTH env variable and file are missing -> throw explicit error
                if (serviceAccount == null) {
                    System.err.println("🔥 CRITICAL ERROR: Firebase credentials NOT FOUND!");
                    System.err.println("Ensure FIREBASE_TYPE, FIREBASE_PROJECT_ID, FIREBASE_PRIVATE_KEY, and FIREBASE_CLIENT_EMAIL are set in Render.");
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
