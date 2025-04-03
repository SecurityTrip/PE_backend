package com.example.sea_battle.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Matches {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long winner;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public long getWinner() {
        return winner;
    }

    public void setWinner(long winner) {
        this.winner = winner;
    }
}
