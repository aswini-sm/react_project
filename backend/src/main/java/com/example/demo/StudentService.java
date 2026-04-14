package com.example.demo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class StudentService {

    private static final String COLLECTION_NAME = "students"; // EXACT collection name

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public List<Student> getAllStudents() throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        List<Student> list = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            Student student = doc.toObject(Student.class);
            if (student != null) {
                student.setId(doc.getId()); // IMPORTANT mapping
                list.add(student);
            }
        }
        return list;
    }

    public Student getStudentById(String id) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(id, "id cannot be null"));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            Student s = document.toObject(Student.class);
            if (s != null) {
                s.setId(document.getId());
                return s;
            }
        }
        return null;
    }

    public String saveStudent(Student student) throws ExecutionException, InterruptedException {
        if (student.getId() == null || student.getId().trim().isEmpty()) {
            // Generate a random UUID String for the ID
            student.setId(UUID.randomUUID().toString());
        }
        Firestore db = getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(student.getId(), "student ID cannot be null"))
                .set(Objects.requireNonNull(student, "student cannot be null"));
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String updateStudent(String id, Student studentUpdates) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(id, "id cannot be null"));
        
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            Student existing = document.toObject(Student.class);
            if (existing != null) {
                if (studentUpdates.getStatus() != null) existing.setStatus(studentUpdates.getStatus());
                if (studentUpdates.getName() != null) existing.setName(studentUpdates.getName());
                ApiFuture<WriteResult> updateFuture = docRef.set(existing);
                return updateFuture.get().getUpdateTime().toString();
            }
        }
        return null; // Not found
    }

    public String updateStudentStatus(String id, String newStatus) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(id, "id cannot be null"));
        
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Document with ID '" + id + "' not found in Firestore.");
        }

        ApiFuture<WriteResult> updateFuture = docRef.update("status", newStatus);
        return updateFuture.get().getUpdateTime().toString();
    }

    public String deleteStudent(String id) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(id, "id cannot be null")).delete();
        return writeResult.get().getUpdateTime().toString();
    }
}
