package com.example.sea_battle.controllers;

import org.springframework.web.bind.annotation.*;

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