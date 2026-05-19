package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.portal.implementation.EmployerPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployerPortalTest {

    private EmployerPortal portal;
    private IdentityManagementService managementService;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);
        portal = new EmployerPortal(consumptionService);
    }

    private DigitalID createIdentity() {
        return managementService.createIdentity(
                "NIN-EMP-001", "David", "Brown",
                LocalDate.of(1988, 11, 5), "Leeds", "British", "Test");
    }

    @Test
    void activeIdentityPassesVerification() {
        DigitalID id = createIdentity();
        assertTrue(portal.verify(id.getId()).valid());
    }

    @Test
    void successMessageContainsEmployeeName() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.reason().contains("David"));
        assertTrue(result.reason().contains("Brown"));
    }

    @Test
    void suspendedIdentityFailsVerification() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        assertFalse(portal.verify(id.getId()).valid());
    }

    @Test
    void failureMessageIsGenericAndDoesNotExposeName() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertEquals("Identity could not be verified", result.reason());
        assertFalse(result.reason().contains("David"));
    }

    @Test
    void revokedIdentityFailsVerification() {
        DigitalID id = createIdentity();
        managementService.revokeIdentity(id.getId(), "Test");
        assertFalse(portal.verify(id.getId()).valid());
    }

    @Test
    void nonExistentIdentityFailsVerification() {
        assertFalse(portal.verify("does-not-exist").valid());
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Employer", portal.getOrganisationName());
    }
}
