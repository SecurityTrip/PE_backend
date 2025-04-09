package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private boolean ready;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_board_id")
    private GameBoard gameBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Player() {
        this.ready = false;
        this.gameBoard = new GameBoard();
    }

    public Player(String username) {
        this.username = username;
        this.ready = false;
        this.gameBoard = new GameBoard();
    }
} 