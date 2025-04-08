package com.example.sea_battle.dto;

import com.example.sea_battle.entities.Game;
import com.example.sea_battle.entities.Shot;
import lombok.Data;

import java.util.List;

@Data
public class GameDTO {
    private Long id;
    private String currentPlayer;
    private Game.GameStatus status;
    private int turnNumber;
    private List<GameBoardDTO> boards;

    @Data
    public static class GameBoardDTO {
        private Long id;
        private String player;
        private boolean ready;
        private List<ShipDTO> ships;
        private List<ShotDTO> shots;

        @Data
        public static class ShipDTO {
            private Long id;
            private int x;
            private int y;
            private int size;
            private boolean isHorizontal;
            private boolean isSunk;
        }

        @Data
        public static class ShotDTO {
            private int x;
            private int y;
            private Shot.ShotResult result;
        }
    }
} 