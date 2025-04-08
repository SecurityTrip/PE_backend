package com.example.sea_battle.controllers;

import com.example.sea_battle.dto.*;
import com.example.sea_battle.services.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Контроллер игры", description = "API для игрового процесса")
@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @Operation(summary = "Получить состояние игры по ID")
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGameState(@PathVariable String gameId, Authentication authentication) {
        try {
            String username = authentication.getName();
            GameStateResponse gameState = gameService.getGameState(gameId, username);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Разместить корабли")
    @PostMapping("/{gameId}/place-ships")
    public ResponseEntity<?> placeShips(
            @PathVariable String gameId,
            @RequestBody PlaceShipsRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            GameStateResponse gameState = gameService.placeShips(gameId, username, request.getShips());
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Сделать выстрел")
    @PostMapping("/{gameId}/shot")
    public ResponseEntity<?> makeShot(
            @PathVariable String gameId,
            @RequestBody ShotRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            GameDTO result = gameService.makeShot(Long.parseLong(gameId), username, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Выйти из игры")
    @PostMapping("/{gameId}/surrender")
    public ResponseEntity<?> surrender(@PathVariable String gameId, Authentication authentication) {
        try {
            String username = authentication.getName();
            gameService.surrender(gameId, username);
            return ResponseEntity.ok("Вы сдались в игре");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Получить информацию о текущей игре пользователя")
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentGame(Authentication authentication) {
        try {
            String username = authentication.getName();
            CurrentGameResponse response = gameService.getCurrentGame(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
