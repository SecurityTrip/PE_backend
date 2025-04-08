package com.example.sea_battle.dto;

import lombok.Data;

@Data
public class CurrentGameResponse {
    private String gameId;
    private String lobbyId;
    private String lobbyName;
    private String status;
    private String opponentName;
    private boolean isYourTurn;
} 