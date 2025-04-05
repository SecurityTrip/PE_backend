package com.example.sea_battle.entities;

public class ShipBlock {
    private int[][] cords;
    private boolean isDamaged;

    public ShipBlock(int[][] cords) {
        this.cords = cords;
        this.isDamaged = false;
    }
}
