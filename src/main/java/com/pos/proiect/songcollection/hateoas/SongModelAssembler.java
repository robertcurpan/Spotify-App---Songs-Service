package com.pos.proiect.songcollection.hateoas;

import com.pos.proiect.songcollection.controller.SongsController;
import com.pos.proiect.songcollection.dto.SongDTO;
import com.pos.proiect.songcollection.exception.*;
import com.pos.proiect.songcollection.structures.SongTypeEnum;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SongModelAssembler implements RepresentationModelAssembler<SongDTO, EntityModel<SongDTO>> {
    @Override
    public EntityModel<SongDTO> toModel(SongDTO songDTO) {
        try {
            EntityModel<SongDTO> songDTOEntity = EntityModel.of(songDTO,
                    linkTo(methodOn(SongsController.class).getSongById(songDTO.getSongId())).withSelfRel(),
                    linkTo(methodOn(SongsController.class).getSongs(null, null)).withRel("songs"),
                    linkTo(methodOn(SongsController.class).removeSong(null, songDTO.getSongId())).withRel("removeSong").withType("DELETE"),
                    linkTo(methodOn(SongsController.class).updateSong(null, songDTO.getSongId(), null)).withRel("updateSong").withType("PUT")
            );

            if(songDTO.getSongType() != SongTypeEnum.ALBUM && songDTO.getAlbumId() == null) {
                songDTOEntity.add(linkTo(methodOn(SongsController.class).addSongToAlbum(null, songDTO.getSongId(), null))
                        .withRel("addSongToAlbum").withType("POST"));
            }

            if(songDTO.getSongType() == SongTypeEnum.SONG && songDTO.getAlbumId() != null) {
                songDTOEntity.add(linkTo(methodOn(SongsController.class).removeSongFromAlbum(null, songDTO.getSongId()))
                        .withRel("removeSongFromAlbum").withType("DELETE"));
            }

            return songDTOEntity;

        } catch (SongNotFoundException | AlbumDoesNotExistException | CantCreateWithPutMethodException |
                 AlbumCanNotBePartOfAnotherAlbumException | SongIsAlreadyAssignedToAnAlbumException |
                 SongIsNotAssignedToAnAlbumException | AccessForbiddenException | JwsFormatNotValidException |
                 JwsSignatureNotValidException | JwsTokenCouldNotBeValidatedException | JwsExpiredException |
                 AuthorizationHeaderMissingException e) {
            throw new RuntimeException(e);
        }
    }
}
