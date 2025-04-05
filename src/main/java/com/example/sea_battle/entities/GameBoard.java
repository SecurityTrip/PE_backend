package com.example.sea_battle.entities;

public class GameBoard {
    private boolean[][] board;

    GameBoard() {
        this.board = new boolean[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = false;
            }
        }
    }
}
