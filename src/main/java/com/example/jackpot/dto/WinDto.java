package com.example.jackpot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Win response.
 * Contains win information for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Win information")
public class WinDto {

    @Schema(description = "Win timestamp", example = "2025-12-28T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Player alias", example = "player123")
    private String playerAlias;

    @Schema(description = "Win amount", example = "200.00")
    private BigDecimal winAmount;
}
