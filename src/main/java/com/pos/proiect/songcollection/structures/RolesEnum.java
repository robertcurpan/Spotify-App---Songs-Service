package com.pos.proiect.songcollection.structures;

public enum RolesEnum {
    CLIENT("client"),
    ARTIST("artist"),
    CONTENT_MANAGER("content_manager"),
    APP_ADMIN("administrator_aplicatie");

    private final String role;

    RolesEnum(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}