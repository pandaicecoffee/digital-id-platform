package com.qmul.digitalid.exception;

public class DuplicateIdentityException extends RuntimeException {
    public DuplicateIdentityException(String nationalIdNumber) {
        super("A Digital ID already exists for this national ID number: " + nationalIdNumber);
    }
}
