package com.example.service;

import com.example.model.Game;
import com.example.model.Ship;
import com.example.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    
    public Game createGame() {
        Game game = new Game();
        return gameRepository.save(game);
    }
    
    public boolean placeShip(Long gameId, int x, int y, boolean horizontal, int size) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
                
        if (game.getState() != Game.GameState.PLACING_SHIPS) {
            return false;
        }
        
        Ship ship = new Ship(x, y, horizontal, size);
        boolean success = game.getPlayerBoard().placeShip(ship);
        
        if (success && game.getPlayerBoard().areAllShipsPlaced()) {
            game.setState(Game.GameState.IN_PROGRESS);
            gameRepository.save(game);
        }
        
        return success;
    }
    
    public boolean makeMove(Long gameId, int x, int y) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
                
        if (game.getState() != Game.GameState.IN_PROGRESS) {
            return false;
        }
        
        boolean success = game.makeMove(x, y, game.isPlayerTurn());
        if (success) {
            game.setPlayerTurn(!game.isPlayerTurn());
            if (game.isGameOver()) {
                game.setState(Game.GameState.FINISHED);
            }
            gameRepository.save(game);
        }
        
        return success;
    }
    
    public Game.GameState getGameStatus(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        return game.getState();
    }
} 