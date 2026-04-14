package com.example.demo;

import java.util.Date;

public class Student {

    private String id;
    private String name;
    private String status;
    private Date date;

    // 1️⃣ No-args constructor (Mandatory for Firestore deserialization)
    public Student() {
    }

    // 2️⃣ All-args constructor (Optional, but convenient)
    public Student(String id, String name, String status, Date date) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.date = date;
    }

    // 3️⃣ Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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