package com.qmul.digitalid.portal.impl;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.ManagementPortal;
import com.qmul.digitalid.service.IdentityManagementService;
import java.time.LocalDate;

public class CentralAuthorityPortal implements ManagementPortal {

    private static final String ORG_NAME = "Central Authority";
    private final IdentityManagementService managementService;

    public CentralAuthorityPortal(IdentityManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Management"; }

    @Override
    public DigitalID createIdentity(String nationalIdNumber, String firstName, String lastName,
                                    LocalDate dateOfBirth, String address, String nationality) {
        return managementService.createIdentity(nationalIdNumber, firstName, lastName,
                dateOfBirth, address, nationality, ORG_NAME);
    }

    @Override
    public void updateFirstName(String id, String newFirstName) {
        managementService.updateFirstName(id, newFirstName, ORG_NAME);
    }

    @Override
    public void updateLastName(String id, String newLastName) {
        managementService.updateLastName(id, newLastName, ORG_NAME);
    }

    @Override
    public void updateAddress(String id, String newAddress) {
        managementService.updateAddress(id, newAddress, ORG_NAME);
    }

    @Override
    public void suspendIdentity(String id) {
        managementService.suspendIdentity(id, ORG_NAME);
    }

    @Override
    public void reactivateIdentity(String id) {
        managementService.reactivateIdentity(id, ORG_NAME);
    }

    @Override
    public void revokeIdentity(String id) {
        managementService.revokeIdentity(id, ORG_NAME);
    }

    @Override
    public DigitalID lookupIdentity(String id) {
        return managementService.findById(id);
    }
}
