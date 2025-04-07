package com.example.sea_battle.entities.schemas;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String lobbyID;
    
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User lobbyOwner;
    
    private String lobbyName;
    
    private String password;
    
    private boolean isPrivate;
    
    @Enumerated(EnumType.STRING)
    private LobbyStatus status = LobbyStatus.WAITING;
    
    private int maxPlayers = 2;
    
    @ManyToMany
    @JoinTable(
        name = "lobby_players",
        joinColumns = @JoinColumn(name = "lobby_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> players = new HashSet<>();
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean canJoin() {
        return status == LobbyStatus.WAITING && players.size() < maxPlayers;
    }
    
    public enum LobbyStatus {
        WAITING,
        FULL,
        IN_GAME,
        FINISHED
    }
}
