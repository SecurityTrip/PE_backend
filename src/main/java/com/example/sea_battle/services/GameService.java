package com.example.sea_battle.services;

import com.example.sea_battle.dto.*;
import com.example.sea_battle.entities.Game;
import com.example.sea_battle.entities.GameBoard;
import com.example.sea_battle.entities.Ship;
import com.example.sea_battle.entities.Shot;
import com.example.sea_battle.entities.User;
import com.example.sea_battle.repositories.GameRepository;
import com.example.sea_battle.repositories.GameBoardRepository;
import com.example.sea_battle.repositories.ShipRepository;
import com.example.sea_battle.repositories.ShotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    
    private final GameRepository gameRepository;
    private final GameBoardRepository gameBoardRepository;
    private final ShotRepository shotRepository;
    private final ShipRepository shipRepository;

    @Autowired
    public GameService(GameRepository gameRepository, 
                      GameBoardRepository gameBoardRepository,
                      ShotRepository shotRepository,
                      ShipRepository shipRepository) {
        this.gameRepository = gameRepository;
        this.gameBoardRepository = gameBoardRepository;
        this.shotRepository = shotRepository;
        this.shipRepository = shipRepository;
        logger.info("GameService инициализирован");
    }

    @Transactional
    public GameDTO setPlayerReady(Long gameId, String username) {
        logger.info("Установка готовности игрока: gameId={}, username={}", gameId, username);
        
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.error("Игра не найдена: gameId={}", gameId);
                    return new RuntimeException("Игра не найдена");
                });

        GameBoard playerBoard = game.getBoards().stream()
                .filter(board -> board.getPlayer().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Доска игрока не найдена: gameId={}, username={}", gameId, username);
                    return new RuntimeException("Доска игрока не найдена");
                });

        playerBoard.setReady(true);
        gameBoardRepository.save(playerBoard);
        logger.info("Игрок отмечен как готовый: gameId={}, username={}", gameId, username);

        // Проверяем, готовы ли все игроки
        boolean allPlayersReady = game.getBoards().stream()
                .allMatch(GameBoard::isReady);

        if (allPlayersReady) {
            // Начинаем игру
            logger.info("Все игроки готовы, начинаем игру: gameId={}", gameId);
            game.setStatus(Game.GameStatus.IN_PROGRESS);
            game.setCurrentPlayer(game.getBoards().get(0).getPlayer()); // Первый игрок ходит первым
            gameRepository.save(game);
            logger.info("Игра начата: gameId={}, первый ход: {}", gameId, game.getCurrentPlayer().getUsername());
        }

        return convertToDTO(game);
    }

    @Transactional
    public GameDTO makeShot(Long gameId, String username, ShotRequest shotRequest) {
        logger.info("Попытка выстрела: gameId={}, username={}, координаты=({},{})", 
                gameId, username, shotRequest.getX(), shotRequest.getY());
        
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.error("Игра не найдена: gameId={}", gameId);
                    return new RuntimeException("Игра не найдена");
                });

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            logger.error("Попытка сделать ход в игре, которая не находится в процессе: gameId={}, status={}", 
                    gameId, game.getStatus());
            throw new RuntimeException("Игра еще не началась");
        }

        if (!game.getCurrentPlayer().getUsername().equals(username)) {
            logger.error("Попытка хода не в свою очередь: gameId={}, username={}, currentPlayer={}", 
                    gameId, username, game.getCurrentPlayer().getUsername());
            throw new RuntimeException("Не ваш ход");
        }

        GameBoard opponentBoard = game.getBoards().stream()
                .filter(board -> !board.getPlayer().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Доска противника не найдена: gameId={}, username={}", gameId, username);
                    return new RuntimeException("Доска противника не найдена");
                });
                
        // Проверяем, не стреляли ли мы уже в эту клетку
        boolean alreadyShot = opponentBoard.getShots().stream()
                .anyMatch(s -> s.getX() == shotRequest.getX() && s.getY() == shotRequest.getY());
                
        if (alreadyShot) {
            logger.error("Попытка выстрелить в ту же клетку: gameId={}, coordinates=({},{})", 
                gameId, shotRequest.getX(), shotRequest.getY());
            throw new RuntimeException("Вы уже стреляли в эту клетку");
        }

        Shot shot = new Shot();
        shot.setBoard(opponentBoard);
        shot.setX(shotRequest.getX());
        shot.setY(shotRequest.getY());

        // Проверяем попадание
        boolean hit = opponentBoard.getShips().stream()
                .anyMatch(ship -> isShipHit(ship, shotRequest.getX(), shotRequest.getY()));

        if (hit) {
            shot.setResult(Shot.ShotResult.HIT);
            logger.info("Попадание: gameId={}, username={}, координаты=({},{})", 
                    gameId, username, shotRequest.getX(), shotRequest.getY());
                    
            // Проверяем, потоплен ли корабль
            Ship hitShip = opponentBoard.getShips().stream()
                    .filter(ship -> isShipHit(ship, shotRequest.getX(), shotRequest.getY()))
                    .findFirst()
                    .orElseThrow();

            if (isShipSunk(hitShip, opponentBoard.getShots())) {
                shot.setResult(Shot.ShotResult.SUNK);
                hitShip.setSunk(true);
                logger.info("Корабль потоплен: gameId={}, username={}, размер корабля={}", 
                        gameId, username, hitShip.getSize());
            }
        } else {
            shot.setResult(Shot.ShotResult.MISS);
            logger.info("Промах: gameId={}, username={}, координаты=({},{})", 
                    gameId, username, shotRequest.getX(), shotRequest.getY());
        }

        shotRepository.save(shot);
        opponentBoard.getShots().add(shot);

        // Проверяем, закончена ли игра
        if (isGameFinished(opponentBoard)) {
            game.setStatus(Game.GameStatus.FINISHED);
            logger.info("Игра завершена, победитель: {}, gameId={}", username, gameId);
        } else {
            // Передаем ход другому игроку
            game.setCurrentPlayer(opponentBoard.getPlayer());
            game.setTurnNumber(game.getTurnNumber() + 1);
            logger.info("Ход передан: gameId={}, новый текущий игрок={}, номер хода={}", 
                    gameId, game.getCurrentPlayer().getUsername(), game.getTurnNumber());
        }

        gameRepository.save(game);
        return convertToDTO(game);
    }

    @Transactional
    public GameDTO placeShip(Long gameId, String username, ShipPlacementRequest request) {
        logger.info("Запрос на размещение корабля: gameId={}, username={}, size={}, x={}, y={}, horizontal={}",
                gameId, username, request.getSize(), request.getX(), request.getY(), request.isHorizontal());
        
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.error("Игра не найдена: gameId={}", gameId);
                    return new RuntimeException("Игра не найдена");
                });

        // Проверяем, что игра в режиме ожидания
        if (game.getStatus() != Game.GameStatus.WAITING) {
            logger.error("Попытка разместить корабль когда игра не в режиме ожидания: gameId={}, status={}",
                    gameId, game.getStatus());
            throw new RuntimeException("Корабли можно размещать только в режиме ожидания");
        }
        
        GameBoard playerBoard = game.getBoards().stream()
                .filter(board -> board.getPlayer().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Доска игрока не найдена: gameId={}, username={}", gameId, username);
                    return new RuntimeException("Доска игрока не найдена");
                });
        
        // Проверяем, что доска не отмечена как готовая
        if (playerBoard.isReady()) {
            logger.error("Попытка разместить корабль на готовой доске: gameId={}, username={}", gameId, username);
            throw new RuntimeException("Нельзя изменять расположение кораблей после подтверждения готовности");
        }
        
        // Проверяем, что корабль имеет допустимый размер (1-4)
        if (request.getSize() < 1 || request.getSize() > 4) {
            logger.error("Недопустимый размер корабля: gameId={}, username={}, size={}", 
                    gameId, username, request.getSize());
            throw new RuntimeException("Недопустимый размер корабля (должен быть от 1 до 4)");
        }
        
        // Проверяем, что не превышено допустимое количество кораблей каждого типа
        int countShipsOfSize = (int) playerBoard.getShips().stream()
                .filter(ship -> ship.getSize() == request.getSize())
                .count();
        
        int maxShipsAllowed = getMaxShipsForSize(request.getSize());
        if (countShipsOfSize >= maxShipsAllowed) {
            logger.error("Превышено максимальное количество кораблей размера {}: gameId={}, username={}, current={}, max={}",
                    request.getSize(), gameId, username, countShipsOfSize, maxShipsAllowed);
            throw new RuntimeException("Превышено максимальное количество кораблей размера " + request.getSize());
        }
        
        // Создаем и добавляем корабль
        Ship ship = new Ship();
        ship.setX(request.getX());
        ship.setY(request.getY());
        ship.setSize(request.getSize());
        ship.setHorizontal(request.isHorizontal());
        ship.setBoard(playerBoard);
        
        // Проверяем возможность размещения корабля
        if (playerBoard.addShip(ship)) {
            shipRepository.save(ship);
            logger.info("Корабль успешно размещен: gameId={}, username={}, size={}, x={}, y={}, horizontal={}",
                    gameId, username, request.getSize(), request.getX(), request.getY(), request.isHorizontal());
        } else {
            logger.error("Невозможно разместить корабль в указанной позиции: gameId={}, username={}, size={}, x={}, y={}, horizontal={}",
                    gameId, username, request.getSize(), request.getX(), request.getY(), request.isHorizontal());
            throw new RuntimeException("Невозможно разместить корабль в указанной позиции");
        }
        
        gameBoardRepository.save(playerBoard);
        
        return convertToDTO(game);
    }

    /**
     * Возвращает максимальное количество кораблей каждого размера
     */
    private int getMaxShipsForSize(int size) {
        switch (size) {
            case 1: return 4; // 4 однопалубных
            case 2: return 3; // 3 двухпалубных
            case 3: return 2; // 2 трехпалубных
            case 4: return 1; // 1 четырехпалубный
            default: return 0;
        }
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
        dto.setCurrentPlayer(game.getCurrentPlayer() != null ? game.getCurrentPlayer().getUsername() : null);
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
        dto.setReady(board.isReady());

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

    @Transactional(readOnly = true)
    public GameStateResponse getGameState(String gameId, String username) {
        logger.info("Получение состояния игры: gameId={}, username={}", gameId, username);
        
        Game game = gameRepository.findById(Long.parseLong(gameId))
                .orElseThrow(() -> {
                    logger.error("Игра не найдена: gameId={}", gameId);
                    return new RuntimeException("Игра не найдена");
                });
                
        GameStateResponse response = new GameStateResponse();
        response.setGameId(gameId);
        response.setStatus(game.getStatus().toString());
        response.setCurrentPlayer(game.getCurrentPlayer() != null ? game.getCurrentPlayer().getUsername() : null);
        response.setTurnNumber(game.getTurnNumber());
        
        List<GameStateResponse.BoardInfo> boards = game.getBoards().stream()
                .map(board -> {
                    GameStateResponse.BoardInfo boardInfo = new GameStateResponse.BoardInfo();
                    boardInfo.setPlayerId(board.getPlayer().getId().toString());
                    boardInfo.setPlayerName(board.getPlayer().getUsername());
                    boardInfo.setReady(board.isReady());
                    
                    // Показываем корабли только владельцу доски
                    if (board.getPlayer().getUsername().equals(username)) {
                        boardInfo.setShips(board.getShips().stream()
                                .map(ship -> {
                                    GameStateResponse.ShipInfo shipInfo = new GameStateResponse.ShipInfo();
                                    shipInfo.setX(ship.getX());
                                    shipInfo.setY(ship.getY());
                                    shipInfo.setSize(ship.getSize());
                                    shipInfo.setHorizontal(ship.isHorizontal());
                                    shipInfo.setSunk(ship.isSunk());
                                    return shipInfo;
                                })
                                .collect(Collectors.toList()));
                    }
                    
                    boardInfo.setShots(board.getShots().stream()
                            .map(shot -> {
                                GameStateResponse.ShotInfo shotInfo = new GameStateResponse.ShotInfo();
                                shotInfo.setX(shot.getX());
                                shotInfo.setY(shot.getY());
                                shotInfo.setResult(shot.getResult().toString());
                                return shotInfo;
                            })
                            .collect(Collectors.toList()));
                            
                    return boardInfo;
                })
                .collect(Collectors.toList());
                
        response.setBoards(boards);
        return response;
    }

    @Transactional
    public GameStateResponse placeShips(String gameId, String username, List<PlaceShipsRequest.ShipInfo> ships) {
        logger.info("Размещение кораблей: gameId={}, username={}, количество кораблей={}", 
                gameId, username, ships.size());
                
        Game game = gameRepository.findById(Long.parseLong(gameId))
                .orElseThrow(() -> {
                    logger.error("Игра не найдена: gameId={}", gameId);
                    return new RuntimeException("Игра не найдена");
                });
                
        for (PlaceShipsRequest.ShipInfo shipInfo : ships) {
            ShipPlacementRequest request = new ShipPlacementRequest();
            request.setX(shipInfo.getX());
            request.setY(shipInfo.getY());
            request.setSize(shipInfo.getSize());
            request.setHorizontal(shipInfo.isHorizontal());
            placeShip(Long.parseLong(gameId), username, request);
        }
        
        return getGameState(gameId, username);
    }

    @Transactional
    public void surrender(String gameId, String username) {
        logger.info("Запрос на сдачу: gameId={}, username={}", gameId, username);
        
        Game game = gameRepository.findById(Long.parseLong(gameId))
                .orElseThrow(() -> {
                    logger.error("Игра не найдена: gameId={}", gameId);
                    return new RuntimeException("Игра не найдена");
                });
                
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            logger.error("Попытка сдаться в игре, которая не в процессе: gameId={}, status={}", 
                    gameId, game.getStatus());
            throw new RuntimeException("Нельзя сдаться в игре, которая не в процессе");
        }
        
        // Находим доску игрока, который сдается
        GameBoard playerBoard = game.getBoards().stream()
                .filter(board -> board.getPlayer().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Доска игрока не найдена: gameId={}, username={}", gameId, username);
                    return new RuntimeException("Доска игрока не найдена");
                });
                
        // Помечаем все корабли игрока как потопленные
        playerBoard.getShips().forEach(ship -> ship.setSunk(true));
        
        // Завершаем игру
        game.setStatus(Game.GameStatus.FINISHED);
        
        // Определяем победителя (противника сдавшегося игрока)
        User winner = game.getBoards().stream()
                .map(GameBoard::getPlayer)
                .filter(player -> !player.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
                
        game.setCurrentPlayer(winner);
        gameRepository.save(game);
        
        logger.info("Игрок сдался: gameId={}, username={}, победитель={}", 
                gameId, username, winner.getUsername());
    }

    @Transactional(readOnly = true)
    public CurrentGameResponse getCurrentGame(String username) {
        logger.info("Получение текущей игры для пользователя: username={}", username);
        
        // Ищем игру, где игрок участвует и статус IN_PROGRESS
        Optional<Game> gameOpt = gameRepository.findByBoardsPlayerUsernameAndStatus(
                username, Game.GameStatus.IN_PROGRESS);
                
        if (gameOpt.isEmpty()) {
            logger.info("Активная игра не найдена для пользователя: username={}", username);
            return null;
        }
        
        Game game = gameOpt.get();
        CurrentGameResponse response = new CurrentGameResponse();
        response.setGameId(game.getId().toString());
        response.setLobbyId(game.getLobby().getLobbyID());
        response.setLobbyName(game.getLobby().getLobbyName());
        response.setStatus(game.getStatus().toString());
        
        // Находим противника
        String opponentName = game.getBoards().stream()
                .map(board -> board.getPlayer().getUsername())
                .filter(playerName -> !playerName.equals(username))
                .findFirst()
                .orElse(null);
                
        response.setOpponentName(opponentName);
        response.setYourTurn(game.getCurrentPlayer() != null && 
                game.getCurrentPlayer().getUsername().equals(username));
                
        return response;
    }
} 