package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;

public class DrivingLicenceAuthorityPortal implements VerificationPortal {

    private static final String ORG_NAME = "Driving Licence Authority";
    private final IdentityConsumptionService consumptionService;

    public DrivingLicenceAuthorityPortal(IdentityConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Verification"; }

    @Override
    public VerificationResult verify(String identificationID) {
        VerificationResult result = consumptionService.verifyIsActive(identificationID, ORG_NAME);
        if (!result.valid()) {
            return result;
        }
        return new VerificationResult(true,
                "Digital ID is active and eligible for licence issuance");
    }
}
