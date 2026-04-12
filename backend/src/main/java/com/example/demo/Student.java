package com.example.demo;

import java.util.Date;

public class Student {

    // Using object wrappers (Integer) instead of primitive (int) 
    // prevents NullPointerExceptions if a document is missing this field.
    private Integer id;
    private String name;
    private Integer age;
    private String status;
    
    // Firestore Timestamps cleanly map to java.util.Date
    private Date date;

    // 1️⃣ No-args constructor (Mandatory for Firestore deserialization)
    public Student() {
    }

    // 2️⃣ All-args constructor (Optional, but convenient)
    public Student(Integer id, String name, Integer age, String status, Date date) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.status = status;
        this.date = date;
    }

    // 3️⃣ Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age; // If 'age' is missing in Firestore, this returns null instead of crashing
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}