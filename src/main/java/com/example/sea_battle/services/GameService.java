package com.example.sea_battle.services;

import com.example.sea_battle.dto.GameDTO;
import com.example.sea_battle.dto.ShotRequest;
import com.example.sea_battle.entities.Game;
import com.example.sea_battle.entities.GameBoard;
import com.example.sea_battle.entities.Ship;
import com.example.sea_battle.entities.Shot;
import com.example.sea_battle.repositories.GameRepository;
import com.example.sea_battle.repositories.GameBoardRepository;
import com.example.sea_battle.repositories.ShotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final GameBoardRepository gameBoardRepository;
    private final ShotRepository shotRepository;

    @Autowired
    public GameService(GameRepository gameRepository, 
                      GameBoardRepository gameBoardRepository,
                      ShotRepository shotRepository) {
        this.gameRepository = gameRepository;
        this.gameBoardRepository = gameBoardRepository;
        this.shotRepository = shotRepository;
    }

    @Transactional
    public GameDTO makeShot(Long gameId, String username, ShotRequest shotRequest) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Игра не найдена"));

        if (!game.getCurrentPlayer().getUsername().equals(username)) {
            throw new RuntimeException("Не ваш ход");
        }

        GameBoard opponentBoard = game.getBoards().stream()
                .filter(board -> !board.getPlayer().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Доска противника не найдена"));

        Shot shot = new Shot();
        shot.setBoard(opponentBoard);
        shot.setX(shotRequest.getX());
        shot.setY(shotRequest.getY());

        // Проверяем попадание
        boolean hit = opponentBoard.getShips().stream()
                .anyMatch(ship -> isShipHit(ship, shotRequest.getX(), shotRequest.getY()));

        if (hit) {
            shot.setResult(Shot.ShotResult.HIT);
            // Проверяем, потоплен ли корабль
            Ship hitShip = opponentBoard.getShips().stream()
                    .filter(ship -> isShipHit(ship, shotRequest.getX(), shotRequest.getY()))
                    .findFirst()
                    .orElseThrow();

            if (isShipSunk(hitShip, opponentBoard.getShots())) {
                shot.setResult(Shot.ShotResult.SUNK);
                hitShip.setSunk(true);
            }
        } else {
            shot.setResult(Shot.ShotResult.MISS);
        }

        shotRepository.save(shot);
        opponentBoard.getShots().add(shot);

        // Проверяем, закончена ли игра
        if (isGameFinished(opponentBoard)) {
            game.setStatus(Game.GameStatus.FINISHED);
        } else {
            // Передаем ход другому игроку
            game.setCurrentPlayer(opponentBoard.getPlayer());
            game.setTurnNumber(game.getTurnNumber() + 1);
        }

        gameRepository.save(game);
        return convertToDTO(game);
    }

    private boolean isShipHit(Ship ship, int x, int y) {
        if (ship.isHorizontal()) {
            return ship.getY() == y && x >= ship.getX() && x < ship.getX() + ship.getSize();
        } else {
            return ship.getX() == x && y >= ship.getY() && y < ship.getY() + ship.getSize();
        }
    }

    private boolean isShipSunk(Ship ship, List<Shot> shots) {
        int hitCount = 0;
        for (int i = 0; i < ship.getSize(); i++) {
            int checkX = ship.isHorizontal() ? ship.getX() + i : ship.getX();
            int checkY = ship.isHorizontal() ? ship.getY() : ship.getY() + i;
            
            boolean isHit = shots.stream()
                    .anyMatch(shot -> shot.getX() == checkX && 
                                    shot.getY() == checkY && 
                                    shot.getResult() != Shot.ShotResult.MISS);
            
            if (isHit) hitCount++;
        }
        return hitCount == ship.getSize();
    }

    private boolean isGameFinished(GameBoard board) {
        return board.getShips().stream().allMatch(Ship::isSunk);
    }

    private GameDTO convertToDTO(Game game) {
        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setCurrentPlayer(game.getCurrentPlayer().getUsername());
        dto.setStatus(game.getStatus());
        dto.setTurnNumber(game.getTurnNumber());

        dto.setBoards(game.getBoards().stream()
                .map(this::convertBoardToDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private GameDTO.GameBoardDTO convertBoardToDTO(GameBoard board) {
        GameDTO.GameBoardDTO dto = new GameDTO.GameBoardDTO();
        dto.setId(board.getId());
        dto.setPlayer(board.getPlayer().getUsername());

        dto.setShips(board.getShips().stream()
                .map(this::convertShipToDTO)
                .collect(Collectors.toList()));

        dto.setShots(board.getShots().stream()
                .map(this::convertShotToDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private GameDTO.GameBoardDTO.ShipDTO convertShipToDTO(Ship ship) {
        GameDTO.GameBoardDTO.ShipDTO dto = new GameDTO.GameBoardDTO.ShipDTO();
        dto.setId(ship.getId());
        dto.setX(ship.getX());
        dto.setY(ship.getY());
        dto.setSize(ship.getSize());
        dto.setHorizontal(ship.isHorizontal());
        dto.setSunk(ship.isSunk());
        return dto;
    }

    private GameDTO.GameBoardDTO.ShotDTO convertShotToDTO(Shot shot) {
        GameDTO.GameBoardDTO.ShotDTO dto = new GameDTO.GameBoardDTO.ShotDTO();
        dto.setX(shot.getX());
        dto.setY(shot.getY());
        dto.setResult(shot.getResult());
        return dto;
    }
} 