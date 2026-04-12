package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    private final FirestoreService firestoreService;

    @Autowired
    public TestController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping("/test-firestore")
    public ResponseEntity<String> testFirestore() {
        try {
            // Prepare sample data
            Map<String, Object> data = new HashMap<>();
            data.put("name", "Firebase Test User");
            data.put("status", "Online");
            data.put("timestamp", Instant.now().toString());

            // Insert into 'test' collection with document id 'sample'
            String updateTime = firestoreService.insertData("test", "sample", data);
            
            String successMessage = "🔥 Firestore connection successful! Document 'sample' in collection 'test' created/updated at: " + updateTime;
            System.out.println(successMessage);
            
            return ResponseEntity.ok(successMessage);
        } catch (Exception e) {
            String errorMessage = "⚠️ Firestore connection failed: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}
