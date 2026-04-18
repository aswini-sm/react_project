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

    public CompletableFuture<List<Student>> getAllStudents() {
        CompletableFuture<List<Student>> future = new CompletableFuture<>();
        getStudentsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Student> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Student student = snapshot.getValue(Student.class);
                    if (student != null) {
                        student.setId(snapshot.getKey());
                        list.add(student);
                    }
                }
                future.complete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }

    public CompletableFuture<String> addStudent(Student newStudent) {
        CompletableFuture<String> future = new CompletableFuture<>();
        // Generate unique ID
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

    public CompletableFuture<Student> markAttendance(String id, String type) {
        CompletableFuture<Student> future = new CompletableFuture<>();
        DatabaseReference studentRef = getStudentsRef().child(id);
        
        System.out.println("[SERVICE DEBUG] Fetching student from Firebase with ID: " + id);
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Student student = dataSnapshot.getValue(Student.class);
                    if (student != null) {
                        System.out.println("[SERVICE DEBUG] Student found: " + student.getName());
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        long newTotalDays = student.getTotalDays() + 1;
                        updates.put("totalDays", newTotalDays);
                        
                        if ("present".equalsIgnoreCase(type)) {
                            long newPresentCount = student.getPresentCount() + 1;
                            updates.put("presentCount", newPresentCount);
                            student.setPresentCount(newPresentCount);
                        } else if ("absent".equalsIgnoreCase(type)) {
                            // Only total days incremented above
                        } else {
                            future.completeExceptionally(new IllegalArgumentException("Invalid attendance type"));
                            return;
                        }
                        
                        student.setTotalDays(newTotalDays);
                        
                        studentRef.updateChildren(updates, (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                future.completeExceptionally(databaseError.toException());
                            } else {
                                future.complete(student);
                            }
                        });
                    }
                } else {
                    System.err.println("[SERVICE DEBUG] Student Snapshot does not exist for ID: " + id);
                    future.completeExceptionally(new RuntimeException("Student not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("[SERVICE DEBUG] Firebase read cancelled: " + databaseError.getMessage());
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }
}
