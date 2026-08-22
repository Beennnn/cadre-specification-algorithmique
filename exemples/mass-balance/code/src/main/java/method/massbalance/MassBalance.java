// Implementation of SPEC-MAS-001 — Batch mass balance, version 1.0.0.
//
// Every element carries @ImplementsSpec with the identifiers it covers. That annotation
// is the only machine-readable link between the specification and the code: starting
// from a rule you find the code, and starting from the code you find the rule and why
// it says what it says.
//
// What this file does NOT decide, because the specification already did: the rounding
// mode (P-01), the residual bound (P-02), who receives the residual and how a tie is
// broken (RG-050). Those change results, so they are business decisions.
//
// What this file does decide, and the specification says nothing about: BigDecimal
// rather than an integer count of steps, the record layout, the traversal order, the
// fact that everything is computed in a single pass. See CADRE.md §1.4.

package method.massbalance;

import method.spec.ImplementsSpec;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MassBalance {

    // --- Parameters (§6 of the specification) -------------------------------
    // They live here as named constants, never inline in an expression: that is the
    // "a parameter is not a magic value" rule (C-05).

    /** P-01 — rounding mode for doses. HALF_EVEN cancels the systematic drift. */
    @ImplementsSpec("P-01")
    static final RoundingMode P_01_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /** P-02 — maximum tolerated residual, expressed in balance steps. */
    @ImplementsSpec("P-02")
    static final int P_02_MAX_RESIDUAL_STEPS = 3;

    // --- Contract (§4 and §5) -----------------------------------------------
    // The ISO/IEC 11404 families of the contract map here: Scaled → BigDecimal,
    // CharacterString → String, Sequence → List. BigDecimal is not a free choice:
    // the Scaled family requires exact decimal arithmetic (EX-01).

    public record Component(String componentId, BigDecimal targetMassFraction) {}

    public record Request(BigDecimal targetBatchMass,
                          BigDecimal balanceStep,
                          List<Component> components) {}

    public record Dispensed(String componentId,
                            BigDecimal nominalMass,
                            BigDecimal dispensedMass) {}

    public record Result(List<Dispensed> dispensed, BigDecimal residual) {}

    /** A business error of §9: it carries its identifier, not only a message. */
    public static final class BusinessError extends RuntimeException {
        public final String code;
        BusinessError(String code, String message) {
            super(code + " — " + message);
            this.code = code;
        }
    }

    private MassBalance() {}

    // --- Preconditions (§4) -------------------------------------------------

    @ImplementsSpec({"E-MAS-001", "E-MAS-003", "E-MAS-004"})
    private static void checkPreconditions(Request request) {
        // E-MAS-001 — the fractions add up to exactly 1.
        // compareTo, not equals: BigDecimal.equals tells 1.0 from 1.00.
        BigDecimal sum = request.components().stream()
                .map(Component::targetMassFraction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.ONE) != 0)
            throw new BusinessError("E-MAS-001",
                    "target mass fractions sum to " + sum + ", not exactly 1");

        // E-MAS-003 — identifiers are unique, otherwise RG-050 has nothing to break
        // the tie on.
        Set<String> seen = new HashSet<>();
        for (Component c : request.components())
            if (!seen.add(c.componentId()))
                throw new BusinessError("E-MAS-003",
                        "duplicate component_id: " + c.componentId());

        // E-MAS-004 — the target mass is an integer multiple of the balance step,
        // without which the exact conservation of INV-01 would be impossible.
        if (request.targetBatchMass().remainder(request.balanceStep())
                .compareTo(BigDecimal.ZERO) != 0)
            throw new BusinessError("E-MAS-004",
                    "target_batch_mass " + request.targetBatchMass()
                            + " is not a multiple of balance_step " + request.balanceStep());
    }

    // --- The rules ----------------------------------------------------------

    /** RG-010 — nominal mass of a component, unrounded. */
    @ImplementsSpec("RG-010")
    private static BigDecimal nominalMass(Request request, Component component) {
        return request.targetBatchMass().multiply(component.targetMassFraction());
    }

    /**
     * RG-020 — rounding to the balance step.
     * What is rounded is a NUMBER OF STEPS, not a mass: rounding to three decimals
     * would be wrong as soon as the step is not 0.001 kg.
     */
    @ImplementsSpec("RG-020")
    private static BigDecimal roundedMass(BigDecimal nominal, BigDecimal balanceStep) {
        BigDecimal steps = nominal.divide(balanceStep, 0, P_01_ROUNDING_MODE);
        return steps.multiply(balanceStep);
    }

    /** RG-050 — who receives the residual: largest fraction, then smallest identifier. */
    @ImplementsSpec("RG-050")
    private static String residualReceiver(List<Component> components) {
        return components.stream()
                .max(Comparator.comparing(Component::targetMassFraction)
                        // The tie-break is REVERSED here: max() keeps the last of the
                        // equals under this comparator, so identifiers are ordered
                        // descending for the SMALLEST one to win (RG-050).
                        .thenComparing(Comparator.comparing(Component::componentId).reversed()))
                .orElseThrow()
                .componentId();
    }

    /** Applies RG-010 to RG-050 in order, on a valid request. */
    @ImplementsSpec({"RG-030", "RG-040", "E-MAS-002"})
    public static Result compute(Request request) {
        checkPreconditions(request);

        List<Dispensed> rounded = new ArrayList<>();
        for (Component component : request.components()) {
            BigDecimal nominal = nominalMass(request, component);                   // RG-010
            BigDecimal step = roundedMass(nominal, request.balanceStep());          // RG-020
            rounded.add(new Dispensed(component.componentId(), nominal, step));
        }

        // RG-030 — the residual, signed.
        BigDecimal dispensedTotal = rounded.stream()
                .map(Dispensed::dispensedMass)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal residual = request.targetBatchMass().subtract(dispensedTotal);

        // RG-040 — acceptability. The bound is strict: a residual worth exactly P-02
        // steps is ACCEPTED. That is the "> and not ≥" of the specification, and it is
        // the kind of detail a developer would settle alone without it.
        BigDecimal bound = request.balanceStep()
                .multiply(BigDecimal.valueOf(P_02_MAX_RESIDUAL_STEPS));
        if (residual.abs().compareTo(bound) > 0)
            throw new BusinessError("E-MAS-002",
                    "residual " + residual + " exceeds " + P_02_MAX_RESIDUAL_STEPS
                            + " balance steps (" + bound + ")");

        // RG-050 — allocation of the residual.
        String receiver = residualReceiver(request.components());
        List<Dispensed> dispensed = new ArrayList<>();
        for (Dispensed d : rounded) {
            BigDecimal mass = d.componentId().equals(receiver)
                    ? d.dispensedMass().add(residual)
                    : d.dispensedMass();
            dispensed.add(new Dispensed(d.componentId(), d.nominalMass(), mass));
        }

        return new Result(List.copyOf(dispensed), residual);
    }
}
