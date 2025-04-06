package com.example.sea_battle.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Контроллер лобби")
@RestController
@RequestMapping("/lobby")
public class LobbyController {

    @PostMapping("/create")
    public String createLobby() {
        return java.util.UUID.randomUUID().toString();
    }


}
