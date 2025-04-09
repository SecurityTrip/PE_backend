package com.example.exception;

public class InvalidMoveException extends GameException {
    public InvalidMoveException(String message) {
        super(message);
    }
} 