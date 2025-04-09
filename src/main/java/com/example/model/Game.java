package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "player_board_id")
    private GameBoard playerBoard;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ai_board_id")
    private GameBoard aiBoard;
    
    @Column(nullable = false)
    private boolean isPlayerTurn;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState state;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameType type;
    
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "player1_id")
    private Player player1;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "player2_id")
    private Player player2;
    
    public Game() {
        this.playerBoard = new GameBoard();
        this.aiBoard = new GameBoard();
        this.isPlayerTurn = true;
        this.state = GameState.PLACING_SHIPS;
    }
    
    public boolean makeMove(int x, int y, boolean isPlayerMove) {
        GameBoard targetBoard = isPlayerMove ? aiBoard : playerBoard;
        List<Cell> cells = targetBoard.getCells();
        
        for (Cell cell : cells) {
            if (cell.getX() == x && cell.getY() == y) {
                if (cell.getState() == Cell.CellState.EMPTY || cell.getState() == Cell.CellState.SHIP) {
                    cell.hit();
                    if (cell.getState() == Cell.CellState.HIT && cell.getShip() != null && cell.getShip().isSunk()) {
                        if (isPlayerMove) {
                            aiBoard.sinkShip(cell.getShip());
                        } else {
                            playerBoard.sinkShip(cell.getShip());
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    
    public boolean isGameOver() {
        return playerBoard.areAllShipsSunk() || aiBoard.areAllShipsSunk();
    }
    
    public enum GameState {
        PLACING_SHIPS,
        IN_PROGRESS,
        FINISHED
    }
} 