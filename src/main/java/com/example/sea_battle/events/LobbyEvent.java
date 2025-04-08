package com.example.sea_battle.events;

import com.example.sea_battle.dto.LobbyDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LobbyEvent {
    private String eventType;
    private LobbyDTO lobby;
    private LocalDateTime timestamp;
    
    public static LobbyEvent ofUpdate(LobbyDTO lobby) {
        LobbyEvent event = new LobbyEvent();
        event.setEventType("lobby_update");
        event.setLobby(lobby);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    public static LobbyEvent ofCreation(LobbyDTO lobby) {
        LobbyEvent event = new LobbyEvent();
        event.setEventType("lobby_created");
        event.setLobby(lobby);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    public static LobbyEvent ofDeletion(LobbyDTO lobby) {
        LobbyEvent event = new LobbyEvent();
        event.setEventType("lobby_deleted");
        event.setLobby(lobby);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
} 