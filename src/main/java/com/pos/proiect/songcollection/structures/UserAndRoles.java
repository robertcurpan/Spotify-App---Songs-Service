package com.pos.proiect.songcollection.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAndRoles {

    private Integer userId;
    private String userName;
    private List<String> userRoles;
}
