package com.qmul.digitalid.service;

import com.qmul.digitalid.exception.*;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.DigitalIDStatus;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class IdentityManagementServiceTest {

    private IdentityManagementService service;
    private InMemoryDigitalIdRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        service = new IdentityManagementServiceImpl(repository, logService);
    }

    private DigitalID createAleena() {
        return service.createIdentity("NIN-001", "Aleena", "Joseph",
                LocalDate.of(1990, 1, 1), "London", "British", "Test");
    }

    @Test
    void createsIdentityWithActiveStatus() {
        DigitalID id = createAleena();
        assertEquals(DigitalIDStatus.ACTIVE, id.getStatus());
    }

    @Test
    void newIdentityHasCorrectAttributes() {
        DigitalID id = createAleena();
        assertEquals("Aleena", id.getFirstName());
        assertEquals("Joseph", id.getLastName());
        assertEquals("NIN-001", id.getNationalIdNumber());
    }

    @Test
    void rejectsDuplicateNationalIdNumber() {
        createAleena();
        assertThrows(DuplicateIdentityException.class, this::createAleena);
    }

    @Test
    void canSuspendActiveIdentity() {
        DigitalID id = createAleena();
        service.suspendIdentity(id.getId(), "Test");
        assertEquals(DigitalIDStatus.SUSPENDED, repository.findById(id.getId()).get().getStatus());
    }

    @Test
    void cannotSuspendAlreadySuspendedIdentity() {
        DigitalID id = createAleena();
        service.suspendIdentity(id.getId(), "Test");
        assertThrows(InvalidOperationException.class,
                () -> service.suspendIdentity(id.getId(), "Test"));
    }

    @Test
    void canReactivateSuspendedIdentity() {
        DigitalID id = createAleena();
        service.suspendIdentity(id.getId(), "Test");
        service.reactivateIdentity(id.getId(), "Test");
        assertEquals(DigitalIDStatus.ACTIVE, repository.findById(id.getId()).get().getStatus());
    }

    @Test
    void cannotReactivateActiveIdentity() {
        DigitalID id = createAleena();
        assertThrows(InvalidOperationException.class,
                () -> service.reactivateIdentity(id.getId(), "Test"));
    }

    @Test
    void canRevokeActiveIdentity() {
        DigitalID id = createAleena();
        service.revokeIdentity(id.getId(), "Test");
        assertEquals(DigitalIDStatus.REVOKED, repository.findById(id.getId()).get().getStatus());
    }

    @Test
    void canRevokeSuspendedIdentity() {
        DigitalID id = createAleena();
        service.suspendIdentity(id.getId(), "Test");
        service.revokeIdentity(id.getId(), "Test");
        assertEquals(DigitalIDStatus.REVOKED, repository.findById(id.getId()).get().getStatus());
    }

    @Test
    void cannotRevokeAlreadyRevokedIdentity() {
        DigitalID id = createAleena();
        service.revokeIdentity(id.getId(), "Test");
        assertThrows(InvalidOperationException.class,
                () -> service.revokeIdentity(id.getId(), "Test"));
    }

    @Test
    void cannotUpdateAddressOnRevokedIdentity() {
        DigitalID id = createAleena();
        service.revokeIdentity(id.getId(), "Test");
        assertThrows(InvalidOperationException.class,
                () -> service.updateAddress(id.getId(), "New address", "Test"));
    }

    @Test
    void updatesAddressOnActiveIdentity() {
        DigitalID id = createAleena();
        service.updateAddress(id.getId(), "New Road, Manchester", "Test");
        assertEquals("New Road, Manchester",
                repository.findById(id.getId()).get().getAddress());
    }

    @Test
    void throwsWhenIdNotFound() {
        assertThrows(DigitalIdNotFoundException.class,
                () -> service.findById("does-not-exist"));
    }
}