package com.example.jackpot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a jackpot request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new jackpot")
public class CreateJackpotRequest {

    @Schema(description = "Jackpot name", example = "Super Jackpot")
    private String name;

    @Schema(description = "Win probability (0.0 - 1.0)", example = "0.1")
    private Double winProbability;
}
