package com.example.sea_battle.controllers;

import com.example.sea_battle.dto.CreateLobbyRequest;
import com.example.sea_battle.dto.JoinLobbyRequest;
import com.example.sea_battle.dto.LobbyDTO;
import com.example.sea_battle.dto.ResponseMessage;
import com.example.sea_battle.jwt.JwtCore;
import com.example.sea_battle.services.LobbyService;
import com.example.sea_battle.services.sse.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Tag(name = "Контроллер лобби", description = "API для управления игровыми лобби")
@RestController
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true", 
    allowedHeaders = {"Authorization", "Content-Type", "X-Requested-With"},
    exposedHeaders = {"Authorization"})
@RequestMapping("/lobby")
@RequiredArgsConstructor
public class LobbyController {
    private static final Logger logger = LoggerFactory.getLogger(LobbyController.class);

    private final LobbyService lobbyService;
    private final SseEmitterService sseEmitterService;
    private final JwtCore jwtCore;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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

    @Operation(summary = "Подписаться на обновления лобби через Server-Sent Events")
    @GetMapping("/sse")
    public SseEmitter subscribeToLobbyEvents(Authentication authentication, @RequestParam(value = "token", required = false) String token) {
        final String userId;
        
        // Проверяем аутентификацию из контекста безопасности
        if (authentication != null) {
            userId = authentication.getName();
            logger.info("Запрос на подписку на обновления лобби от пользователя (через Authentication): {}", userId);
        } 
        // Если аутентификации нет, пытаемся использовать токен из параметров запроса
        else if (token != null && !token.isEmpty()) {
            try {
                // Получаем ID пользователя из токена
                userId = jwtCore.getUsernameFromJwt(token);
                
                logger.info("Запрос на подписку на обновления лобби от пользователя (через token): {}", userId);
            } catch (Exception e) {
                logger.error("Ошибка при обработке токена: {}", e.getMessage());
                throw new SecurityException("Недействительный токен");
            }
        } else {
            logger.error("Попытка подключения к SSE без аутентификации");
            throw new SecurityException("Требуется аутентификация");
        }
        
        final SseEmitter emitter = sseEmitterService.createEmitter(userId);
        
        // Отправляем текущий список лобби сразу после подключения
        // Это делаем асинхронно, чтобы не блокировать создание эмиттера
        executorService.execute(() -> {
            try {
                // Небольшая задержка, чтобы клиент успел установить соединение
                Thread.sleep(500);
                
                // Проверяем, что соединение все еще активно
                if (sseEmitterService.hasEmitter(userId)) {
                    List<LobbyDTO> currentLobbies = lobbyService.getPublicLobbies();
                    
                    // Используем прямое сообщение конкретному пользователю
                    if (emitter != null) {
                        emitter.send(SseEmitter.event()
                            .name("initial_lobbies")
                            .data(currentLobbies));
                        logger.info("Отправлен начальный список лобби пользователю: {}", userId);
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при отправке начального списка лобби пользователю {}: {}", userId, e.getMessage());
                sseEmitterService.removeEmitter(userId);
            }
        });
        
        return emitter;
    }

    @Operation(summary = "Отписаться от обновлений лобби")
    @GetMapping("/sse/disconnect")
    public ResponseEntity<ResponseMessage> disconnectFromLobbyEvents(Authentication authentication, @RequestParam(value = "token", required = false) String token) {
        final String userId;
        
        // Проверяем аутентификацию из контекста безопасности
        if (authentication != null) {
            userId = authentication.getName();
            logger.info("Запрос на отписку от обновлений лобби от пользователя (через Authentication): {}", userId);
        } 
        // Если аутентификации нет, пытаемся использовать токен из параметров запроса
        else if (token != null && !token.isEmpty()) {
            try {
                // Получаем ID пользователя из токена
                userId = jwtCore.getUsernameFromJwt(token);
                
                logger.info("Запрос на отписку от обновлений лобби от пользователя (через token): {}", userId);
            } catch (Exception e) {
                logger.error("Ошибка при обработке токена: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMessage("Недействительный токен"));
            }
        } else {
            logger.error("Попытка отключения от SSE без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMessage("Требуется аутентификация"));
        }
        
        sseEmitterService.removeEmitter(userId);
        return ResponseEntity.ok(new ResponseMessage("Отключено от обновлений лобби"));
    }
    
    @Operation(summary = "Подписаться на обновления конкретного лобби через Server-Sent Events")
    @GetMapping("/{lobbyId}/sse")
    public SseEmitter subscribeToLobbyDetailEvents(
            @PathVariable String lobbyId,
            Authentication authentication, 
            @RequestParam(value = "token", required = false) String token) {
        
        final String userId;
        
        // Проверяем аутентификацию из контекста безопасности
        if (authentication != null) {
            userId = authentication.getName();
            logger.info("Запрос на подписку на обновления лобби {} от пользователя (через Authentication): {}", lobbyId, userId);
        } 
        // Если аутентификации нет, пытаемся использовать токен из параметров запроса
        else if (token != null && !token.isEmpty()) {
            try {
                // Получаем ID пользователя из токена
                userId = jwtCore.getUsernameFromJwt(token);
                
                logger.info("Запрос на подписку на обновления лобби {} от пользователя (через token): {}", lobbyId, userId);
            } catch (Exception e) {
                logger.error("Ошибка при обработке токена: {}", e.getMessage());
                throw new SecurityException("Недействительный токен");
            }
        } else {
            logger.error("Попытка подключения к SSE без аутентификации");
            throw new SecurityException("Требуется аутентификация");
        }
        
        // Создаем уникальный идентификатор для подписки на конкретное лобби
        final String emitterId = "lobby_" + lobbyId + "_" + userId;
        final SseEmitter emitter = sseEmitterService.createEmitter(emitterId);
        
        // Отправляем текущие данные лобби сразу после подключения
        executorService.execute(() -> {
            try {
                // Небольшая задержка, чтобы клиент успел установить соединение
                Thread.sleep(500);
                
                // Проверяем, что соединение все еще активно
                if (sseEmitterService.hasEmitter(emitterId)) {
                    Optional<LobbyDTO> lobbyOpt = lobbyService.getLobbyById(lobbyId);
                    
                    if (lobbyOpt.isPresent()) {
                        emitter.send(SseEmitter.event()
                            .name("lobby_update")
                            .data(lobbyOpt.get()));
                        logger.info("Отправлены данные лобби {} пользователю: {}", lobbyId, userId);
                    } else {
                        // Если лобби не найдено, отправляем сообщение об ошибке
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data(new ResponseMessage("Лобби не найдено")));
                        logger.warn("Лобби {} не найдено при подписке пользователя: {}", lobbyId, userId);
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при отправке данных лобби {} пользователю {}: {}", lobbyId, userId, e.getMessage());
                sseEmitterService.removeEmitter(emitterId);
            }
        });
        
        return emitter;
    }
}
