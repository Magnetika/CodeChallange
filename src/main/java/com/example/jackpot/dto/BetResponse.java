package com.example.jackpot.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bet response.
 * Contains bet result and jackpot information after the bet.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response after placing a bet")
public class BetResponse {

    @Schema(description = "Whether the jackpot was won", example = "false")
    private Boolean won;

    @Schema(description = "Win amount (0 if not won)", example = "0.00")
    private BigDecimal winAmount;

    @Schema(description = "New jackpot size after the bet", example = "200.00")
    private BigDecimal newJackpotSize;

    @Schema(description = "Message about the bet result", example = "Better luck next time!")
    private String message;
}
