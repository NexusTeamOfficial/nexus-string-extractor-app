package com.nexusteam.framework.xml.string.exception;

/**
 * Thrown when an extraction or transformation operation fails.
 */
public class ExtractionException extends Exception {

    public ExtractionException(String message) {
        super(message);
    }

    public ExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
