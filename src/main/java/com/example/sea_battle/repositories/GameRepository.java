package com.example.sea_battle.repositories;

import com.example.sea_battle.entities.Game;
import com.example.sea_battle.entities.Game.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByLobbyId(Long lobbyId);
    Optional<Game> findByBoardsPlayerUsernameAndStatus(String username, GameStatus status);
} 