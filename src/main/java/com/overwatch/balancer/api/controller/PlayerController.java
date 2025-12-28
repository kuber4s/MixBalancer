package com.overwatch.balancer.api.controller;

import com.overwatch.balancer.api.dto.player.PlayerRequestDTO;
import com.overwatch.balancer.api.dto.player.PlayerResponseDTO;
import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.exception.PlayerNotFoundException;
import com.overwatch.balancer.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
@Tag(name = "Players", description = "Player registration and management")
public class PlayerController {

    private final PlayerService service;

    @PostMapping
    @Operation(summary = "Register or update a player",
            description = "Creates a new player or updates existing one with the given ratings")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Player created"),
            @ApiResponse(responseCode = "200", description = "Player updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<PlayerResponseDTO> registerPlayer(
            @Valid @RequestBody PlayerRequestDTO request) {

        boolean exists = service.exists(request.getId());

        Player player = service.registerOrUpdate(
                request.getId(),
                request.getName(),
                request.toRatingsMap()
        );

        HttpStatus status = exists ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(PlayerResponseDTO.from(player));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Player found"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<PlayerResponseDTO> getPlayer(
            @Parameter(description = "Player ID") @PathVariable String id) {

        return service.getPlayer(id)
                .map(p -> ResponseEntity.ok(PlayerResponseDTO.from(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all registered players")
    public ResponseEntity<List<PlayerResponseDTO>> getAllPlayers() {
        List<PlayerResponseDTO> players = service.getAllPlayers().stream()
                .map(PlayerResponseDTO::from)
                .toList();
        return ResponseEntity.ok(players);
    }

    @PatchMapping("/{id}/ratings")
    @Operation(summary = "Update player ratings",
            description = "Updates only the specified ratings, keeping others unchanged")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ratings updated"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<PlayerResponseDTO> updateRatings(
            @Parameter(description = "Player ID") @PathVariable String id,
            @Valid @RequestBody PlayerRequestDTO request) {

        try {
            Player player = service.updateRatings(id, request.toRatingsMap());
            return ResponseEntity.ok(PlayerResponseDTO.from(player));
        } catch (PlayerNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a player")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Player deleted"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<Void> deletePlayer(
            @Parameter(description = "Player ID") @PathVariable String id) {

        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        service.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get total player count")
    public ResponseEntity<Long> getPlayerCount() {
        return ResponseEntity.ok(service.getPlayerCount());
    }

}
