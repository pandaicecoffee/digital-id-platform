package com.qmul.digitalid.portal;

//for organisations that use the digital id service
public interface VerificationPortal extends Portal{
    VerificationResult verify(String identificationID);

}

