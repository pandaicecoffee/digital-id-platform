package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class DrivingLicenceAuthorityPortal implements VerificationPortal {

    private static final String ORG_NAME = "Driving Licence Authority";
    private static final int MINIMUM_DRIVING_AGE = 17;
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
        VerificationResult activeCheck = consumptionService.verifyIsActive(identificationID, ORG_NAME);
        if (!activeCheck.valid()) {
            return activeCheck;
        }

        Optional<DigitalID> found = consumptionService.lookup(identificationID, ORG_NAME);
        if (found.isEmpty()) {
            return new VerificationResult(false, "Identity could not be retrieved for eligibility check");
        }

        DigitalID identity = found.get();
        int age = Period.between(identity.getDateOfBirth(), LocalDate.now()).getYears();

        if (age < MINIMUM_DRIVING_AGE) {
            return new VerificationResult(false,
                    "Applicant does not meet the minimum driving age of " + MINIMUM_DRIVING_AGE
                            + " (current age: " + age + ")");
        }
        
        return new VerificationResult(true,
                "Digital ID is active and eligible for licence issuance");
    }
}
