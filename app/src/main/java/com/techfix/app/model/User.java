package com.techfix.app.model;

public class User {
    public long id;
    public String name;
    public String email;
    public String password;
    public String phone;
    public String role;

    public User() {}

    public User(long id, String name, String email, String password, String phone, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }
}
