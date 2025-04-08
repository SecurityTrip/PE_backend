package com.example.sea_battle.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaceShipsRequest {
    private List<ShipInfo> ships;
    
    @Data
    public static class ShipInfo {
        private int x;
        private int y;
        private int size;
        private boolean isHorizontal;
    }
} 