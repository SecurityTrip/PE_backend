package com.example.sea_battle.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Контроллер регистрации", description = "Содержит методы для авторизации")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/registration")
    public String registration(@RequestParam String username, @RequestParam String password) {
        // TODO Registrtion logic
        return "registration successful";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // TODO Login logic
        return "login successful";
    }

    @PostMapping("/logout")
    public String logout() {
        // TODO Logout logic
        return "logout successful";
    }

}