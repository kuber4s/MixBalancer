package com.overwatch.balancer.api.dto.map;

import com.overwatch.balancer.domain.enumeration.GameMap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Map information")
public class MapResponseDTO {

    @Schema(description = "Map name")
    private String name;

    @Schema(description = "Game mode")
    private String mode;

    @Schema(description = "Mode emoji")
    private String emoji;

    public static MapResponseDTO from(GameMap map) {
        return MapResponseDTO.builder()
                .name(map.getDisplayName())
                .mode(map.getMode().getDisplayName())
                .emoji(map.getMode().getEmoji())
                .build();
    }

}
