package com.example.sea_battle.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lobby_id")
    private Lobby lobby;

    @ManyToOne
    @JoinColumn(name = "current_player_id")
    private User currentPlayer;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<GameBoard> boards = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.IN_PROGRESS;

    private int turnNumber = 0;

    public enum GameStatus {
        IN_PROGRESS,
        FINISHED
    }
} 