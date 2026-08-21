package com.techfix.app.model;

public class Appointment {
    public long id;
    public long customerId;
    public long branchId;
    public long technicianId;
    public long serviceId;
    public String deviceNote;
    public String status;
    public String createdAt;
    public String customerName;
    public String branchName;
    public String technicianName;
    public String serviceName;
    public double servicePrice;

    public Appointment() {}
}
