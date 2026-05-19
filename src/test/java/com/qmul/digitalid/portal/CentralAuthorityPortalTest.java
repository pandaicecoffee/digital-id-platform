package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.DigitalIDStatus;
import com.qmul.digitalid.portal.impl.CentralAuthorityPortal;
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
        return portal.createIdentity("NIN-001", "Aleena", "London",
                LocalDate.of(1990, 3, 15), "London", "British");
    }

    @Test
    void createsIdentityViaPortal() {
        DigitalID id = createAleena();
        assertNotNull(id);
        assertEquals(DigitalIDStatus.ACTIVE, id.getStatus());
    }

    @Test
    void suspendsIdentityViaPortal() {
        DigitalID id = createAleena();
        portal.suspendIdentity(id.getId());
        assertEquals(DigitalIDStatus.SUSPENDED, portal.lookupIdentity(id.getId()).getStatus());
    }

    @Test
    void revokesIdentityViaPortal() {
        DigitalID id = createAleena();
        portal.revokeIdentity(id.getId());
        assertEquals(DigitalIDStatus.REVOKED, portal.lookupIdentity(id.getId()).getStatus());
    }
}
