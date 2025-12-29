package com.example.jackpot.repository;

import com.example.jackpot.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Bet entity.
 * Provides database operations for bets.
 */
@Repository
public interface BetRepository extends JpaRepository<Bet, UUID> {
}
