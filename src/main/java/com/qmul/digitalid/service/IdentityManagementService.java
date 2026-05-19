package com.qmul.digitalid.service;

import com.qmul.digitalid.model.DigitalID;
import java.time.LocalDate;

public interface IdentityManagementService {
    DigitalID createIdentity(String nationalIdNumber, String firstName, String lastName,
                             LocalDate dateOfBirth, String address, String nationality,
                             String requestedBy);

    void updateFirstName(String id, String newFirstName, String requestedBy);
    void updateLastName(String id, String newLastName, String requestedBy);
    void updateAddress(String id, String newAddress, String requestedBy);
    void updateNationality(String id, String newNationality, String requestedBy);


    void suspendIdentity(String id, String requestedBy);
    void reactivateIdentity(String id, String requestedBy);
    void revokeIdentity(String id, String requestedBy);

    DigitalID findById(String id);
}
