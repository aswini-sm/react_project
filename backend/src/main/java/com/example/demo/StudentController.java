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
            List<Student> students = studentService.getAllStudents().get();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/attendance")
    public ResponseEntity<?> markAttendance(@PathVariable String id, @RequestParam("type") String type) {
        System.out.println("\n--- [DEBUG] PUT /students/" + id + "/attendance?type=" + type + " RECEIVED ---");
        try {
            studentService.markAttendance(id, type).get();
            return ResponseEntity.ok("{\"message\": \"Marked " + type + " successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}