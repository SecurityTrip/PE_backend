package com.example.sea_battle.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Shot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private GameBoard board;

    private int x;
    private int y;

    @Enumerated(EnumType.STRING)
    private ShotResult result;

    public enum ShotResult {
        MISS,
        HIT,
        SUNK
    }
} 