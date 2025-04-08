package com.example.sea_battle.services.sse;

import com.example.sea_battle.dto.LobbyDTO;
import com.example.sea_battle.events.LobbyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SseEmitterService {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private final Map<String, SseEmitter> lobbyEmitters = new ConcurrentHashMap<>();
    private final Set<String> brokenConnections = new CopyOnWriteArraySet<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public SseEmitter createEmitter(String userId) {
        logger.info("Создание нового SSE эмиттера для пользователя: {}", userId);
        
        // Удаляем старый эмиттер, если он существует
        removeEmitter(userId);
        
        // Удаляем из списка проблемных соединений, если там был
        brokenConnections.remove(userId);
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        emitter.onCompletion(() -> {
            logger.info("SSE соединение завершено для пользователя: {}", userId);
            lobbyEmitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            logger.info("SSE соединение истекло для пользователя: {}", userId);
            lobbyEmitters.remove(userId);
            brokenConnections.add(userId);
        });
        
        emitter.onError(e -> {
            logger.error("Ошибка SSE соединения для пользователя {}: {}", userId, e.getMessage());
            lobbyEmitters.remove(userId);
            brokenConnections.add(userId);
        });
        
        // Сначала сохраняем эмиттер в коллекцию
        lobbyEmitters.put(userId, emitter);
        logger.info("SSE эмиттер успешно создан для пользователя: {}", userId);
        
        // Затем пытаемся отправить начальное событие
        try {
            emitter.send(SseEmitter.event()
                .name("init")
                .data("Connected successfully"));
        } catch (IOException e) {
            logger.error("Ошибка при отправке начального события: {}", e.getMessage());
            // Не вызываем completeWithError, так как это может привести к ошибкам
            // Обработчик onError позаботится об очистке ресурсов
            brokenConnections.add(userId);
            return emitter; // Возвращаем эмиттер даже при ошибке инициализации
        }
        
        // Отправляем пинг каждые 15 секунд для поддержания соединения
        executorService.execute(() -> {
            int count = 0;
            while (hasEmitter(userId) && count < 120) { // Максимум 30 минут (120 * 15 секунд)
                try {
                    Thread.sleep(15000); // 15 секунд
                    if (hasEmitter(userId)) {
                        SseEmitter currentEmitter = lobbyEmitters.get(userId);
                        if (currentEmitter != null) {
                            try {
                                currentEmitter.send(SseEmitter.event()
                                    .name("ping")
                                    .data("ping"));
                                logger.debug("Отправлен пинг пользователю: {}", userId);
                            } catch (Exception e) {
                                logger.error("Ошибка при отправке пинга для пользователя {}: {}", userId, e.getMessage());
                                break; // Выходим из цикла при ошибке отправки
                            }
                        }
                    } else {
                        break;
                    }
                    count++;
                } catch (Exception e) {
                    logger.error("Ошибка в цикле поддержания соединения для пользователя {}: {}", userId, e.getMessage());
                    break;
                }
            }
            
            // Если соединение все еще активно после 30 минут, завершаем его
            if (hasEmitter(userId)) {
                logger.info("Завершение SSE соединения по таймауту для пользователя: {}", userId);
                removeEmitter(userId);
            }
        });
        
        return emitter;
    }

    public void sendLobbyUpdateToAll(LobbyEvent event) {
        logger.debug("Отправка обновления лобби всем пользователям");
        
        lobbyEmitters.forEach((userId, emitter) -> {
            if (brokenConnections.contains(userId)) {
                logger.debug("Пропуск отправки для пользователя с проблемным соединением: {}", userId);
                return;
            }
            
            try {
                emitter.send(SseEmitter.event()
                    .name("lobby_update")
                    .data(event));
                logger.debug("Обновление лобби отправлено пользователю: {}", userId);
            } catch (IOException e) {
                logger.error("Ошибка при отправке обновления лобби пользователю {}: {}", userId, e.getMessage());
                removeEmitter(userId);
                brokenConnections.add(userId);
            }
        });
    }
    
    public void sendLobbyListToAll(List<LobbyDTO> lobbies) {
        logger.debug("Отправка списка лобби всем пользователям");
        
        lobbyEmitters.forEach((userId, emitter) -> {
            if (brokenConnections.contains(userId)) {
                logger.debug("Пропуск отправки для пользователя с проблемным соединением: {}", userId);
                return;
            }
            
            try {
                emitter.send(SseEmitter.event()
                    .name("lobby_list")
                    .data(lobbies));
                logger.debug("Список лобби отправлен пользователю: {}", userId);
            } catch (IOException e) {
                logger.error("Ошибка при отправке списка лобби пользователю {}: {}", userId, e.getMessage());
                removeEmitter(userId);
                brokenConnections.add(userId);
            }
        });
    }

    public void sendEventToUser(String userId, LobbyEvent event) {
        logger.debug("Отправка события пользователю: {}", userId);
        
        if (brokenConnections.contains(userId)) {
            logger.debug("Пропуск отправки для пользователя с проблемным соединением: {}", userId);
            return;
        }
        
        SseEmitter emitter = lobbyEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("lobby_update")
                    .data(event));
                logger.debug("Событие успешно отправлено пользователю: {}", userId);
            } catch (IOException e) {
                logger.error("Ошибка при отправке события пользователю {}: {}", userId, e.getMessage());
                removeEmitter(userId);
                brokenConnections.add(userId);
            }
        } else {
            logger.warn("Эмиттер не найден для пользователя: {}", userId);
        }
    }

    public void removeEmitter(String userId) {
        logger.debug("Удаление эмиттера для пользователя: {}", userId);
        
        SseEmitter emitter = lobbyEmitters.remove(userId);
        if (emitter != null) {
            try {
                // Не пытаемся отправлять данные перед закрытием соединения
                // Просто завершаем соединение
                emitter.complete();
                logger.info("Эмиттер успешно удален для пользователя: {}", userId);
            } catch (Exception e) {
                logger.error("Ошибка при закрытии эмиттера для пользователя {}: {}", userId, e.getMessage());
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    // Игнорируем ошибки при закрытии соединения с ошибкой
                    logger.debug("Не удалось завершить соединение с ошибкой для пользователя {}: {}", userId, ex.getMessage());
                }
            }
        }
    }

    public boolean hasEmitter(String userId) {
        return lobbyEmitters.containsKey(userId) && !brokenConnections.contains(userId);
    }
    
    public int getActiveEmittersCount() {
        return (int) lobbyEmitters.keySet().stream()
            .filter(userId -> !brokenConnections.contains(userId))
            .count();
    }
    
    // Очистка ресурсов при остановке приложения
    public void shutdown() {
        logger.info("Завершение работы SseEmitterService, закрытие всех соединений");
        closeAllConnections();
        executorService.shutdown();
    }
    
    // Закрыть все соединения
    private void closeAllConnections() {
        lobbyEmitters.forEach((userId, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                logger.error("Ошибка при закрытии соединения для пользователя {}: {}", userId, e.getMessage());
            }
        });
        lobbyEmitters.clear();
        brokenConnections.clear();
    }
} 