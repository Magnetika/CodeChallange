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
 * Win entity representing a successful jackpot win.
 * Records the winner, the amount won, and the timestamp of the win.
 */
@Entity
@Table(name = "wins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Win {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jackpot_id", nullable = false)
    private Jackpot jackpot;

    @Column(nullable = false)
    private String playerAlias;

    @Column(nullable = false)
    private BigDecimal winAmount;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
