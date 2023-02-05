package com.pos.proiect.songcollection.service;

import com.pos.proiect.songcollection.dto.ArtistDTO;
import com.pos.proiect.songcollection.entity.Artist;
import com.pos.proiect.songcollection.entity.Song;
import com.pos.proiect.songcollection.exception.*;
import com.pos.proiect.songcollection.repository.ArtistsRepository;
import com.pos.proiect.songcollection.repository.SongsRepository;
import com.pos.proiect.songcollection.util.ArtistUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ArtistsService {

    private ArtistsRepository artistsRepository;
    private SongsRepository songsRepository;


    public List<ArtistDTO> getArtists(PageRequest pageRequest) {
        List<Artist> artists = artistsRepository.findAll(pageRequest).getContent();
        List<ArtistDTO> artistsDTO = new ArrayList<>();
        for(Artist artist : artists) {
            artistsDTO.add(ArtistUtil.getArtistDtoFromArtist(artist));
        }

        return artistsDTO;
    }

    public ArtistDTO getArtistById(String uuidString) throws ArtistNotFoundException, UUIDFormatException {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new UUIDFormatException();
        }

        Artist artist = artistsRepository.findByArtistId(uuid).orElseThrow(ArtistNotFoundException::new);
        return ArtistUtil.getArtistDtoFromArtist(artist);
    }

    public Pair<ArtistDTO, Boolean> addArtist(ArtistDTO artistDTO, String uuidString) throws UUIDFormatException {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new UUIDFormatException();
        }

        Artist artist = ArtistUtil.getArtistFromArtistDto(artistDTO, uuid);
        boolean isCreated = !artistsRepository.existsById(uuid);
        return Pair.of(ArtistUtil.getArtistDtoFromArtist(artistsRepository.save(artist)), isCreated);
    }

    public void removeArtist(String uuidString) throws ResourceIdentifierIsNullException, ArtistNotFoundException, UUIDFormatException {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new UUIDFormatException();
        }

        boolean exists = artistsRepository.existsById(uuid);

        if(!exists) {
            throw new ArtistNotFoundException();
        }

        try {
            artistsRepository.deleteById(uuid);
        } catch (IllegalArgumentException ex) {
            throw new ResourceIdentifierIsNullException();
        }
    }

    public ArtistDTO assignSongToArtist(String uuidString, Integer songId) throws ArtistNotFoundException, SongNotFoundException, SongIsAlreadyAssignedToArtistException, UUIDFormatException {
        UUID artistUUID;
        try {
            artistUUID = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new UUIDFormatException();
        }

        Artist artist = artistsRepository.findByArtistId(artistUUID).orElseThrow(ArtistNotFoundException::new);
        Song song = songsRepository.findBySongId(songId).orElseThrow(SongNotFoundException::new);

        List<Song> songsByArtist = artist.getSongsByArtist();
        if(songsByArtist.contains(song)) {
            throw new SongIsAlreadyAssignedToArtistException();
        }
        songsByArtist.add(song);
        artist.setSongsByArtist(songsByArtist);

        return ArtistUtil.getArtistDtoFromArtist(artistsRepository.save(artist));
    }

    public ArtistDTO removeSongFromArtist(String uuidString, Integer songId) throws ArtistNotFoundException, SongNotFoundException, SongIsNotAssignedToArtistException, UUIDFormatException {
        UUID artistUUID;
        try {
            artistUUID = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new UUIDFormatException();
        }

        Artist artist = artistsRepository.findByArtistId(artistUUID).orElseThrow(ArtistNotFoundException::new);
        Song song = songsRepository.findBySongId(songId).orElseThrow(SongNotFoundException::new);

        List<Song> songsByArtist = artist.getSongsByArtist();
        if (!songsByArtist.contains(song)) {
            throw new SongIsNotAssignedToArtistException();
        }

        songsByArtist.remove(song);
        artist.setSongsByArtist(songsByArtist);
        return ArtistUtil.getArtistDtoFromArtist(artistsRepository.save(artist));
    }

    public List<ArtistDTO> getArtistsByName(String artistName, PageRequest pageRequest) throws ArtistNotFoundException {
        List<Artist> artists = artistsRepository.findByArtistName(artistName, pageRequest);
        if(artists.size() == 0) {
            throw new ArtistNotFoundException();
        }

        List<ArtistDTO> artistsDTO = new ArrayList<>();
        for(Artist artist : artists) {
            artistsDTO.add(ArtistUtil.getArtistDtoFromArtist(artist));
        }

        return artistsDTO;
    }

    public List<ArtistDTO> getArtistsByNameContaining(String keyword, PageRequest pageRequest) throws ArtistNotFoundException {
        List<Artist> artists = artistsRepository.findByArtistNameContaining(keyword, pageRequest);
        if(artists.size() == 0) {
            throw new ArtistNotFoundException();
        }

        List<ArtistDTO> artistsDTO = new ArrayList<>();
        for(Artist artist : artists) {
            artistsDTO.add(ArtistUtil.getArtistDtoFromArtist(artist));
        }

        return artistsDTO;
    }

}
