package com.qmul.digitalid.model;

import java.time.LocalDate;
import java.util.Objects;

public class DigitalID {
    private final String id;
    private final String nationalIdNumber;
    private final LocalDate dateOfBirth;

    private String firstName;
    private String lastName;
    private String address;
    private String nationality;
    private DigitalIDStatus status;

    public DigitalID(String id, String nationalIdNumber, String firstName, String lastName, LocalDate dateOfBirth, String address, String nationality) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.nationalIdNumber = Objects.requireNonNull(nationalIdNumber, "National ID number must not be null");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "Date of birth must not be null");
        this.firstName = Objects.requireNonNull(firstName, "First name must not be null");
        this.lastName = Objects.requireNonNull(lastName, "Last name must not be null");
        this.address = Objects.requireNonNull(address, "Address must not be null");
        this.nationality = Objects.requireNonNull(nationality, "Nationality must not be null");
        this.status = DigitalIDStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }
    public String getNationalIdNumber() {
        return nationalIdNumber;
    }
    public LocalDate getDateOfBirth() {return dateOfBirth;}
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

    //package private methods for the service layer
    public void updateFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName, "First name must not be null");
    }

    public void updateLastName(String lastName) {
        this.lastName = Objects.requireNonNull(lastName, "Last name must not be null");
    }

    public void updateAddress(String address) {
        this.address = Objects.requireNonNull(address, "Address must not be null");
    }

    public void updateNationality(String nationality) {
        this.nationality = Objects.requireNonNull(nationality, "Nationality must not be null");
    }



    public void suspend() {
        this.status = DigitalIDStatus.SUSPENDED;
    }

    public void reactivate() {
        this.status = DigitalIDStatus.ACTIVE;
    }

    public void revoke() {
        this.status = DigitalIDStatus.REVOKED;
    }

    @Override
    public String toString() {
        return "DigitalID{id='" + id + "', name='" + firstName + " " + lastName +
                "', status=" + status + ", nationalId='" + nationalIdNumber + "'}";
    }


}
