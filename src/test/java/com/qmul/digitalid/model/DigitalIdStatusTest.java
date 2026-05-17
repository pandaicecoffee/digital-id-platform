package com.qmul.digitalid.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DigitalIDStatusTest {

    @Test
    void activeCanBeSuspended() {
        assertTrue(DigitalIDStatus.ACTIVE.canBeSuspended());
    }

    @Test
    void suspendedCannotBeSuspended() {
        assertFalse(DigitalIDStatus.SUSPENDED.canBeSuspended());
    }

    @Test
    void revokedCannotBeSuspended() {
        assertFalse(DigitalIDStatus.REVOKED.canBeSuspended());
    }

    @Test
    void suspendedCanBeReactivated() {
        assertTrue(DigitalIDStatus.SUSPENDED.canBeReactivated());
    }

    @Test
    void activeCannotBeReactivated() {
        assertFalse(DigitalIDStatus.ACTIVE.canBeReactivated());
    }

    @Test
    void revokedCannotBeUpdated() {
        assertFalse(DigitalIDStatus.REVOKED.canBeUpdated());
    }

    @Test
    void activeIsUsableByConsumers() {
        assertTrue(DigitalIDStatus.ACTIVE.isUsableByConsumers());
    }

    @Test
    void suspendedIsNotUsableByConsumers() {
        assertFalse(DigitalIDStatus.SUSPENDED.isUsableByConsumers());
    }
}
