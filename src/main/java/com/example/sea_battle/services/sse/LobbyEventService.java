package com.example.sea_battle.services.sse;

import com.example.sea_battle.dto.LobbyDTO;
import com.example.sea_battle.events.LobbyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LobbyEventService {

    private final SseEmitterService sseEmitterService;

    /**
     * Уведомляет всех подписанных клиентов о создании нового лобби
     * 
     * @param lobby Данные о созданном лобби
     */
    public void notifyLobbyCreated(LobbyDTO lobby) {
        LobbyEvent event = LobbyEvent.ofCreation(lobby);
        sseEmitterService.sendLobbyUpdateToAll(event);
    }

    /**
     * Уведомляет всех подписанных клиентов об обновлении лобби
     * 
     * @param lobby Обновленные данные о лобби
     */
    public void notifyLobbyUpdated(LobbyDTO lobby) {
        LobbyEvent event = LobbyEvent.ofUpdate(lobby);
        sseEmitterService.sendLobbyUpdateToAll(event);
    }

    /**
     * Уведомляет всех подписанных клиентов об удалении лобби
     * 
     * @param lobby Данные об удаленном лобби
     */
    public void notifyLobbyDeleted(LobbyDTO lobby) {
        LobbyEvent event = LobbyEvent.ofDeletion(lobby);
        sseEmitterService.sendLobbyUpdateToAll(event);
    }

    /**
     * Уведомляет всех подписанных клиентов об обновлении списка лобби
     * 
     * @param lobbies Обновленный список лобби
     */
    public void notifyLobbiesUpdated(List<LobbyDTO> lobbies) {
        for (LobbyDTO lobby : lobbies) {
            LobbyEvent event = LobbyEvent.ofUpdate(lobby);
            sseEmitterService.sendLobbyUpdateToAll(event);
        }
    }

    /**
     * Уведомляет конкретного пользователя об обновлении лобби
     * 
     * @param userId ID пользователя
     * @param lobby Обновленные данные о лобби
     */
    public void notifyUserAboutLobby(String userId, LobbyDTO lobby) {
        LobbyEvent event = LobbyEvent.ofUpdate(lobby);
        sseEmitterService.sendEventToUser(userId, event);
    }
} 