package com.techfix.app.model;

public class Notification {
    public long id;
    public long userId;
    public long appointmentId;
    public String title;
    public String message;
    public String createdAt;
    public int isRead;

    public Notification() {}
}
