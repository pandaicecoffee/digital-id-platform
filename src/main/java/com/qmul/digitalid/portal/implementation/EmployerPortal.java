package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;

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
        // Employer: limited check — is it valid right now? No attribute access.
        VerificationResult result = consumptionService.verifyIsActive(identificationID, ORG_NAME);
        // Employer gets a simpler, redacted response
        if (result.valid()) {
            return new VerificationResult(true, "Identity verified");
        }
        return new VerificationResult(false, "Identity could not be verified");
    }
}