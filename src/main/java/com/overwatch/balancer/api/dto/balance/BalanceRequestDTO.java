package com.overwatch.balancer.api.dto.balance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to balance teams")
public class BalanceRequestDTO {

    @Schema(description = "Number of matches to create", example = "1", minimum = "1", maximum = "5")
    @Min(1) @Max(5)
    private Integer matchCount = 1;

    @Schema(description = "Map game mode filter (optional)", example = "CONTROL")
    private String mapMode;

}
