package com.pos.proiect.songcollection.exception;

public class JwsSignatureNotValidException extends Exception {
    public JwsSignatureNotValidException() {
        super();
    }

    public JwsSignatureNotValidException(String message) {
        super(message);
    }
}
