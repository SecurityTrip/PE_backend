package com.example.sea_battle.entities.schemas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String lobbyID;
    private Long lobbyOwner;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(String lobbyID) {
        this.lobbyID = lobbyID;
    }

    public Long getLobbyOwner() {
        return lobbyOwner;
    }

    public void setLobbyOwner(Long lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }
}
