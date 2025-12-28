package com.overwatch.balancer.api.dto.match;

import com.overwatch.balancer.api.dto.player.PlayerResponseDTO;
import com.overwatch.balancer.domain.model.Match;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Schema(description = "Match result")
public class MatchResponseDTO {

    @Schema(description = "Match ID")
    private String id;

    @Schema(description = "Lobby ID")
    private String lobbyId;

    @Schema(description = "Map name")
    private String mapName;

    @Schema(description = "Map game mode")
    private String mapMode;

    @Schema(description = "Team 1")
    private TeamResponseDTO team1;

    @Schema(description = "Team 2")
    private TeamResponseDTO team2;

    @Schema(description = "SR difference between teams")
    private int srDifference;

    @Schema(description = "Tank SR difference")
    private int tankSRDiff;

    @Schema(description = "DPS SR difference")
    private int dpsSRDiff;

    @Schema(description = "Support SR difference")
    private int supportSRDiff;

    @Schema(description = "Balance quality")
    private String balanceQuality;

    @Schema(description = "Players in queue")
    private List<PlayerResponseDTO> queue;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    public static MatchResponseDTO from(Match match) {
        MatchResponseDTOBuilder builder = MatchResponseDTO.builder()
                .id(match.getId())
                .lobbyId(match.getLobbyId())
                .mapName(match.getMapName())
                .mapMode(match.getMapMode())
                .srDifference(match.getSrDifference())
                .tankSRDiff(match.getTankSRDiff())
                .dpsSRDiff(match.getDpsSRDiff())
                .supportSRDiff(match.getSupportSRDiff())
                .balanceQuality(match.getBalanceQuality())
                .createdAt(match.getCreatedAt());

        //add teams if available
        if (match.getTeam1() != null) {
            builder.team1(TeamResponseDTO.from(match.getTeam1()));
        }
        if (match.getTeam2() != null) {
            builder.team2(TeamResponseDTO.from(match.getTeam2()));
        }
        if (match.getQueue() != null) {
            builder.queue(match.getQueue().stream()
                    .map(PlayerResponseDTO::from)
                    .toList());
        }

        return builder.build();
    }

}
