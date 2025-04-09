package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "game_boards")
public class GameBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_board_id")
    private List<Cell> cells = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_board_id")
    private List<Ship> ships = new ArrayList<>();
    
    public GameBoard() {
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                cells.add(new Cell(x, y));
            }
        }
    }
    
    public boolean placeShip(Ship ship) {
        if (!isValidShipPlacement(ship)) {
            return false;
        }
        
        int x = ship.getStartX();
        int y = ship.getStartY();
        
        for (int i = 0; i < ship.getSize(); i++) {
            Cell cell = getCell(x, y);
            cell.setShip(ship);
            if (ship.isHorizontal()) {
                x++;
            } else {
                y++;
            }
        }
        
        ships.add(ship);
        return true;
    }
    
    private boolean isValidShipPlacement(Ship ship) {
        int x = ship.getStartX();
        int y = ship.getStartY();
        
        // Проверка границ
        if (ship.isHorizontal()) {
            if (x + ship.getSize() > 10) return false;
        } else {
            if (y + ship.getSize() > 10) return false;
        }
        
        // Проверка соседних клеток
        for (int i = -1; i <= ship.getSize(); i++) {
            for (int j = -1; j <= 1; j++) {
                int checkX = ship.isHorizontal() ? x + i : x + j;
                int checkY = ship.isHorizontal() ? y + j : y + i;
                
                if (checkX >= 0 && checkX < 10 && checkY >= 0 && checkY < 10) {
                    Cell cell = getCell(checkX, checkY);
                    if (cell.getState() == Cell.CellState.SHIP) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private Cell getCell(int x, int y) {
        return cells.stream()
                .filter(cell -> cell.getX() == x && cell.getY() == y)
                .findFirst()
                .orElse(null);
    }
    
    public boolean areAllShipsPlaced() {
        return ships.size() == 10; // 10 кораблей в игре
    }
    
    public boolean areAllShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }
    
    public void sinkShip(Ship ship) {
        ship.setSunk(true);
    }
} 