// Implementation of SPEC-WTH-001 — Daily weather station summary, version 1.1.0.
//
// Every element carries @ImplementsSpec with the identifiers it covers: starting from a
// rule you find the code, and starting from the code you find the rule and why it says
// what it says.
//
// What this file does NOT decide, because the specification already did: the rejection
// factor and the number of rounds (P-01, P-02), what happens when the loop runs out
// (RG-060 labels, it does not fail), the strictness of the frost threshold (P-06, with
// "<" and not "≤"), and that SUSPECT readings are discarded rather than kept.
//
// What this file does decide, and the specification says nothing about: double precision
// (EX-01 says it is enough), records rather than classes, a single pass over the readings,
// the fact that the parameters are passed as a value rather than read from constants.
// See CADRE.md §1.4.

package method.weathersummary;

import method.spec.ImplementsSpec;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WeatherSummary {

    /** The physical range of RG-010. Outside it, a reading is not a temperature. */
    private static final double PHYSICAL_MIN = -90.0;
    private static final double PHYSICAL_MAX = 60.0;

    public enum Flag { VALID, SUSPECT, FAULTY }

    public enum QualityLabel { GOOD, ACCEPTABLE, SUSPECT }

    /**
     * A reading. {@code temperature} is {@code null} when the value is ABSENT.
     *
     * <p>ABSENT is a value of its own, never −999: the specification bans the impossible
     * number precisely because it survives every range check ever written.
     */
    public record Reading(LocalDateTime recordedAt, Double temperature, Flag qualityFlag) {}

    public record Request(String stationId, LocalDate observationDate, List<Reading> readings) {}

    public record FrostEpisode(LocalDateTime startedAt, LocalDateTime endedAt, int readingCount) {}

    public record Result(String stationId,
                         int retainedCount,
                         int rejectedCount,
                         BigDecimal meanTemperature,
                         double minTemperature,
                         double maxTemperature,
                         List<FrostEpisode> frostEpisodes,
                         QualityLabel qualityLabel) {}

    /**
     * The six parameters of §6, as one value.
     *
     * <p>They are passed rather than read from constants for one reason: it lets the
     * qualification harness re-run a case with one parameter changed and report whether
     * that case actually <em>decides</em> the parameter. A parameter no case arbitrates is
     * approved, dated, implemented — and verified by nothing.
     */
    public record Parameters(double p01RejectionFactor,
                             int p02MaxRounds,
                             int p03MinReadings,
                             RoundingMode p04RoundingMode,
                             int p05Decimals,
                             double p06FrostThreshold) {}

    /** The approved values of §6, effective 2026-01-01. */
    @ImplementsSpec({"P-01", "P-02", "P-03", "P-04", "P-05", "P-06"})
    public static final Parameters APPROVED =
            new Parameters(2.0, 5, 6, RoundingMode.HALF_EVEN, 1, 0.0);

    /** A business error of §9: it carries its identifier, not only a message. */
    public static final class BusinessError extends RuntimeException {
        public final String code;
        BusinessError(String code, String message) {
            super(code + " — " + message);
            this.code = code;
        }
    }

    private WeatherSummary() {}

    public static Result summarise(Request request) {
        return summarise(request, APPROVED);
    }

    // --- Preconditions (§4, §9) ---------------------------------------------

    @ImplementsSpec({"E-WTH-001", "E-WTH-002"})
    private static void checkPreconditions(Request request) {
        // E-WTH-001 — two readings at the same instant would leave "the first of the run"
        // in RG-050 undefined. The check is on the raw input, before any filtering:
        // a duplicate is a defect of the acquisition chain, not a reading to discard.
        Set<LocalDateTime> seen = new HashSet<>();
        for (Reading r : request.readings())
            if (!seen.add(r.recordedAt()))
                throw new BusinessError("E-WTH-001",
                        "two readings share the instant " + r.recordedAt());

        // E-WTH-002 — the summary describes one day. A reading from another one would
        // make it describe two, silently.
        for (Reading r : request.readings())
            if (!r.recordedAt().toLocalDate().equals(request.observationDate()))
                throw new BusinessError("E-WTH-002",
                        "reading at " + r.recordedAt() + " is outside the observation date "
                                + request.observationDate());
    }

    // --- The rules ----------------------------------------------------------

    /** RG-010 — usable readings: present, VALID, and physically possible. */
    @ImplementsSpec("RG-010")
    private static List<Reading> usable(List<Reading> readings) {
        List<Reading> kept = new ArrayList<>();
        for (Reading r : readings) {
            if (r.temperature() == null) continue;                 // ABSENT
            if (r.qualityFlag() != Flag.VALID) continue;           // SUSPECT or FAULTY
            double t = r.temperature();
            if (t < PHYSICAL_MIN || t > PHYSICAL_MAX) continue;    // BETWEEN is inclusive
            kept.add(r);
        }
        // Chronological order is fixed HERE, once, and everything downstream inherits it.
        // The specification only asks for it in RG-050, but INV-04 asks for more than
        // that: double-precision addition is not associative, so a mean summed in the
        // order the readings happened to arrive is not the same number twice. Sorting is
        // a technical decision — it changes no result — and it is what makes the
        // replayability of EX-03 hold at all. E-WTH-001 is what makes the order total.
        kept.sort(Comparator.comparing(Reading::recordedAt));
        return kept;
    }

    /** Sample standard deviation, divisor n − 1, as RG-030 requires. */
    private static double sampleStandardDeviation(List<Reading> readings, double mean) {
        double sum = 0.0;
        for (Reading r : readings) {
            double d = r.temperature() - mean;
            sum += d * d;
        }
        return Math.sqrt(sum / (readings.size() - 1));
    }

    private static double mean(List<Reading> readings) {
        double sum = 0.0;
        for (Reading r : readings) sum += r.temperature();
        return sum / readings.size();
    }

    /** The outcome of RG-030: what is left, and whether the loop converged. */
    private record Clipping(List<Reading> retained, boolean converged) {}

    /**
     * RG-030 — iterative outlier rejection.
     *
     * The loop is genuinely iterative: removing an outlier changes the mean, which can
     * expose another. It stops when a round discards nothing, or after P-02 rounds — and
     * running out is NOT a failure. RG-060 labels the result SUSPECT and it is published.
     */
    @ImplementsSpec("RG-030")
    private static Clipping clip(List<Reading> usable, Parameters p) {
        List<Reading> retained = new ArrayList<>(usable);
        int roundNumber = 0;
        boolean converged = false;
        while (!converged && roundNumber < p.p02MaxRounds()) {
            double meanNow = mean(retained);
            double spread = sampleStandardDeviation(retained, meanNow);
            // The bound is STRICT: a reading at exactly P-01 deviations is kept.
            List<Reading> kept = new ArrayList<>();
            for (Reading r : retained)
                if (!(Math.abs(r.temperature() - meanNow) > p.p01RejectionFactor() * spread))
                    kept.add(r);
            if (kept.size() == retained.size()) {
                converged = true;
            } else {
                retained = kept;
                roundNumber++;
            }
        }
        return new Clipping(retained, converged);
    }

    /**
     * RG-050 — frost episodes: maximal runs of consecutive readings strictly below P-06,
     * in chronological order.
     *
     * The order comes from recorded_at, never from the order of the input — that is what
     * INV-04 requires, and E-WTH-001 is what makes "the first of the run" designate one
     * reading and only one.
     */
    @ImplementsSpec("RG-050")
    private static List<FrostEpisode> frostEpisodes(List<Reading> retained, Parameters p) {
        List<Reading> chronological = new ArrayList<>(retained);
        chronological.sort(Comparator.comparing(Reading::recordedAt));

        List<FrostEpisode> episodes = new ArrayList<>();
        List<Reading> run = new ArrayList<>();
        for (Reading r : chronological) {
            // STRICTLY below: a reading at exactly 0.0 °C is not frost.
            if (r.temperature() < p.p06FrostThreshold()) {
                run.add(r);
            } else if (!run.isEmpty()) {
                episodes.add(episodeOf(run));
                run = new ArrayList<>();
            }
        }
        if (!run.isEmpty()) episodes.add(episodeOf(run));
        return List.copyOf(episodes);
    }

    private static FrostEpisode episodeOf(List<Reading> run) {
        return new FrostEpisode(run.get(0).recordedAt(),
                                run.get(run.size() - 1).recordedAt(),
                                run.size());
    }

    /** RG-005, RG-020, RG-040 and RG-060, in the order the business states them. */
    @ImplementsSpec({"RG-005", "RG-020", "RG-040", "RG-060", "E-WTH-003"})
    static Result summarise(Request request, Parameters p) {
        checkPreconditions(request);

        List<Reading> usable = usable(request.readings());                     // RG-010

        // RG-020 — publishing nothing beats publishing a mean over two readings.
        if (usable.size() < p.p03MinReadings())
            throw new BusinessError("E-WTH-003",
                    "only " + usable.size() + " usable reading(s), " + p.p03MinReadings()
                            + " required to publish a summary");

        Clipping clipping = clip(usable, p);                                   // RG-030
        List<Reading> retained = clipping.retained();

        // RG-040 — the statistics, on the retained readings only. Only the mean is
        // rounded: the minimum and the maximum are readings, published as measured.
        int retainedCount = retained.size();
        int rejectedCount = usable.size() - retainedCount;
        // The PUBLISHED mean is computed in exact decimal arithmetic, and this is not a
        // stylistic preference. It is the one quantity here subject to a rounding
        // decision, and a rounding decision taken on a float that is a hair off the tie
        // goes the wrong way: 0.25 summed in double can land at 0.2500000000000001, which
        // HALF_EVEN sends to 0.3 instead of 0.2. Readings carry one decimal, so their sum
        // is exact in decimal and the division rounds correctly by construction.
        // The clipping statistics of RG-030 stay in double: they feed a comparison
        // against a threshold, not a published number. See EX-01.
        BigDecimal exactSum = BigDecimal.ZERO;
        for (Reading r : retained) exactSum = exactSum.add(BigDecimal.valueOf(r.temperature()));
        BigDecimal meanTemperature = exactSum.divide(
                BigDecimal.valueOf(retained.size()), p.p05Decimals(), p.p04RoundingMode());
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (Reading r : retained) {
            min = Math.min(min, r.temperature());
            max = Math.max(max, r.temperature());
        }

        List<FrostEpisode> episodes = frostEpisodes(retained, p);              // RG-050

        // RG-060 — what the summary is worth. The three branches of the decision table,
        // in its order: non-convergence first, because it outranks everything else.
        QualityLabel label = !clipping.converged() ? QualityLabel.SUSPECT
                           : rejectedCount == 0    ? QualityLabel.GOOD
                                                   : QualityLabel.ACCEPTABLE;

        // RG-005 — the summary carries the station it describes, unchanged.
        return new Result(request.stationId(), retainedCount, rejectedCount,
                          meanTemperature, min, max, episodes, label);
    }
}
