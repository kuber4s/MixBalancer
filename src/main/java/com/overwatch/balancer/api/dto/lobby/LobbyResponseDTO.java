package com.overwatch.balancer.api.dto.lobby;

import com.overwatch.balancer.api.dto.player.PlayerResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Lobby status")
public class LobbyResponseDTO {

    @Schema(description = "Lobby ID")
    private String lobbyId;

    @Schema(description = "Total players in lobby")
    private int playerCount;

    @Schema(description = "Players in lobby")
    private List<PlayerResponseDTO> players;

    @Schema(description = "Can start a match")
    private boolean canStart;

    @Schema(description = "Players needed to start")
    private int playersNeeded;

    @Schema(description = "Maximum matches possible")
    private int maxMatches;

}
