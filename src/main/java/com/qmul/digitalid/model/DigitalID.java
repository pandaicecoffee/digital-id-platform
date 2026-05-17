package com.qmul.digitalid.model;

import java.time.LocalDate;

public class DigitalID {
    private final String id;
    private final String nationalIdNumber;
    private final LocalDate dateOfBirth;

    private String firstName;
    private String lastName;
    private String address;
    private String nationality;
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
    private LocalDate getDateOfBirth() {return dateOfBirth;}
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

    //package private methods for the service layer
    void suspend() {
        this.status = DigitalIDStatus.SUSPENDED;
    }

    void reactivate() {
        this.status = DigitalIDStatus.ACTIVE;
    }

    void revoke() {
        this.status = DigitalIDStatus.REVOKED;
    }

    @Override
    public String toString() {
        return "DigitalID{id='" + id + "', name='" + firstName + " " + lastName +
                "', status=" + status + ", nationalId='" + nationalIdNumber + "'}";
    }


}
