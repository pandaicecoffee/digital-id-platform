package com.qmul.digitalid.model;

public class DigitalID {
    private final String id;
    private final String nationalIdNumber; // immutable
    private String firstName;
    private String lastName;// mutable by central authority
    private String address;
    private String nationality; // mutable
    private DigitalIDStatus status;

    public DigitalID(String id, String nationalIdNumber, String firstName, String lastName, String address, String nationality, DigitalIDStatus status) {
        this.id = id;
        this.nationalIdNumber = nationalIdNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.nationality = nationality;
        this.status = status;
    }

    public String getId() {
        return id;
    }
    public String getNationalIdNumber() {
        return nationalIdNumber;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getAddress() {
        return address;
    }
    public String getNationality() {
        return nationality;
    }
    public DigitalIDStatus getStatus() {
        return status;
    }
    public void setStatus(DigitalIDStatus status) {
        this.status = status;
    }



}
