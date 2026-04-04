package com.example.demo;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin("*")
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository repo;

    public StudentController(StudentRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Student> getAll() {
        return repo.findAll();
    }

  @PostMapping
public Student add(@RequestBody Student s) {
    s.setDate(java.time.LocalDate.now().toString());
    return repo.save(s);
}
  @PutMapping("/{id}")
public Student update(@PathVariable Long id, @RequestBody Student s) {
    if (id == null) {
        throw new RuntimeException("ID cannot be null");
    }

    Student existing = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));

    existing.setStatus(s.getStatus());
    return repo.save(existing);
}
}