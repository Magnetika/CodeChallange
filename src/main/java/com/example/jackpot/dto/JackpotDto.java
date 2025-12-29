package com.example.jackpot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Jackpot response.
 * Contains jackpot information for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Jackpot information")
public class JackpotDto {

    @Schema(description = "Unique jackpot identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Jackpot name", example = "Super Jackpot")
    private String name;

    @Schema(description = "Current jackpot size", example = "150.50")
    private BigDecimal currentSize;

    @Schema(description = "Total number of wins", example = "2")
    private Integer winCount;

    @Schema(description = "Timestamp of last win", example = "2025-12-28T10:30:00")
    private LocalDateTime lastWinTimestamp;
}
