package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.LogEventType;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.portal.implementation.TaxAuthorityPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaxAuthorityPortalTest {

    private TaxAuthorityPortal portal;
    private IdentityManagementService managementService;
    private InMemoryLogService logService;

    private static final LocalDate PERIOD_START = LocalDate.of(2025, 4, 6);
    private static final LocalDate PERIOD_END   = LocalDate.of(2026, 4, 5);

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        logService = new InMemoryLogService();
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
    void activeIdentityWithNoSuspensionsPassesPeriodVerification() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void historicalSuspensionDuringPeriodFailsVerification() {
        DigitalID id = createIdentity();
        // Inject a suspension that happened during the tax year
        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED, id.getId(),
                "Central Authority", "Suspended",
                LocalDateTime.of(2025, 9, 1, 10, 0));
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void suspensionBeforePeriodDoesNotAffectVerification() {
        DigitalID id = createIdentity();
        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED, id.getId(),
                "Central Authority", "Suspended",
                LocalDateTime.of(2024, 1, 15, 10, 0));
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
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
