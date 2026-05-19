package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.implementation.BankPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BankPortalTest {

    private BankPortal portal;
    private IdentityManagementService managementService;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);
        portal = new BankPortal(consumptionService);
    }

    private DigitalID createIdentity() {
        return managementService.createIdentity(
                "NIN-BNK-001", "Carol", "White",
                LocalDate.of(1992, 8, 10), "Birmingham", "British", "Test");
    }

    @Test
    void activeIdentityPassesVerification() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void activeIdentityReturnsRedactedSuccessMessage() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertEquals("Identity verified", result.reason());
    }

    @Test
    void suspendedIdentityFailsVerification() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void suspendedIdentityReturnsRedactedFailureMessage() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertEquals("Identity could not be verified", result.reason());
    }

    @Test
    void revokedIdentityFailsVerification() {
        DigitalID id = createIdentity();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void revokedIdentityReturnsRedactedFailureMessage() {
        DigitalID id = createIdentity();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertEquals("Identity could not be verified", result.reason());
    }

    @Test
    void nonExistentIdentityFailsVerification() {
        VerificationResult result = portal.verify("does-not-exist");
        assertFalse(result.valid());
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Bank Portal", portal.getOrganisationName());
    }
}