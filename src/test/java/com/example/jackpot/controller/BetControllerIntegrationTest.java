package com.example.jackpot.controller;

import com.example.jackpot.dto.BetRequest;
import com.example.jackpot.dto.BetResponse;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.repository.BetRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.WinRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JackpotRepository jackpotRepository;

        @Autowired
        private BetRepository betRepository;

        @Autowired
        private WinRepository winRepository;

    @BeforeEach
    void clean() {
                winRepository.deleteAll();
                betRepository.deleteAll();
                jackpotRepository.deleteAll();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void placeBet_ShouldReturn200AndIncreaseJackpotSize() throws Exception {
        Jackpot jackpot = jackpotRepository.save(Jackpot.builder()
                .name("Test Jackpot")
                .winProbability(0.0)
                .currentSize(BigDecimal.ZERO)
                .winCount(0)
                .build());

        BetRequest request = BetRequest.builder()
                .jackpotId(jackpot.getId())
                .playerAlias("alice")
                .betAmount(BigDecimal.valueOf(50))
                .build();

        String responseBody = mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BetResponse response = objectMapper.readValue(responseBody, BetResponse.class);

        assertThat(response.getWon()).isFalse();
        assertThat(response.getWinAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getNewJackpotSize()).isEqualByComparingTo(BigDecimal.valueOf(50));

        Jackpot updated = jackpotRepository.findById(jackpot.getId()).orElseThrow();
        assertThat(updated.getCurrentSize()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void placeBet_ShouldReturn400_WhenJackpotNotFound() throws Exception {
        BetRequest request = BetRequest.builder()
                .jackpotId(UUID.randomUUID())
                .playerAlias("alice")
                .betAmount(BigDecimal.valueOf(10))
                .build();

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeBet_ShouldReturn400_WhenBetAmountInvalid() throws Exception {
        Jackpot jackpot = jackpotRepository.save(Jackpot.builder()
                .name("Test Jackpot")
                .winProbability(0.2)
                .currentSize(BigDecimal.ZERO)
                .winCount(0)
                .build());

        BetRequest request = BetRequest.builder()
                .jackpotId(jackpot.getId())
                .playerAlias("alice")
                .betAmount(BigDecimal.valueOf(-5))
                .build();

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeBet_ShouldWin_WhenProbabilityIsOne() throws Exception {
        Jackpot jackpot = jackpotRepository.save(Jackpot.builder()
                .name("Guaranteed Win")
                .winProbability(1.0)
                .currentSize(BigDecimal.valueOf(20))
                .winCount(0)
                .build());

        BetRequest request = BetRequest.builder()
                .jackpotId(jackpot.getId())
                .playerAlias("bob")
                .betAmount(BigDecimal.valueOf(30))
                .build();

        String responseBody = mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BetResponse response = objectMapper.readValue(responseBody, BetResponse.class);

        assertThat(response.getWon()).isTrue();
        assertThat(response.getWinAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(response.getNewJackpotSize()).isEqualByComparingTo(BigDecimal.valueOf(50));

        Jackpot updated = jackpotRepository.findById(jackpot.getId()).orElseThrow();
        assertThat(updated.getCurrentSize()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updated.getWinCount()).isEqualTo(1);
        assertThat(updated.getLastWinTimestamp()).isNotNull();
    }
}
