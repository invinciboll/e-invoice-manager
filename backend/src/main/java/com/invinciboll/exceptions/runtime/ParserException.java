package com.invinciboll.exceptions.runtime;

public class ParserException extends InvoiceProcessingException {
    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
