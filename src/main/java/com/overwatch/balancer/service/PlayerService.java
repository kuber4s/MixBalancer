package com.overwatch.balancer.service;

import com.overwatch.balancer.domain.enumeration.Role;
import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.exception.PlayerNotFoundException;
import com.overwatch.balancer.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    /**
     * registers a new player or updates existing
     *
     * @param id      unique identifier (e.g., discord ID)
     * @param name    display name
     * @param ratings role -> SR map
     * @return the player
     */
    public Player registerOrUpdate(String id, String name, Map<Role, Integer> ratings) {
        Player player = playerRepository.findById(id)
                .orElseGet(() -> new Player(id, name));

        player.setName(name);
        player.setRatings(ratings);

        Player saved = playerRepository.save(player);
        log.info("[Tech][INFO] Registered/updated player: {} with roles {}", name, ratings);

        return saved;
    }

    public Player updateRatings(String id, Map<Role, Integer> ratings) {
        Player player = getPlayerOrThrow(id);

        ratings.forEach(player::setRating);

        return playerRepository.save(player);
    }

    public Optional<Player> getPlayer(String id) {
        return playerRepository.findById(id);
    }

    public Player getPlayerOrThrow(String id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    public boolean exists(String id) {
        return playerRepository.existsById(id);
    }

    public List<Player> getPlayers(Collection<String> ids) {
        return playerRepository.findAllById(ids);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public void deletePlayer(String id) {
        playerRepository.deleteById(id);
        log.info("Deleted player: {}", id);
    }

    public long getPlayerCount() {
        return playerRepository.count();
    }

    public void recordGamesPlayed(Collection<Player> players) {
        players.forEach(Player::recordGamePlayed);
        playerRepository.saveAll(players);
    }

    public void recordGamesSkipped(Collection<Player> players) {
        players.forEach(Player::recordGameSkipped);
        playerRepository.saveAll(players);
    }

}
