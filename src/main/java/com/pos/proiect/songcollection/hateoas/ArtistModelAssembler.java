package com.pos.proiect.songcollection.hateoas;


import com.pos.proiect.songcollection.controller.ArtistsController;
import com.pos.proiect.songcollection.dto.ArtistDTO;
import com.pos.proiect.songcollection.exception.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ArtistModelAssembler implements RepresentationModelAssembler<ArtistDTO, EntityModel<ArtistDTO>> {
    @Override
    public EntityModel<ArtistDTO> toModel(ArtistDTO artistDTO) {
        try {
            EntityModel<ArtistDTO> artistDTOEntity = EntityModel.of(artistDTO,
                    WebMvcLinkBuilder.linkTo(methodOn(ArtistsController.class).getArtistById(artistDTO.getArtistId())).withSelfRel(),
                    linkTo(methodOn(ArtistsController.class).getArtists(null, null)).withRel("artists"),
                    linkTo(methodOn(ArtistsController.class).removeArtist(null, artistDTO.getArtistId())).withRel("removeArtist").withType("DELETE"),
                    linkTo(methodOn(ArtistsController.class).assignSongToArtist(null, artistDTO.getArtistId(), null)).withRel("assignSongToArtist").withType("POST")
                    );

            if(artistDTO.getSongsByArtist() != null && artistDTO.getSongsByArtist().size() > 0) {
                artistDTOEntity.add(
                        linkTo(methodOn(ArtistsController.class).removeSongFromArtist(null, artistDTO.getArtistId(), null)).withRel("removeSongFromArtist").withType("DELETE")
                );
            }

            return artistDTOEntity;

        } catch (ArtistNotFoundException | ResourceIdentifierIsNullException | UUIDFormatException |
                 SongNotFoundException | SongIsAlreadyAssignedToArtistException | SongIsNotAssignedToArtistException |
                 AccessForbiddenException | JwsFormatNotValidException | JwsSignatureNotValidException |
                 JwsTokenCouldNotBeValidatedException | JwsExpiredException | AuthorizationHeaderMissingException e) {
            throw new RuntimeException(e);
        }
    }
}
