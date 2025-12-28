package com.overwatch.balancer.api.controller;

import com.overwatch.balancer.api.dto.balance.BalanceRequestDTO;
import com.overwatch.balancer.api.dto.balance.DirectBalanceRequestDTO;
import com.overwatch.balancer.api.dto.error.ErrorResponseDTO;
import com.overwatch.balancer.api.dto.map.MapResponseDTO;
import com.overwatch.balancer.api.dto.match.MatchResponseDTO;
import com.overwatch.balancer.domain.enumeration.GameMap;
import com.overwatch.balancer.domain.enumeration.GameMode;
import com.overwatch.balancer.domain.model.Match;
import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.exception.BalanceException;
import com.overwatch.balancer.service.BalancerService;
import com.overwatch.balancer.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
@Tag(name = "Balancer", description = "Team balancing and match creation")
public class BalancerController {

    private final BalancerService balancerService;
    private final PlayerService playerService;

    @PostMapping("/lobby/{lobbyId}")
    @Operation(summary = "Balance teams from lobby",
            description = "Creates balanced match(es) from players in the lobby")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match created successfully"),
            @ApiResponse(responseCode = "400", description = "Not enough players or invalid roles")
    })
    public ResponseEntity<?> balanceFromLobby(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId,
            @Valid @RequestBody(required = false) BalanceRequestDTO request) {

        int matchCount = request != null && request.getMatchCount() != null
                ? request.getMatchCount() : 1;

        try {
            List<Match> matches = balancerService.balanceMultiple(lobbyId, matchCount);

            List<MatchResponseDTO> responses = matches.stream()
                    .map(MatchResponseDTO::from)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "matchCount", responses.size(),
                    "matches", responses
            ));
        } catch (BalanceException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponseDTO.of(e.getMessage(), "BALANCE_FAILED"));
        }
    }

    @PostMapping("/direct")
    @Operation(summary = "Balance from player list",
            description = "Creates balanced match from provided player data (no lobby required)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid players or can't balance")
    })
    public ResponseEntity<?> balanceDirect(
            @Valid @RequestBody DirectBalanceRequestDTO request) {

        try {
            // convert request players to domain players
            List<Player> players = request.getPlayers().stream()
                    .map(pr -> {
                        String id = pr.getId() != null ? pr.getId() : UUID.randomUUID().toString();
                        Player p = new Player(id, pr.getName());
                        p.setRatings(pr.toRatingsMap());
                        return p;
                    })
                    .toList();

            //parse map mode if provided
            GameMode mapMode = null;
            if (request.getMapMode() != null && !request.getMapMode().isBlank()) {
                try {
                    mapMode = GameMode.valueOf(request.getMapMode().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ErrorResponseDTO.of("Invalid map mode: " + request.getMapMode(), "INVALID_MAP_MODE"));
                }
            }

            Match match = balancerService.balancePlayers(players, mapMode);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "match", MatchResponseDTO.from(match)
            ));
        } catch (BalanceException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponseDTO.of(e.getMessage(), "BALANCE_FAILED"));
        }
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "Get match by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match found"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    public ResponseEntity<?> getMatch(
            @Parameter(description = "Match ID") @PathVariable String matchId) {

        return balancerService.getMatch(matchId)
                .map(m -> ResponseEntity.ok(MatchResponseDTO.from(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/matches/recent")
    @Operation(summary = "Get recent matches")
    public ResponseEntity<List<MatchResponseDTO>> getRecentMatches(
            @Parameter(description = "Maximum number of matches")
            @RequestParam(defaultValue = "10") int limit) {

        List<MatchResponseDTO> matches = balancerService.getRecentMatches(limit).stream()
                .map(MatchResponseDTO::from)
                .toList();

        return ResponseEntity.ok(matches);
    }

    @GetMapping("/matches/lobby/{lobbyId}")
    @Operation(summary = "Get matches for a lobby")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesForLobby(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId) {

        List<MatchResponseDTO> matches = balancerService.getMatchesForLobby(lobbyId).stream()
                .map(MatchResponseDTO::from)
                .toList();

        return ResponseEntity.ok(matches);
    }

    @GetMapping("/maps")
    @Operation(summary = "Get all available maps")
    public ResponseEntity<List<MapResponseDTO>> getAllMaps() {
        List<MapResponseDTO> maps = balancerService.getAllMaps().stream()
                .map(MapResponseDTO::from)
                .toList();

        return ResponseEntity.ok(maps);
    }

    @GetMapping("/maps/{mode}")
    @Operation(summary = "Get maps by game mode")
    public ResponseEntity<?> getMapsByMode(
            @Parameter(description = "Game mode (CONTROL, ESCORT, FLASHPOINT, HYBRID, PUSH)")
            @PathVariable String mode) {

        try {
            GameMode gameMode = GameMode.valueOf(mode.toUpperCase());

            List<MapResponseDTO> maps = balancerService.getMapsByMode(gameMode).stream()
                    .map(MapResponseDTO::from)
                    .toList();

            return ResponseEntity.ok(maps);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponseDTO.of("Invalid mode. Valid modes: " +
                            Arrays.toString(GameMode.values()), "INVALID_MODE"));
        }
    }

    @GetMapping("/maps/random")
    @Operation(summary = "Get a random map")
    public ResponseEntity<MapResponseDTO> getRandomMap(
            @Parameter(description = "Optional game mode filter")
            @RequestParam(required = false) String mode) {

        GameMap map;

        if (mode != null && !mode.isBlank()) {
            try {
                GameMode gameMode = GameMode.valueOf(mode.toUpperCase());
                map = GameMap.randomByMode(gameMode);
            } catch (IllegalArgumentException e) {
                map = GameMap.random();
            }
        } else {
            map = GameMap.random();
        }

        return ResponseEntity.ok(MapResponseDTO.from(map));
    }

}
