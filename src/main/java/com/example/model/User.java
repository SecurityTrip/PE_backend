package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "avatar_id")
    private Long avatarId;
    
    public User() {}
    
    public User(String username, String password, Long avatarId) {
        this.username = username;
        this.password = password;
        this.avatarId = avatarId;
    }
} 