package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        System.out.println("\n--- [DEBUG] GET /students RECEIVED ---");
        try {
            List<Student> students = studentService.getAllStudents().get();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/present")
    public ResponseEntity<?> markPresent(@PathVariable String id) {
        System.out.println("\n--- [DEBUG] PUT /students/" + id + "/present RECEIVED ---");
        try {
            studentService.markPresent(id).get();
            return ResponseEntity.ok("{\"message\": \"Marked present successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/absent")
    public ResponseEntity<?> markAbsent(@PathVariable String id) {
        System.out.println("\n--- [DEBUG] PUT /students/" + id + "/absent RECEIVED ---");
        try {
            studentService.markAbsent(id).get();
            return ResponseEntity.ok("{\"message\": \"Marked absent successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}