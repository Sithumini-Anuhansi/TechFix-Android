package com.techfix.app.model;

public class Payment {
    public long id;
    public long appointmentId;
    public double amount;
    public String method;
    public int paid;
    public String paidAt;
    public String customerName;
    public String serviceName;

    public Payment() {}
}
