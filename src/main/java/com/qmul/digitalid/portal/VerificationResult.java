package com.qmul.digitalid.portal;

public record VerificationResult(boolean valid, String reason) {

    @Override
    public String toString() {
        return "VerificationResult{" +
                "valid=" + valid +
                ", reason='" + reason + '\'' +
                '}';
    }
}
