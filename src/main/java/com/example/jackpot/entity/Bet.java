package com.example.jackpot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bet entity representing a player's bet on a jackpot.
 * Every bet is recorded in the database for auditing and replay purposes.
 */
@Entity
@Table(name = "bets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jackpot_id", nullable = false)
    private Jackpot jackpot;

    @Column(nullable = false)
    private String playerAlias;

    @Column(nullable = false)
    private BigDecimal betAmount;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
