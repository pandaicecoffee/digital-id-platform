package com.qmul.digitalid.model;

import java.util.function.Consumer;

public final class DigitalIDOperations {

    private DigitalIDOperations() {
    }

    public static void suspend(DigitalID id) {
        id.suspend();
    }

    public static void reactivate(DigitalID id) {
        id.reactivate();
    }

    public static void revoke(DigitalID id) {
        id.revoke();
    }

    public static void updateFirstName(DigitalID id, String value) {
        id.updateFirstName(value);
    }

    public static void updateLastName(DigitalID id, String value) {
        id.updateLastName(value);
    }

    public static void updateAddress(DigitalID id, String value) {
        id.updateAddress(value);
    }

    public static void updateNationality(DigitalID id, String value) {
        id.updateNationality(value);
    }

    public static void applyUpdate(DigitalID id, Consumer<DigitalID> mutation) {
        mutation.accept(id);
    }
}