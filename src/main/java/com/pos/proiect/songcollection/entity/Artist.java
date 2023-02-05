package com.pos.proiect.songcollection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "artists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Artist {

    @Id @Column(name = "artist_id")
    @Type(type = "uuid-char")
    private UUID artistId;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "active")
    private Boolean active;

    // For the artists-song Many-to-Many relationship
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "songs_artists",
            joinColumns = @JoinColumn(name = "artist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songsByArtist;

}
