package com.example.sea_battle.controllers;

import com.example.sea_battle.dto.GameDTO;
import com.example.sea_battle.dto.ShotRequest;
import com.example.sea_battle.services.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Контроллер игры", description = "Содержит методы для ведения игры")
@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Сделать выстрел")
    @PostMapping("/{gameId}/shot")
    public ResponseEntity<?> makeShot(@PathVariable Long gameId, @RequestBody ShotRequest shotRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            GameDTO game = gameService.makeShot(gameId, username, shotRequest);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Получить состояние игры")
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGameState(@PathVariable Long gameId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // TODO: Реализовать получение состояния игры
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
