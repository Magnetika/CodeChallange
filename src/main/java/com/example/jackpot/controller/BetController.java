package com.example.jackpot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.jackpot.dto.BetRequest;
import com.example.jackpot.dto.BetResponse;
import com.example.jackpot.service.BetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Bet operations.
 * Provides endpoints for placing bets on jackpots.
 */
@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
@Tag(name = "Bets", description = "Bet placement endpoints")
public class BetController {

    private final BetService betService;

    /**
     * Place a bet on a jackpot.
     *
     * @param request contains jackpot ID, player alias, and bet amount
     * @return bet response with win information
     */
    @PostMapping
    @Operation(summary = "Place a bet", description = "Places a bet on a jackpot and determines if the jackpot is won")
    @ApiResponse(responseCode = "200", description = "Bet placed successfully",
            content = @Content(schema = @Schema(implementation = BetResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid bet request")
    @ApiResponse(responseCode = "404", description = "Jackpot not found")
    public ResponseEntity<BetResponse> placeBet(@Valid @RequestBody BetRequest request) {
        BetResponse response = betService.placeBet(request);
        return ResponseEntity.ok(response);
    }
}
