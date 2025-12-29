package com.example.jackpot.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jackpot.entity.Jackpot;

/**
 * Repository interface for Jackpot entity.
 * Provides database operations for jackpots.
 */
@Repository
public interface JackpotRepository extends JpaRepository<Jackpot, UUID> {
}
