package com.invinciboll.exceptions.runtime;


public class ValidatorException extends InvoiceProcessingException {
    public ValidatorException(String message) {
        super(message);
    }

    public ValidatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
