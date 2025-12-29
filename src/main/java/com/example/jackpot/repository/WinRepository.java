package com.example.jackpot.repository;

import com.example.jackpot.entity.Win;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Win entity.
 * Provides database operations for wins with pagination and filtering support.
 */
@Repository
public interface WinRepository extends JpaRepository<Win, UUID> {
    /**
     * Find all wins with pagination support.
     *
     * @param pageable pagination information
     * @return page of wins
     */
    @Override
    Page<Win> findAll(Pageable pageable);

    /**
     * Find wins with optional filtering by player alias and/or jackpot ID.
     *
     * @param playerAlias optional player alias filter
     * @param jackpotId optional jackpot ID filter
     * @param pageable pagination information
     * @return page of wins matching the filters
     */
    @Query("SELECT w FROM Win w WHERE " +
           "(:playerAlias IS NULL OR w.playerAlias = :playerAlias) AND " +
           "(:jackpotId IS NULL OR w.jackpot.id = :jackpotId)")
    Page<Win> findWithFilters(
        @Param("playerAlias") String playerAlias,
        @Param("jackpotId") UUID jackpotId,
        Pageable pageable
    );
}
