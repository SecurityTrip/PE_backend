package com.example.sea_battle.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class GameBoard {
    private static final Logger logger = LoggerFactory.getLogger(GameBoard.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private User player;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Ship> ships = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Shot> shots = new ArrayList<>();

    private boolean ready = false;
    
    private int size = 10; // Стандартный размер поля 10x10
    
    /**
     * Метод для добавления корабля на доску
     * @param ship Корабль для добавления
     * @return true если корабль был успешно добавлен, false если нет
     */
    public boolean addShip(Ship ship) {
        // Проверка выхода за границы поля
        if (!isValidShipPosition(ship)) {
            logger.warn("Попытка добавить корабль вне границ поля: boardId={}, playerUsername={}, shipSize={}, x={}, y={}, isHorizontal={}",
                id, player.getUsername(), ship.getSize(), ship.getX(), ship.getY(), ship.isHorizontal());
            return false;
        }
        
        // Проверка пересечения с другими кораблями
        if (isOverlappingWithOtherShips(ship)) {
            logger.warn("Попытка добавить корабль, пересекающийся с другими: boardId={}, playerUsername={}, shipSize={}, x={}, y={}, isHorizontal={}",
                id, player.getUsername(), ship.getSize(), ship.getX(), ship.getY(), ship.isHorizontal());
            return false;
        }
        
        ship.setBoard(this);
        ships.add(ship);
        logger.info("Корабль успешно добавлен: boardId={}, playerUsername={}, shipSize={}, x={}, y={}, isHorizontal={}",
            id, player.getUsername(), ship.getSize(), ship.getX(), ship.getY(), ship.isHorizontal());
        return true;
    }
    
    /**
     * Проверяет, что позиция корабля находится в пределах игрового поля
     */
    private boolean isValidShipPosition(Ship ship) {
        if (ship.getX() < 0 || ship.getY() < 0) {
            return false;
        }
        
        if (ship.isHorizontal()) {
            return ship.getX() + ship.getSize() <= size && ship.getY() < size;
        } else {
            return ship.getX() < size && ship.getY() + ship.getSize() <= size;
        }
    }
    
    /**
     * Проверяет, не пересекается ли корабль с другими кораблями на доске
     */
    private boolean isOverlappingWithOtherShips(Ship newShip) {
        // Создаем зону безопасности вокруг корабля (1 клетка)
        for (Ship existingShip : ships) {
            if (areShipsOverlapping(existingShip, newShip)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Проверяет, пересекаются ли два корабля (с учетом зоны безопасности в 1 клетку)
     */
    private boolean areShipsOverlapping(Ship ship1, Ship ship2) {
        // Получаем все клетки, которые занимает корабль ship1 с зоной безопасности
        List<Point> ship1Area = getShipAreaWithSafetyZone(ship1);
        
        // Получаем все клетки, которые занимает корабль ship2
        List<Point> ship2Cells = getShipCells(ship2);
        
        // Проверяем, есть ли пересечения
        for (Point cell : ship2Cells) {
            if (ship1Area.contains(cell)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Получает все клетки, которые занимает корабль, включая зону безопасности
     */
    private List<Point> getShipAreaWithSafetyZone(Ship ship) {
        List<Point> area = new ArrayList<>();
        
        int startX = Math.max(0, ship.getX() - 1);
        int startY = Math.max(0, ship.getY() - 1);
        int endX, endY;
        
        if (ship.isHorizontal()) {
            endX = Math.min(size - 1, ship.getX() + ship.getSize());
            endY = Math.min(size - 1, ship.getY() + 1);
        } else {
            endX = Math.min(size - 1, ship.getX() + 1);
            endY = Math.min(size - 1, ship.getY() + ship.getSize());
        }
        
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                area.add(new Point(x, y));
            }
        }
        
        return area;
    }
    
    /**
     * Получает все клетки, которые занимает корабль
     */
    private List<Point> getShipCells(Ship ship) {
        List<Point> cells = new ArrayList<>();
        
        if (ship.isHorizontal()) {
            for (int x = ship.getX(); x < ship.getX() + ship.getSize(); x++) {
                cells.add(new Point(x, ship.getY()));
            }
        } else {
            for (int y = ship.getY(); y < ship.getY() + ship.getSize(); y++) {
                cells.add(new Point(ship.getX(), y));
            }
        }
        
        return cells;
    }
    
    /**
     * Вспомогательный класс для представления точки на игровом поле
     */
    private static class Point {
        private final int x;
        private final int y;
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }
        
        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }
} 