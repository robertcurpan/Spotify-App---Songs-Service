package com.pos.proiect.songcollection.exception;

public class SongIsAlreadyAssignedToArtistException extends Exception {
    public SongIsAlreadyAssignedToArtistException() {
        super();
    }

    public SongIsAlreadyAssignedToArtistException(String message) {
        super(message);
    }
}
