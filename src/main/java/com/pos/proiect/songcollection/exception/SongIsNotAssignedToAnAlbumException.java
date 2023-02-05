package com.pos.proiect.songcollection.exception;

public class SongIsNotAssignedToAnAlbumException extends Exception {
    public SongIsNotAssignedToAnAlbumException() {
        super();
    }

    public SongIsNotAssignedToAnAlbumException(String message) {
        super(message);
    }
}
