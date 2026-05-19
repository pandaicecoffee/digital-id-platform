package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.portal.implementation.AirportServicesPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

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
        portal = new AirportServicesPortal(consumptionService, Set.of("British", "Irish"));
    }

    private DigitalID createBritishCitizen() {
        return managementService.createIdentity(
                "NIN-AIR-001", "Eve", "Taylor",
                LocalDate.of(1995, 4, 22), "Bristol", "British", "Test");
    }

    private DigitalID createNonCTACitizen() {
        return managementService.createIdentity(
                "NIN-AIR-002", "Yuki", "Tanaka",
                LocalDate.of(1992, 5, 18), "London", "Japanese", "Test");
    }

    @Test
    void activeBritishCitizenIsCleared() {
        DigitalID id = createBritishCitizen();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void clearedTravellerReceivesCheckInMessage() {
        DigitalID id = createBritishCitizen();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.reason().contains("cleared for check-in"));
    }

    @Test
    void nonCTANationalityIsRejected() {
        DigitalID id = createNonCTACitizen();
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
        assertTrue(result.reason().contains("not on the permitted list"));
    }


    @Test
    void nonExistentIdentityIsRejected() {
        VerificationResult result = portal.verify("does-not-exist");
        assertFalse(result.valid());
        assertTrue(result.reason().contains("denied"));

    }

    @Test
    void suspendedIdentityIsRejected() {
        DigitalID id = createBritishCitizen();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
        assertTrue(result.reason().contains("denied"));

    }

    @Test
    void revokedIdentityIsRejected() {
        DigitalID id = createBritishCitizen();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
        assertTrue(result.reason().contains("denied"));

    }

    @Test
    void reactivatedBritishCitizenIsClearedAfterSuspension() {
        DigitalID id = createBritishCitizen();
        managementService.suspendIdentity(id.getId(), "Test");
        managementService.reactivateIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Airport Services", portal.getOrganisationName());
    }

    @Test
    void emptyPermittedListAllowsAnyNationality() {
        // Create a portal with no nationality restrictions
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        IdentityManagementService mgmt = new IdentityManagementServiceImpl(repository, logService);
        IdentityConsumptionService cons = new IdentityConsumptionServiceImpl(repository, logService);
        AirportServicesPortal openPortal = new AirportServicesPortal(cons, Set.of());

        DigitalID id = mgmt.createIdentity("NIN-OPEN-001", "Test", "User",
                LocalDate.of(1990, 1, 1), "London", "Martian", "Test");

        VerificationResult result = openPortal.verify(id.getId());
        assertTrue(result.valid());
    }
}
