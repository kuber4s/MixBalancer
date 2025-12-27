package com.overwatch.balancer.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@RequiredArgsConstructor
public enum GameMap {

    ANTARCTIC_PENINSULA("Antarctic Peninsula", GameMode.CONTROL),
    BUSAN("Busan", GameMode.CONTROL),
    ILIOS("Ilios", GameMode.CONTROL),
    LIJIANG_TOWER("Lijiang Tower", GameMode.CONTROL),
    NEPAL("Nepal", GameMode.CONTROL),
    OASIS("Oasis", GameMode.CONTROL),
    SAMOA("Samoa", GameMode.CONTROL),

    // Escort Maps (Payload)
    CIRCUIT_ROYAL("Circuit Royal", GameMode.ESCORT),
    DORADO("Dorado", GameMode.ESCORT),
    HAVANA("Havana", GameMode.ESCORT),
    JUNKERTOWN("Junkertown", GameMode.ESCORT),
    RIALTO("Rialto", GameMode.ESCORT),
    ROUTE_66("Route 66", GameMode.ESCORT),
    SHAMBALI_MONASTERY("Shambali Monastery", GameMode.ESCORT),
    WATCHPOINT_GIBRALTAR("Watchpoint: Gibraltar", GameMode.ESCORT),

    // Flashpoint Maps
    NEW_JUNK_CITY("New Junk City", GameMode.FLASHPOINT),
    SURAVASA("Suravasa", GameMode.FLASHPOINT),

    // Hybrid Maps
    BLIZZARD_WORLD("Blizzard World", GameMode.HYBRID),
    EICHENWALDE("Eichenwalde", GameMode.HYBRID),
    HOLLYWOOD("Hollywood", GameMode.HYBRID),
    KINGS_ROW("King's Row", GameMode.HYBRID),
    MIDTOWN("Midtown", GameMode.HYBRID),
    NUMBANI("Numbani", GameMode.HYBRID),
    PARAISO("Paraíso", GameMode.HYBRID),

    // Push Maps
    COLOSSEO("Colosseo", GameMode.PUSH),
    ESPERANCA("Esperança", GameMode.PUSH),
    NEW_QUEEN_STREET("New Queen Street", GameMode.PUSH),
    RUNASAPI("Runasapi", GameMode.PUSH);

    private final String displayName;
    private final GameMode mode;

    /**
     * Selects a random map from all maps.
     */
    public static GameMap random() {
        GameMap[] maps = values();
        return maps[ThreadLocalRandom.current().nextInt(maps.length)];
    }

    public static GameMap randomByMode(GameMode mode) {
        List<GameMap> maps = getByMode(mode);
        if (maps.isEmpty()) {
            return random();
        }
        return maps.get(ThreadLocalRandom.current().nextInt(maps.size()));
    }

    public static List<GameMap> getByMode(GameMode mode) {
        return Arrays.stream(values())
                .filter(map -> map.mode == mode)
                .toList();
    }

    public static List<GameMap> getCompetitiveMaps() {
        return Arrays.stream(values())
                .filter(map -> map.mode != GameMode.FLASHPOINT)
                .toList();
    }

    @Override
    public String toString() {
        return displayName + " (" + mode.getDisplayName() + ")";
    }

}
