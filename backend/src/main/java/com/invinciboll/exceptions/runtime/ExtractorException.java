package com.invinciboll.exceptions.runtime;


public class ExtractorException extends InvoiceProcessingException {
    public ExtractorException(String message) {
        super(message);
    }

    public ExtractorException(String message, Throwable cause) {
        super(message, cause);
    }
}
