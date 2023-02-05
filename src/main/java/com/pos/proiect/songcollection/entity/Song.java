package com.pos.proiect.songcollection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pos.proiect.songcollection.structures.GenreEnum;
import com.pos.proiect.songcollection.structures.SongTypeEnum;
import lombok.*;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "songs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Song {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private Integer songId;

    @Column(name = "name")
    private String name;

    @Column(name = "genre")
    @Enumerated(EnumType.STRING)
    private GenreEnum genre;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private SongTypeEnum songType;

    @Column(name = "album_id")
    private Integer albumId;

    // For the artists-song Many-to-Many relationship
    @JsonIgnore
    @ManyToMany(mappedBy = "songsByArtist")
    private List<Artist> artistsOfSong;

    // For the self join (an album can have multiple songs)
    @ManyToOne
    @JoinColumn(name = "album_id", referencedColumnName = "song_id", insertable = false, updatable = false)
    @JsonIgnore
    private Song album;

    @OneToMany(mappedBy = "album")
    @JsonIgnore
    private List<Song> songsOfAlbum;

}
