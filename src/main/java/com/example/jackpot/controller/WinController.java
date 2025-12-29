package com.example.jackpot.controller;

import com.example.jackpot.dto.WinDto;
import com.example.jackpot.service.WinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Win records.
 * Provides endpoints for retrieving win history.
 */
@RestController
@RequestMapping("/api/wins")
@RequiredArgsConstructor
@Tag(name = "Wins", description = "Win history endpoints")
public class WinController {

    private final WinService winService;
    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;

    /**
     * Get wins with pagination and filtering support.
     *
     * @param limit maximum number of wins to return (default: 10)
     * @param offset page offset (0-based, default: 0)
     * @param playerAlias optional filter by player alias
     * @param jackpotId optional filter by jackpot ID
     * @return list of wins
     */
    @GetMapping
    @Operation(summary = "Get wins", description = "Retrieves all recorded wins with pagination and filtering support")
    @ApiResponse(responseCode = "200", description = "Wins retrieved successfully",
            content = @Content(schema = @Schema(implementation = WinDto.class)))
    public ResponseEntity<List<WinDto>> getWins(
            @RequestParam(value = "limit", defaultValue = DEFAULT_LIMIT + "") int limit,
            @RequestParam(value = "offset", defaultValue = DEFAULT_OFFSET + "") int offset,
            @RequestParam(value = "playerAlias", required = false) String playerAlias,
            @RequestParam(value = "jackpotId", required = false) UUID jackpotId) {
        
        // Validate parameters
        if (limit <= 0) limit = DEFAULT_LIMIT;
        if (offset < 0) offset = DEFAULT_OFFSET;

        List<WinDto> wins = winService.getWins(limit, offset, playerAlias, jackpotId);
        return ResponseEntity.ok(wins);
    }
}
