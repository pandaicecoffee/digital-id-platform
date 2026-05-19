package com.qmul.digitalid.portal;

import com.qmul.digitalid.exception.DuplicateIdentityException;
import com.qmul.digitalid.exception.InvalidOperationException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.DigitalIDStatus;
import com.qmul.digitalid.portal.implementation.CentralAuthorityPortal;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class CentralAuthorityPortalTest {

    private CentralAuthorityPortal portal;

    @BeforeEach
    void setUp() {
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        LogService logService = new InMemoryLogService();
        IdentityManagementService managementService =
                new IdentityManagementServiceImpl(repository, logService);
        portal = new CentralAuthorityPortal(managementService);
    }

    private DigitalID createAleena() {
            return portal.createIdentity("NIN-001", "Aleena", "Joseph",
                LocalDate.of(1990, 3, 15), "London", "British");
    }

    @Test
    void createsIdentityViaPortal() {
        DigitalID id = createAleena();
        assertNotNull(id);
        assertEquals(DigitalIDStatus.ACTIVE, id.getStatus());
    }

    @Test
    void createdIdentityHasCorrectName() {
        DigitalID id = createAleena();
        assertEquals("Aleena", id.getFirstName());
        assertEquals("Joseph", id.getLastName());
    }

    @Test
    void rejectsDuplicateNationalIdViaPortal() {
        createAleena();
        assertThrows(DuplicateIdentityException.class, this::createAleena);
    }

    @Test
    void updatesFirstNameViaPortal() {
        DigitalID id = createAleena();
        portal.updateFirstName(id.getId(), "Zara");
        assertEquals("Zara", portal.lookupIdentity(id.getId()).getFirstName());
    }

    @Test
    void updatesLastNameViaPortal() {
        DigitalID id = createAleena();
        portal.updateLastName(id.getId(), "Williams");
        assertEquals("Williams", portal.lookupIdentity(id.getId()).getLastName());
    }

    @Test
    void updatesAddressViaPortal() {
        DigitalID id = createAleena();
        portal.updateAddress(id.getId(), "456 New Road, Manchester");
        assertEquals("456 New Road, Manchester", portal.lookupIdentity(id.getId()).getAddress());
    }

    @Test
    void updatesNationalityViaPortal() {
        DigitalID id = createAleena();
        portal.updateNationality(id.getId(), "Irish");
        assertEquals("Irish", portal.lookupIdentity(id.getId()).getNationality());
    }

    @Test
    void suspendsIdentityViaPortal() {
        DigitalID id = createAleena();
        portal.suspendIdentity(id.getId());
        assertEquals(DigitalIDStatus.SUSPENDED, portal.lookupIdentity(id.getId()).getStatus());
    }

    @Test
    void reactivatesIdentityViaPortal() {
        DigitalID id = createAleena();
        portal.suspendIdentity(id.getId());
        portal.reactivateIdentity(id.getId());
        assertEquals(DigitalIDStatus.ACTIVE, portal.lookupIdentity(id.getId()).getStatus());
    }

    @Test
    void revokesIdentityViaPortal() {
        DigitalID id = createAleena();
        portal.revokeIdentity(id.getId());
        assertEquals(DigitalIDStatus.REVOKED, portal.lookupIdentity(id.getId()).getStatus());
    }

    @Test
    void rejectsUpdateOnRevokedIdentity() {
        DigitalID id = createAleena();
        portal.revokeIdentity(id.getId());
        assertThrows(InvalidOperationException.class,
                () -> portal.updateAddress(id.getId(), "Should fail"));
    }

    @Test
    void rejectsSuspendOnRevokedIdentity() {
        DigitalID id = createAleena();
        portal.revokeIdentity(id.getId());
        assertThrows(InvalidOperationException.class,
                () -> portal.suspendIdentity(id.getId()));
    }

    @Test
    void rejectsNationalityUpdateOnRevokedIdentity() {
        DigitalID id = createAleena();
        portal.revokeIdentity(id.getId());
        assertThrows(InvalidOperationException.class,
                () -> portal.updateNationality(id.getId(), "Irish"));
    }

    @Test
    void portalReportsCorrectOrganisationName() {
        assertEquals("Central Authority", portal.getOrganisationName());
    }

    @Test
    void portalReportsManagementType() {
        assertEquals("Management", portal.getPortalType());
    }
}
