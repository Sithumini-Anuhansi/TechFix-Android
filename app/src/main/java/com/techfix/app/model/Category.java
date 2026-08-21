package com.techfix.app.model;

public class Category {
    public long id;
    public String name;
    public String description;

    public Category() {}

    public Category(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
