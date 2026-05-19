package com.qmul.digitalid.service;


import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.model.LogEventType;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.repository.DigitalIdRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class IdentityConsumptionServiceImpl implements IdentityConsumptionService {
    private final DigitalIdRepository repository;
    private final LogService logService;

    public IdentityConsumptionServiceImpl(DigitalIdRepository repository, LogService logService) {
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

        if (!digitalID.getStatus().isUsableByConsumers()) {
            logService.record(LogEventType.VERIFICATION_FAILED, digitalIdRef,
                    requestedBy, "ID is " + digitalID.getStatus());
            return new VerificationResult(false, "Digital ID is not currently active (status: " + digitalID.getStatus() + ")");
        }

        logService.record(LogEventType.VERIFICATION_SUCCESS, digitalIdRef,
                requestedBy, "Active verification passed");
        return new VerificationResult(true, "Digital ID is active and valid");    }

    @Override
    public VerificationResult verifyActiveForPeriod(String digitalIdRef, LocalDate from, LocalDate to, String requestedBy) {
        VerificationResult activeCheck = verifyIsActive(digitalIdRef, requestedBy);
        if (!activeCheck.valid()) {
            return activeCheck;
        }

        List<LogEvent> history = logService.getByDigitalId(digitalIdRef);
        boolean wasSuspendedInPeriod = history.stream()
                .filter(e -> e.getType() == LogEventType.STATUS_SUSPENDED)
                .anyMatch(e -> !e.getTimestamp().toLocalDate().isBefore(from)
                        && !e.getTimestamp().toLocalDate().isAfter(to));

        if (wasSuspendedInPeriod) {
            logService.record(LogEventType.VERIFICATION_FAILED, digitalIdRef,
                    requestedBy, "ID was suspended during period " + from + " to " + to);
            return new VerificationResult(false,
                    "Digital ID was suspended during the reporting period");
        }

        logService.record(LogEventType.VERIFICATION_SUCCESS, digitalIdRef,
                requestedBy, "Period verification passed: " + from + " to " + to);
        return new VerificationResult(true,
                "Digital ID was active throughout the reporting period");    }

    @Override
    public Optional<DigitalID> lookup(String digitalIdRef, String requestedBy) {
        Optional<DigitalID> result = repository.findById(digitalIdRef);
        if (result.isPresent()) {
            logService.record(LogEventType.VERIFICATION_SUCCESS, digitalIdRef,
                    requestedBy, "Lookup performed");
        } else {
            logService.record(LogEventType.VERIFICATION_FAILED, digitalIdRef,
                    requestedBy, "Lookup failed — ID not found");
        }

        return result;
    }
}
