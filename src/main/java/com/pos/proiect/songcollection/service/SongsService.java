package com.pos.proiect.songcollection.service;

import com.pos.proiect.songcollection.dto.SongDTO;
import com.pos.proiect.songcollection.dto.SongForPlaylistDTO;
import com.pos.proiect.songcollection.entity.Artist;
import com.pos.proiect.songcollection.entity.Song;
import com.pos.proiect.songcollection.exception.*;
import com.pos.proiect.songcollection.repository.ArtistsRepository;
import com.pos.proiect.songcollection.repository.SongsRepository;
import com.pos.proiect.songcollection.structures.GenreEnum;
import com.pos.proiect.songcollection.structures.SongTypeEnum;
import com.pos.proiect.songcollection.util.SongUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SongsService {

    private SongsRepository songsRepository;
    private ArtistsRepository artistsRepository;

    public List<SongDTO> getSongs(PageRequest pageRequest) {
        List<Song> songs = (pageRequest == null) ? songsRepository.findAll() : songsRepository.findAll(pageRequest).getContent();
        List<SongDTO> songsDTO = new ArrayList<>();
        for(Song song : songs) {
            songsDTO.add(SongUtil.getSongDtoFromSong(song));
        }

        return songsDTO;
    }

    public SongDTO getSongById(Integer songId) throws SongNotFoundException {
        Song song = songsRepository.findBySongId(songId).orElseThrow(SongNotFoundException::new);
        return SongUtil.getSongDtoFromSong(song);
    }

    public SongDTO addSong(SongDTO songDTO) throws AlbumDoesNotExistException, AlbumCanNotBePartOfAnotherAlbumException {
        Integer albumId = songDTO.getAlbumId();
        if(songDTO.getSongType() == SongTypeEnum.ALBUM && albumId != null) {
            throw new AlbumCanNotBePartOfAnotherAlbumException();
        }

        if(albumId != null && !(songsRepository.existsById(albumId) && songsRepository.findBySongId(albumId).get().getSongType() == SongTypeEnum.ALBUM)) {
            throw new AlbumDoesNotExistException(albumId);
        }

        Song song = SongUtil.getSongFromSongDto(songDTO, 0);
        return SongUtil.getSongDtoFromSong(songsRepository.save(song));
    }

    public void removeSong(Integer songId) throws SongNotFoundException {
        Song song = songsRepository.findBySongId(songId).orElseThrow(SongNotFoundException::new);
        List<Artist> artists = song.getArtistsOfSong();

        for(Artist artist : artists) {
            List<Song> songsByArtist = artist.getSongsByArtist();
            songsByArtist.remove(song);
            artist.setSongsByArtist(songsByArtist);
            artistsRepository.save(artist);
        }

        // Vom considera ca daca stergem un album, se sterg toate melodiile componente
        if(song.getSongType() == SongTypeEnum.ALBUM) {
            List<Song> songsOfAlbum = song.getSongsOfAlbum();
            for(Song componentSong: songsOfAlbum) {
                removeSong(componentSong.getSongId());
            }
        }

        songsRepository.deleteById(songId);
    }

    public SongDTO updateSong(SongDTO songDTO, Integer songId) throws CantCreateWithPutMethodException, AlbumDoesNotExistException, AlbumCanNotBePartOfAnotherAlbumException {
        if(!songsRepository.existsById(songId)) {
            throw new CantCreateWithPutMethodException();
        }

        // Facem replace (inlocuire completa) a melodiei din colectie (id-ul ei se pastreaza)
        Integer albumId = songDTO.getAlbumId();
        if(songDTO.getSongType() == SongTypeEnum.ALBUM && albumId != null) {
            throw new AlbumCanNotBePartOfAnotherAlbumException();
        }

        if(albumId != null && !(songsRepository.existsById(albumId) && songsRepository.findBySongId(albumId).get().getSongType() == SongTypeEnum.ALBUM)) {
            throw new AlbumDoesNotExistException(albumId);
        }
        Song song = SongUtil.getSongFromSongDto(songDTO, songId);
        return SongUtil.getSongDtoFromSong(songsRepository.save(song));
    }

    public SongDTO addSongToAlbum(Integer songId, Integer albumId) throws SongNotFoundException, SongIsAlreadyAssignedToAnAlbumException, AlbumDoesNotExistException, AlbumCanNotBePartOfAnotherAlbumException {
        Song song = songsRepository.findBySongId(songId).orElseThrow(SongNotFoundException::new);

        if(song.getSongType() == SongTypeEnum.ALBUM) {
            throw new AlbumCanNotBePartOfAnotherAlbumException();
        }

        if(song.getAlbumId() == null) {
            // Verificam daca exista un album cu id-ul "albumId"
            if(albumId == null || !(songsRepository.existsById(albumId) && songsRepository.findBySongId(albumId).get().getSongType() == SongTypeEnum.ALBUM)) {
                throw new AlbumDoesNotExistException(albumId);
            }
            song.setAlbumId(albumId);
            song.setSongType(SongTypeEnum.SONG);
            songsRepository.save(song);
            return SongUtil.getSongDtoFromSong(songsRepository.findBySongId(albumId).get());
        }

        throw new SongIsAlreadyAssignedToAnAlbumException();
    }

    public SongDTO removeSongFromAlbum(Integer songId) throws SongNotFoundException, SongIsNotAssignedToAnAlbumException, AlbumDoesNotExistException {
        Song song = songsRepository.findBySongId(songId).orElseThrow(SongNotFoundException::new);

        Integer albumId = song.getAlbumId();
        if(albumId != null) {
            song.setAlbumId(null);
            song.setSongType(SongTypeEnum.SINGLE);
            songsRepository.save(song);
            Song album = songsRepository.findBySongId(albumId).orElseThrow(AlbumDoesNotExistException::new);

            return SongUtil.getSongDtoFromSong(album);
        }

        throw new SongIsNotAssignedToAnAlbumException();
    }

    public List<SongDTO> getSongsByTitle(String title, PageRequest pageRequest) throws SongNotFoundException {
        List<Song> songs = (pageRequest == null) ? songsRepository.findByName(title) : songsRepository.findByName(title, pageRequest);
        if(songs.size() == 0) {
            throw new SongNotFoundException();
        }

        List<SongDTO> songsDTO = new ArrayList<>();
        for(Song song : songs) {
            songsDTO.add(SongUtil.getSongDtoFromSong(song));
        }

        return songsDTO;
    }

    public List<SongDTO> getSongsByTitleContaining(String keyword, PageRequest pageRequest) throws SongNotFoundException {
        List<Song> songs = songsRepository.findByNameContaining(keyword, pageRequest);
        if(songs.size() == 0) {
            throw new SongNotFoundException();
        }

        List<SongDTO> songsDTO = new ArrayList<>();
        for(Song song : songs) {
            songsDTO.add(SongUtil.getSongDtoFromSong(song));
        }

        return songsDTO;
    }

    public List<SongDTO> getSongsByGenre(GenreEnum genre, PageRequest pageRequest) throws SongNotFoundException {
        List<Song> songs = songsRepository.findByGenre(genre, pageRequest);
        if(songs.size() == 0) {
            throw new SongNotFoundException();
        }

        List<SongDTO> songsDTO = new ArrayList<>();
        for(Song song : songs) {
            songsDTO.add(SongUtil.getSongDtoFromSong(song));
        }

        return songsDTO;
    }

    public List<SongDTO> getSongsByReleaseYear(Integer releaseYear, PageRequest pageRequest) throws SongNotFoundException {
        List<Song> songs = songsRepository.findByReleaseYear(releaseYear, pageRequest);
        if(songs.size() == 0) {
            throw new SongNotFoundException();
        }

        List<SongDTO> songsDTO = new ArrayList<>();
        for(Song song : songs) {
            songsDTO.add(SongUtil.getSongDtoFromSong(song));
        }

        return songsDTO;
    }

    public List<SongDTO> getAlbums() {
        List<Song> songs = songsRepository.findBySongType(SongTypeEnum.ALBUM);
        List<SongDTO> songsDTO = new ArrayList<>();
        for(Song song : songs) {
            songsDTO.add(SongUtil.getSongDtoFromSong(song));
        }

        return songsDTO;
    }

    public boolean isArtistAnAuthorOfTheSong(String artistName, Integer songId) throws SongNotFoundException {
        SongDTO song = getSongById(songId);

        for(Artist artist : song.getArtistsOfSong()) {
            if(artist.getArtistName().equals(artistName)) {
                return true;
            }
        }

        return false;
    }

    public HttpStatus deleteSongInAllProfilesAndPlaylists(String jws, Integer userId, SongForPlaylistDTO songForPlaylistDTO) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8081/api/profiles/" + userId.toString() + "/deleteSong";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jws);
        HttpEntity<SongForPlaylistDTO> entity = new HttpEntity<SongForPlaylistDTO>(songForPlaylistDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getStatusCode();
    }

}
