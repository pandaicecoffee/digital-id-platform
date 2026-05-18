package com.qmul.digitalid.exception;

//meaningful exceptions
public class DigitalIdNotFoundException extends RuntimeException {
    public DigitalIdNotFoundException(String id) {
        super("Digital ID not found: " + id);
    }
}
