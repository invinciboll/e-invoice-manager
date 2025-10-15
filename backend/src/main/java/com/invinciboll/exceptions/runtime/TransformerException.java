package com.invinciboll.exceptions.runtime;


public class TransformerException extends InvoiceProcessingException {
    public TransformerException(String message) {
        super(message);
    }

    public TransformerException(String message, Throwable cause) {
        super(message, cause);
    }
}
