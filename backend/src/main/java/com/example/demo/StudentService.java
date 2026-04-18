package com.example.demo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StudentService {

    private DatabaseReference getStudentsRef() {
        return FirebaseDatabase.getInstance().getReference("students");
    }

    public List<java.util.Map<String, Object>> getAllStudents() {
        System.out.println("Fetching students synchronously from Firebase using ApiFuture...");
        try {
            // Using synchronous ApiFuture wait to completely bypass background thread listener issues
            com.google.api.core.ApiFuture<DataSnapshot> future = getStudentsRef().get();
            DataSnapshot dataSnapshot = future.get(20, TimeUnit.SECONDS);

            List<java.util.Map<String, Object>> list = new ArrayList<>();
            System.out.println("[DEBUG] Firebase Snapshot received. exists()=" + (dataSnapshot != null ? dataSnapshot.exists() : false));
            System.out.println("[DEBUG] Number of children: " + (dataSnapshot != null ? dataSnapshot.getChildrenCount() : 0));
            System.out.println("[DEBUG] Raw Firebase Snapshot value: " + (dataSnapshot != null ? dataSnapshot.getValue() : "null"));

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
            return list;
        } catch (java.util.concurrent.TimeoutException e) {
            System.err.println("🔥 CRITICAL TIMEOUT: Firebase get(...) timed out after 20 seconds.");
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("🔥 ERROR: Firebase fetch failed: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String addStudent(Student newStudent) {
        String id = java.util.UUID.randomUUID().toString();
        newStudent.setId(id);
        newStudent.setPresentCount(0);
        newStudent.setTotalDays(0);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorRef = new AtomicReference<>(null);

        DatabaseReference studentRef = getStudentsRef().child(id);
        studentRef.setValue(newStudent, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                errorRef.set(databaseError.getMessage());
            }
            latch.countDown();
        });
        
        try {
            latch.await(15, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Add student timed out");
        }
        
        if (errorRef.get() != null) {
            throw new RuntimeException(errorRef.get());
        }
        return id;
    }

    public java.util.Map<String, Object> markAttendance(String id, String type) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<java.util.Map<String, Object>> resultRef = new AtomicReference<>(null);
        AtomicReference<String> errorRef = new AtomicReference<>(null);
        
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
                        errorRef.set("Invalid attendance type");
                        latch.countDown();
                        return;
                    }

                    java.util.Map<String, Object> updatedStudent = new java.util.HashMap<>();
                    updatedStudent.put("id", dataSnapshot.getKey());
                    updatedStudent.put("name", dataSnapshot.child("name").getValue());
                    updatedStudent.put("age", dataSnapshot.child("age").getValue());
                    updatedStudent.put("presentCount", "present".equalsIgnoreCase(type) ? newPresentCount : (presentObj != null ? presentObj : 0));
                    updatedStudent.put("totalDays", newTotalDays);
                    
                    studentRef.updateChildren(updates, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            errorRef.set(databaseError.getMessage());
                        } else {
                            resultRef.set(updatedStudent);
                        }
                        latch.countDown();
                    });
                } else {
                    errorRef.set("Student not found");
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorRef.set(databaseError.getMessage());
                latch.countDown();
            }
        });
        
        try {
            boolean done = latch.await(15, java.util.concurrent.TimeUnit.SECONDS);
            if (!done) {
                throw new RuntimeException("Firebase markAttendance timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted");
        }
        
        if (errorRef.get() != null) {
            throw new RuntimeException(errorRef.get());
        }
        
        return resultRef.get();
    }
}
