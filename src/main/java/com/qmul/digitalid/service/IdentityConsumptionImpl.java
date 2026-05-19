package com.qmul.digitalid.service;


import com.qmul.digitalid.repository.DigitalIdRepository;

public class IdentityConsumptionImpl {
    private final DigitalIdRepository repository;
    private final LogService logService;

    public IdentityConsumptionImpl(DigitalIdRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }
}
