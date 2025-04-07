package com.example.sea_battle.repositories;

import com.example.sea_battle.entities.schemas.Lobby;
import com.example.sea_battle.entities.schemas.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    Optional<Lobby> findByLobbyID(String lobbyID);
    List<Lobby> findByStatus(Lobby.LobbyStatus status);
    List<Lobby> findByLobbyOwner(User owner);
    List<Lobby> findByPlayersContaining(User player);
    List<Lobby> findByIsPrivateFalseAndStatus(Lobby.LobbyStatus status);
    boolean existsByLobbyID(String lobbyID);
}
