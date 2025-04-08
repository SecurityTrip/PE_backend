package com.example.sea_battle.repositories;

import com.example.sea_battle.entities.Shot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShotRepository extends JpaRepository<Shot, Long> {
} 