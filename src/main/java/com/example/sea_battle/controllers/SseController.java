package com.example.sea_battle.controllers;

import com.example.sea_battle.services.sse.SseEmitterService;
import com.example.sea_battle.dto.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseEmitterService sseEmitterService;

    @Autowired
    public SseController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String userId) {
        return sseEmitterService.createEmitter(userId);
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam String userId) {
        sseEmitterService.removeEmitter(userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/auth-check")
    public ResponseEntity<ResponseMessage> checkAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(new ResponseMessage("Проверка аутентификации успешна. Пользователь: " + username));
    }
} 