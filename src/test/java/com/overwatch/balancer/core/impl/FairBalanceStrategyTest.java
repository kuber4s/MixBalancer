package com.overwatch.balancer.core.impl;

import com.overwatch.balancer.core.BalanceStrategy;
import com.overwatch.balancer.domain.enumeration.Role;
import com.overwatch.balancer.domain.model.Player;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FairBalanceStrategy")
class FairBalanceStrategyTest {

    private FairBalanceStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FairBalanceStrategy(1000);
    }

    @Nested
    @DisplayName("balance")
    class Balance {

        @Test
        @DisplayName("should balance 10 players into 2 teams")
        void shouldBalance10Players() {
            List<Player> players = createBalancedPlayers(10);

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();
            assertThat(result.get().team1().isFull()).isTrue();
            assertThat(result.get().team2().isFull()).isTrue();
        }

        @RepeatedTest(20)
        @DisplayName("should produce balanced teams with low SR difference")
        void shouldProduceBalancedTeams() {
            List<Player> players = createBalancedPlayers(10);

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();
            assertThat(result.get().metrics().overallSRDiff()).isLessThan(300);
        }

        @RepeatedTest(20)
        @DisplayName("should minimize tank SR difference")
        void shouldMinimizeTankDifference() {
            List<Player> players = createBalancedPlayers(10);

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();

            assertThat(result.get().metrics().tankSRDiff()).isLessThan(600);
        }

        @Test
        @DisplayName("should return empty if not enough players")
        void shouldReturnEmptyIfNotEnoughPlayers() {
            List<Player> players = createBalancedPlayers(5);

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty if not enough tanks")
        void shouldReturnEmptyIfNotEnoughTanks() {
            List<Player> players = new ArrayList<>();
            players.add(createPlayer("1", "KSAA", Role.TANK, 4000));
            //only 1 tank, need 2
            players.add(createPlayer("2", "Povelitel", Role.DPS, 3500));
            players.add(createPlayer("3", "Txao", Role.DPS, 3500));
            players.add(createPlayer("4", "NGINX", Role.DPS, 3500));
            players.add(createPlayer("5", "GetMads", Role.DPS, 3500));
            players.add(createPlayer("6", "YKSZETA", Role.DPS, 3500));
            players.add(createPlayer("7", "yreen", Role.SUPPORT, 3500));
            players.add(createPlayer("8", "SACR1FICED", Role.SUPPORT, 3500));
            players.add(createPlayer("9", "Pr1de", Role.SUPPORT, 3500));
            players.add(createPlayer("10", "FunnyAstro", Role.SUPPORT, 3500));

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle extra players as queue")
        void shouldHandleExtraPlayersAsQueue() {
            List<Player> players = createBalancedPlayers(15);

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();
            assertThat(result.get().queue()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("priority")
    class Priority {

        @Test
        @DisplayName("should prioritize players with skipped games")
        void shouldPrioritizeSkippedPlayers() {
            List<Player> players = createBalancedPlayers(15);

            // give high priority to players 12, 13, 14
            for (int i = 12; i < 15; i++) {
                players.get(i).recordGameSkipped();
                players.get(i).recordGameSkipped();
            }

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();

            //at least some priority players should be in match
            List<Player> playing = new ArrayList<>();
            playing.addAll(result.get().team1().getPlayers());
            playing.addAll(result.get().team2().getPlayers());

            long priorityInMatch = playing.stream()
                    .filter(p -> p.getGamesSkipped() > 0)
                    .count();

            // !!!this is probabilistic, but high priority should usually get in
            assertThat(priorityInMatch).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("flex players")
    class FlexPlayers {

        @Test
        @DisplayName("should use flex players to fill roles")
        void shouldUseFlexPlayers() {
            List<Player> players = new ArrayList<>();

            //1 pure tank
            players.add(createPlayer("1", "NGINX", Role.TANK, 4000));

            // 1 flex player (can tank + dps)
            Player flex = createPlayer("2", "Povelitel", Role.DPS, 3800);
            flex.setRating(Role.TANK, 3600);
            players.add(flex);

            // 4 DPS
            players.add(createPlayer("3", "Txao", Role.DPS, 3500));
            players.add(createPlayer("4", "KSAA", Role.DPS, 3600));
            players.add(createPlayer("5", "GetMads", Role.DPS, 3700));
            players.add(createPlayer("6", "YKSZETA", Role.DPS, 3800));

            // 4 Support
            players.add(createPlayer("7", "yreen", Role.SUPPORT, 3500));
            players.add(createPlayer("8", "SACR1FICED", Role.SUPPORT, 3600));
            players.add(createPlayer("9", "Pr1de", Role.SUPPORT, 3700));
            players.add(createPlayer("10", "FunnyAstro", Role.SUPPORT, 3800));

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();
            assertThat(result.get().team1().isFull()).isTrue();
            assertThat(result.get().team2().isFull()).isTrue();
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle wide SR range")
        void shouldHandleWideSRRange() {
            List<Player> players = new ArrayList<>();

            // 2 tanks with big SR gap
            players.add(createPlayer("1", "Txao", Role.TANK, 4500));
            players.add(createPlayer("2", "NGINX", Role.TANK, 2500));

            // 4 DPS mixed
            players.add(createPlayer("3", "AoD", Role.DPS, 4200));
            players.add(createPlayer("4", "ScorpixSHOW", Role.DPS, 4000));
            players.add(createPlayer("5", "GetMads", Role.DPS, 2800));
            players.add(createPlayer("6", "Xapu3ma", Role.DPS, 2600));

            // 4 Support mixed
            players.add(createPlayer("7", "Povelitel", Role.SUPPORT, 4100));
            players.add(createPlayer("8", "ML7", Role.SUPPORT, 3900));
            players.add(createPlayer("9", "Awkward", Role.SUPPORT, 2700));
            players.add(createPlayer("10", "Galaa", Role.SUPPORT, 2500));

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();
            // anyway should still balance, though not perfectly!
            assertThat(result.get().team1().isFull()).isTrue();
            assertThat(result.get().team2().isFull()).isTrue();
        }

        @Test
        @DisplayName("should handle all same SR")
        void shouldHandleSameSR() {
            List<Player> players = new ArrayList<>();
            int sr = 3500;

            players.add(createPlayer("1", "KSAA", Role.TANK, sr));
            players.add(createPlayer("2", "Povelitel", Role.TANK, sr));
            players.add(createPlayer("3", "Txao", Role.DPS, sr));
            players.add(createPlayer("4", "NGINX", Role.DPS, sr));
            players.add(createPlayer("5", "GetMads", Role.DPS, sr));
            players.add(createPlayer("6", "Pr1de", Role.DPS, sr));
            players.add(createPlayer("7", "yreen", Role.SUPPORT, sr));
            players.add(createPlayer("8", "Winton", Role.SUPPORT, sr));
            players.add(createPlayer("9", "Qurare", Role.SUPPORT, sr));
            players.add(createPlayer("10", "Dafran", Role.SUPPORT, sr));

            Optional<BalanceStrategy.BalanceResult> result = strategy.balance(players);

            assertThat(result).isPresent();
            assertThat(result.get().metrics().overallSRDiff()).isZero();
        }
    }

    private static final String[] PLAYER_NAMES = {
            "NGINX", "yreen", "Txao", "KSAA", "GetMads", "Pr1de", "Povelitel",
            "Xapu3ma", "ScorpixSHOW", "Kevster", "ShockWave", "Averet", "LhCloudy", "Hadi", "YKSZETA"
    };

    private List<Player> createBalancedPlayers(int count) {
        List<Player> players = new ArrayList<>();

        int tanks = Math.max(2, count / 5);
        int dps = count * 2 / 5;
        int supports = count - tanks - dps;

        int nameIndex = 0;
        for (int i = 0; i < tanks; i++) {
            String name = PLAYER_NAMES[nameIndex++ % PLAYER_NAMES.length];
            players.add(createPlayer(String.valueOf(nameIndex), name, Role.TANK, 3500 + i * 100));
        }
        for (int i = 0; i < dps; i++) {
            String name = PLAYER_NAMES[nameIndex++ % PLAYER_NAMES.length];
            players.add(createPlayer(String.valueOf(nameIndex), name, Role.DPS, 3500 + i * 50));
        }
        for (int i = 0; i < supports; i++) {
            String name = PLAYER_NAMES[nameIndex++ % PLAYER_NAMES.length];
            players.add(createPlayer(String.valueOf(nameIndex), name, Role.SUPPORT, 3500 + i * 50));
        }

        return players;
    }

    private Player createPlayer(String id, String name, Role role, int sr) {
        Player p = new Player(id, name);
        p.setRating(role, sr);
        return p;
    }

}