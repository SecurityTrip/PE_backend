package com.example.exception;

import java.util.UUID;

public class GameNotFoundException extends GameException {
    public GameNotFoundException(UUID gameId) {
        super("Game with id " + gameId + " not found");
    }
} 