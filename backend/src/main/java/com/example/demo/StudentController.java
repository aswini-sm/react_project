package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        System.out.println("\n--- [DEBUG] POST /students RECEIVED ---");
        try {
            String id = studentService.addStudent(student).get();
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
            // NEVER HANG: Timeout after 15 seconds
            List<Student> students = studentService.getAllStudents().get(15, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println("Students fetched: " + (students != null ? students.size() : 0));
            return ResponseEntity.ok(students != null ? students : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
            // ALWAYS return empty list on failure, never hang
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @PutMapping("/{id}/attendance")
    public ResponseEntity<?> markAttendance(@PathVariable String id, @RequestParam("type") String type) {
        System.out.println("\n--- [DEBUG] PUT /students/" + id + "/attendance?type=" + type + " RECEIVED ---");
        try {
            Student updatedStudent = studentService.markAttendance(id, type).get();
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}