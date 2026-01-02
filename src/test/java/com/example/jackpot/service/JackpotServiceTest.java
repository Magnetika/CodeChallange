package com.example.jackpot.service;

import com.example.jackpot.dto.CreateJackpotRequest;
import com.example.jackpot.dto.JackpotDto;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.exception.JackpotNotFoundException;
import com.example.jackpot.repository.JackpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JackpotService.
 * Tests jackpot management operations including creation, retrieval, and validation.
 */
@ExtendWith(MockitoExtension.class)
class JackpotServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @InjectMocks
    private JackpotService jackpotService;

    private UUID jackpotId;
    private Jackpot testJackpot;
    private CreateJackpotRequest createRequest;

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

        // Create test request
        createRequest = new CreateJackpotRequest();
        createRequest.setName("New Jackpot");
        createRequest.setWinProbability(0.3);
    }

    // ========== Create Jackpot Tests ==========

    @Test
    void testCreateJackpot_WithValidRequest_ShouldSaveAndReturn() {
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNotNull(result);
        assertEquals(testJackpot.getId(), result.getId());
        assertEquals(testJackpot.getName(), result.getName());
        verify(jackpotRepository, times(1)).save(any(Jackpot.class));
    }

    @Test
    void testCreateJackpot_ShouldInitializeWithCorrectValues() {
        when(jackpotRepository.save(any(Jackpot.class))).thenAnswer(invocation -> {
            Jackpot jackpot = invocation.getArgument(0);
            jackpot.setId(jackpotId);
            return jackpot;
        });

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertEquals(createRequest.getName(), result.getName());
        assertEquals(BigDecimal.ZERO, result.getCurrentSize());
        assertEquals(0, result.getWinCount());
        assertNull(result.getLastWinTimestamp());
    }

    @Test
    void testCreateJackpot_ShouldGenerateUniqueId() {
        Jackpot jackpot1 = Jackpot.builder()
                .id(UUID.randomUUID())
                .name("Jackpot 1")
                .currentSize(BigDecimal.ZERO)
                .winProbability(0.5)
                .winCount(0)
                .build();

        Jackpot jackpot2 = Jackpot.builder()
                .id(UUID.randomUUID())
                .name("Jackpot 2")
                .currentSize(BigDecimal.ZERO)
                .winProbability(0.5)
                .winCount(0)
                .build();

        when(jackpotRepository.save(any(Jackpot.class)))
                .thenReturn(jackpot1)
                .thenReturn(jackpot2);

        JackpotDto result1 = jackpotService.createJackpot(createRequest);
        JackpotDto result2 = jackpotService.createJackpot(createRequest);

        assertNotEquals(result1.getId(), result2.getId());
    }

    @Test
    void testCreateJackpot_ShouldInitializeCurrentSizeToZero() {
        Jackpot newJackpot = Jackpot.builder()
                .id(UUID.randomUUID())
                .name(createRequest.getName())
                .currentSize(BigDecimal.ZERO)
                .winProbability(createRequest.getWinProbability())
                .winCount(0)
                .build();
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(newJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertEquals(BigDecimal.ZERO, result.getCurrentSize());
    }

    @Test
    void testCreateJackpot_ShouldInitializeWinCountToZero() {
        Jackpot newJackpot = Jackpot.builder()
                .id(UUID.randomUUID())
                .name(createRequest.getName())
                .currentSize(BigDecimal.ZERO)
                .winProbability(createRequest.getWinProbability())
                .winCount(0)
                .build();
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(newJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertEquals(0, result.getWinCount());
    }

    // ========== Get All Jackpots Tests ==========

    @Test
    void testGetAllJackpots_ShouldReturnEmptyList_WhenNoJackpotsExist() {
        when(jackpotRepository.findAll()).thenReturn(Arrays.asList());

        List<JackpotDto> result = jackpotService.getAllJackpots();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllJackpots_ShouldReturnAllJackpots() {
        Jackpot jackpot1 = Jackpot.builder()
                .id(UUID.randomUUID())
                .name("Jackpot 1")
                .currentSize(BigDecimal.valueOf(100))
                .winProbability(0.5)
                .winCount(0)
                .build();

        Jackpot jackpot2 = Jackpot.builder()
                .id(UUID.randomUUID())
                .name("Jackpot 2")
                .currentSize(BigDecimal.valueOf(200))
                .winProbability(0.3)
                .winCount(1)
                .build();

        when(jackpotRepository.findAll()).thenReturn(Arrays.asList(jackpot1, jackpot2));

        List<JackpotDto> result = jackpotService.getAllJackpots();

        assertEquals(2, result.size());
        assertEquals("Jackpot 1", result.get(0).getName());
        assertEquals("Jackpot 2", result.get(1).getName());
    }

    @Test
    void testGetAllJackpots_ShouldMaintainJackpotData() {
        Jackpot jackpot = Jackpot.builder()
                .id(jackpotId)
                .name("Test Jackpot")
                .currentSize(BigDecimal.valueOf(150))
                .winProbability(0.7)
                .winCount(3)
                .lastWinTimestamp(LocalDateTime.now())
                .build();

        when(jackpotRepository.findAll()).thenReturn(Arrays.asList(jackpot));

        List<JackpotDto> result = jackpotService.getAllJackpots();

        assertEquals(1, result.size());
        assertEquals(jackpotId, result.get(0).getId());
        assertEquals("Test Jackpot", result.get(0).getName());
        assertEquals(BigDecimal.valueOf(150), result.get(0).getCurrentSize());
        assertEquals(3, result.get(0).getWinCount());
    }

    // ========== Get Jackpot By ID Tests ==========

    @Test
    void testGetJackpotById_WithValidId_ShouldReturnJackpot() {
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));

        JackpotDto result = jackpotService.getJackpotById(jackpotId);

        assertNotNull(result);
        assertEquals(jackpotId, result.getId());
        assertEquals("Test Jackpot", result.getName());
    }

    @Test
    void testGetJackpotById_WithValidId_ShouldReturnCorrectData() {
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(testJackpot));

        JackpotDto result = jackpotService.getJackpotById(jackpotId);

        assertEquals(testJackpot.getCurrentSize(), result.getCurrentSize());
        assertEquals(testJackpot.getWinCount(), result.getWinCount());
    }

    @Test
    void testGetJackpotById_WithInvalidId_ShouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        when(jackpotRepository.findById(invalidId)).thenReturn(Optional.empty());

        JackpotNotFoundException exception = assertThrows(JackpotNotFoundException.class, () -> {
            jackpotService.getJackpotById(invalidId);
        });

        assertTrue(exception.getMessage().contains("Jackpot not found"));
    }

    @Test
    void testGetJackpotById_WithInvalidId_ShouldThrowExceptionWithCorrectId() {
        UUID invalidId = UUID.randomUUID();
        when(jackpotRepository.findById(invalidId)).thenReturn(Optional.empty());

        JackpotNotFoundException exception = assertThrows(JackpotNotFoundException.class, () -> {
            jackpotService.getJackpotById(invalidId);
        });

        assertTrue(exception.getMessage().contains(invalidId.toString()));
    }

    // ========== DTO Mapping Tests ==========

    @Test
    void testDtoMapping_ShouldIncludeAllFields() {
        LocalDateTime winTime = LocalDateTime.now();
        testJackpot.setLastWinTimestamp(winTime);

        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNotNull(result.getId());
        assertNotNull(result.getName());
        assertNotNull(result.getCurrentSize());
        assertNotNull(result.getWinCount());
    }

    @Test
    void testDtoMapping_ShouldPreserveNullLastWinTimestamp() {
        testJackpot.setLastWinTimestamp(null);
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNull(result.getLastWinTimestamp());
    }

    @Test
    void testDtoMapping_ShouldPreserveLastWinTimestamp() {
        LocalDateTime winTime = LocalDateTime.of(2024, 12, 25, 10, 30);
        testJackpot.setLastWinTimestamp(winTime);

        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertEquals(winTime, result.getLastWinTimestamp());
    }

    // ========== Multiple Jackpots Tests ==========

    @Test
    void testMultipleJackpots_ShouldCreateIndependentJackpots() {
        Jackpot jackpot1 = Jackpot.builder()
                .id(UUID.randomUUID())
                .name("Fast Jackpot")
                .currentSize(BigDecimal.valueOf(50))
                .winProbability(0.8)
                .winCount(0)
                .build();

        Jackpot jackpot2 = Jackpot.builder()
                .id(UUID.randomUUID())
                .name("Slow Jackpot")
                .currentSize(BigDecimal.valueOf(500))
                .winProbability(0.1)
                .winCount(5)
                .build();

        when(jackpotRepository.save(any(Jackpot.class)))
                .thenReturn(jackpot1)
                .thenReturn(jackpot2);

        JackpotDto result1 = jackpotService.createJackpot(createRequest);
        JackpotDto result2 = jackpotService.createJackpot(createRequest);

        assertNotEquals(result1.getId(), result2.getId());
        assertNotEquals(result1.getName(), result2.getName());
        assertNotEquals(result1.getWinCount(), result2.getWinCount());
    }

    // ========== Win Probability Tests ==========

    @Test
    void testCreateJackpot_WithHighWinProbability_ShouldBeValid() {
        createRequest.setWinProbability(0.9);
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNotNull(result);
        verify(jackpotRepository, times(1)).save(any(Jackpot.class));
    }

    @Test
    void testCreateJackpot_WithLowWinProbability_ShouldBeValid() {
        createRequest.setWinProbability(0.01);
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNotNull(result);
        verify(jackpotRepository, times(1)).save(any(Jackpot.class));
    }

    @Test
    void testCreateJackpot_WithZeroWinProbability_ShouldBeValid() {
        createRequest.setWinProbability(0.0);
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNotNull(result);
        verify(jackpotRepository, times(1)).save(any(Jackpot.class));
    }

    @Test
    void testCreateJackpot_WithMaxWinProbability_ShouldBeValid() {
        createRequest.setWinProbability(1.0);
        when(jackpotRepository.save(any(Jackpot.class))).thenReturn(testJackpot);

        JackpotDto result = jackpotService.createJackpot(createRequest);

        assertNotNull(result);
        verify(jackpotRepository, times(1)).save(any(Jackpot.class));
    }
}
