package com.axis.nachimbal.global.exception;

public class EmailNotRegisteredException extends RuntimeException {
    public EmailNotRegisteredException(String message) {
        super(message);
    }
}