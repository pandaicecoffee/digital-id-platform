package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;
import java.util.Optional;
import java.util.Set;

public class AirportServicesPortal implements VerificationPortal {

    private static final String ORG_NAME = "Airport Services";
    private final IdentityConsumptionService consumptionService;
    private final Set<String> permittedNationalities;

    public AirportServicesPortal(IdentityConsumptionService consumptionService,
                                 Set<String> permittedNationalities) {
        this.consumptionService = consumptionService;
        this.permittedNationalities = Set.copyOf(permittedNationalities);
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Verification"; }

    @Override
    public VerificationResult verify(String identificationID) {
        VerificationResult activeCheck = consumptionService.verifyIsActive(identificationID, ORG_NAME);
        if (!activeCheck.valid()) {
            return new VerificationResult(false,
                    "Check-in denied — " + activeCheck.reason());
        }

        Optional<DigitalID> found = consumptionService.lookup(identificationID, ORG_NAME);

        if (found.isEmpty()) {
            return new VerificationResult(false, "Check-in denied as identity could not be retrieved for this traveller");
        }

        DigitalID identity = found.get();

        if (!permittedNationalities.isEmpty()
                && !permittedNationalities.contains(identity.getNationality())) {
            return new VerificationResult(false,
                    "Check-in denied — nationality '" + identity.getNationality()
                            + "' is not on the permitted list for this route");
        }

        return new VerificationResult(true,
                "Digital ID verified — traveller cleared for check-in");
    }
}