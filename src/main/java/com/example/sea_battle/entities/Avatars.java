package com.example.sea_battle.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Avatars {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String avatarBitMap;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getAvatarBitMap() {
        return avatarBitMap;
    }

    public void setAvatarBitMap(String avatarBitMap) {
        this.avatarBitMap = avatarBitMap;
    }
}
