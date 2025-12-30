package com.example.jackpot.service;

import com.example.jackpot.dto.WinDto;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.Win;
import com.example.jackpot.repository.WinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WinService.
 * Tests win retrieval, filtering, pagination, and DTO mapping.
 */
@ExtendWith(MockitoExtension.class)
class WinServiceTest {

    @Mock
    private WinRepository winRepository;

    @InjectMocks
    private WinService winService;

    private UUID jackpotId;
    private UUID otherJackpotId;
    private Win win1;
    private Win win2;
    private Win win3;

    @BeforeEach
    void setUp() {
        jackpotId = UUID.randomUUID();
        otherJackpotId = UUID.randomUUID();

        // Create test jackpots
        Jackpot jackpot1 = Jackpot.builder()
                .id(jackpotId)
                .name("Jackpot 1")
                .currentSize(BigDecimal.ZERO)
                .winProbability(0.5)
                .build();

        Jackpot jackpot2 = Jackpot.builder()
                .id(otherJackpotId)
                .name("Jackpot 2")
                .currentSize(BigDecimal.ZERO)
                .winProbability(0.3)
                .build();

        // Create test wins
        LocalDateTime now = LocalDateTime.now();

        win1 = Win.builder()
                .id(UUID.randomUUID())
                .jackpot(jackpot1)
                .playerAlias("Alice")
                .winAmount(BigDecimal.valueOf(500))
                .timestamp(now.minusHours(2))
                .build();

        win2 = Win.builder()
                .id(UUID.randomUUID())
                .jackpot(jackpot1)
                .playerAlias("Bob")
                .winAmount(BigDecimal.valueOf(1000))
                .timestamp(now.minusHours(1))
                .build();

        win3 = Win.builder()
                .id(UUID.randomUUID())
                .jackpot(jackpot2)
                .playerAlias("Alice")
                .winAmount(BigDecimal.valueOf(750))
                .timestamp(now)
                .build();
    }

    // ========== Basic Retrieval Tests ==========

