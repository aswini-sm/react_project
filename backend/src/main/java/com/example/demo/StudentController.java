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
        return ResponseEntity.ok("{\"status\": \"UP\", \"mode\": \"readonly\"}");
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        System.out.println("\n--- [DEBUG] GET /students RECEIVED ---");
        try {
            List<java.util.Map<String, Object>> students = studentService.getAllStudentsSync();
            System.out.println("Students fetched: " + (students != null ? students.size() : 0));
            return ResponseEntity.ok(students != null ? students : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList()); // never hang, always fallback
        }
    }
}