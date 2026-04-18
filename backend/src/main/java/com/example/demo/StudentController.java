package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok("{\"status\": \"UP\"}");
    }

    @PostMapping
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        System.out.println("\n--- [DEBUG] POST /students RECEIVED ---");
        try {
            String id = studentService.addStudent(student).get(15, TimeUnit.SECONDS);
            return ResponseEntity.ok("{\"message\": \"Student created\", \"id\": \"" + id + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        System.out.println("\n--- [DEBUG] GET /students RECEIVED ---");
        try {
            List<Student> students = studentService.getAllStudents().get(15, TimeUnit.SECONDS);
            System.out.println("Students fetched: " + (students != null ? students.size() : 0));
            return ResponseEntity.ok(students != null ? students : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList()); // never hang, always fallback
        }
    }

    @PutMapping("/{id}/attendance")
    public ResponseEntity<?> markAttendance(@PathVariable String id, @RequestParam("type") String type) {
        System.out.println("\n--- [DEBUG] PUT /students/" + id + "/attendance?type=" + type + " RECEIVED ---");
        System.out.println("[DEBUG] Payload received -> ID: " + id + ", Type: " + type);
        try {
            Student updatedStudent = studentService.markAttendance(id, type).get(15, TimeUnit.SECONDS);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}