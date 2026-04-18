package com.example.demo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class StudentService {

    private DatabaseReference getStudentsRef() {
        return FirebaseDatabase.getInstance().getReference("students");
    }

    public List<Map<String, Object>> getAllStudentsSync() {
        System.out.println("Fetching students synchronously (read-only mode)...");
        List<Map<String, Object>> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        try {
            getStudentsRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot == null || !snapshot.exists()) continue;
                                
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", snapshot.getKey());
                                Object name = snapshot.child("name").getValue();
                                Object age = snapshot.child("age").getValue();
                                Object presentCount = snapshot.child("presentCount").getValue();
                                Object totalDays = snapshot.child("totalDays").getValue();
                                
                                map.put("name", name != null ? name : "Unknown");
                                map.put("age", age != null ? age : 0);
                                map.put("presentCount", presentCount != null ? presentCount : 0);
                                map.put("totalDays", totalDays != null ? totalDays : 0);
                                result.add(map);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing Firebase snapshot: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Database error: " + databaseError.getMessage());
                    latch.countDown();
                }
            });
            
            // Block Spring Thread securely for maximum 10 seconds waiting on Firebase Thread
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("Firebase fetch strictly timed out after 10 seconds.");
            }
        } catch (Exception e) {
            System.err.println("Error executing synchronous fetch: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        
        return result;
    }
}
