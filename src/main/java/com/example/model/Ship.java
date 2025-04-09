package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "ships")
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int startX;
    
    @Column(nullable = false)
    private int startY;
    
    @Column(nullable = false)
    private boolean horizontal;
    
    @Column(nullable = false)
    private int size;
    
    @Column(nullable = false)
    private int hits;
    
    @Column(nullable = false)
    private boolean sunk;
    
    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL)
    private List<Cell> cells = new ArrayList<>();
    
    public Ship() {
        this.hits = 0;
        this.sunk = false;
    }
    
    public Ship(int startX, int startY, boolean horizontal, int size) {
        this.startX = startX;
        this.startY = startY;
        this.horizontal = horizontal;
        this.size = size;
        this.hits = 0;
        this.sunk = false;
    }
    
    public void hit() {
        hits++;
        if (hits >= size) {
            sunk = true;
        }
    }
    
    public boolean isSunk() {
        return sunk;
    }
} 