package com.qmul.digitalid.service;

import com.qmul.digitalid.exception.DigitalIdNotFoundException;
import com.qmul.digitalid.exception.DuplicateIdentityException;
import com.qmul.digitalid.exception.InvalidOperationException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.DigitalIDOperations;
import com.qmul.digitalid.model.LogEventType;
import com.qmul.digitalid.repository.DigitalIdRepository;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

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

        repository.save(digitalID);

        logService.record(
                LogEventType.IDENTITY_CREATED,
                id,
                requestedBy,
                "Created identity for " + firstName + " " + lastName
        );

        return digitalID;
    }

    private void applyAttributeUpdate(String id, String requestedBy,
                                      String fieldName, String newValue,
                                      Consumer<DigitalID> mutation) {
        DigitalID digitalID = getOrThrow(id);
        guardAgainstNonUpdatable(digitalID, requestedBy, "update " + fieldName);

        DigitalIDOperations.applyUpdate(digitalID, mutation);

        logService.record(
                LogEventType.IDENTITY_UPDATED,
                id,
                requestedBy,
                fieldName + " updated to: " + newValue
        );

        repository.save(digitalID);
    }

    @Override
    public void updateFirstName(String id, String newFirstName, String requestedBy) {
        applyAttributeUpdate(id, requestedBy, "First name", newFirstName,
                dig -> DigitalIDOperations.updateFirstName(dig, newFirstName));
    }

    @Override
    public void updateLastName(String id, String newLastName, String requestedBy) {
        applyAttributeUpdate(id, requestedBy, "Last name", newLastName,
                dig -> DigitalIDOperations.updateLastName(dig, newLastName));
    }

    @Override
    public void updateAddress(String id, String newAddress, String requestedBy) {
        applyAttributeUpdate(id, requestedBy, "Address", newAddress,
                dig -> DigitalIDOperations.updateAddress(dig, newAddress));
    }

    @Override
    public void updateNationality(String id, String newNationality, String requestedBy) {
        applyAttributeUpdate(id, requestedBy, "Nationality", newNationality,
                dig -> DigitalIDOperations.updateNationality(dig, newNationality));
    }

    @Override
    public void suspendIdentity(String id, String requestedBy) {
        DigitalID digitalID = getOrThrow(id);

        if (!digitalID.getStatus().canBeSuspended()) {
            throw new InvalidOperationException(
                    "Cannot suspend Digital ID " + id
            );
        }

        DigitalIDOperations.suspend(digitalID);

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
                    "Cannot reactivate Digital ID " + id + " (current status: " + digitalID.getStatus() + ")"
            );
        }

        DigitalIDOperations.reactivate(digitalID);

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
                    "Cannot revoke Digital ID " + id + " (current status: " + digitalID.getStatus() + ")"
            );
        }

        DigitalIDOperations.revoke(digitalID);

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

    private void guardAgainstNonUpdatable(DigitalID digitalID,
                                          String requestedBy,
                                          String operation) {

        if (!digitalID.getStatus().canBeUpdated()) {

            logService.record(
                    LogEventType.OPERATION_REJECTED,
                    digitalID.getId(),
                    requestedBy,
                    "Rejected " + operation + " on revoked identity"
            );

            throw new InvalidOperationException(
                    "Cannot " + operation
                            + " on a revoked Digital ID: "
                            + digitalID.getId() + " (status: " + digitalID.getStatus() + ")"
            );
        }
    }
}

