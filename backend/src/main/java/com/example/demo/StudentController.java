package com.example.demo;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    // Use Service Layer instead of JPA repository
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Student> students = studentService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable String id) {
        try {
            Student student = studentService.getStudentById(id);
            if (student != null) {
                return ResponseEntity.ok(student);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<String> add(@RequestBody Student s) {
        try {
            s.setDate(new java.util.Date()); // Use java.util.Date for Firestore Timestamp
            String updateTime = studentService.saveStudent(s);
            return ResponseEntity.status(HttpStatus.CREATED).body("Student created at: " + updateTime);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody Student s) {
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("ID cannot be empty");
        }
        try {
            String updateTime = studentService.updateStudent(id, s);
            if (updateTime != null) {
                return ResponseEntity.ok("Student updated at: " + updateTime);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String id, @RequestBody StatusUpdateRequest payload) {
        String newStatus = payload.getStatus();

        System.out.println("\n--- [DEBUG] UPDATE REQUEST RECEIVED ---");
        System.out.println("ID Received: " + id);
        System.out.println("Status Received: " + newStatus);

        if (id == null || id.trim().isEmpty() || newStatus == null || newStatus.trim().isEmpty()) {
            System.err.println("❌ ERROR: Missing ID or status value from frontend.");
            return ResponseEntity.badRequest().body("ID and valid status string are required");
        }

        try {
            String updateTime = studentService.updateStudentStatus(id, newStatus);
            System.out.println("✅ SUCCESS: Document updated in Firestore at: " + updateTime);
            System.out.println("---------------------------------------\n");
            return ResponseEntity.ok("Student status updated at: " + updateTime);
        } catch (RuntimeException e) {
            System.err.println("❌ ERROR (Not Found): " + e.getMessage());
            System.out.println("---------------------------------------\n");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ERROR (Firestore Failure): " + e.getMessage());
            e.printStackTrace();
            System.out.println("---------------------------------------\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            String updateTime = studentService.deleteStudent(id);
            return ResponseEntity.ok("Student deleted at: " + updateTime);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}