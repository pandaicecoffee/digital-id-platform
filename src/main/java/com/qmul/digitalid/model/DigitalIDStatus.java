package com.qmul.digitalid.model;

import java.awt.*;

public enum DigitalIDStatus {
    ACTIVE,
    SUSPENDED,
    REVOKED;

    public boolean isUsableByConsumers() {
        return this == ACTIVE;
    }

    public boolean canBeUpdated() {
        return this == ACTIVE || this == SUSPENDED;
    }

    public boolean canBeSuspended() {
        return this == ACTIVE;
    }

    public boolean canBeReactivated() {
        return this == SUSPENDED;
    }

    public boolean canBeRevoked() {
        return this == REVOKED;
    }


}

