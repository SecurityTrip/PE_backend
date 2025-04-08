package com.example.sea_battle.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class LobbyDTO {
    private Long id;
    private String lobbyID;
    private String lobbyName;
    private String ownerUsername;
    private boolean isPrivate;
    private String status;
    private int currentPlayers;
    private int maxPlayers;
    private List<String> players;
    private LocalDateTime createdAt;
    private String gameId;
}