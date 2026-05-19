package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.portal.implementation.DrivingLicenceAuthorityPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DrivingLicenceAuthorityTest {

    private DrivingLicenceAuthorityPortal portal;
    private IdentityManagementService managementService;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);
        portal = new DrivingLicenceAuthorityPortal(consumptionService);
    }

    private DigitalID createAdult() {
        return managementService.createIdentity(
                "NIN-DVL-001", "Bob", "Jones",
                LocalDate.of(1985, 3, 20), "Manchester", "British", "Test");
    }

    private DigitalID createMinor() {
        return managementService.createIdentity(
                "NIN-DVL-002", "James", "Taylor",
                LocalDate.now().minusYears(15), "London", "British", "Test");
    }

    @Test
    void adultActiveIdentityPassesVerification() {
        DigitalID id = createAdult();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void adultActiveIdentityReceivesEligibilityMessage() {
        DigitalID id = createAdult();
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.reason().contains("eligible"));
    }

    @Test
    void minorIsRejectedDueToAge() {
        DigitalID id = createMinor();
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
        assertTrue(result.reason().contains("minimum driving age"));
    }

    @Test
    void suspendedIdentityFailsVerification() {
        DigitalID id = createAdult();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertFalse(result.valid());
    }

    @Test
    void revokedIdentityFailsVerification() {
        DigitalID id = createAdult();
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
    void reactivatedAdultPassesVerificationAgain() {
        DigitalID id = createAdult();
        managementService.suspendIdentity(id.getId(), "Test");
        managementService.reactivateIdentity(id.getId(), "Test");
        VerificationResult result = portal.verify(id.getId());
        assertTrue(result.valid());
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Driving Licence Authority", portal.getOrganisationName());
    }
}