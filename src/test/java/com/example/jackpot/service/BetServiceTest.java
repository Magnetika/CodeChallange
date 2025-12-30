package com.example.jackpot.service;

import com.example.jackpot.dto.BetRequest;
import com.example.jackpot.dto.BetResponse;
import com.example.jackpot.entity.Bet;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.Win;
import com.example.jackpot.repository.BetRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.WinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BetService.
 * Tests the core business logic of bet placement and jackpot management.
 */
@ExtendWith(MockitoExtension.class)
class BetServiceTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private WinRepository winRepository;

    @InjectMocks
    private BetService betService;

    private UUID jackpotId;
    private Jackpot testJackpot;
    private BetRequest betRequest;

    @BeforeEach
    void setUp() {
        jackpotId = UUID.randomUUID();

        // Create test jackpot
        testJackpot = Jackpot.builder()
                .id(jackpotId)
                .name("Test Jackpot")
                .currentSize(BigDecimal.valueOf(100))
                .winProbability(0.5)
                .winCount(0)
                .lastWinTimestamp(null)
                .build();

        // Create test bet request
        betRequest = new BetRequest();
        betRequest.setJackpotId(jackpotId);
        betRequest.setPlayerAlias("TestPlayer");
        betRequest.setBetAmount(BigDecimal.valueOf(50));
    }

    // ========== Validation Tests ==========

    @Test
    void testPlaceBet_WithNegativeBetAmount_ShouldThrowException() {
        betRequest.setBetAmount(BigDecimal.valueOf(-10));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            betService.placeBet(betRequest);
        });

        assertEquals("Bet amount must be positive", exception.getMessage());
        verify(betRepository, never()).save(any());
    }

    @Test
    void testPlaceBet_WithZeroBetAmount_ShouldThrowException() {
        betRequest.setBetAmount(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            betService.placeBet(betRequest);
        });

        assertEquals("Bet amount must be positive", exception.getMessage());
        verify(betRepository, never()).save(any());
    }

    @Test
    void testPlaceBet_WithNullBetAmount_ShouldThrowException() {
        betRequest.setBetAmount(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            betService.placeBet(betRequest);
        });

        assertEquals("Bet amount must be positive", exception.getMessage());
        verify(betRepository, never()).save(any());
    }

    @Test
    void testPlaceBet_WithMissingJackpot_ShouldThrowException() {
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            betService.placeBet(betRequest);
        });

        assertTrue(exception.getMessage().contains("Jackpot not found"));
        verify(betRepository, never()).save(any());
    }

    // ========== Bet Placement Tests ==========

    @Test
    void testPlaceBet_WithValidBet_ShouldSaveBet() {
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        betService.placeBet(betRequest);

        verify(betRepository, times(1)).save(any(Bet.class));
        verify(jackpotRepository, times(1)).save(testJackpot);
    }

    @Test
    void testPlaceBet_ShouldIncreaseJackpotSize() {
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal initialSize = testJackpot.getCurrentSize();
        BigDecimal betAmount = betRequest.getBetAmount();

        BetResponse response = betService.placeBet(betRequest);

        BigDecimal expectedNewSize = initialSize.add(betAmount);
        assertEquals(expectedNewSize, response.getNewJackpotSize());
        assertEquals(expectedNewSize, testJackpot.getCurrentSize());
    }

    // ========== Win Logic Tests ==========

    @Test
    void testPlaceBet_WhenJackpotWon_ShouldRecordWin() {
        // Setup: Force a win by using a high probability jackpot
        testJackpot.setWinProbability(1.0); // 100% win probability

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(winRepository.save(any(Win.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BetResponse response = betService.placeBet(betRequest);

        assertTrue(response.getWon());
        assertEquals("Congratulations! You won!", response.getMessage());
        verify(winRepository, times(1)).save(any(Win.class));
    }

    @Test
    void testPlaceBet_WhenJackpotWon_ShouldResetJackpotSize() {
        testJackpot.setWinProbability(1.0);

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(winRepository.save(any(Win.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BetResponse response = betService.placeBet(betRequest);

        assertTrue(response.getWon());
        assertEquals(BigDecimal.ZERO, testJackpot.getCurrentSize());
    }

    @Test
    void testPlaceBet_WhenJackpotWon_ShouldIncrementWinCount() {
        testJackpot.setWinProbability(1.0);
        int initialWinCount = testJackpot.getWinCount();

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(winRepository.save(any(Win.class))).thenAnswer(invocation -> invocation.getArgument(0));

        betService.placeBet(betRequest);

        assertEquals(initialWinCount + 1, testJackpot.getWinCount());
    }

    @Test
    void testPlaceBet_WhenJackpotWon_ShouldUpdateLastWinTimestamp() {
        testJackpot.setWinProbability(1.0);

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(winRepository.save(any(Win.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeBet = LocalDateTime.now();
        betService.placeBet(betRequest);
        LocalDateTime afterBet = LocalDateTime.now();

        assertNotNull(testJackpot.getLastWinTimestamp());
        assertTrue(testJackpot.getLastWinTimestamp().isAfter(beforeBet.minusSeconds(1)));
        assertTrue(testJackpot.getLastWinTimestamp().isBefore(afterBet.plusSeconds(1)));
    }

    @Test
    void testPlaceBet_WhenJackpotWon_ShouldReturnWinAmount() {
        testJackpot.setWinProbability(1.0);
        BigDecimal expectedWinAmount = testJackpot.getCurrentSize().add(betRequest.getBetAmount());

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(winRepository.save(any(Win.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BetResponse response = betService.placeBet(betRequest);

        assertEquals(expectedWinAmount, response.getWinAmount());
    }

    // ========== Loss Logic Tests ==========

    @Test
    void testPlaceBet_WhenJackpotNotWon_ShouldNotRecordWin() {
        // Setup: Force a loss
        testJackpot.setWinProbability(0.0); // 0% win probability

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BetResponse response = betService.placeBet(betRequest);

        assertFalse(response.getWon());
        assertEquals("Better luck next time!", response.getMessage());
        verify(winRepository, never()).save(any(Win.class));
    }

    @Test
    void testPlaceBet_WhenJackpotNotWon_ShouldNotResetJackpotSize() {
        testJackpot.setWinProbability(0.0);
        BigDecimal initialSize = testJackpot.getCurrentSize();

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        betService.placeBet(betRequest);

        BigDecimal expectedSize = initialSize.add(betRequest.getBetAmount());
        assertEquals(expectedSize, testJackpot.getCurrentSize());
    }

    @Test
    void testPlaceBet_WhenJackpotNotWon_ShouldNotIncrementWinCount() {
        testJackpot.setWinProbability(0.0);
        int initialWinCount = testJackpot.getWinCount();

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        betService.placeBet(betRequest);

        assertEquals(initialWinCount, testJackpot.getWinCount());
    }

    @Test
    void testPlaceBet_WhenJackpotNotWon_ShouldReturnZeroWinAmount() {
        testJackpot.setWinProbability(0.0);

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BetResponse response = betService.placeBet(betRequest);

        assertEquals(BigDecimal.ZERO, response.getWinAmount());
    }

    // ========== Multiple Bets Tests ==========

    @Test
    void testPlaceBet_MultipleBetsWithoutWin_ShouldAccumulateSize() {
        testJackpot.setWinProbability(0.0);
        BigDecimal initialSize = testJackpot.getCurrentSize();
        BigDecimal betAmount = betRequest.getBetAmount();

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Place 3 bets
        betService.placeBet(betRequest);
        betService.placeBet(betRequest);
        betService.placeBet(betRequest);

        BigDecimal expectedSize = initialSize.add(betAmount.multiply(BigDecimal.valueOf(3)));
        assertEquals(expectedSize, testJackpot.getCurrentSize());
        assertEquals(0, testJackpot.getWinCount());
    }

    @Test
    void testPlaceBet_AfterWin_JackpotResetForNextBet() {
        testJackpot.setWinProbability(1.0);

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(winRepository.save(any(Win.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // First bet wins
        BetResponse response1 = betService.placeBet(betRequest);
        assertTrue(response1.getWon());
        assertEquals(BigDecimal.ZERO, testJackpot.getCurrentSize());

        // Second bet after win
        BetResponse response2 = betService.placeBet(betRequest);
        assertEquals(betRequest.getBetAmount(), response2.getNewJackpotSize());
    }
}
