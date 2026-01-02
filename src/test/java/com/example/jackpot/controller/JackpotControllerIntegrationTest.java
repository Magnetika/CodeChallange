package com.example.jackpot.controller;

import com.example.jackpot.dto.CreateJackpotRequest;
import com.example.jackpot.dto.JackpotDto;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.repository.JackpotRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JackpotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JackpotRepository jackpotRepository;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb3;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void clean() {
        jackpotRepository.deleteAll();
    }

    @Test
    void createJackpot_ShouldReturn201AndPersist() throws Exception {
        CreateJackpotRequest request = CreateJackpotRequest.builder()
                .name("New Jackpot")
                .winProbability(0.25)
                .build();

        String body = mockMvc.perform(post("/api/jackpots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JackpotDto dto = objectMapper.readValue(body, JackpotDto.class);

        assertThat(dto.getName()).isEqualTo("New Jackpot");
        assertThat(dto.getCurrentSize()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getWinCount()).isZero();

        assertThat(jackpotRepository.findAll()).hasSize(1);
    }

    @Test
    void getAllJackpots_ShouldReturnList() throws Exception {
        jackpotRepository.save(Jackpot.builder()
                .name("J1")
                .winProbability(0.1)
                .currentSize(BigDecimal.valueOf(50))
                .winCount(1)
                .build());

        jackpotRepository.save(Jackpot.builder()
                .name("J2")
                .winProbability(0.2)
                .currentSize(BigDecimal.valueOf(75))
                .winCount(0)
                .build());

        String body = mockMvc.perform(get("/api/jackpots")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<JackpotDto> jackpots = objectMapper.readValue(body, new TypeReference<List<JackpotDto>>() {});

        assertThat(jackpots).hasSize(2);
        assertThat(jackpots).extracting(JackpotDto::getName).containsExactlyInAnyOrder("J1", "J2");
    }

    @Test
    void getJackpot_ShouldReturn200() throws Exception {
        Jackpot saved = jackpotRepository.save(Jackpot.builder()
                .name("Target")
                .winProbability(0.3)
                .currentSize(BigDecimal.valueOf(25))
                .winCount(2)
                .build());

        String body = mockMvc.perform(get("/api/jackpots/" + saved.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JackpotDto dto = objectMapper.readValue(body, JackpotDto.class);

        assertThat(dto.getName()).isEqualTo("Target");
        assertThat(dto.getCurrentSize()).isEqualByComparingTo(BigDecimal.valueOf(25));
    }

    @Test
    void getJackpot_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/jackpots/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createJackpot_ShouldReturn400_WhenInvalidPayload() throws Exception {
        // Missing name and probability
        CreateJackpotRequest request = CreateJackpotRequest.builder().build();

        mockMvc.perform(post("/api/jackpots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
