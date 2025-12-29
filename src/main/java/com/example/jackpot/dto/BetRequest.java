package com.example.jackpot.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for placing a bet request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to place a bet on a jackpot")
public class BetRequest {

    @Schema(description = "Jackpot ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID jackpotId;

    @Schema(description = "Player alias", example = "player123")
    private String playerAlias;

    @Schema(description = "Bet amount", example = "50.00")
    private BigDecimal betAmount;
}
