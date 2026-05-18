package com.qmul.digitalid.service;

import com.qmul.digitalid.exception.DigitalIdNotFoundException;
import com.qmul.digitalid.exception.InvalidOperationException;
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
        DigitalID digitalID = getOrThrow(id);

        digitalID.updateFirstName(newFirstName);

        repository.save(digitalID);
    }

    @Override
    public void updateLastName(String id, String newLastName, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        digitalID.updateLastName(newLastName);

        repository.save(digitalID);
    }

    @Override
    public void updateAddress(String id, String newAddress, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        digitalID.updateAddress(newAddress);

        repository.save(digitalID);
    }

    @Override
    public void suspendIdentity(String id, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        if (!digitalID.getStatus().canBeSuspended()) {
            throw new InvalidOperationException(
                    "Cannot suspend Digital ID " + id
            );
        }

        digitalID.suspend();

        repository.save(digitalID);
    }

    @Override
    public void reactivateIdentity(String id, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        if (!digitalID.getStatus().canBeReactivated()) {
            throw new InvalidOperationException(
                    "Cannot reactivate Digital ID " + id
            );
        }

        digitalID.reactivate();

        repository.save(digitalID);
    }

    @Override
    public void revokeIdentity(String id, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        if (!digitalID.getStatus().canBeRevoked()) {
            throw new InvalidOperationException(
                    "Cannot revoke Digital ID " + id
            );
        }

        digitalID.revoke();

        repository.save(digitalID);
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

