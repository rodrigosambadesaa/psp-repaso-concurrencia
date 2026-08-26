package dev.rodrigosambade.sprint;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.random.RandomGenerator;

public final class SprintRace {

    public record Athlete(String name, int bib) {
        public Athlete {
            Objects.requireNonNull(name, "name");
        }
    }

    public record Result(Athlete athlete, double seconds) {
    }

    private final List<Athlete> athletes;
    private final RandomGenerator random;

    public SprintRace(List<Athlete> athletes, RandomGenerator random) {
        Objects.requireNonNull(athletes, "athletes");
        if (athletes.isEmpty()) {
            throw new IllegalArgumentException("At least one athlete is required");
        }

        this.athletes = List.copyOf(athletes);
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<Result> run(Duration minimum, Duration maximum)
            throws InterruptedException {
        validateDurations(minimum, maximum);

        CountDownLatch startingGun = new CountDownLatch(1);
        Queue<Result> results = new ConcurrentLinkedQueue<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(athletes.size())) {
            List<Future<?>> tasks = submitAthletes(
                    executor,
                    startingGun,
                    results,
                    minimum,
                    maximum);

            announceStart(minimum);
            startingGun.countDown();
            waitForAll(tasks);
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(Result::seconds))
                .toList();
    }

    private List<Future<?>> submitAthletes(
            ExecutorService executor,
            CountDownLatch startingGun,
            Queue<Result> results,
            Duration minimum,
            Duration maximum) {
        List<Future<?>> tasks = new ArrayList<>(athletes.size());

        for (Athlete athlete : athletes) {
            Duration runningTime = randomRunningTime(minimum, maximum);
            tasks.add(executor.submit(
                    () -> runAthlete(athlete, runningTime, startingGun, results)));
        }

        return tasks;
    }

    private static void runAthlete(
            Athlete athlete,
            Duration runningTime,
            CountDownLatch startingGun,
            Queue<Result> results) {
        try {
            startingGun.await();
            long startNanos = System.nanoTime();
            Thread.sleep(runningTime);
            long elapsedNanos = System.nanoTime() - startNanos;

            results.add(new Result(
                    athlete,
                    elapsedNanos / 1_000_000_000.0));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Athlete interrupted", exception);
        }
    }

    private Duration randomRunningTime(Duration minimum, Duration maximum) {
        long minMillis = minimum.toMillis();
        long maxMillis = maximum.toMillis();

        if (minMillis == maxMillis) {
            return minimum;
        }

        long millis = minMillis + random.nextLong(maxMillis - minMillis + 1);
        return Duration.ofMillis(millis);
    }

    private static void announceStart(Duration minimum) throws InterruptedException {
        Duration pause = Duration.ofMillis(
                Math.min(100, Math.max(0, minimum.toMillis() / 10)));

        System.out.println("Preparados");
        Thread.sleep(pause);
        System.out.println("Listos");
        Thread.sleep(pause);
        System.out.println("¡Ya!");
    }

    private static void waitForAll(List<Future<?>> tasks) throws InterruptedException {
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (ExecutionException exception) {
                throw new IllegalStateException("Athlete task failed", exception.getCause());
            }
        }
    }

    private static void validateDurations(Duration minimum, Duration maximum) {
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(maximum, "maximum");

        if (minimum.isNegative() || maximum.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(
                    "maximum duration must be greater than or equal to minimum duration");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Athlete> athletes = createDemoAthletes();
        SprintRace race = new SprintRace(athletes, RandomGenerator.getDefault());

        List<Result> results = race.run(
                Duration.ofMillis(90),
                Duration.ofMillis(120));

        for (Result result : results) {
            System.out.printf(
                    Locale.ROOT,
                    "%d tarda %.3f s%n",
                    result.athlete().bib(),
                    result.seconds());
        }
    }

    private static List<Athlete> createDemoAthletes() {
        List<Athlete> athletes = new ArrayList<>(8);
        for (int bib = 1; bib <= 8; bib++) {
            athletes.add(new Athlete("Atleta " + bib, bib));
        }
        return athletes;
    }
}
