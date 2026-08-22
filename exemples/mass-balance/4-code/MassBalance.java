// Implémentation de SPEC-MAS-001 — Batch mass balance, version 1.0.0.
//
// Chaque méthode porte l'identifiant de la règle qu'elle met en œuvre. C'est la
// traçabilité exigée par le guide 6 : partant d'une règle de la spécification, on
// trouve le code ; partant du code, on retrouve la règle et sa justification.
//
// Ce que ce fichier NE décide PAS, parce que la spécification l'a déjà tranché :
// le mode d'arrondi (P-01), la borne du résidu (P-02), qui reçoit le résidu et
// comment on départage une égalité (RG-050). Ce sont des décisions métier.
//
// Ce que ce fichier décide, et dont la spécification ne parle pas : BigDecimal
// plutôt qu'un entier de pas, la structure des enregistrements, l'ordre de
// parcours, le fait de tout calculer en un passage. Ce sont des décisions
// techniques — voir CADRE.md §1.4.

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MassBalance {

    // --- Paramètres (§6 de la spécification) --------------------------------
    // Ils vivent ici en constantes nommées, jamais au milieu d'une expression :
    // c'est la règle « un paramètre n'est pas une valeur magique » (C-05).

    /** P-01 — mode d'arrondi des doses. HALF_EVEN annule le biais systématique. */
    static final RoundingMode P_01_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /** P-02 — résidu maximal toléré, exprimé en pas de balance. */
    static final int P_02_MAX_RESIDUAL_STEPS = 3;

    // --- Contrat (§4 et §5 de la spécification) -----------------------------
    // Les familles ISO/IEC 11404 du contrat se traduisent ici : Scaled → BigDecimal,
    // CharacterString → String, Sequence → List. Le choix de BigDecimal n'est pas
    // libre : la famille Scaled exige une arithmétique décimale exacte (EX-01).

    public record Component(String componentId, BigDecimal targetMassFraction) {}

    public record Request(BigDecimal targetBatchMass,
                          BigDecimal balanceStep,
                          List<Component> components) {}

    public record Dispensed(String componentId,
                            BigDecimal nominalMass,
                            BigDecimal dispensedMass) {}

    public record Result(List<Dispensed> dispensed, BigDecimal residual) {}

    /** Une erreur métier du §9 : elle porte son identifiant, pas seulement un message. */
    public static final class BusinessError extends RuntimeException {
        public final String code;
        BusinessError(String code, String message) {
            super(code + " — " + message);
            this.code = code;
        }
    }

    private MassBalance() {}

    // --- Préconditions (§4) -------------------------------------------------

    private static void checkPreconditions(Request request) {
        // E-MAS-001 : la somme des fractions vaut exactement 1.
        // compareTo et non equals : BigDecimal.equals distingue 1.0 de 1.00.
        BigDecimal sum = request.components().stream()
                .map(Component::targetMassFraction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.ONE) != 0)
            throw new BusinessError("E-MAS-001",
                    "target mass fractions sum to " + sum + ", not exactly 1");

        // E-MAS-003 : les identifiants sont uniques — sinon RG-050 ne départage plus.
        Set<String> seen = new HashSet<>();
        for (Component c : request.components())
            if (!seen.add(c.componentId()))
                throw new BusinessError("E-MAS-003",
                        "duplicate component_id: " + c.componentId());

        // E-MAS-004 : la masse cible est un multiple entier du pas de balance,
        // sans quoi la conservation exacte de INV-01 serait impossible.
        if (request.targetBatchMass().remainder(request.balanceStep())
                .compareTo(BigDecimal.ZERO) != 0)
            throw new BusinessError("E-MAS-004",
                    "target_batch_mass " + request.targetBatchMass()
                            + " is not a multiple of balance_step " + request.balanceStep());
    }

    // --- Les règles ---------------------------------------------------------

    /** RG-010 — masse nominale d'un composant, non arrondie. */
    private static BigDecimal nominalMass(Request request, Component component) {
        return request.targetBatchMass().multiply(component.targetMassFraction());
    }

    /**
     * RG-020 — arrondi au pas de la balance.
     * On arrondit un NOMBRE DE PAS, pas une masse : un arrondi à trois décimales
     * serait faux dès que le pas ne vaut pas 0,001 kg.
     */
    private static BigDecimal roundedMass(BigDecimal nominal, BigDecimal balanceStep) {
        BigDecimal steps = nominal.divide(balanceStep, 0, P_01_ROUNDING_MODE);
        return steps.multiply(balanceStep);
    }

    /** RG-050 — le composant qui reçoit le résidu : plus grande fraction, puis plus petit identifiant. */
    private static String residualReceiver(List<Component> components) {
        return components.stream()
                .max(Comparator.comparing(Component::targetMassFraction)
                        // Le départage est INVERSÉ ici : max() retient le dernier des
                        // ex æquo selon ce comparateur, donc on ordonne les identifiants
                        // en décroissant pour que le PLUS PETIT l'emporte (RG-050).
                        .thenComparing(Comparator.comparing(Component::componentId).reversed()))
                .orElseThrow()
                .componentId();
    }

    /** Met en œuvre RG-010 à RG-050 dans l'ordre, sur une demande valide. */
    public static Result compute(Request request) {
        checkPreconditions(request);

        List<Dispensed> rounded = new ArrayList<>();
        for (Component component : request.components()) {
            BigDecimal nominal = nominalMass(request, component);                   // RG-010
            BigDecimal step = roundedMass(nominal, request.balanceStep());          // RG-020
            rounded.add(new Dispensed(component.componentId(), nominal, step));
        }

        // RG-030 — le résidu, signé.
        BigDecimal dispensedTotal = rounded.stream()
                .map(Dispensed::dispensedMass)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal residual = request.targetBatchMass().subtract(dispensedTotal);

        // RG-040 — recevabilité. La borne est stricte : un résidu valant exactement
        // P-02 pas est ACCEPTÉ. C'est le « > et non ≥ » de la spécification, et
        // c'est le genre de détail qu'un développeur trancherait seul sans elle.
        BigDecimal bound = request.balanceStep()
                .multiply(BigDecimal.valueOf(P_02_MAX_RESIDUAL_STEPS));
        if (residual.abs().compareTo(bound) > 0)
            throw new BusinessError("E-MAS-002",
                    "residual " + residual + " exceeds " + P_02_MAX_RESIDUAL_STEPS
                            + " balance steps (" + bound + ")");

        // RG-050 — affectation du résidu.
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
