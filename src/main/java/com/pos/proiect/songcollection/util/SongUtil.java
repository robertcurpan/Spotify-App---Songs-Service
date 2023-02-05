package com.pos.proiect.songcollection.util;

import com.pos.proiect.songcollection.dto.SongDTO;
import com.pos.proiect.songcollection.entity.Song;

public class SongUtil {

    public static SongDTO getSongDtoFromSong(Song song) {
        SongDTO songDTO = new SongDTO(song.getName(), song.getGenre(), song.getReleaseYear(), song.getSongType(), song.getAlbumId());
        songDTO.setSongId(song.getSongId());
        songDTO.setArtistsOfSong(song.getArtistsOfSong());
        songDTO.setAlbum(song.getAlbum());
        songDTO.setSongsOfAlbum(song.getSongsOfAlbum());
        return songDTO;
    }

    public static Song getSongFromSongDto(SongDTO songDTO, Integer songId) {
        return new Song(songId, songDTO.getName(), songDTO.getGenre(), songDTO.getReleaseYear(), songDTO.getSongType(), songDTO.getAlbumId(), songDTO.getArtistsOfSong(), songDTO.getAlbum(), songDTO.getSongsOfAlbum());
    }
}
