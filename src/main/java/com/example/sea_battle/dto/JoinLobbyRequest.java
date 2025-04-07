package com.example.sea_battle.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinLobbyRequest {
    private String lobbyID;
    private String password;
} 