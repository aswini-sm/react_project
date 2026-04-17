package com.example.demo;

public class Student {

    private String id;
    private String name;
    private long totalClasses;
    private long presentCount;
    private Integer age;

    public Student() {
    }

    public Student(String id, String name, long totalClasses, long presentCount, Integer age) {
        this.id = id;
        this.name = name;
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
        this.age = age;
    }

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

    public long getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(long totalClasses) {
        this.totalClasses = totalClasses;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}