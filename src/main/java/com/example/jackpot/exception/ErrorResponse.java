package com.example.jackpot.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO for API errors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Jackpot not found")
    private String message;

    @Schema(description = "Error type", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Timestamp of the error", example = "2025-12-28T10:30:00")
    private String timestamp;
}
