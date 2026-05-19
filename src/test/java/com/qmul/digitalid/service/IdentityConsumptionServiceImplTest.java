package com.qmul.digitalid.service;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.LogEventType;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    void historicalSuspensionDetectedViaPeriodCheck() {
        // Create identity, then inject a historical suspension log event
        DigitalID id = createAlice();

        // Inject a suspension event that happened on 2025-06-15
        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED,
                id.getId(),
                "Central Authority",
                "Identity suspended",
                LocalDateTime.of(2025, 6, 15, 10, 0)
        );

        // The identity is currently active (we didn't actually suspend it),
        // but the log shows a suspension during the period
        VerificationResult result = consumptionService.verifyActiveForPeriod(
                id.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Tax Authority"
        );
        assertFalse(result.valid());
        assertTrue(result.reason().contains("suspended during the reporting period"));
    }

    @Test
    void suspensionOutsidePeriodDoesNotAffectVerification() {
        DigitalID id = createAlice();

        // Inject a suspension event outside the check period (2023)
        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED,
                id.getId(),
                "Central Authority",
                "Identity suspended",
                LocalDateTime.of(2023, 3, 10, 9, 0)
        );

        // Check period is 2025 — the 2023 suspension should not affect it
        VerificationResult result = consumptionService.verifyActiveForPeriod(
                id.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Tax Authority"
        );
        assertTrue(result.valid());
    }

    @Test
    void suspensionOnExactStartDateOfPeriodFailsVerification() {
        DigitalID id = createAlice();

        // Suspension on exactly the first day of the period
        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED,
                id.getId(),
                "Central Authority",
                "Identity suspended",
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );

        VerificationResult result = consumptionService.verifyActiveForPeriod(
                id.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Tax Authority"
        );
        assertFalse(result.valid());
    }

    @Test
    void suspensionOnExactEndDateOfPeriodFailsVerification() {
        DigitalID id = createAlice();

        // Suspension on exactly the last day of the period
        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED,
                id.getId(),
                "Central Authority",
                "Identity suspended",
                LocalDateTime.of(2025, 12, 31, 23, 59)
        );

        VerificationResult result = consumptionService.verifyActiveForPeriod(
                id.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Tax Authority"
        );
        assertFalse(result.valid());
    }

    @Test
    void suspensionOneDayBeforePeriodDoesNotFail() {
        DigitalID id = createAlice();

        logService.recordWithTimestamp(
                LogEventType.STATUS_SUSPENDED,
                id.getId(),
                "Central Authority",
                "Identity suspended",
                LocalDateTime.of(2024, 12, 31, 23, 59)
        );

        VerificationResult result = consumptionService.verifyActiveForPeriod(
                id.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Tax Authority"
        );
        assertTrue(result.valid());
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
