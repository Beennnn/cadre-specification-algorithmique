// Implementation of SPEC-SPD-001 — Average speed of a journey, version 1.0.0.
//
// Each element carries @ImplementsSpec with the identifiers it covers. That annotation
// is what the coverage tool reads: starting from a rule you find the code, and starting
// from the code you find the rule and why it says what it says.

package method.averagespeed;

import method.spec.ImplementsSpec;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@ImplementsSpec({"FN-001", "FN-002"})
public final class AverageSpeed {

    /** P-01 — rounding mode of the published speed. */
    @ImplementsSpec("P-01")
    static final RoundingMode P_01_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /** P-02 — decimals of the published speed. */
    @ImplementsSpec("P-02")
    static final int P_02_DECIMALS = 1;

    /** Working precision for the unrounded quantities of RG-010 and RG-020. */
    private static final MathContext WORKING = new MathContext(20);

    public record Leg(BigDecimal distance, BigDecimal speed) {}

    public record Result(BigDecimal totalDistance,
                         BigDecimal totalDuration,
                         BigDecimal averageSpeed) {}

    public static final class BusinessError extends RuntimeException {
        public final String code;
        BusinessError(String code, String message) {
            super(code + " — " + message);
            this.code = code;
        }
    }

    private AverageSpeed() {}

    /** RG-010 — duration of one leg. Never rounded: only RG-030 rounds. */
    @ImplementsSpec("RG-010")
    private static BigDecimal legDuration(Leg leg) {
        return leg.distance().divide(leg.speed(), WORKING);
    }

    @ImplementsSpec({"RG-020", "RG-030", "E-SPD-001"})
    public static Result compute(List<Leg> legs) {
        // E-SPD-001 — a null or negative speed means an infinite duration.
        for (Leg leg : legs)
            if (leg.speed().compareTo(BigDecimal.ZERO) <= 0)
                throw new BusinessError("E-SPD-001",
                        "leg speed must be strictly positive, found " + leg.speed());

        // RG-020 — journey totals.
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalDuration = BigDecimal.ZERO;
        for (Leg leg : legs) {
            totalDistance = totalDistance.add(leg.distance());
            totalDuration = totalDuration.add(legDuration(leg));
        }

        // RG-030 — average speed. Total distance over total duration, NOT the mean of
        // the leg speeds: the slow legs take longer, so they weigh more.
        BigDecimal averageSpeed = totalDistance
                .divide(totalDuration, P_02_DECIMALS, P_01_ROUNDING_MODE);

        return new Result(totalDistance, totalDuration, averageSpeed);
    }
}
