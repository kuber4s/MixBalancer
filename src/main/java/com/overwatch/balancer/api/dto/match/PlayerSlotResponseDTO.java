package com.overwatch.balancer.api.dto.match;

import com.overwatch.balancer.domain.model.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Player slot in a team")
public class PlayerSlotResponseDTO {

    @Schema(description = "Player ID")
    private String playerId;

    @Schema(description = "Player name")
    private String playerName;

    @Schema(description = "Assigned role")
    private String role;

    @Schema(description = "SR for this role")
    private int sr;

    public static PlayerSlotResponseDTO from(Team.TeamSlot slot) {
        return PlayerSlotResponseDTO.builder()
                .playerId(slot.player().getId())
                .playerName(slot.player().getName())
                .role(slot.role().getName())
                .sr(slot.sr())
                .build();
    }

}
