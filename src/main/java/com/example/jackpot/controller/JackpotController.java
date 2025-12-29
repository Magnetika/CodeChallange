package com.example.jackpot.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jackpot.dto.CreateJackpotRequest;
import com.example.jackpot.dto.JackpotDto;
import com.example.jackpot.service.JackpotService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Jackpot management.
 * Provides endpoints for creating and retrieving jackpots.
 */
@RestController
@RequestMapping("/api/jackpots")
@RequiredArgsConstructor
@Tag(name = "Jackpots", description = "Jackpot management endpoints")
public class JackpotController {

    private final JackpotService jackpotService;

    /**
     * Create a new jackpot.
     *
     * @param request contains jackpot name and win probability
     * @return created jackpot with 201 status
     */
    @PostMapping
    @Operation(summary = "Create a new jackpot", description = "Creates a new jackpot with specified name and win probability")
    @ApiResponse(responseCode = "201", description = "Jackpot created successfully",
            content = @Content(schema = @Schema(implementation = JackpotDto.class)))
    public ResponseEntity<JackpotDto> createJackpot(@RequestBody CreateJackpotRequest request) {
        JackpotDto jackpot = jackpotService.createJackpot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(jackpot);
    }

    /**
     * Get all jackpots.
     *
     * @return list of all jackpots
     */
    @GetMapping
    @Operation(summary = "Get all jackpots", description = "Retrieves all available jackpots with their current state")
    @ApiResponse(responseCode = "200", description = "List of jackpots retrieved successfully",
            content = @Content(schema = @Schema(implementation = JackpotDto.class)))
    public ResponseEntity<List<JackpotDto>> getAllJackpots() {
        List<JackpotDto> jackpots = jackpotService.getAllJackpots();
        return ResponseEntity.ok(jackpots);
    }

    /**
     * Get a specific jackpot by ID.
     *
     * @param jackpotId the jackpot ID
     * @return jackpot with the specified ID
     */
    @GetMapping("/{jackpotId}")
    @Operation(summary = "Get jackpot by ID", description = "Retrieves a specific jackpot by its ID")
    @ApiResponse(responseCode = "200", description = "Jackpot retrieved successfully",
            content = @Content(schema = @Schema(implementation = JackpotDto.class)))
    @ApiResponse(responseCode = "404", description = "Jackpot not found")
    public ResponseEntity<JackpotDto> getJackpot(@PathVariable UUID jackpotId) {
        JackpotDto jackpot = jackpotService.getJackpotById(jackpotId);
        return ResponseEntity.ok(jackpot);
    }
}
