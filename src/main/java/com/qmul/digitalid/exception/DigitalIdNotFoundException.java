package com.qmul.digitalid.exception;

public class DigitalIdNotFoundException extends RuntimeException {
    public DigitalIdNotFoundException(String id) {
        super("Digital ID not found: " + id);
    }
}
