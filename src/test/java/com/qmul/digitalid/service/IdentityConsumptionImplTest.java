package com.qmul.digitalid.service;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class IdentityConsumptionImplTest {

    private IdentityManagementService managementService;
    private IdentityConsumptionService consumptionService;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        managementService = new IdentityManagementServiceImpl(repository, logService);
        consumptionService = new IdentityConsumptionImpl(repository, logService);
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
}