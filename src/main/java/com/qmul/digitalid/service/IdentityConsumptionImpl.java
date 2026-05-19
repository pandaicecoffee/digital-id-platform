package com.qmul.digitalid.service;


import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.DigitalIDStatus;
import com.qmul.digitalid.model.LogEventType;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.repository.DigitalIdRepository;

import java.time.LocalDate;
import java.util.Optional;

public class IdentityConsumptionImpl implements IdentityConsumptionService {
    private final DigitalIdRepository repository;
    private final LogService logService;

    public IdentityConsumptionImpl(DigitalIdRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    @Override
    public VerificationResult verifyIsActive(String digitalIdRef, String requestedBy) {
        Optional<DigitalID> found = repository.findById(digitalIdRef);

        if (found.isEmpty()) {
            logService.record(LogEventType.VERIFICATION_FAILED, digitalIdRef,
                    requestedBy, "ID not found");
            return new VerificationResult(false, "Digital ID does not exist");
        }

        DigitalID digitalID = found.get();

        if (digitalID.getStatus() == DigitalIDStatus.REVOKED) {
            logService.record(LogEventType.VERIFICATION_FAILED, digitalIdRef,
                    requestedBy, "ID is revoked");
            return new VerificationResult(false, "Digital ID has been permanently revoked");
        }

        if (digitalID.getStatus() == DigitalIDStatus.SUSPENDED) {
            logService.record(LogEventType.VERIFICATION_FAILED, digitalIdRef,
                    requestedBy, "ID is suspended");
            return new VerificationResult(false, "Digital ID is currently suspended");
        }

        logService.record(LogEventType.VERIFICATION_SUCCESS, digitalIdRef,
                requestedBy, "Active verification passed");
        return new VerificationResult(true, "Digital ID is active and valid");    }

    @Override
    public VerificationResult verifyActiveForPeriod(String digitalIdRef, LocalDate from, LocalDate to, String requestedBy) {
        return null;
    }

    @Override
    public Optional<DigitalID> lookup(String digitalIdRef, String requestedBy) {
        Optional<DigitalID> result = repository.findById(digitalIdRef);
        if (result.isPresent()) {
            logService.record(LogEventType.VERIFICATION_SUCCESS, digitalIdRef,
                    requestedBy, "Lookup performed");
        }
        return result;
    }
}
