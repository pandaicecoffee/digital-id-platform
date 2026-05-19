package com.qmul.digitalid.service;


import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.repository.DigitalIdRepository;

import java.time.LocalDate;
import java.util.Optional;

public class IdentityConsumptionImpl extends IdentityConsumptionService{
    private final DigitalIdRepository repository;
    private final LogService logService;

    public IdentityConsumptionImpl(DigitalIdRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    @Override
    public VerificationResult verifyIsActive(String digitalIdRef, String requestedBy) {
        return null;
    }

    @Override
    public VerificationResult verifyActiveForPeriod(String digitalIdRef, LocalDate from, LocalDate to, String requestedBy) {
        return null;
    }

    @Override
    public Optional<DigitalID> lookup(String digitalIdRef, String requestedBy) {
        return null;
    }
}
