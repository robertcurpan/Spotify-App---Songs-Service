package com.pos.proiect.songcollection.exception;

import lombok.Getter;

@Getter
public class AlbumDoesNotExistException extends Exception {
    private Integer albumId;

    public AlbumDoesNotExistException() {
        super();
    }

    public AlbumDoesNotExistException(String message) {
        super(message);
    }

    public AlbumDoesNotExistException(Integer albumId) {
        super();
        this.albumId = albumId;
    }
}
