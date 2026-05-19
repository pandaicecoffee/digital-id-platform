package com.qmul.digitalid.portal.implementation;

import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.service.IdentityConsumptionService;
import java.time.LocalDate;

public class TaxAuthorityPortal implements VerificationPortal {

    private static final String ORG_NAME = "Tax Authority";
    private final IdentityConsumptionService consumptionService;
    private final LocalDate reportingPeriodStart;
    private final LocalDate reportingPeriodEnd;

    public TaxAuthorityPortal(IdentityConsumptionService consumptionService,
                              LocalDate reportingPeriodStart,
                              LocalDate reportingPeriodEnd) {
        this.consumptionService = consumptionService;
        this.reportingPeriodStart = reportingPeriodStart;
        this.reportingPeriodEnd = reportingPeriodEnd;
    }

    @Override
    public String getOrganisationName() { return ORG_NAME; }

    @Override
    public String getPortalType() { return "Verification"; }

    @Override
    public VerificationResult verify(String identificationID) {
        return consumptionService.verifyActiveForPeriod(
                identificationID, reportingPeriodStart, reportingPeriodEnd, ORG_NAME);
    }
}