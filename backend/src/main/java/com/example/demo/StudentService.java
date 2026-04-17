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

    public CompletableFuture<String> markPresent(String id) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DatabaseReference studentRef = getStudentsRef().child(id);
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Student student = dataSnapshot.getValue(Student.class);
                    if (student != null) {
                        student.setPresentCount(student.getPresentCount() + 1);
                        student.setTotalClasses(student.getTotalClasses() + 1);
                        studentRef.setValue(student, (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                future.completeExceptionally(databaseError.toException());
                            } else {
                                future.complete(id);
                            }
                        });
                    }
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

    public CompletableFuture<String> markAbsent(String id) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DatabaseReference studentRef = getStudentsRef().child(id);
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Student student = dataSnapshot.getValue(Student.class);
                    if (student != null) {
                        student.setTotalClasses(student.getTotalClasses() + 1);
                        studentRef.setValue(student, (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                future.completeExceptionally(databaseError.toException());
                            } else {
                                future.complete(id);
                            }
                        });
                    }
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

