package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;

import java.util.Optional;

public class EmployerPortal implements VerificationPortal {

    private static final String ORG_NAME = "Employer";
    private final IdentityConsumptionService consumptionService;

    public EmployerPortal(IdentityConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Verification"; }

    @Override
    public VerificationResult verify(String identificationID) {
        VerificationResult activeCheck = consumptionService.verifyIsActive(identificationID, ORG_NAME);
        if (!activeCheck.valid()) {
            return new VerificationResult(false, "Identity could not be verified");
        }

        Optional<DigitalID> found = consumptionService.lookup(identificationID, ORG_NAME);
        if (found.isEmpty()) {
            return new VerificationResult(false, "Identity could not be verified");
        }

        DigitalID identity = found.get();
        return new VerificationResult(true,
                "Identity verified for: " + identity.getFirstName() + " " + identity.getLastName());
    }


}