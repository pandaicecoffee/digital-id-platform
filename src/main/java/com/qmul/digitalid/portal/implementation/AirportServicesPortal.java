package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.DigitalIDStatus;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;
import java.util.Optional;

public class AirportServicesPortal implements VerificationPortal {

    private static final String ORG_NAME = "Airport Services";
    private final IdentityConsumptionService consumptionService;

    public AirportServicesPortal(IdentityConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Verification"; }

    @Override
    public VerificationResult verify(String identificationID) {
        // Airport needs: exists, not suspended, not revoked, active
        Optional<DigitalID> found = consumptionService.lookup(identificationID, ORG_NAME);

        if (found.isEmpty()) {
            return new VerificationResult(false, "No registered Digital ID found for this traveller");
        }

        DigitalID digitalID = found.get();

        if (digitalID.getStatus() == DigitalIDStatus.REVOKED) {
            return new VerificationResult(false,
                    "Digital ID has been permanently revoked — check-in denied");
        }

        if (digitalID.getStatus() == DigitalIDStatus.SUSPENDED) {
            return new VerificationResult(false,
                    "Digital ID is currently suspended — check-in denied");
        }

        return new VerificationResult(true,
                "Digital ID verified — traveller cleared for check-in");
    }
}