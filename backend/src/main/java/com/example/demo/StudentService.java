package com.example.demo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class StudentService {

    private DatabaseReference getStudentsRef() {
        return FirebaseDatabase.getInstance().getReference("students");
    }

    public CompletableFuture<List<java.util.Map<String, Object>>> getAllStudents() {
        System.out.println("Fetching students...");
        CompletableFuture<List<java.util.Map<String, Object>>> future = new CompletableFuture<>();
        try {
            getStudentsRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        List<java.util.Map<String, Object>> list = new ArrayList<>();
                        System.out.println("[DEBUG] Firebase Snapshot received.");
                        System.out.println("[DEBUG] Snapshot value: " + (dataSnapshot != null ? dataSnapshot.getValue() : "null"));
                        System.out.println("[DEBUG] Number of children: " + (dataSnapshot != null ? dataSnapshot.getChildrenCount() : 0));

                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot == null || !snapshot.exists()) continue;
                                
                                java.util.Map<String, Object> studentMap = new java.util.HashMap<>();
                                studentMap.put("id", snapshot.getKey());
                                studentMap.put("name", snapshot.child("name").getValue());
                                studentMap.put("age", snapshot.child("age").getValue());
                                studentMap.put("presentCount", snapshot.child("presentCount").getValue());
                                studentMap.put("totalDays", snapshot.child("totalDays").getValue());
                                
                                list.add(studentMap);
                            }
                        }
                        future.complete(list);
                    } catch (Exception e) {
                        System.err.println("Error mapping students: " + e.getMessage());
                        future.complete(new ArrayList<>()); // Fallback to empty
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Database error: " + databaseError.getMessage());
                    future.complete(new ArrayList<>()); // Fallback empty, never hang
                }
            });
        } catch (Exception e) {
            future.complete(new ArrayList<>()); // Complete immediately if initial call fails
        }
        return future;
    }

    public CompletableFuture<String> addStudent(Student newStudent) {
        CompletableFuture<String> future = new CompletableFuture<>();
        String id = java.util.UUID.randomUUID().toString();
        newStudent.setId(id);
        newStudent.setPresentCount(0);
        newStudent.setTotalDays(0);

        DatabaseReference studentRef = getStudentsRef().child(id);
        studentRef.setValue(newStudent, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(id);
            }
        });
        return future;
    }

    public CompletableFuture<java.util.Map<String, Object>> markAttendance(String id, String type) {
        CompletableFuture<java.util.Map<String, Object>> future = new CompletableFuture<>();
        DatabaseReference studentRef = getStudentsRef().child(id);
        
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                    
                    Object totalObj = dataSnapshot.child("totalDays").getValue();
                    long currentTotal = 0;
                    if (totalObj instanceof Number) currentTotal = ((Number)totalObj).longValue();
                    else if (totalObj != null) {
                        try { currentTotal = Long.parseLong(String.valueOf(totalObj)); } catch(Exception e){}
                    }
                    
                    long newTotalDays = currentTotal + 1;
                    updates.put("totalDays", newTotalDays);
                    
                    long newPresentCount = 0;
                    Object presentObj = dataSnapshot.child("presentCount").getValue();
                    if (presentObj instanceof Number) newPresentCount = ((Number)presentObj).longValue();
                    else if (presentObj != null) {
                        try { newPresentCount = Long.parseLong(String.valueOf(presentObj)); } catch(Exception e){}
                    }
                    
                    if ("present".equalsIgnoreCase(type)) {
                        newPresentCount += 1;
                        updates.put("presentCount", newPresentCount);
                    } else if (!"absent".equalsIgnoreCase(type)) {
                        future.completeExceptionally(new IllegalArgumentException("Invalid attendance type"));
                        return;
                    }

                    // Return map with updated values
                    java.util.Map<String, Object> updatedStudent = new java.util.HashMap<>();
                    updatedStudent.put("id", dataSnapshot.getKey());
                    updatedStudent.put("name", dataSnapshot.child("name").getValue());
                    updatedStudent.put("age", dataSnapshot.child("age").getValue());
                    updatedStudent.put("presentCount", "present".equalsIgnoreCase(type) ? newPresentCount : (presentObj != null ? presentObj : 0));
                    updatedStudent.put("totalDays", newTotalDays);
                    
                    studentRef.updateChildren(updates, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            future.completeExceptionally(databaseError.toException());
                        } else {
                            future.complete(updatedStudent);
                        }
                    });
                } else {
                    future.completeExceptionally(new RuntimeException("Student not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }
}
