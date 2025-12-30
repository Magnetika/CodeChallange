package com.example.jackpot.controller;

import com.example.jackpot.dto.WinDto;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.Win;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.WinRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WinControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WinRepository winRepository;

    @Autowired
    private JackpotRepository jackpotRepository;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb2;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void clean() {
        winRepository.deleteAll();
        jackpotRepository.deleteAll();
    }

    private void seedData() {
        LocalDateTime now = LocalDateTime.now();

        Jackpot jackpot1 = jackpotRepository.save(Jackpot.builder()
                .name("Jackpot One")
                .winProbability(0.5)
                .currentSize(BigDecimal.ZERO)
                .winCount(0)
                .build());

        Jackpot jackpot2 = jackpotRepository.save(Jackpot.builder()
                .name("Jackpot Two")
                .winProbability(0.2)
                .currentSize(BigDecimal.ZERO)
                .winCount(0)
                .build());

        winRepository.save(Win.builder()
                .jackpot(jackpot1)
                .playerAlias("alice")
                .winAmount(BigDecimal.valueOf(100))
                .timestamp(now.minusHours(3))
                .build());

        winRepository.save(Win.builder()
                .jackpot(jackpot1)
                .playerAlias("bob")
                .winAmount(BigDecimal.valueOf(200))
                .timestamp(now.minusHours(2))
                .build());

        winRepository.save(Win.builder()
                .jackpot(jackpot2)
                .playerAlias("alice")
                .winAmount(BigDecimal.valueOf(300))
                .timestamp(now.minusHours(1))
                .build());
    }

    @Test
    void getWins_ShouldReturnAllInDescendingOrder() throws Exception {
        seedData();

        String body = mockMvc.perform(get("/api/wins")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<WinDto> wins = objectMapper.readValue(body, new TypeReference<List<WinDto>>() {});

        assertThat(wins).hasSize(3);
        assertThat(wins.get(0).getWinAmount()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(wins.get(1).getWinAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(wins.get(2).getWinAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void getWins_ShouldFilterByPlayerAlias() throws Exception {
        seedData();

        String body = mockMvc.perform(get("/api/wins")
                        .param("playerAlias", "alice")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<WinDto> wins = objectMapper.readValue(body, new TypeReference<List<WinDto>>() {});

        assertThat(wins).hasSize(2);
        assertThat(wins).allMatch(w -> w.getPlayerAlias().equals("alice"));
    }

    @Test
    void getWins_ShouldFilterByJackpotId() throws Exception {
        seedData();
        UUID jackpotId = jackpotRepository.findAll().get(0).getId();

        String body = mockMvc.perform(get("/api/wins")
                        .param("jackpotId", jackpotId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<WinDto> wins = objectMapper.readValue(body, new TypeReference<List<WinDto>>() {});

        assertThat(wins).hasSize(2);
        assertThat(wins)
                        .extracting(WinDto::getWinAmount)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactlyInAnyOrder(BigDecimal.valueOf(200), BigDecimal.valueOf(100));
    }

    @Test
    void getWins_ShouldPaginate() throws Exception {
        seedData();

        String body = mockMvc.perform(get("/api/wins")
                        .param("limit", "1")
                        .param("offset", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<WinDto> wins = objectMapper.readValue(body, new TypeReference<List<WinDto>>() {});

        assertThat(wins).hasSize(1);
        // By timestamp desc, this should be the middle record
        assertThat(wins.get(0).getWinAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }
}
