package com.overwatch.balancer.api.dto.match;

import com.overwatch.balancer.domain.enumeration.Role;
import com.overwatch.balancer.domain.model.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Team information")
public class TeamResponseDTO {

    @Schema(description = "Team name")
    private String name;

    @Schema(description = "Team emoji")
    private String emoji;

    @Schema(description = "Average SR")
    private int averageSR;

    @Schema(description = "Tank player")
    private PlayerSlotResponseDTO tank;

    @Schema(description = "DPS players")
    private List<PlayerSlotResponseDTO> dps;

    @Schema(description = "Support players")
    private List<PlayerSlotResponseDTO> support;

    public static TeamResponseDTO from(Team team) {
        return TeamResponseDTO.builder()
                .name(team.getName())
                .emoji(team.getEmoji())
                .averageSR((int) team.getAverageSR())
                .tank(team.getSlots(Role.TANK).stream()
                        .findFirst()
                        .map(PlayerSlotResponseDTO::from)
                        .orElse(null))
                .dps(team.getSlots(Role.DPS).stream()
                        .map(PlayerSlotResponseDTO::from)
                        .toList())
                .support(team.getSlots(Role.SUPPORT).stream()
                        .map(PlayerSlotResponseDTO::from)
                        .toList())
                .build();
    }

}
