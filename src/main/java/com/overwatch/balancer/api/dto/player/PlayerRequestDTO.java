package com.overwatch.balancer.api.dto.player;

import com.overwatch.balancer.domain.enumeration.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to register or update a player")
public class PlayerRequestDTO {

    @NotBlank(message = "Player ID is required")
    @Schema(description = "Unique player identifier (e.g., Discord ID)", example = "123456789")
    private String id;

    @NotBlank(message = "Player name is required")
    @Size(min = 1, max = 32, message = "Name must be 1-32 characters")
    @Schema(description = "Display name", example = "ProGamer")
    private String name;

    @Schema(description = "Tank SR (0 = doesn't play)", example = "4200", minimum = "0", maximum = "5000")
    @Min(0) @Max(5000)
    private Integer tankSR = 0;

    @Schema(description = "DPS SR (0 = doesn't play)", example = "3800", minimum = "0", maximum = "5000")
    @Min(0) @Max(5000)
    private Integer dpsSR = 0;

    @Schema(description = "Support SR (0 = doesn't play)", example = "4000", minimum = "0", maximum = "5000")
    @Min(0) @Max(5000)
    private Integer supportSR = 0;

    public Map<Role, Integer> toRatingsMap() {
        return Map.of(
                Role.TANK, tankSR != null ? tankSR : 0,
                Role.DPS, dpsSR != null ? dpsSR : 0,
                Role.SUPPORT, supportSR != null ? supportSR : 0
        );
    }

}
