package com.invinciboll.exceptions.runtime;

public class InvoiceProcessingException extends RuntimeException {
    public InvoiceProcessingException(String message) {
        super(message);
    }

    public InvoiceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
