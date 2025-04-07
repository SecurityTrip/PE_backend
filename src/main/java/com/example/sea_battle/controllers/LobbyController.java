package com.example.sea_battle.controllers;

import com.example.sea_battle.dto.CreateLobbyRequest;
import com.example.sea_battle.dto.JoinLobbyRequest;
import com.example.sea_battle.dto.LobbyDTO;
import com.example.sea_battle.services.LobbyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Контроллер лобби", description = "API для управления игровыми лобби")
@RestController
@RequestMapping("/lobby")
public class LobbyController {

    private LobbyService lobbyService;

    @Autowired
    public void setLobbyService(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @Operation(summary = "Создать новое лобби")
    @PostMapping("/create")
    public ResponseEntity<?> createLobby(@RequestBody CreateLobbyRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            LobbyDTO lobby = lobbyService.createLobby(
                username, 
                request.getLobbyName(), 
                request.isPrivate(), 
                request.getPassword()
            );
            
            return ResponseEntity.ok(lobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Получить список публичных лобби")
    @GetMapping("/public")
    public ResponseEntity<List<LobbyDTO>> getPublicLobbies() {
        List<LobbyDTO> lobbies = lobbyService.getPublicLobbies();
        return ResponseEntity.ok(lobbies);
    }

    @Operation(summary = "Получить информацию о лобби по ID")
    @GetMapping("/{lobbyId}")
    public ResponseEntity<?> getLobbyById(@PathVariable String lobbyId) {
        Optional<LobbyDTO> lobbyOpt = lobbyService.getLobbyById(lobbyId);
        
        if (lobbyOpt.isPresent()) {
            return ResponseEntity.ok(lobbyOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лобби не найдено");
        }
    }

    @Operation(summary = "Присоединиться к лобби")
    @PostMapping("/join")
    public ResponseEntity<?> joinLobby(@RequestBody JoinLobbyRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            LobbyDTO lobby = lobbyService.joinLobby(
                request.getLobbyID(), 
                username, 
                request.getPassword()
            );
            
            return ResponseEntity.ok(lobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Выйти из лобби")
    @PostMapping("/{lobbyId}/leave")
    public ResponseEntity<?> leaveLobby(@PathVariable String lobbyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            lobbyService.leaveLobby(lobbyId, username);
            return ResponseEntity.ok("Успешно покинули лобби");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Запустить игру")
    @PostMapping("/{lobbyId}/start")
    public ResponseEntity<?> startGame(@PathVariable String lobbyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            LobbyDTO lobby = lobbyService.startGame(lobbyId, username);
            return ResponseEntity.ok(lobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Получить список лобби, в которых участвует текущий пользователь")
    @GetMapping("/my")
    public ResponseEntity<List<LobbyDTO>> getMyLobbies() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<LobbyDTO> lobbies = lobbyService.getPlayerLobbies(username);
        return ResponseEntity.ok(lobbies);
    }
}
