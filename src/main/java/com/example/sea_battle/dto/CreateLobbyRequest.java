package com.example.sea_battle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLobbyRequest {

    private String lobbyName;
    private boolean isPrivate;
    private String password;

}
