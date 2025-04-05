package com.example.sea_battle.entities;

import java.util.List;

public class Ship {
    private ShipType type;
    private List<ShipBlock> shipBlocks;
    private boolean isDead;
    private int blockCounter;

    // TODO Make correct constructor for ships
    public Ship(ShipType type) {
        this.type = type;
        this.shipBlocks = shipBlocks;
        this.isDead = false;

        if (this.type == ShipType.ONE_BLOCK){
            this.blockCounter = 1;
        }
        if (this.type == ShipType.TWO_BLOCK){
            this.blockCounter = 2;
        }
        if (this.type == ShipType.THREE_BLOCK){
            this.blockCounter = 3;
        }
        if (this.type == ShipType.FOUR_BLOCK){
            this.blockCounter = 4;
        }
    }
}
