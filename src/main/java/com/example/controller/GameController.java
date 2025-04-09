package com.example.controller;

import com.example.model.Game;
import com.example.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;
    
    @PostMapping("/start")
    public ResponseEntity<Game> startGame() {
        Game game = gameService.createGame();
        return ResponseEntity.ok(game);
    }
    
    @PostMapping("/{gameId}/place-ship")
    public ResponseEntity<Boolean> placeShip(
            @PathVariable Long gameId,
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam boolean horizontal,
            @RequestParam int size) {
        boolean success = gameService.placeShip(gameId, x, y, horizontal, size);
        return ResponseEntity.ok(success);
    }
    
    @PostMapping("/{gameId}/shoot")
    public ResponseEntity<Boolean> shoot(
            @PathVariable Long gameId,
            @RequestParam int x,
            @RequestParam int y) {
        boolean success = gameService.makeMove(gameId, x, y);
        return ResponseEntity.ok(success);
    }
    
    @GetMapping("/{gameId}/status")
    public ResponseEntity<Game.GameState> getGameStatus(@PathVariable Long gameId) {
        Game.GameState status = gameService.getGameStatus(gameId);
        return ResponseEntity.ok(status);
    }
} 