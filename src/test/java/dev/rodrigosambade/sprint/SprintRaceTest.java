package dev.rodrigosambade.sprint;

import java.time.Duration;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SprintRaceTest {

    @Test
    void returnsEveryRunnerSortedByElapsedTime() throws InterruptedException {
        List<SprintRace.Athlete> athletes = List.of(
                new SprintRace.Athlete("A", 1),
                new SprintRace.Athlete("B", 2),
                new SprintRace.Athlete("C", 3));
        RandomGenerator random = RandomGeneratorFactory
                .<RandomGenerator>of("L64X128MixRandom")
                .create(1);
        SprintRace race = new SprintRace(athletes, random);

        List<SprintRace.Result> results = race.run(
                Duration.ZERO,
                Duration.ofMillis(2));

        assertEquals(3, results.size());
        for (int index = 1; index < results.size(); index++) {
            assertTrue(results.get(index - 1).seconds() <= results.get(index).seconds());
        }
    }
}
