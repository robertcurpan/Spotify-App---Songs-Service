package com.pos.proiect.songcollection.dto;

import com.pos.proiect.songcollection.entity.Artist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SongForPlaylistDTO {

    private Integer songId;
    private String name;
    private List<Artist> artistsOfSong;
    private String selfLink;
    // Aici teoretic am putea baga un link pt delete (ca sa putem sterge o melodie direct din playlist - nu e prea frumos asa, dar s-ar putea)
}
