package com.pos.proiect.songcollection.exception;

public class JwsTokenCouldNotBeValidatedException extends Exception {
    public JwsTokenCouldNotBeValidatedException() {
        super();
    }

    public JwsTokenCouldNotBeValidatedException(String message) {
        super(message);
    }
}
