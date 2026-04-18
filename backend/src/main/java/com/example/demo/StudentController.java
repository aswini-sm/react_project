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
    public CompletableFuture<ResponseEntity<?>> addStudent(@RequestBody Student student) {
        System.out.println("\n--- [DEBUG] POST /students RECEIVED ---");
        return studentService.addStudent(student).thenApply(id -> 
            (ResponseEntity<?>) ResponseEntity.ok("{\"message\": \"Student created\", \"id\": \"" + id + "\"}")
        ).exceptionally(e -> {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        });
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<?>> getAll() {
        System.out.println("\n--- [DEBUG] GET /students RECEIVED ---");
        return studentService.getAllStudents().thenApply(students -> {
            System.out.println("Students fetched: " + (students != null ? students.size() : 0));
            return (ResponseEntity<?>) ResponseEntity.ok(students != null ? students : java.util.Collections.emptyList());
        }).exceptionally(e -> {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        });
    }

    @PutMapping("/{id}/attendance")
    public CompletableFuture<ResponseEntity<?>> markAttendance(@PathVariable String id, @RequestParam("type") String type) {
        System.out.println("\n--- [DEBUG] PUT /students/" + id + "/attendance?type=" + type + " RECEIVED ---");
        return studentService.markAttendance(id, type).thenApply(updatedStudent -> 
            (ResponseEntity<?>) ResponseEntity.ok(updatedStudent)
        ).exceptionally(e -> {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        });
    }
}