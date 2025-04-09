package com.example.ai;

import com.example.model.Cell;
import com.example.model.GameBoard;
import com.example.model.Difficulty;
import com.example.model.Ship;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class BattleshipAI {
    private final Random random;
    private final Difficulty difficulty;
    private List<Cell> lastHits;
    private Cell lastHit;
    private final List<Ship> shipsToPlace;
    private int lastHitX = -1;
    private int lastHitY = -1;
    private boolean isHunting = true;
    
    public BattleshipAI(Difficulty difficulty) {
        this.random = new Random();
        this.difficulty = difficulty;
        this.lastHits = new ArrayList<>();
        this.shipsToPlace = new ArrayList<>();
        // Инициализация кораблей: 1x4, 2x3, 3x2, 4x1
        for (int size = 4; size >= 1; size--) {
            int count = 5 - size;
            for (int i = 0; i < count; i++) {
                shipsToPlace.add(new Ship(0, 0, true, size));
            }
        }
    }
    
    public void placeShips(GameBoard board) {
        for (Ship ship : shipsToPlace) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(10);
                int y = random.nextInt(10);
                boolean horizontal = random.nextBoolean();
                
                ship = new Ship(x, y, horizontal, ship.getSize());
                placed = board.placeShip(ship);
            }
        }
    }
    
    public int[] makeMove(GameBoard board) {
        if (isHunting) {
            return makeHuntingMove(board);
        } else {
            return makeTargetingMove(board);
        }
    }
    
    private int[] makeHuntingMove(GameBoard board) {
        int x, y;
        do {
            x = random.nextInt(10);
            y = random.nextInt(10);
        } while (!isValidMove(board, x, y));
        
        return new int[]{x, y};
    }
    
    private int[] makeTargetingMove(GameBoard board) {
        List<int[]> possibleMoves = new ArrayList<>();
        
        // Проверяем все возможные ходы вокруг последнего попадания
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] dir : directions) {
            int newX = lastHitX + dir[0];
            int newY = lastHitY + dir[1];
            
            if (isValidMove(board, newX, newY)) {
                possibleMoves.add(new int[]{newX, newY});
            }
        }
        
        if (possibleMoves.isEmpty()) {
            isHunting = true;
            return makeHuntingMove(board);
        }
        
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }
    
    private boolean isValidMove(GameBoard board, int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            return false;
        }
        
        Cell cell = board.getCells().stream()
                .filter(c -> c.getX() == x && c.getY() == y)
                .findFirst()
                .orElse(null);
                
        return cell != null && (cell.getState() == Cell.CellState.EMPTY || cell.getState() == Cell.CellState.SHIP);
    }
    
    public void updateStrategy(int x, int y, boolean hit, boolean sunk) {
        if (hit) {
            lastHitX = x;
            lastHitY = y;
            isHunting = false;
        }
        
        if (sunk) {
            isHunting = true;
        }
    }
} 