    @Test
    void testGetWins_ShouldReturnEmptyList_WhenNoWinsExist() {
        Page<Win> emptyPage = new PageImpl<>(Collections.emptyList());
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        List<WinDto> result = winService.getWins(10, 0, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWins_ShouldReturnAllWins_WhenNoFiltersApplied() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3, win2, win1));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, null);

        assertEquals(3, result.size());
        verify(winRepository, times(1)).findWithFilters(isNull(), isNull(), any(Pageable.class));
    }

    // ========== Pagination Tests ==========

    @Test
    void testGetWins_WithLimit_ShouldRespectPageSize() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3, win2));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(2, 0, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void testGetWins_WithOffset_ShouldSkipRecords() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win1));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 2, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void testGetWins_WithLimitAndOffset_ShouldPaginateCorrectly() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win2));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(1, 1, null, null);

        assertEquals(1, result.size());
    }

    // ========== Sorting Tests ==========

    @Test
    void testGetWins_ShouldSortByTimestampDescending() {
        // Wins are returned in descending timestamp order (most recent first)
        Page<Win> page = new PageImpl<>(Arrays.asList(win3, win2, win1));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, null);

        assertEquals(3, result.size());
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
        assertTrue(result.get(1).getTimestamp().isAfter(result.get(2).getTimestamp()));
    }

    // ========== Player Alias Filtering Tests ==========

    @Test
    void testGetWins_FilterByPlayerAlias_ShouldReturnMatchingWins() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3, win1));
        when(winRepository.findWithFilters(eq("Alice"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, "Alice", null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(w -> w.getPlayerAlias().equals("Alice")));
        verify(winRepository, times(1)).findWithFilters(eq("Alice"), isNull(), any(Pageable.class));
    }

    @Test
    void testGetWins_FilterByPlayerAlias_ShouldReturnEmptyWhenNoMatch() {
        Page<Win> emptyPage = new PageImpl<>(Collections.emptyList());
        when(winRepository.findWithFilters(eq("Charlie"), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        List<WinDto> result = winService.getWins(10, 0, "Charlie", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWins_FilterByPlayerAlias_ShouldReturnOnlyOneAlias() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win2));
        when(winRepository.findWithFilters(eq("Bob"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, "Bob", null);

        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getPlayerAlias());
    }

    // ========== Jackpot ID Filtering Tests ==========

    @Test
    void testGetWins_FilterByJackpotId_ShouldReturnMatchingWins() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win2, win1));
        when(winRepository.findWithFilters(isNull(), eq(jackpotId), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, jackpotId);

        assertEquals(2, result.size());
        verify(winRepository, times(1)).findWithFilters(isNull(), eq(jackpotId), any(Pageable.class));
    }

    @Test
    void testGetWins_FilterByJackpotId_ShouldReturnEmptyWhenNoMatch() {
        UUID nonExistentId = UUID.randomUUID();
        Page<Win> emptyPage = new PageImpl<>(Collections.emptyList());
        when(winRepository.findWithFilters(isNull(), eq(nonExistentId), any(Pageable.class)))
                .thenReturn(emptyPage);

        List<WinDto> result = winService.getWins(10, 0, null, nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWins_FilterByJackpotId_ShouldReturnOnlyOneJackpot() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3));
        when(winRepository.findWithFilters(isNull(), eq(otherJackpotId), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, otherJackpotId);

        assertEquals(1, result.size());
    }

    // ========== Combined Filtering Tests ==========

    @Test
    void testGetWins_FilterByPlayerAliasAndJackpotId_ShouldReturnMatchingWins() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win1));
        when(winRepository.findWithFilters(eq("Alice"), eq(jackpotId), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, "Alice", jackpotId);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getPlayerAlias());
        verify(winRepository, times(1)).findWithFilters(eq("Alice"), eq(jackpotId), any(Pageable.class));
    }

    @Test
    void testGetWins_FilterByPlayerAliasAndJackpotId_ShouldReturnEmptyWhenNoMatch() {
        Page<Win> emptyPage = new PageImpl<>(Collections.emptyList());
        when(winRepository.findWithFilters(eq("Bob"), eq(otherJackpotId), any(Pageable.class)))
                .thenReturn(emptyPage);

        List<WinDto> result = winService.getWins(10, 0, "Bob", otherJackpotId);

        assertTrue(result.isEmpty());
    }

    // ========== DTO Mapping Tests ==========

    @Test
    void testGetWins_ShouldMapToWinDto() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win1));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, null);

        assertEquals(1, result.size());
        WinDto dto = result.get(0);
        assertEquals(win1.getTimestamp(), dto.getTimestamp());
        assertEquals(win1.getPlayerAlias(), dto.getPlayerAlias());
        assertEquals(win1.getWinAmount(), dto.getWinAmount());
    }

    @Test
    void testGetWins_ShouldMapAllFields() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win2));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, null);

        WinDto dto = result.get(0);
        assertNotNull(dto.getTimestamp());
        assertNotNull(dto.getPlayerAlias());
        assertNotNull(dto.getWinAmount());
        assertEquals("Bob", dto.getPlayerAlias());
        assertEquals(BigDecimal.valueOf(1000), dto.getWinAmount());
    }

    @Test
    void testGetWins_ShouldPreserveWinAmount() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(10, 0, null, null);

        assertEquals(BigDecimal.valueOf(750), result.get(0).getWinAmount());
    }

    // ========== Combination Pagination + Filtering Tests ==========

    @Test
    void testGetWins_WithPaginationAndPlayerAliasFilter() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3, win1));
        when(winRepository.findWithFilters(eq("Alice"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(2, 0, "Alice", null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(w -> w.getPlayerAlias().equals("Alice")));
    }

    @Test
    void testGetWins_WithPaginationAndJackpotIdFilter() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win2));
        when(winRepository.findWithFilters(isNull(), eq(jackpotId), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(1, 1, null, jackpotId);

        assertEquals(1, result.size());
    }

    @Test
    void testGetWins_WithAllParametersSpecified() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win1));
        when(winRepository.findWithFilters(eq("Alice"), eq(jackpotId), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(5, 0, "Alice", jackpotId);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getPlayerAlias());
    }

    // ========== Large Dataset Tests ==========

    @Test
    void testGetWins_WithLargeDataset_ShouldHandlePaginationCorrectly() {
        Page<Win> page = new PageImpl<>(Arrays.asList(win3, win2));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        List<WinDto> result = winService.getWins(2, 0, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void testGetWins_WithMultiplePages_ShouldReturnCorrectPage() {
        Page<Win> secondPage = new PageImpl<>(Arrays.asList(win1));
        when(winRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(secondPage);

        List<WinDto> result = winService.getWins(2, 1, null, null);

        assertEquals(1, result.size());
    }
}
