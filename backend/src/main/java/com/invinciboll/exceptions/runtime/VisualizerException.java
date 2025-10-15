package com.invinciboll.exceptions.runtime;


public class VisualizerException extends InvoiceProcessingException {
    public VisualizerException(String message) {
        super(message);
    }

    public VisualizerException(String message, Throwable cause) {
        super(message, cause);
    }
}
