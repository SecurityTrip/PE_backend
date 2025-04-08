package com.example.sea_battle.repositories;

import com.example.sea_battle.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByLobbyId(Long lobbyId);
} 