package com.qmul.digitalid.service;

import com.qmul.digitalid.exception.DigitalIdNotFoundException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.repository.DigitalIdRepository;

import java.time.LocalDate;
import java.util.UUID;

public class IdentityManagementServiceImpl implements IdentityManagementService {

    private final DigitalIdRepository repository;
    private final LogService logService;

    public IdentityManagementServiceImpl(DigitalIdRepository repository,
                                         LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    @Override
    public DigitalID createIdentity(String nationalIdNumber, String firstName, String lastName, LocalDate dateOfBirth, String address, String nationality, String requestedBy) {

        if (repository.existsByNationalIdNumber(nationalIdNumber)) {
            throw new DuplicateIdentityException(nationalIdNumber);
        }

        String id = UUID.randomUUID().toString();

        DigitalID digitalID = new DigitalID(
                id,
                nationalIdNumber,
                firstName,
                lastName,
                dateOfBirth,
                address,
                nationality
        )

        repository.save(digitalID);

        return digitalID;
    }

    @Override
    public void updateFirstName(String id, String newFirstName, String requestedBy) {

    }

    @Override
    public void updateLastName(String id, String newLastName, String requestedBy) {

    }

    @Override
    public void updateAddress(String id, String newAddress, String requestedBy) {

    }

    @Override
    public void suspendIdentity(String id, String requestedBy) {

    }

    @Override
    public void reactivateIdentity(String id, String requestedBy) {

    }

    @Override
    public void revokeIdentity(String id, String requestedBy) {

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

