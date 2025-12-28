package com.overwatch.balancer.api.dto.player;

import com.overwatch.balancer.domain.enumeration.Role;
import com.overwatch.balancer.domain.model.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Player information")
public class PlayerResponseDTO {

    @Schema(description = "Player ID")
    private String id;

    @Schema(description = "Display name")
    private String name;

    @Schema(description = "Tank SR")
    private int tankSR;

    @Schema(description = "DPS SR")
    private int dpsSR;

    @Schema(description = "Support SR")
    private int supportSR;

    @Schema(description = "Primary role (highest SR)")
    private String primaryRole;

    @Schema(description = "Games played")
    private int gamesPlayed;

    @Schema(description = "Games skipped (waiting in queue)")
    private int gamesSkipped;

    @Schema(description = "Registration date")
    private Instant createdAt;

    public static PlayerResponseDTO from(Player player) {
        Role primary = player.getPrimaryRole();
        return PlayerResponseDTO.builder()
                .id(player.getId())
                .name(player.getName())
                .tankSR(player.getTankSR())
                .dpsSR(player.getDpsSR())
                .supportSR(player.getSupportSR())
                .primaryRole(primary != null ? primary.getName() : null)
                .gamesPlayed(player.getGamesPlayed())
                .gamesSkipped(player.getGamesSkipped())
                .createdAt(player.getCreatedAt())
                .build();
    }

}
