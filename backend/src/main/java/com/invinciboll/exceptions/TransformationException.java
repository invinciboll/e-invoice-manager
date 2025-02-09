package com.invinciboll.exceptions;

public class TransformationException extends Exception {
    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformationException(String message) {
        super(message);
    }
}
