package com.pos.proiect.songcollection.controller;

import com.pos.proiect.songcollection.dto.SongDTO;
import com.pos.proiect.songcollection.dto.SongForPlaylistDTO;
import com.pos.proiect.songcollection.exception.*;
import com.pos.proiect.songcollection.hateoas.SongModelAssembler;
import com.pos.proiect.songcollection.service.AuthorizationService;
import com.pos.proiect.songcollection.service.SongsService;
import com.pos.proiect.songcollection.structures.GenreEnum;
import com.pos.proiect.songcollection.structures.RolesEnum;
import com.pos.proiect.songcollection.structures.UserAndRoles;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/api/songcollection/songs")
@AllArgsConstructor
@CrossOrigin("http://localhost:3000")
public class SongsController {

    @Autowired
    private SongsService songsService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private SongModelAssembler songModelAssembler;

    @GetMapping("")
    public ResponseEntity<CollectionModel<EntityModel<SongDTO>>> getSongs(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "items_per_page", defaultValue = "5") Integer itemsPerPage
    ) {
        PageRequest pageRequest = PageRequest.of(page, itemsPerPage);

        List<EntityModel<SongDTO>> songs = songsService.getSongs(pageRequest).stream()
                .map(songModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SongDTO>> songsCollection = CollectionModel.of(songs,
                linkTo(methodOn(SongsController.class).getSongs(page, itemsPerPage)).withSelfRel()
                );

        if(page > 0) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongs(page - 1, itemsPerPage)).withRel("prevPage"));
        }

        List<SongDTO> aux = songsService.getSongs(null);
        int noSongs = aux.size();
        int noPages = (noSongs % itemsPerPage == 0) ? noSongs/itemsPerPage : (noSongs / itemsPerPage + 1);
        if(page < noPages - 1) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongs(page + 1, itemsPerPage)).withRel("nextPage"));
        }

        return new ResponseEntity<>(songsCollection, HttpStatus.OK);
    }

    @GetMapping("/{songId}")
    public ResponseEntity<EntityModel<SongDTO>> getSongById(@PathVariable("songId") Integer songId) throws SongNotFoundException {
        SongDTO songDTO = songsService.getSongById(songId);
        EntityModel<SongDTO> songDTOEntity = songModelAssembler.toModel(songDTO);
        return new ResponseEntity<>(songDTOEntity, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<EntityModel<SongDTO>> addSong(HttpServletRequest request, @RequestBody SongDTO songDTO) throws AlbumDoesNotExistException, AlbumCanNotBePartOfAnotherAlbumException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        SongDTO createdSongDTO = songsService.addSong(songDTO);
        URI location = URI.create("/api/songcollection/songs/" + createdSongDTO.getSongId());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(songModelAssembler.toModel(createdSongDTO), responseHeaders, HttpStatus.CREATED);
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<String> removeSong(HttpServletRequest request, @PathVariable("songId") Integer songId) throws SongNotFoundException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER) ||
                        (authorizationService.authorizeRole(userAndRoles, RolesEnum.ARTIST) &&
                        songsService.isArtistAnAuthorOfTheSong(userAndRoles.getUserName(), songId));
        if(!hasProperRoles) throw new AccessForbiddenException();

        SongDTO songDTO = songsService.getSongById(songId);
        String selfLink = WebMvcLinkBuilder.linkTo(methodOn(SongsController.class).getSongById(songDTO.getSongId())).withSelfRel().getHref();
        SongForPlaylistDTO songForPlaylistDTO = new SongForPlaylistDTO(songDTO.getSongId(), songDTO.getName(), songDTO.getArtistsOfSong(), selfLink);

        songsService.removeSong(songId);
        HttpStatus responseStatus = songsService.deleteSongInAllProfilesAndPlaylists(jws, userAndRoles.getUserId(), songForPlaylistDTO);
        if(responseStatus == HttpStatus.OK) {
            return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>("Error", HttpStatus.CONFLICT);
    }

    @PutMapping("/{songId}")
    public ResponseEntity<String> updateSong(HttpServletRequest request, @PathVariable("songId") Integer songId, @RequestBody SongDTO songDTO) throws CantCreateWithPutMethodException, AlbumDoesNotExistException, AlbumCanNotBePartOfAnotherAlbumException, AccessForbiddenException, SongNotFoundException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER) ||
                (authorizationService.authorizeRole(userAndRoles, RolesEnum.ARTIST) &&
                        songsService.isArtistAnAuthorOfTheSong(userAndRoles.getUserName(), songId));
        if(!hasProperRoles) throw new AccessForbiddenException();

        SongDTO updatedSongDTO = songsService.updateSong(songDTO, songId);
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{songId}/album/{albumId}")
    public ResponseEntity<EntityModel<SongDTO>> addSongToAlbum(HttpServletRequest request, @PathVariable("songId") Integer songId, @PathVariable("albumId") Integer albumId) throws SongNotFoundException, SongIsAlreadyAssignedToAnAlbumException, AlbumDoesNotExistException, AlbumCanNotBePartOfAnotherAlbumException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        SongDTO songDTO = songsService.addSongToAlbum(songId, albumId);
        return ResponseEntity
                .created(linkTo(methodOn(SongsController.class).getSongById(songId)).toUri())
                .body(songModelAssembler.toModel(songDTO)
                );
    }

    @PostMapping("/{songId}/album")
    public ResponseEntity<EntityModel<SongDTO>> removeSongFromAlbum(HttpServletRequest request, @PathVariable("songId") Integer songId) throws SongIsNotAssignedToAnAlbumException, SongNotFoundException, AlbumDoesNotExistException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        SongDTO albumDTO = songsService.removeSongFromAlbum(songId);
        EntityModel<SongDTO> albumDTOEntity = songModelAssembler.toModel(albumDTO);
        return new ResponseEntity<>(albumDTOEntity, HttpStatus.OK);
    }

    @GetMapping(value = "", params = { "title" })
    public ResponseEntity<CollectionModel<EntityModel<SongDTO>>> getSongsByTitle(
            @RequestParam(name = "title") String songTitle,
            @RequestParam(name = "match", required = false) String match,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "items_per_page", defaultValue = "5") Integer itemsPerPage
    ) throws SongNotFoundException {

        if(match != null && !Objects.equals(match, "exact")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        PageRequest pageRequest = PageRequest.of(page, itemsPerPage);

        List<SongDTO> songsDTO = (match != null) ? songsService.getSongsByTitle(songTitle, pageRequest) : songsService.getSongsByTitleContaining(songTitle, pageRequest);
        List<EntityModel<SongDTO>> songs = songsDTO.stream()
                .map(songModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SongDTO>> songsCollection = CollectionModel.of(songs,
                linkTo(methodOn(SongsController.class).getSongsByTitle(songTitle, match, page, itemsPerPage)).withSelfRel()
        );

        if(page > 0) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongsByTitle(songTitle, match, page - 1, itemsPerPage)).withRel("prevPage"));
        }

        List<SongDTO> aux = songsService.getSongsByTitle(songTitle, null);
        int noSongs = aux.size();
        int noPages = (noSongs % itemsPerPage == 0) ? noSongs/itemsPerPage : (noSongs / itemsPerPage + 1);
        if(page < noPages - 1) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongsByTitle(songTitle, match, page + 1, itemsPerPage)).withRel("nextPage"));
        }

        return new ResponseEntity<>(songsCollection, HttpStatus.OK);
    }

    @GetMapping(value = "", params = { "genre" })
    public ResponseEntity<CollectionModel<EntityModel<SongDTO>>> getSongsByGenre(
            @RequestParam(name = "genre") GenreEnum genre,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "items_per_page", defaultValue = "5") Integer itemsPerPage
    ) throws SongNotFoundException {

        PageRequest pageRequest = PageRequest.of(page, itemsPerPage);

        List<SongDTO> songsDTO = songsService.getSongsByGenre(genre, pageRequest);
        List<EntityModel<SongDTO>> songs = songsDTO.stream()
                .map(songModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SongDTO>> songsCollection = CollectionModel.of(songs,
                linkTo(methodOn(SongsController.class).getSongsByGenre(genre, page, itemsPerPage)).withSelfRel()
        );

        if(page > 0) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongsByGenre(genre, page - 1, itemsPerPage)).withRel("prevPage"));
        }

        List<SongDTO> aux = songsService.getSongsByGenre(genre, null);
        int noSongs = aux.size();
        int noPages = (noSongs % itemsPerPage == 0) ? noSongs/itemsPerPage : (noSongs / itemsPerPage + 1);
        if(page < noPages - 1) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongsByGenre(genre, page + 1, itemsPerPage)).withRel("nextPage"));
        }

        return new ResponseEntity<>(songsCollection, HttpStatus.OK);
    }

    @GetMapping(value = "", params = { "releaseYear" })
    public ResponseEntity<CollectionModel<EntityModel<SongDTO>>> getSongsByReleaseYear(
            @RequestParam(name = "releaseYear") Integer releaseYear,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "items_per_page", defaultValue = "5") Integer itemsPerPage
    ) throws SongNotFoundException {

        PageRequest pageRequest = PageRequest.of(page, itemsPerPage);

        List<SongDTO> songsDTO = songsService.getSongsByReleaseYear(releaseYear, pageRequest);
        List<EntityModel<SongDTO>> songs = songsDTO.stream()
                .map(songModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SongDTO>> songsCollection = CollectionModel.of(songs,
                linkTo(methodOn(SongsController.class).getSongsByReleaseYear(releaseYear, page, itemsPerPage)).withSelfRel()
        );

        if(page > 0) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongsByReleaseYear(releaseYear, page - 1, itemsPerPage)).withRel("prevPage"));
        }

        List<SongDTO> aux = songsService.getSongsByReleaseYear(releaseYear, null);
        int noSongs = aux.size();
        int noPages = (noSongs % itemsPerPage == 0) ? noSongs/itemsPerPage : (noSongs / itemsPerPage + 1);
        if(page < noPages - 1) {
            songsCollection.add(linkTo(methodOn(SongsController.class).getSongsByReleaseYear(releaseYear, page + 1, itemsPerPage)).withRel("nextPage"));
        }

        return new ResponseEntity<>(songsCollection, HttpStatus.OK);
    }

    @GetMapping("/albums")
    public ResponseEntity<CollectionModel<EntityModel<SongDTO>>> getAlbums() {
        List<EntityModel<SongDTO>> songs = songsService.getAlbums().stream()
                .map(songModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SongDTO>> songsCollection = CollectionModel.of(songs,
                linkTo(methodOn(SongsController.class).getAlbums()).withSelfRel()
        );

        return new ResponseEntity<>(songsCollection, HttpStatus.OK);
    }

    @GetMapping("/forPlaylists/{songId}")
    public ResponseEntity<SongForPlaylistDTO> getSongForPlaylistById(@PathVariable("songId") Integer songId) throws SongNotFoundException {
        SongDTO songDTO = songsService.getSongById(songId);
        String selfLink = WebMvcLinkBuilder.linkTo(methodOn(SongsController.class).getSongById(songDTO.getSongId())).withSelfRel().getHref();
        SongForPlaylistDTO songForPlaylistDTO = new SongForPlaylistDTO(songDTO.getSongId(), songDTO.getName(), songDTO.getArtistsOfSong(), selfLink);
        return new ResponseEntity<>(songForPlaylistDTO, HttpStatus.OK);
    }
}
