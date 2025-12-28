package com.overwatch.balancer.api.controller;

import com.overwatch.balancer.api.dto.error.ErrorResponseDTO;
import com.overwatch.balancer.api.dto.lobby.LobbyJoinRequestDTO;
import com.overwatch.balancer.api.dto.lobby.LobbyResponseDTO;
import com.overwatch.balancer.api.dto.player.PlayerResponseDTO;
import com.overwatch.balancer.api.dto.queue.QueueResponseDTO;
import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.exception.PlayerNotFoundException;
import com.overwatch.balancer.service.LobbyService;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lobbies")
@RequiredArgsConstructor
@Tag(name = "Lobbies", description = "Game lobby and queue management")
public class LobbyController {

    private final LobbyService lobbyService;
    private final PlayerService playerService;

    @GetMapping("/{lobbyId}")
    @Operation(summary = "Get lobby status",
            description = "Returns current players in lobby and match readiness")
    public ResponseEntity<LobbyResponseDTO> getLobby(
            @Parameter(description = "Lobby ID (e.g., Discord guild ID)")
            @PathVariable String lobbyId) {

        List<Player> players = lobbyService.getPlayers(lobbyId);
        int count = players.size();

        LobbyResponseDTO response = LobbyResponseDTO.builder()
                .lobbyId(lobbyId)
                .playerCount(count)
                .players(players.stream().map(PlayerResponseDTO::from).toList())
                .canStart(lobbyService.canStartMatch(lobbyId))
                .playersNeeded(Math.max(0, 10 - count))
                .maxMatches(lobbyService.getMaxMatches(lobbyId))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{lobbyId}/join")
    @Operation(summary = "Join a lobby",
            description = "Adds a player to the lobby queue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joined successfully or already in lobby"),
            @ApiResponse(responseCode = "400", description = "Player not registered or no active roles"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<?> joinLobby(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId,
            @Valid @RequestBody LobbyJoinRequestDTO request) {

        try {
            boolean joined = lobbyService.join(lobbyId, request.getPlayerId());

            int count = lobbyService.getPlayerCount(lobbyId);

            return ResponseEntity.ok(Map.of(
                    "success", joined,
                    "message", joined ? "Joined lobby" : "Already in lobby",
                    "playerCount", count,
                    "canStart", lobbyService.canStartMatch(lobbyId)
            ));
        } catch (PlayerNotFoundException e) {
            return ResponseEntity.status(404).body(ErrorResponseDTO.of("Player not found", "PLAYER_NOT_FOUND"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO.of(e.getMessage(), "INVALID_STATE"));
        }
    }

    @PostMapping("/{lobbyId}/leave")
    @Operation(summary = "Leave a lobby",
            description = "Removes a player from the lobby queue")
    public ResponseEntity<?> leaveLobby(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId,
            @Valid @RequestBody LobbyJoinRequestDTO request) {

        boolean left = lobbyService.leave(lobbyId, request.getPlayerId());

        int count = lobbyService.getPlayerCount(lobbyId);

        return ResponseEntity.ok(Map.of(
                "success", left,
                "message", left ? "Left lobby" : "Was not in lobby",
                "playerCount", count
        ));
    }

    @GetMapping("/{lobbyId}/queue")
    @Operation(summary = "Get queue status",
            description = "Shows who will play next and who is waiting")
    public ResponseEntity<QueueResponseDTO> getQueue(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId) {

        LobbyService.QueueStatus status = lobbyService.getQueueStatus(lobbyId);

        QueueResponseDTO response = QueueResponseDTO.builder()
                .lobbyId(lobbyId)
                .totalPlayers(status.totalPlayers())
                .playingNext(status.playingNext().stream()
                        .map(PlayerResponseDTO::from)
                        .toList())
                .waiting(status.waiting().stream()
                        .map(PlayerResponseDTO::from)
                        .toList())
                .canStart(status.canStart())
                .playersNeeded(status.getPlayersNeeded())
                .roleAvailability(status.roleAvailability().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().getName(),
                                Map.Entry::getValue
                        )))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{lobbyId}/players")
    @Operation(summary = "Get players in lobby grouped by role")
    public ResponseEntity<?> getPlayersGrouped(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId) {

        String formatted = lobbyService.formatPlayerList(lobbyId);
        List<Player> players = lobbyService.getPlayers(lobbyId);

        return ResponseEntity.ok(Map.of(
                "lobbyId", lobbyId,
                "playerCount", players.size(),
                "formatted", formatted,
                "players", players.stream().map(PlayerResponseDTO::from).toList()
        ));
    }

    @DeleteMapping("/{lobbyId}")
    @Operation(summary = "Clear a lobby",
            description = "Removes all players from the lobby")
    public ResponseEntity<Void> clearLobby(
            @Parameter(description = "Lobby ID") @PathVariable String lobbyId) {

        lobbyService.clearLobby(lobbyId);
        return ResponseEntity.noContent().build();
    }

}
