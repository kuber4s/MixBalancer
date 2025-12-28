package com.overwatch.balancer.api.dto.lobby;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to join/leave a lobby")
public class LobbyJoinRequestDTO {

    @NotBlank(message = "Player ID is required")
    @Schema(description = "Player ID to join/leave", example = "123456789")
    private String playerId;

}
