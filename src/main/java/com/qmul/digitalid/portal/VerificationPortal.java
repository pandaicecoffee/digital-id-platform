package com.qmul.digitalid.portal;

import com.qmul.digitalid.model.VerificationResult;

//for organisations that use the digital id service
public interface VerificationPortal extends Portal{
    VerificationResult verify(String identificationID);

}

