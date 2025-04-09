package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cells")
public class Cell {
    public enum CellState {
        EMPTY,
        SHIP,
        HIT,
        MISS
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int x;
    
    @Column(nullable = false)
    private int y;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CellState state;
    
    @ManyToOne
    @JoinColumn(name = "ship_id")
    private Ship ship;
    
    public Cell() {
        this.state = CellState.EMPTY;
    }
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = CellState.EMPTY;
    }
    
    public void setShip(Ship ship) {
        this.ship = ship;
        this.state = CellState.SHIP;
    }
    
    public void hit() {
        if (state == CellState.SHIP) {
            state = CellState.HIT;
            if (ship != null) {
                ship.hit();
            }
        } else if (state == CellState.EMPTY) {
            state = CellState.MISS;
        }
    }
} 