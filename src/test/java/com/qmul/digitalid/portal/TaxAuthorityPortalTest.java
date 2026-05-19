package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.portal.implementation.TaxAuthorityPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaxAuthorityPortalTest {

    private TaxAuthorityPortal portal;
    private IdentityManagementService managementService;

    private static final LocalDate PERIOD_START = LocalDate.of(2024, 1, 1);
    private static final LocalDate PERIOD_END   = LocalDate.now();

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);
        portal = new TaxAuthorityPortal(consumptionService, PERIOD_START, PERIOD_END);
    }

    private DigitalID createIdentity() {
        return managementService.createIdentity(
                "NIN-TAX-001", "Alice", "Smith",
                LocalDate.of(1990, 6, 15), "London", "British", "Test");
    }

    @Test
    void activeIdentityPassesPeriodVerification() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void identitySuspendedDuringPeriodFailsVerification() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        managementService.reactivateIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void currentlySuspendedIdentityFailsVerification() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void revokedIdentityFailsVerification() {
        DigitalID id = createIdentity();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void nonExistentIdentityFailsVerification() {
        VerificationResult result = portal.verify("does-not-exist");
        assertFalse(result.valid());
    }

    @Test
    void failedVerificationIncludesReason() {
        VerificationResult result = portal.verify("does-not-exist");
        assertNotNull(result.reason());
        assertFalse(result.reason().isBlank());
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Tax Authority", portal.getOrganisationName());
    }
}