package com.pos.proiect.songcollection.repository;

import com.pos.proiect.songcollection.entity.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtistsRepository extends JpaRepository<Artist, UUID> {

    Optional<Artist> findByArtistId(UUID artistId);
    List<Artist> findByArtistName(String artistName, Pageable pageable);
    List<Artist> findByArtistNameContaining(String keyword, Pageable pageable);
}
