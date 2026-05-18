package com.qmul.digitalid.service;

import com.qmul.digitalid.exception.DigitalIdNotFoundException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.repository.DigitalIdRepository;

public class IdentityManagementServiceImpl implements IdentityManagementService {

    private final DigitalIdRepository repository;
    private final LogService logService;

    public IdentityManagementServiceImpl(DigitalIdRepository repository,
                                         LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    @Override
    public DigitalID findById(String id) {
        return getOrThrow(id);
    }

    private DigitalID getOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new DigitalIdNotFoundException(id));
    }
}

