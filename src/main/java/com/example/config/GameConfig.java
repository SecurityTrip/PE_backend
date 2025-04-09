package com.example.config;

import com.example.model.Lobby;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {
    @Bean
    public Lobby lobby() {
        return new Lobby();
    }
} 