package com.example.nhom4_quanlycongviec;
public class Task {
    public int id;
    public String name, description, deadline, priority;
    public boolean completed;
    public Task(int id, String name, String description, String deadline, String priority, boolean completed) {
        this.id=id; this.name=name; this.description=description;
        this.deadline=deadline; this.priority=priority; this.completed=completed;
    }
}
