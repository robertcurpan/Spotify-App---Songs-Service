package com.pos.proiect.songcollection.exception;

public class SongIsAlreadyAssignedToAnAlbumException extends Exception {
    public SongIsAlreadyAssignedToAnAlbumException() {
        super();
    }

    public SongIsAlreadyAssignedToAnAlbumException(String message) {
        super(message);
    }
}
