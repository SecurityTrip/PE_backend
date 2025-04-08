package com.example.sea_battle.services;

import com.example.sea_battle.dto.LobbyDTO;
import com.example.sea_battle.entities.Game;
import com.example.sea_battle.entities.GameBoard;
import com.example.sea_battle.entities.Lobby;
import com.example.sea_battle.entities.User;
import com.example.sea_battle.repositories.GameRepository;
import com.example.sea_battle.repositories.LobbyRepository;
import com.example.sea_battle.repositories.UserRepository;
import com.example.sea_battle.services.sse.LobbyEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LobbyService {
    private static final Logger logger = LoggerFactory.getLogger(LobbyService.class);
    
    private final LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final LobbyEventService lobbyEventService;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, 
                       UserRepository userRepository,
                       GameRepository gameRepository,
                       LobbyEventService lobbyEventService) {
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.lobbyEventService = lobbyEventService;
        logger.info("LobbyService инициализирован");
    }

    // Создание нового лобби
    @Transactional
    public LobbyDTO createLobby(String ownerUsername, String lobbyName, boolean isPrivate, String password) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Lobby lobby = new Lobby();
        lobby.setLobbyID(generateUniqueLobbyId());
        lobby.setLobbyName(lobbyName);
        lobby.setLobbyOwner(owner);
        lobby.setPrivate(isPrivate);
        lobby.setPassword(password);
        lobby.getPlayers().add(owner);

        Lobby savedLobby = lobbyRepository.save(lobby);
        LobbyDTO lobbyDTO = convertToDTO(savedLobby);
        
        // Отправляем уведомление о создании лобби всем подписанным клиентам
        lobbyEventService.notifyLobbyCreated(lobbyDTO);
        
        return lobbyDTO;
    }

    // Получение списка доступных публичных лобби
    @Transactional(readOnly = true)
    public List<LobbyDTO> getPublicLobbies() {
        return lobbyRepository.findByIsPrivateFalseAndStatus(Lobby.LobbyStatus.WAITING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получение лобби по ID
    @Transactional(readOnly = true)
    public Optional<LobbyDTO> getLobbyById(String lobbyId) {
        return lobbyRepository.findByLobbyID(lobbyId)
                .map(this::convertToDTO);
    }

    // Присоединение к лобби
    @Transactional
    public LobbyDTO joinLobby(String lobbyId, String username, String password) {
        Lobby lobby = lobbyRepository.findByLobbyID(lobbyId)
                .orElseThrow(() -> new RuntimeException("Лобби не найдено"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка, может ли пользователь присоединиться
        if (!lobby.canJoin()) {
            throw new RuntimeException("Невозможно присоединиться к лобби. Лобби заполнено или игра уже началась.");
        }

        // Проверка пароля для приватных лобби
        if (lobby.isPrivate() && (password == null || !password.equals(lobby.getPassword()))) {
            throw new RuntimeException("Неверный пароль для приватного лобби");
        }

        // Добавляем игрока в лобби
        lobby.getPlayers().add(user);
        
        // Обновляем статус лобби, если оно заполнено
        if (lobby.getPlayers().size() >= lobby.getMaxPlayers()) {
            lobby.setStatus(Lobby.LobbyStatus.FULL);
        }

        Lobby updatedLobby = lobbyRepository.save(lobby);
        LobbyDTO lobbyDTO = convertToDTO(updatedLobby);
        
        // Отправляем уведомление об обновлении лобби всем подписанным клиентам
        lobbyEventService.notifyLobbyUpdated(lobbyDTO);
        
        return lobbyDTO;
    }

    // Выход из лобби
    @Transactional
    public void leaveLobby(String lobbyId, String username) {
        Lobby lobby = lobbyRepository.findByLobbyID(lobbyId)
                .orElseThrow(() -> new RuntimeException("Лобби не найдено"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем, находится ли пользователь в лобби
        if (!lobby.getPlayers().contains(user)) {
            throw new RuntimeException("Пользователь не находится в этом лобби");
        }

        // Удаляем игрока из лобби
        lobby.getPlayers().remove(user);

        // Если это владелец лобби, то удаляем лобби или назначаем нового владельца
        if (lobby.getLobbyOwner().equals(user)) {
            if (lobby.getPlayers().isEmpty()) {
                LobbyDTO lobbyDTO = convertToDTO(lobby);
                lobbyRepository.delete(lobby);
                
                // Отправляем уведомление об удалении лобби всем подписанным клиентам
                lobbyEventService.notifyLobbyDeleted(lobbyDTO);
                return;
            } else {
                // Назначаем новым владельцем первого игрока из оставшихся
                User newOwner = lobby.getPlayers().iterator().next();
                lobby.setLobbyOwner(newOwner);
            }
        }

        // Обновляем статус лобби
        if (lobby.getStatus() == Lobby.LobbyStatus.FULL) {
            lobby.setStatus(Lobby.LobbyStatus.WAITING);
        }

        Lobby updatedLobby = lobbyRepository.save(lobby);
        
        // Отправляем уведомление об обновлении лобби всем подписанным клиентам
        lobbyEventService.notifyLobbyUpdated(convertToDTO(updatedLobby));
    }

    // Запуск игры из лобби
    @Transactional
    public LobbyDTO startGame(String lobbyId, String ownerUsername) {
        logger.info("Запрос на начало игры: lobbyId={}, ownerUsername={}", lobbyId, ownerUsername);
        
        Lobby lobby = lobbyRepository.findByLobbyID(lobbyId)
                .orElseThrow(() -> {
                    logger.error("Лобби не найдено: lobbyId={}", lobbyId);
                    return new RuntimeException("Лобби не найдено");
                });

        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> {
                    logger.error("Пользователь не найден: username={}", ownerUsername);
                    return new RuntimeException("Пользователь не найден");
                });

        // Проверяем, является ли пользователь владельцем лобби
        if (!lobby.getLobbyOwner().equals(owner)) {
            logger.error("Попытка начать игру не владельцем лобби: lobbyId={}, username={}", lobbyId, ownerUsername);
            throw new RuntimeException("Только владелец лобби может начать игру");
        }

        // Проверяем количество игроков (минимум 2 для игры)
        if (lobby.getPlayers().size() < 2) {
            logger.error("Недостаточно игроков для начала игры: lobbyId={}, playersCount={}", 
                    lobbyId, lobby.getPlayers().size());
            throw new RuntimeException("Для начала игры необходимо минимум 2 игрока");
        }

        // Создаем новую игру
        logger.info("Создание новой игры для лобби: lobbyId={}", lobbyId);
        Game game = new Game();
        game.setLobby(lobby);
        game.setStatus(Game.GameStatus.WAITING); // Начальный статус - ожидание готовности игроков

        // Создаем игровые доски для каждого игрока
        int boardCounter = 0;
        for (User player : lobby.getPlayers()) {
            GameBoard board = new GameBoard();
            board.setGame(game);
            board.setPlayer(player);
            board.setReady(false); // Изначально игроки не готовы
            game.getBoards().add(board);
            boardCounter++;
            logger.info("Создана игровая доска для игрока: lobbyId={}, username={}, boardNumber={}", 
                    lobbyId, player.getUsername(), boardCounter);
        }

        Game savedGame = gameRepository.save(game);
        logger.info("Игра создана: lobbyId={}, gameId={}", lobbyId, savedGame.getId());

        // Устанавливаем статус IN_GAME для лобби
        lobby.setStatus(Lobby.LobbyStatus.IN_GAME);
        Lobby updatedLobby = lobbyRepository.save(lobby);
        logger.info("Лобби переведено в статус IN_GAME: lobbyId={}", lobbyId);
        
        LobbyDTO lobbyDTO = convertToDTO(updatedLobby);
        
        // Отправляем уведомление об обновлении лобби всем подписанным клиентам
        lobbyEventService.notifyLobbyUpdated(lobbyDTO);
        
        return lobbyDTO;
    }

    // Получение списка лобби, в которых участвует игрок
    @Transactional(readOnly = true)
    public List<LobbyDTO> getPlayerLobbies(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return lobbyRepository.findByPlayersContaining(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Генерация уникального ID для лобби
    private String generateUniqueLobbyId() {
        String lobbyId;
        do {
            lobbyId = UUID.randomUUID().toString().substring(0, 8);
        } while (lobbyRepository.existsByLobbyID(lobbyId));
        
        return lobbyId;
    }

    // Преобразование Lobby в LobbyDTO
    private LobbyDTO convertToDTO(Lobby lobby) {
        LobbyDTO dto = new LobbyDTO();
        dto.setId(lobby.getId());
        dto.setLobbyID(lobby.getLobbyID());
        dto.setLobbyName(lobby.getLobbyName());
        dto.setOwnerUsername(lobby.getLobbyOwner().getUsername());
        dto.setPrivate(lobby.isPrivate());
        dto.setStatus(lobby.getStatus().toString());
        dto.setCurrentPlayers(lobby.getPlayers().size());
        dto.setMaxPlayers(lobby.getMaxPlayers());
        dto.setPlayers(lobby.getPlayers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList()));
        dto.setCreatedAt(lobby.getCreatedAt());
        dto.setGameId(lobby.getGame() != null ? lobby.getGame().getId().toString() : null);
        
        return dto;
    }
}
