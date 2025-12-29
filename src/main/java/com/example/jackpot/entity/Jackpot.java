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
 * Jackpot entity representing a jackpot in the system.
 * Each jackpot has a win probability and tracks its current size and win count.
 */
@Entity
@Table(name = "jackpots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jackpot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double winProbability;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal currentSize = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer winCount = 0;

    @Column
    private LocalDateTime lastWinTimestamp;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
