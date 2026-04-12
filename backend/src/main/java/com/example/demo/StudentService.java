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
import java.util.concurrent.ExecutionException;

@Service
public class StudentService {

    private static final String COLLECTION_NAME = "students";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public List<Student> getAllStudents() throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null")).get();
        
        List<Student> students = new ArrayList<>();
        // future.get() blocks on completion
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            students.add(document.toObject(Student.class));
        }
        return students;
    }

    public Student getStudentById(Integer id) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(String.valueOf(id), "id cannot be null"));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            return document.toObject(Student.class);
        }
        return null;
    }

    public String saveStudent(Student student) throws ExecutionException, InterruptedException {
        if (student.getId() == null) {
            // Generate a random integer ID (since Firestore timestamp is too big for int)
            student.setId((int) (System.currentTimeMillis() & 0xfffffff));
        }
        Firestore db = getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(String.valueOf(student.getId()), "student ID cannot be null"))
                .set(Objects.requireNonNull(student, "student cannot be null"));
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String updateStudent(Integer id, Student studentUpdates) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(String.valueOf(id), "id cannot be null"));
        
        // Fetch existing first to ensure it's a partial update (optional depending on needs, but useful to preserve fields)
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            Student existing = document.toObject(Student.class);
            if (existing != null) {
                if (studentUpdates.getStatus() != null) existing.setStatus(studentUpdates.getStatus());
                if (studentUpdates.getName() != null) existing.setName(studentUpdates.getName());
                // For simplicity, we just set the whole object back
                ApiFuture<WriteResult> updateFuture = docRef.set(existing);
                return updateFuture.get().getUpdateTime().toString();
            }
        }
        return null; // Not found
    }

    public String updateStudentStatus(Integer id, String newStatus) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(String.valueOf(id), "id cannot be null"));
        
        // 🛠️ Debug Verification: Check if document actually exists first
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Document with ID '" + id + "' not found in Firestore.");
        }

        // .update() only modifies the explicitly provided fields, leaving the rest of the document intact
        ApiFuture<WriteResult> updateFuture = docRef.update("status", newStatus);
        return updateFuture.get().getUpdateTime().toString();
    }

    public String deleteStudent(Integer id) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection(Objects.requireNonNull(COLLECTION_NAME, "collection name cannot be null"))
                .document(Objects.requireNonNull(String.valueOf(id), "id cannot be null")).delete();
        return writeResult.get().getUpdateTime().toString();
    }
}
