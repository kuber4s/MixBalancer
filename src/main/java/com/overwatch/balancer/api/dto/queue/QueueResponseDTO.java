package com.overwatch.balancer.api.dto.queue;

import com.overwatch.balancer.api.dto.player.PlayerResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Queue status showing who plays next")
public class QueueResponseDTO {

    @Schema(description = "Lobby ID")
    private String lobbyId;

    @Schema(description = "Total players")
    private int totalPlayers;

    @Schema(description = "Players who will play next match")
    private List<PlayerResponseDTO> playingNext;

    @Schema(description = "Players waiting in queue")
    private List<PlayerResponseDTO> waiting;

    @Schema(description = "Can start a match")
    private boolean canStart;

    @Schema(description = "Players needed")
    private int playersNeeded;

    @Schema(description = "Role availability")
    private Map<String, Long> roleAvailability;

}
