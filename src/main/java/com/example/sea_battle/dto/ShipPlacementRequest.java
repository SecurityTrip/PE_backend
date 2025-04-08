package com.example.sea_battle.dto;

import lombok.Data;

@Data
public class ShipPlacementRequest {
    private int x;
    private int y;
    private int size;
    private boolean horizontal;
} 