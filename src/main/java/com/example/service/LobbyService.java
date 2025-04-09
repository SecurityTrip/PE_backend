package com.example.service;

import com.example.model.Game;
import com.example.model.GameType;
import com.example.model.Player;
import com.example.model.Difficulty;
import com.example.repository.GameRepository;
import com.example.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyService {
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    private final Map<Long, Player> waitingPlayers = new ConcurrentHashMap<>();
    
    public Game createGame(Player player, GameType type, Difficulty difficulty) {
        player = playerRepository.save(player);
        Game game = new Game();
        game.setPlayer1(player);
        game.setType(type);
        game.setDifficulty(difficulty);
        return gameRepository.save(game);
    }
    
    public Game joinGame(Long gameId, Player player) {
        player = playerRepository.save(player);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
                
        if (game.getPlayer2() != null) {
            throw new RuntimeException("Game is full");
        }
        
        game.setPlayer2(player);
        return gameRepository.save(game);
    }
    
    public Game setPlayerReady(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
                
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
                
        if (game.getPlayer1().getId().equals(playerId)) {
            game.getPlayer1().setReady(true);
        } else if (game.getPlayer2() != null && game.getPlayer2().getId().equals(playerId)) {
            game.getPlayer2().setReady(true);
        } else {
            throw new RuntimeException("Player not in game");
        }
        
        if (game.getPlayer1().isReady() && 
            (game.getType() == GameType.SINGLE_PLAYER || 
             (game.getPlayer2() != null && game.getPlayer2().isReady()))) {
            game.setState(Game.GameState.PLACING_SHIPS);
        }
        
        return gameRepository.save(game);
    }
    
    public Map<Long, Player> getWaitingPlayers() {
        return waitingPlayers;
    }
    
    public void addWaitingPlayer(Player player) {
        player = playerRepository.save(player);
        waitingPlayers.put(player.getId(), player);
    }
    
    public void removeWaitingPlayer(Long playerId) {
        waitingPlayers.remove(playerId);
    }
} 