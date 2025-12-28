package com.overwatch.balancer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overwatch.balancer.api.dto.balance.DirectBalanceRequestDTO;
import com.overwatch.balancer.api.dto.lobby.LobbyJoinRequestDTO;
import com.overwatch.balancer.api.dto.player.PlayerRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
@DisplayName("Balancer API Integration Tests")
class BalancerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("should register a player")
    void shouldRegisterPlayer() throws Exception {
        PlayerRequestDTO request = PlayerRequestDTO.builder()
                .id("test-player-1")
                .name("NGINX")
                .tankSR(4200)
                .dpsSR(0)
                .supportSR(3800)
                .build();

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-player-1"))
                .andExpect(jsonPath("$.name").value("NGINX"))
                .andExpect(jsonPath("$.tankSR").value(4200))
                .andExpect(jsonPath("$.supportSR").value(3800))
                .andExpect(jsonPath("$.primaryRole").value("tank"));
    }

    @Test
    @DisplayName("should get player by ID")
    void shouldGetPlayer() throws Exception {
        // first register
        PlayerRequestDTO request = PlayerRequestDTO.builder()
                .id("test-player-get")
                .name("yreen")
                .tankSR(0)
                .dpsSR(0)
                .supportSR(4500)
                .build();

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        //then get
        mockMvc.perform(get("/api/v1/players/test-player-get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("yreen"));
    }

    @Test
    @DisplayName("should return 404 for unknown player")
    void shouldReturn404ForUnknownPlayer() throws Exception {
        mockMvc.perform(get("/api/v1/players/unknown-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should balance from direct player list")
    void shouldBalanceDirectly() throws Exception {
        List<PlayerRequestDTO> players = createTestPlayers(10);

        DirectBalanceRequestDTO request = new DirectBalanceRequestDTO();
        request.setPlayers(players);

        mockMvc.perform(post("/api/v1/balance/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.match").exists())
                .andExpect(jsonPath("$.match.team1").exists())
                .andExpect(jsonPath("$.match.team2").exists())
                .andExpect(jsonPath("$.match.mapName").exists());
    }

    @Test
    @DisplayName("should fail balance with insufficient players")
    void shouldFailBalanceWithInsufficientPlayers() throws Exception {
        List<PlayerRequestDTO> players = createTestPlayers(5);

        DirectBalanceRequestDTO request = new DirectBalanceRequestDTO();
        request.setPlayers(players);

        mockMvc.perform(post("/api/v1/balance/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should get all maps")
    void shouldGetAllMaps() throws Exception {
        mockMvc.perform(get("/api/v1/balance/maps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThan(20)));
    }

    @Test
    @DisplayName("should get maps by mode")
    void shouldGetMapsByMode() throws Exception {
        mockMvc.perform(get("/api/v1/balance/maps/CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].mode").value("Control"));
    }

    @Test
    @DisplayName("should get random map")
    void shouldGetRandomMap() throws Exception {
        mockMvc.perform(get("/api/v1/balance/maps/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.mode").exists());
    }

    @Test
    @DisplayName("should join and leave lobby")
    void shouldJoinAndLeaveLobby() throws Exception {
        // Register player first
        PlayerRequestDTO playerReq = PlayerRequestDTO.builder()
                .id("lobby-test-player")
                .name("Txao")
                .tankSR(5000)
                .dpsSR(0)
                .supportSR(0)
                .build();

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerReq)))
                .andExpect(status().isCreated());

        // join lobby
        LobbyJoinRequestDTO joinReq = new LobbyJoinRequestDTO();
        joinReq.setPlayerId("lobby-test-player");

        mockMvc.perform(post("/api/v1/lobbies/test-lobby/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playerCount").value(1));

        // get lobby status
        mockMvc.perform(get("/api/v1/lobbies/test-lobby"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerCount").value(1))
                .andExpect(jsonPath("$.canStart").value(false));

        //leave lobby
        mockMvc.perform(post("/api/v1/lobbies/test-lobby/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playerCount").value(0));
    }

    private static final String[] PLAYER_NAMES = {
            "NGINX", "yreen", "Txao", "KSAA", "GetMads", "Pr1de", "Povelitel",
            "Averet", "Sacr1ficed", "Nanika"
    };

    private List<PlayerRequestDTO> createTestPlayers(int count) {
        List<PlayerRequestDTO> players = new ArrayList<>();

        int tanks = Math.max(2, count / 5);
        int dps = count * 2 / 5;
        int supports = count - tanks - dps;

        int nameIndex = 0;

        for (int i = 0; i < tanks; i++) {
            players.add(PlayerRequestDTO.builder()
                    .id("tank" + i)
                    .name(PLAYER_NAMES[nameIndex++ % PLAYER_NAMES.length])
                    .tankSR(3500 + i * 100)
                    .dpsSR(0)
                    .supportSR(0)
                    .build());
        }

        for (int i = 0; i < dps; i++) {
            players.add(PlayerRequestDTO.builder()
                    .id("dps" + i)
                    .name(PLAYER_NAMES[nameIndex++ % PLAYER_NAMES.length])
                    .tankSR(0)
                    .dpsSR(3500 + i * 50)
                    .supportSR(0)
                    .build());
        }

        for (int i = 0; i < supports; i++) {
            players.add(PlayerRequestDTO.builder()
                    .id("supp" + i)
                    .name(PLAYER_NAMES[nameIndex++ % PLAYER_NAMES.length])
                    .tankSR(0)
                    .dpsSR(0)
                    .supportSR(3500 + i * 50)
                    .build());
        }

        return players;
    }

}
