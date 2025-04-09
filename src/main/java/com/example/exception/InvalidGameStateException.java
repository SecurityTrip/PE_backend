package com.example.exception;

import com.example.model.Game.GameState;

public class InvalidGameStateException extends GameException {
    public InvalidGameStateException(GameState currentState, GameState requiredState) {
        super("Invalid game state. Current: " + currentState + ", Required: " + requiredState);
    }
} 