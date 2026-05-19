package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.implementation.AirportServicesPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AirportServicesPortalTest {

    private AirportServicesPortal portal;
    private IdentityManagementService managementService;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);
        portal = new AirportServicesPortal(consumptionService);
    }

    private DigitalID createIdentity() {
        return managementService.createIdentity(
                "NIN-AIR-001", "Eve", "Taylor",
                LocalDate.of(1995, 4, 22), "Bristol", "British", "Test");
    }

    @Test
    void activeIdentityIsCleared() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void activeIdentityReceivesClearanceMessage() {
        DigitalID id = createIdentity();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.reason().contains("cleared for check-in"));
    }

    @Test
    void nonExistentIdentityIsRejected() {
        VerificationResult result = portal.verify("does-not-exist");
        assertFalse(result.valid());
    }

    @Test
    void nonExistentIdentityReceivesNotFoundMessage() {
        VerificationResult result = portal.verify("does-not-exist");
        assertTrue(result.reason().contains("No registered Digital ID"));
    }

    @Test
    void suspendedIdentityIsRejected() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void suspendedIdentityReceivesSuspendedMessage() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.reason().contains("suspended"));
    }

    @Test
    void revokedIdentityIsRejected() {
        DigitalID id = createIdentity();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void revokedIdentityReceivesRevocationMessage() {
        DigitalID id = createIdentity();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.reason().contains("revoked"));
    }

    @Test
    void reactivatedIdentityIsClearedAfterSuspension() {
        DigitalID id = createIdentity();
        managementService.suspendIdentity(id.getId(), "Test");
        managementService.reactivateIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Airport Services", portal.getOrganisationName());
    }
}
