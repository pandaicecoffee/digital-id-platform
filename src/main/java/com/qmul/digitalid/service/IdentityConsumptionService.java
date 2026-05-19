package com.qmul.digitalid.service;

import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationResult;
import java.time.LocalDate;
import java.util.Optional;

public interface IdentityConsumptionService {
    VerificationResult verifyIsActive(String digitalIdRef, String requestedBy);

    //check for suspensions
    VerificationResult verifyActiveForPeriod(String digitalIdRef,
                                             LocalDate from, LocalDate to,
                                             String requestedBy);

    Optional<DigitalID> lookup(String digitalIdRef, String requestedBy);
}