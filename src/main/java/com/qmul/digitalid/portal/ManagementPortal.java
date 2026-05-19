package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import java.time.LocalDate;

public interface ManagementPortal extends Portal {
    DigitalID createIdentity(String nationalIdNumber, String firstName, String lastName,
                             LocalDate dateOfBirth, String address, String nationality);
    void updateFirstName(String id, String newFirstName);
    void updateLastName(String id, String newLastName);
    void updateAddress(String id, String newAddress);
    void suspendIdentity(String id);
    void reactivateIdentity(String id);
    void revokeIdentity(String id);
    DigitalID lookupIdentity(String id);
}