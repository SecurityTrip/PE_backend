package com.example.controller;

import com.example.model.Difficulty;
import com.example.model.Game;
import com.example.model.GameType;
import com.example.model.Player;
import com.example.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {
    @Autowired
    private LobbyService lobbyService;
    
    @PostMapping("/create")
    public ResponseEntity<Game> createGame(
            @RequestParam String username,
            @RequestParam GameType type,
            @RequestParam(required = false) Difficulty difficulty) {
        Player player = new Player(username);
        Game game = lobbyService.createGame(player, type, difficulty);
        return ResponseEntity.ok(game);
    }
    
    @PostMapping("/{gameId}/join")
    public ResponseEntity<Game> joinGame(
            @PathVariable Long gameId,
            @RequestParam String username) {
        Player player = new Player(username);
        Game game = lobbyService.joinGame(gameId, player);
        return ResponseEntity.ok(game);
    }
    
    @PostMapping("/{gameId}/ready")
    public ResponseEntity<Game> setReady(
            @PathVariable Long gameId,
            @RequestParam Long playerId) {
        Game game = lobbyService.setPlayerReady(gameId, playerId);
        return ResponseEntity.ok(game);
    }
    
    @GetMapping("/waiting")
    public ResponseEntity<Map<Long, Player>> getWaitingPlayers() {
        Map<Long, Player> waitingPlayers = lobbyService.getWaitingPlayers();
        return ResponseEntity.ok(waitingPlayers);
    }
} 