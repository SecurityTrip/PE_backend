package com.example.sea_battle.dto;

import lombok.Data;

@Data
public class ShotResult {
    private int x;
    private int y;
    private String result; // MISS, HIT, SUNK
    private boolean isGameOver;
    private String winner;
} 