package com.example.sea_battle.dto;

import com.example.sea_battle.entities.Game;
import lombok.Data;

import java.util.List;

@Data
public class GameStateResponse {
    private String gameId;
    private String status;
    private String currentPlayer;
    private int turnNumber;
    private List<BoardInfo> boards;

    @Data
    public static class BoardInfo {
        private String playerId;
        private String playerName;
        private boolean isReady;
        private List<ShipInfo> ships;
        private List<ShotInfo> shots;
    }

    @Data
    public static class ShipInfo {
        private int x;
        private int y;
        private int size;
        private boolean isHorizontal;
        private boolean isSunk;
    }

    @Data
    public static class ShotInfo {
        private int x;
        private int y;
        private String result; // MISS, HIT, SUNK
    }
} 