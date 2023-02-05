package com.pos.proiect.songcollection.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pos.proiect.songcollection.entity.Artist;
import com.pos.proiect.songcollection.entity.Song;
import com.pos.proiect.songcollection.structures.GenreEnum;
import com.pos.proiect.songcollection.structures.SongTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class SongDTO {

    private Integer songId;
    private String name;
    private GenreEnum genre;
    private Integer releaseYear;
    private SongTypeEnum songType;
    private Integer albumId;
    private List<Artist> artistsOfSong;
    @JsonIgnore
    private Song album;
    private List<Song> songsOfAlbum;

    public SongDTO(String name, GenreEnum genre, Integer releaseYear, SongTypeEnum songType, Integer albumId) {
        this.songId = -1;
        this.name = name;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.songType = songType;
        this.albumId = albumId;
        this.artistsOfSong = new ArrayList<>();
        this.album = new Song();
        this.songsOfAlbum = new ArrayList<>();
    }
}
