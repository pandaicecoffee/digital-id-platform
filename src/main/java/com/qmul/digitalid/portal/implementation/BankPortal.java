// portal/impl/BankPortal.java
package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;

public class BankPortal implements VerificationPortal {

    private static final String ORG_NAME = "Bank";
    private final IdentityConsumptionService consumptionService;

    public BankPortal(IdentityConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Verification"; }

    @Override
    public VerificationResult verify(String identificationID) {
        VerificationResult result = consumptionService.verifyIsActive(identificationID, ORG_NAME);

        //bank only gets valid or invalid - kept vague for a reason
        if (result.valid()) {
            return new VerificationResult(true, "Identity verified");
        }
        return new VerificationResult(false, "Identity could not be verified");
    }
}