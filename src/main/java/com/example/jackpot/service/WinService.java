package com.example.jackpot.service;

import com.example.jackpot.dto.WinDto;
import com.example.jackpot.entity.Win;
import com.example.jackpot.repository.WinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Win operations.
 * Handles business logic related to win records and retrieval.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WinService {

    private final WinRepository winRepository;

    /**
     * Get wins with pagination, sorting, and optional filtering.
     *
     * @param limit maximum number of results
     * @param offset page offset (0-based)
     * @param playerAlias optional filter by player alias
     * @param jackpotId optional filter by jackpot ID
     * @return list of wins
     */
    public List<WinDto> getWins(int limit, int offset, String playerAlias, UUID jackpotId) {
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("timestamp").descending());
        Page<Win> wins = winRepository.findWithFilters(playerAlias, jackpotId, pageable);

        return wins.getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map Win entity to DTO.
     *
     * @param win entity
     * @return DTO representation
     */
    private WinDto mapToDto(Win win) {
        return WinDto.builder()
                .timestamp(win.getTimestamp())
                .playerAlias(win.getPlayerAlias())
                .winAmount(win.getWinAmount())
                .build();
    }
}
