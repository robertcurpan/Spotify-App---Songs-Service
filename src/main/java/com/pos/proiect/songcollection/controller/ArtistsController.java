package com.pos.proiect.songcollection.controller;


import com.pos.proiect.songcollection.dto.ArtistDTO;
import com.pos.proiect.songcollection.exception.*;
import com.pos.proiect.songcollection.hateoas.ArtistModelAssembler;
import com.pos.proiect.songcollection.service.ArtistsService;
import com.pos.proiect.songcollection.service.AuthorizationService;
import com.pos.proiect.songcollection.structures.RolesEnum;
import com.pos.proiect.songcollection.structures.UserAndRoles;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
@RequestMapping("/api/songcollection/artists")
@AllArgsConstructor
@CrossOrigin("http://localhost:3000")
public class ArtistsController {

    @Autowired
    private ArtistsService artistsService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ArtistModelAssembler artistModelAssembler;


    @GetMapping("")
    public ResponseEntity<CollectionModel<EntityModel<ArtistDTO>>> getArtists(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "items_per_page", defaultValue = "10") Integer itemsPerPage
    ) {

        PageRequest pageRequest = PageRequest.of(page, itemsPerPage);

        List<EntityModel<ArtistDTO>> artists = artistsService.getArtists(pageRequest).stream()
                .map(artistModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ArtistDTO>> artistsCollection = CollectionModel.of(artists,
                linkTo(methodOn(ArtistsController.class).getArtists(page, itemsPerPage)).withSelfRel()
                );

        return new ResponseEntity<>(artistsCollection, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<EntityModel<ArtistDTO>> getArtistById(@PathVariable("uuid") String uuidString) throws ArtistNotFoundException, UUIDFormatException {
        ArtistDTO artistDTO = artistsService.getArtistById(uuidString);
        EntityModel<ArtistDTO> artistDTOEntity = artistModelAssembler.toModel(artistDTO);
        return new ResponseEntity<>(artistDTOEntity, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<EntityModel<ArtistDTO>> addArtist(HttpServletRequest request, @PathVariable("uuid") String uuidString, @RequestBody ArtistDTO artistDTO) throws UUIDFormatException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        Pair<ArtistDTO, Boolean> pair = artistsService.addArtist(artistDTO, uuidString);
        ArtistDTO newArtistDTO = pair.getFirst();
        boolean isCreated = pair.getSecond();

        if(isCreated) {
            URI location = URI.create("/api/songcollection/artists/" + newArtistDTO.getArtistId());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>(artistModelAssembler.toModel(newArtistDTO), responseHeaders, HttpStatus.CREATED);
        } else {
            // updated resource
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<String> removeArtist(HttpServletRequest request, @PathVariable("uuid") String uuidString) throws ResourceIdentifierIsNullException, ArtistNotFoundException, UUIDFormatException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        artistsService.removeArtist(uuidString);
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{uuid}/songs/{songId}")
    public ResponseEntity<EntityModel<ArtistDTO>> assignSongToArtist(HttpServletRequest request, @PathVariable("uuid") String uuidString,
                                     @PathVariable("songId") Integer songId) throws ArtistNotFoundException, SongNotFoundException, SongIsAlreadyAssignedToArtistException, UUIDFormatException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        ArtistDTO artistDTO = artistsService.assignSongToArtist(uuidString, songId);
        return ResponseEntity
                .created(linkTo(methodOn(ArtistsController.class).getArtistById(uuidString)).toUri())
                .body(artistModelAssembler.toModel(artistDTO)
                );
    }

    @PostMapping("/{uuid}/songs/{songId}")
    public ResponseEntity<EntityModel<ArtistDTO>> removeSongFromArtist(HttpServletRequest request, @PathVariable("uuid") String uuidString,
                                       @PathVariable("songId") Integer songId) throws ArtistNotFoundException, SongNotFoundException, SongIsNotAssignedToArtistException, UUIDFormatException, AccessForbiddenException, JwsFormatNotValidException, JwsSignatureNotValidException, JwsTokenCouldNotBeValidatedException, JwsExpiredException, AuthorizationHeaderMissingException {
        String jws = authorizationService.getJwsFromRequest(request);
        UserAndRoles userAndRoles = authorizationService.validateTokenAndReturnUserAndRoles(jws);
        boolean hasProperRoles = authorizationService.authorizeRole(userAndRoles, RolesEnum.CONTENT_MANAGER);
        if(!hasProperRoles) throw new AccessForbiddenException();

        ArtistDTO artistDTO = artistsService.removeSongFromArtist(uuidString, songId);
        EntityModel<ArtistDTO> artistDTOEntity = artistModelAssembler.toModel(artistDTO);
        return new ResponseEntity<>(artistDTOEntity, HttpStatus.OK);
    }

    @GetMapping(value = "", params = {"name"})
    public ResponseEntity<CollectionModel<EntityModel<ArtistDTO>>> getArtistsByName(
            @RequestParam(name = "name") String artistName,
            @RequestParam(name = "match", required = false) String match,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "items_per_page", defaultValue = "10") Integer itemsPerPage
            ) throws ArtistNotFoundException {

        if(match != null && !Objects.equals(match, "exact")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        PageRequest pageRequest = PageRequest.of(page, itemsPerPage);

        List<ArtistDTO> artistsDTO = (match != null) ? artistsService.getArtistsByName(artistName, pageRequest) : artistsService.getArtistsByNameContaining(artistName, pageRequest);
        List<EntityModel<ArtistDTO>> artists = artistsDTO.stream()
                .map(artistModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ArtistDTO>> artistsCollection = CollectionModel.of(artists,
                linkTo(methodOn(ArtistsController.class).getArtistsByName(artistName, match, page, itemsPerPage)).withSelfRel()
        );

        return new ResponseEntity<>(artistsCollection, HttpStatus.OK);
    }

}
