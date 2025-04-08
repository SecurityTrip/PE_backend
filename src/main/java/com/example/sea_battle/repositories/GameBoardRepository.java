package com.example.sea_battle.repositories;

import com.example.sea_battle.entities.GameBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameBoardRepository extends JpaRepository<GameBoard, Long> {
} 