package com.pos.proiect.songcollection.repository;

import com.pos.proiect.songcollection.entity.Song;
import com.pos.proiect.songcollection.structures.GenreEnum;
import com.pos.proiect.songcollection.structures.SongTypeEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongsRepository extends JpaRepository<Song, Integer> {

    Optional<Song> findBySongId(Integer songId);
    List<Song> findByName(String name);
    List<Song> findByName(String name, Pageable pageable);
    List<Song> findByNameContaining(String name);
    List<Song> findByNameContaining(String name, Pageable pageable);
    List<Song> findByGenre(GenreEnum genreEnum);
    List<Song> findByGenre(GenreEnum genreEnum, Pageable pageable);
    List<Song> findByReleaseYear(Integer releaseYear);
    List<Song> findByReleaseYear(Integer releaseYear, Pageable pageable);
    List<Song> findBySongType(SongTypeEnum songTypeEnum);
    List<Song> findBySongType(SongTypeEnum songTypeEnum, Pageable pageable);
}
