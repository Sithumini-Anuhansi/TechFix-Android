package com.techfix.app.model;

public class User {
    public long id;
    public String name;
    public String email;
    public String password;
    public String phone;
    public String role;
    public long branchId;
    public String branchName;

    public User() {}

    public User(long id, String name, String email, String password, String phone, String role, long branchId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.branchId = branchId;
    }

    @Override
    public String toString() {
        return name;
    }
}
