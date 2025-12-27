package com.overwatch.balancer.domain.model;

import com.overwatch.balancer.service.LobbyService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Lobby {

    private final String id;
    private final LobbyService service;

    public boolean join(String playerId) {
        return service.join(id, playerId);
    }

    public boolean leave(String playerId) {
        return service.leave(id, playerId);
    }

    public List<Player> getPlayers() {
        return service.getPlayers(id);
    }

    public int getPlayerCount() {
        return service.getPlayerCount(id);
    }

    public boolean canStartMatch() {
        return service.canStartMatch(id);
    }

    public LobbyService.QueueStatus getQueueStatus() {
        return service.getQueueStatus(id);
    }

}
