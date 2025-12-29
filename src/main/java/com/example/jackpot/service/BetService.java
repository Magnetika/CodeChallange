package com.example.jackpot.service;

import com.example.jackpot.dto.BetRequest;
import com.example.jackpot.dto.BetResponse;
import com.example.jackpot.entity.Bet;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.Win;
import com.example.jackpot.repository.BetRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.WinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service layer for Bet operations.
 * Handles business logic related to placing bets and determining wins.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BetService {

    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final WinRepository winRepository;
    private final Random random = new Random();

    /**
     * Place a bet on a jackpot.
     *
     * @param request contains jackpot ID, player alias, and bet amount
     * @return response containing win information and new jackpot size
     * @throws IllegalArgumentException if jackpot not found or invalid bet
     */
    public BetResponse placeBet(BetRequest request) {
        // Validate input
        if (request.getBetAmount() == null || request.getBetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }

        // Get jackpot
        Jackpot jackpot = jackpotRepository.findById(request.getJackpotId())
                .orElseThrow(() -> new IllegalArgumentException("Jackpot not found with ID: " + request.getJackpotId()));

        // Create and save bet
        Bet bet = Bet.builder()
                .jackpot(jackpot)
                .playerAlias(request.getPlayerAlias())
                .betAmount(request.getBetAmount())
                .build();
        betRepository.save(bet);

        // Add bet amount to jackpot
        BigDecimal newSize = jackpot.getCurrentSize().add(request.getBetAmount());
        jackpot.setCurrentSize(newSize);

        // Determine if jackpot is won
        boolean isWon = determineWin(jackpot.getWinProbability());

        BetResponse response = new BetResponse();
        response.setWon(isWon);
        response.setNewJackpotSize(newSize);

        if (isWon) {
            // Create win record
            Win win = Win.builder()
                    .jackpot(jackpot)
                    .playerAlias(request.getPlayerAlias())
                    .winAmount(newSize)
                    .build();
            winRepository.save(win);

            // Update jackpot
            jackpot.setCurrentSize(BigDecimal.ZERO);
            jackpot.setWinCount(jackpot.getWinCount() + 1);
            jackpot.setLastWinTimestamp(LocalDateTime.now());

            response.setWinAmount(newSize);
            response.setMessage("Congratulations! You won!");
        } else {
            response.setWinAmount(BigDecimal.ZERO);
            response.setMessage("Better luck next time!");
        }

        // Save updated jackpot
        jackpotRepository.save(jackpot);

        return response;
    }

    /**
     * Determine if a jackpot is won based on win probability.
     *
     * @param winProbability probability of winning (0.0 - 1.0)
     * @return true if jackpot is won, false otherwise
     */
    private boolean determineWin(Double winProbability) {
        return random.nextDouble() < winProbability;
    }
}
