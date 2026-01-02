package com.example.jackpot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "name is required")
    private String name;

    @Schema(description = "Win probability (0.0 - 1.0)", example = "0.1")
    @NotNull(message = "winProbability is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "winProbability must be >= 0.0")
    @DecimalMax(value = "1.0", inclusive = true, message = "winProbability must be <= 1.0")
    private Double winProbability;
}
