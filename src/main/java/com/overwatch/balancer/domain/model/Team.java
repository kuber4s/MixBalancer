package com.overwatch.balancer.domain.model;

import com.overwatch.balancer.domain.enumeration.Role;
import lombok.Getter;

import java.util.*;

@Getter
public class Team {

    public static final int SIZE = 5;

    private final String name;
    private final String emoji;
    private final Map<Role, List<TeamSlot>> roster;

    public Team(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
        this.roster = new EnumMap<>(Role.class);

        for (Role role : Role.values()) {
            roster.put(role, new ArrayList<>());
        }
    }

    public record TeamSlot(Player player, Role role, int sr) {
        public static TeamSlot of(Player player, Role role) {
            return new TeamSlot(player, role, player.getRating(role));
        }
    }

    public boolean addPlayer(Player player, Role role) {
        List<TeamSlot> slots = roster.get(role);

        if (slots.size() >= role.getSlotsPerTeam()) {
            return false;
        }

        if (!player.canPlay(role)) {
            return false;
        }

        if (hasPlayer(player)) {
            return false;
        }

        slots.add(TeamSlot.of(player, role));
        return true;
    }

    public boolean hasPlayer(Player player) {
        return roster.values().stream()
                .flatMap(List::stream)
                .anyMatch(slot -> slot.player().equals(player));
    }

    public Optional<TeamSlot> getSlot(Role role, int index) {
        List<TeamSlot> slots = roster.get(role);
        if (index < 0 || index >= slots.size()) {
            return Optional.empty();
        }
        return Optional.of(slots.get(index));
    }

    public List<TeamSlot> getSlots(Role role) {
        return Collections.unmodifiableList(roster.get(role));
    }

    public int getFreeSlots(Role role) {
        return role.getSlotsPerTeam() - roster.get(role).size();
    }

    public boolean isFull() {
        return getPlayerCount() == SIZE;
    }

    public int getPlayerCount() {
        return roster.values().stream().mapToInt(List::size).sum();
    }

    public List<Player> getPlayers() {
        return roster.values().stream()
                .flatMap(List::stream)
                .map(TeamSlot::player)
                .toList();
    }

    public List<String> getPlayerIds() {
        return getPlayers().stream()
                .map(Player::getId)
                .toList();
    }

    // ==================== SR Calculations ====================

    /**
     * Total SR of the team.
     */
    public int getTotalSR() {
        return roster.values().stream()
                .flatMap(List::stream)
                .mapToInt(TeamSlot::sr)
                .sum();
    }

    /**
     * Average SR of the team.
     */
    public double getAverageSR() {
        int count = getPlayerCount();
        return count > 0 ? (double) getTotalSR() / count : 0;
    }

    /**
     * Total SR for a specific role.
     */
    public int getRoleTotalSR(Role role) {
        return roster.get(role).stream()
                .mapToInt(TeamSlot::sr)
                .sum();
    }

    /**
     * Average SR for a specific role.
     */
    public double getRoleAverageSR(Role role) {
        List<TeamSlot> slots = roster.get(role);
        if (slots.isEmpty()) return 0;

        return slots.stream()
                .mapToInt(TeamSlot::sr)
                .average()
                .orElse(0);
    }

    public Player getTank() {
        List<TeamSlot> tanks = roster.get(Role.TANK);
        return tanks.isEmpty() ? null : tanks.get(0).player();
    }

    public int getTankSR() {
        Player tank = getTank();
        return tank != null ? tank.getRating(Role.TANK) : 0;
    }

    // ==================== Display ====================

    /**
     * Formats team for display.
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(emoji).append(" **").append(name).append("**");
        sb.append(" (Avg SR: ").append((int) getAverageSR()).append(")\n");

        for (Role role : Role.values()) {
            sb.append(role.getEmoji()).append(" ");

            List<TeamSlot> slots = roster.get(role);
            if (slots.isEmpty()) {
                sb.append("_empty_");
            } else {
                StringJoiner sj = new StringJoiner(", ");
                for (TeamSlot slot : slots) {
                    sj.add(slot.player().getName() + " (" + slot.sr() + ")");
                }
                sb.append(sj);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Compact format for API.
     */
    public String toCompactString() {
        List<String> playerNames = getPlayers().stream()
                .map(Player::getName)
                .toList();
        return name + ": " + String.join(", ", playerNames) + " (avg: " + (int) getAverageSR() + ")";
    }

    @Override
    public String toString() {
        return name + " (" + getPlayerCount() + "/" + SIZE + " players)";
    }

}
