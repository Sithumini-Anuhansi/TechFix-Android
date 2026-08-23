package com.techfix.app.model;

public class Payment {
    public long id;
    public long appointmentId;
    public double amount;
    public String method;
    public String status;
    public String paidAt;
    public String customerName;
    public String serviceName;
    public String branchName;

    public Payment() {}
}
