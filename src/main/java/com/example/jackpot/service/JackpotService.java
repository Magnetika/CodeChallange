package com.example.jackpot.service;

import com.example.jackpot.dto.JackpotDto;
import com.example.jackpot.dto.CreateJackpotRequest;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.exception.JackpotNotFoundException;
import com.example.jackpot.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Jackpot operations.
 * Handles business logic related to jackpot management.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class JackpotService {

    private final JackpotRepository jackpotRepository;

    /**
     * Create a new jackpot.
     *
     * @param request contains jackpot name and win probability
     * @return created jackpot DTO
     */
    public JackpotDto createJackpot(CreateJackpotRequest request) {
        Jackpot jackpot = Jackpot.builder()
                .name(request.getName())
                .winProbability(request.getWinProbability())
                .build();

        Jackpot saved = jackpotRepository.save(jackpot);
        return mapToDto(saved);
    }

    /**
     * Get all jackpots.
     *
     * @return list of all jackpots
     */
    @Transactional(readOnly = true)
    public List<JackpotDto> getAllJackpots() {
        return jackpotRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a jackpot by ID.
     *
     * @param jackpotId the jackpot ID
     * @return jackpot DTO
     * @throws IllegalArgumentException if jackpot not found
     */
    @Transactional(readOnly = true)
    public JackpotDto getJackpotById(UUID jackpotId) {
        Jackpot jackpot = jackpotRepository.findById(jackpotId)
                .orElseThrow(() -> new JackpotNotFoundException("Jackpot not found with ID: " + jackpotId));
        return mapToDto(jackpot);
    }

    /**
     * Reset jackpot size to zero (called after a win).
     *
     * @param jackpot the jackpot to reset
     */
    protected void resetJackpotSize(Jackpot jackpot) {
        jackpot.setCurrentSize(java.math.BigDecimal.ZERO);
    }

    /**
     * Map Jackpot entity to DTO.
     *
     * @param jackpot entity
     * @return DTO representation
     */
    private JackpotDto mapToDto(Jackpot jackpot) {
        return JackpotDto.builder()
                .id(jackpot.getId())
                .name(jackpot.getName())
                .currentSize(jackpot.getCurrentSize())
                .winCount(jackpot.getWinCount())
                .lastWinTimestamp(jackpot.getLastWinTimestamp())
                .build();
    }
}
