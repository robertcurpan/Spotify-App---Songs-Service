package com.pos.proiect.songcollection.exception;

public class SongIsNotAssignedToArtistException extends Exception {
    public SongIsNotAssignedToArtistException() {
        super();
    }

    public SongIsNotAssignedToArtistException(String message) {
        super(message);
    }
}
