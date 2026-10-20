package com.nexusteam.framework.xml.string.exception;

/**
 * Thrown when an XML layout file cannot be parsed.
 */
public class ParseException extends ExtractionException {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
