package com.qmul.digitalid.service;

import com.qmul.digitalid.exception.DigitalIdNotFoundException;
import com.qmul.digitalid.exception.DuplicateIdentityException;
import com.qmul.digitalid.exception.InvalidOperationException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.LogEventType;
import com.qmul.digitalid.repository.DigitalIdRepository;
import com.qmul.digitalid.model.DigitalIDStatus;

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
        );

        logService.record(
                LogEventType.IDENTITY_CREATED,
                id,
                requestedBy,
                "Created identity for " + firstName + " " + lastName
        );

        repository.save(digitalID);

        return digitalID;
    }

    @Override
    public void updateFirstName(String id, String newFirstName, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        guardAgainstRevoked(digitalID, requestedBy, "update first name");

        digitalID.updateFirstName(newFirstName);

        logService.record(
                LogEventType.IDENTITY_UPDATED,
                id,
                requestedBy,
                "First name updated to: " + newFirstName
        );

        repository.save(digitalID);
    }

    @Override
    public void updateLastName(String id, String newLastName, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        guardAgainstRevoked(digitalID, requestedBy, "update last name");

        digitalID.updateLastName(newLastName);

        logService.record(
                LogEventType.IDENTITY_UPDATED,
                id,
                requestedBy,
                "Last name updated to: " + newLastName
        );

        repository.save(digitalID);
    }

    @Override
    public void updateAddress(String id, String newAddress, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);
        guardAgainstRevoked(digitalID, requestedBy, "update address");
        digitalID.updateAddress(newAddress);

        logService.record(
                LogEventType.IDENTITY_UPDATED,
                id,
                requestedBy,
                "Address updated to: " + newAddress
        );

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

        logService.record(
                LogEventType.STATUS_SUSPENDED,
                id,
                requestedBy,
                "Identity suspended"
        );

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

        logService.record(
                LogEventType.STATUS_REACTIVATED,
                id,
                requestedBy,
                "Identity reactivated"
        );

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

        logService.record(
                LogEventType.STATUS_REVOKED,
                id,
                requestedBy,
                "Identity revoked"
        );

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

    private void guardAgainstRevoked(DigitalID digitalID,
                                     String requestedBy,
                                     String operation) {

        if (digitalID.getStatus() == DigitalIDStatus.REVOKED) {

            logService.record(
                    LogEventType.OPERATION_REJECTED,
                    digitalID.getId(),
                    requestedBy,
                    "Rejected " + operation + " on revoked identity"
            );

            throw new InvalidOperationException(
                    "Cannot " + operation
                            + " on a revoked Digital ID: "
                            + digitalID.getId()
            );
        }
    }
}

