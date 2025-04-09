package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Entity
@Table(name = "lobbies")
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lobby_id")
    private Map<Long, Game> activeGames = new ConcurrentHashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "waiting_players", joinColumns = @JoinColumn(name = "lobby_id"))
    @Column(name = "player_id")
    private Map<Long, Player> waitingPlayers = new ConcurrentHashMap<>();
    
    public Game createGame(Player player, GameType type) {
        Game game = new Game();
        game.setPlayer1(player);
        game.setType(type);
        activeGames.put(game.getId(), game);
        return game;
    }
    
    public Map<Long, Game> getActiveGames() {
        return activeGames;
    }
    
    public void addWaitingPlayer(Player player) {
        waitingPlayers.put(player.getId(), player);
    }
    
    public void removeWaitingPlayer(Long playerId) {
        waitingPlayers.remove(playerId);
    }
    
    public Map<Long, Player> getWaitingPlayers() {
        return waitingPlayers;
    }
    
    public Game findGameForPlayer(Player player) {
        return activeGames.values().stream()
            .filter(game -> game.getPlayer1().getId().equals(player.getId()) || 
                          (game.getPlayer2() != null && game.getPlayer2().getId().equals(player.getId())))
            .findFirst()
            .orElse(null);
    }
} 