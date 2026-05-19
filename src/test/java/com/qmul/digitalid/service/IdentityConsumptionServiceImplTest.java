package com.qmul.digitalid.service;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class IdentityConsumptionServiceImplTest {

    private IdentityManagementService managementService;
    private IdentityConsumptionService consumptionService;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        consumptionService = new IdentityConsumptionServiceImpl(repository, logService);
    }

    private DigitalID createAlice() {
        return managementService.createIdentity("NIN-001", "Alice", "Smith",
                LocalDate.of(1990, 1, 1), "London", "British", "Test");
    }

    @Test
    void activeIdentityVerifiesSuccessfully() {
        DigitalID id = createAlice();
        VerificationResult result = consumptionService.verifyIsActive(id.getId(), "Test");
        assertTrue(result.valid());
    }

    @Test
    void suspendedIdentityFailsVerification() {
        DigitalID id = createAlice();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = consumptionService.verifyIsActive(id.getId(), "Test");
        assertFalse(result.valid());
    }

    @Test
    void revokedIdentityFailsVerification() {
        DigitalID id = createAlice();
        managementService.revokeIdentity(id.getId(), "Test");
        VerificationResult result = consumptionService.verifyIsActive(id.getId(), "Test");
        assertFalse(result.valid());
    }

    @Test
    void nonExistentIdentityFailsVerification() {
        VerificationResult result = consumptionService.verifyIsActive("bad-id", "Test");
        assertFalse(result.valid());
    }

    @Test
    void verificationFailureHasReason() {
        VerificationResult result = consumptionService.verifyIsActive("bad-id", "Test");
        assertNotNull(result.reason());
        assertFalse(result.reason().isBlank());
    }

    @Test
    void identitySuspendedDuringPeriodFailsPeriodVerification() {
        DigitalID id = createAlice();
        managementService.suspendIdentity(id.getId(), "Test");
        VerificationResult result = consumptionService.verifyActiveForPeriod(
                id.getId(),
                LocalDate.of(2024, 1, 1),
                LocalDate.now(),
                "Tax Authority"
        );
        assertFalse(result.valid());
    }

    @Test
    void lookupReturnsEmptyForUnknownId() {
        assertTrue(consumptionService.lookup("unknown-id", "Test").isEmpty());
    }

    @Test
    void lookupReturnsIdentityWhenExists() {
        DigitalID id = createAlice();
        assertTrue(consumptionService.lookup(id.getId(), "Test").isPresent());
    }
}
