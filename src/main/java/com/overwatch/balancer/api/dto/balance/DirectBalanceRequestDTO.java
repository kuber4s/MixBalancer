package com.overwatch.balancer.api.dto.balance;

import com.overwatch.balancer.api.dto.player.PlayerRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to balance from player list (direct API)")
public class DirectBalanceRequestDTO {

    @NotEmpty(message = "At least 10 players required")
    @Size(min = 10, message = "Need at least 10 players")
    @Schema(description = "List of players to balance")
    private List<PlayerRequestDTO> players;

    @Schema(description = "Map game mode filter", example = "ESCORT")
    private String mapMode;

}
