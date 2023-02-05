package com.pos.proiect.songcollection.dto;

import com.pos.proiect.songcollection.entity.Song;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class ArtistDTO {

    private String artistId; //uuid-ul (nu il bag in constructor)
    private String artistName;
    private Boolean active;
    private List<Song> songsByArtist;

    public ArtistDTO(String artistName, Boolean active) {
        this.artistId = "";
        this.artistName = artistName;
        this.active = active;
        this.songsByArtist = new ArrayList<>();
    }
}
