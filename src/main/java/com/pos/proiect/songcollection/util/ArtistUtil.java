package com.pos.proiect.songcollection.util;

import com.pos.proiect.songcollection.dto.ArtistDTO;
import com.pos.proiect.songcollection.entity.Artist;

import java.util.UUID;

public class ArtistUtil {

    public static ArtistDTO getArtistDtoFromArtist(Artist artist) {
        ArtistDTO artistDTO = new ArtistDTO(artist.getArtistName(), artist.getActive());
        artistDTO.setArtistId(artist.getArtistId().toString());
        artistDTO.setSongsByArtist(artist.getSongsByArtist());
        return artistDTO;
    }

    public static Artist getArtistFromArtistDto(ArtistDTO artistDTO, UUID uuid) {
        return new Artist(uuid, artistDTO.getArtistName(), artistDTO.getActive(), artistDTO.getSongsByArtist());
    }
}
