// Qualification de l'implémentation contre les données de référence.
//
// Ce harnais ne contient AUCUNE valeur attendue en dur : il rejoue le fichier
// reference-data.csv, qui est le §10 de la spécification. C'est ce qui en
// fait une qualification et non un test écrit par celui qui a codé — un test dont
// les valeurs attendues sortent de l'implémentation ne vérifie rien.
//
//     javac -d /tmp/mb *.java && java -cp /tmp/mb MassBalanceTest
//
// Code de retour : 1 si un cas échoue, 0 sinon.

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MassBalanceTest {

    static int echecs = 0;

    record Attendu(String componentId, BigDecimal fraction, String masseAttendue) {}

    public static void main(String[] args) throws IOException {
        // La sortie porte des accents : on impose UTF-8 plutôt que de dépendre
        // de l'encodage par défaut de la machine qui exécute.
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(
                java.io.FileDescriptor.out), true, StandardCharsets.UTF_8));

        Path donnees = Paths.get(args.length > 0 ? args[0] : "reference-data.csv");
        System.out.println("Qualification de MassBalance contre " + donnees + "\n");

        for (String ligne : Files.readAllLines(donnees, StandardCharsets.UTF_8)) {
            if (ligne.isBlank() || ligne.startsWith("#")) continue;
            rejouer(ligne);
        }

        proprieteInvarianceParPermutation();

        System.out.println();
        if (echecs == 0) {
            System.out.println("Tous les cas de référence sont satisfaits.");
        } else {
            System.out.println(echecs + " cas en écart — voir 5-DEVIATIONS.md pour la conduite à tenir.");
        }
        System.exit(echecs > 0 ? 1 : 0);
    }

    /** Rejoue une ligne de données de référence et confronte le résultat. */
    static void rejouer(String ligne) {
        String[] champs = ligne.split(";", -1);
        String cas = champs[0];
        BigDecimal masseCible = new BigDecimal(champs[1]);
        BigDecimal pas = new BigDecimal(champs[2]);
        String erreurAttendue = champs[3].trim();

        List<Attendu> attendus = new ArrayList<>();
        List<MassBalance.Component> composants = new ArrayList<>();
        for (String c : champs[4].split("\\|")) {
            String[] p = c.split(":", -1);
            attendus.add(new Attendu(p[0], new BigDecimal(p[1]), p[2]));
            composants.add(new MassBalance.Component(p[0], new BigDecimal(p[1])));
        }

        MassBalance.Request demande =
                new MassBalance.Request(masseCible, pas, composants);

        if (!erreurAttendue.isEmpty()) {
            try {
                MassBalance.compute(demande);
                echec(cas, "aucune erreur levée, alors que " + erreurAttendue + " est attendue");
            } catch (MassBalance.BusinessError e) {
                if (e.code.equals(erreurAttendue))
                    succes(cas, "rejet attendu " + erreurAttendue);
                else
                    echec(cas, "erreur " + e.code + " levée, " + erreurAttendue + " attendue");
            }
            return;
        }

        MassBalance.Result resultat;
        try {
            resultat = MassBalance.compute(demande);
        } catch (MassBalance.BusinessError e) {
            echec(cas, "rejet inattendu : " + e.getMessage());
            return;
        }

        boolean ok = true;
        for (int i = 0; i < attendus.size(); i++) {
            BigDecimal attendue = new BigDecimal(attendus.get(i).masseAttendue());
            BigDecimal obtenue = resultat.dispensed().get(i).dispensedMass();
            if (obtenue.compareTo(attendue) != 0) {
                echec(cas, attendus.get(i).componentId()
                        + " : attendu " + attendue + ", obtenu " + obtenue);
                ok = false;
            }
        }

        // INV-01 — conservation de la masse, à l'égalité stricte.
        BigDecimal somme = resultat.dispensed().stream()
                .map(MassBalance.Dispensed::dispensedMass)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (somme.compareTo(masseCible) != 0) {
            echec(cas, "INV-01 violé : somme " + somme + " ≠ masse cible " + masseCible);
            ok = false;
        }

        // INV-02 — chaque dose est un multiple entier du pas.
        for (MassBalance.Dispensed d : resultat.dispensed())
            if (d.dispensedMass().remainder(pas).compareTo(BigDecimal.ZERO) != 0) {
                echec(cas, "INV-02 violé : " + d.componentId()
                        + " = " + d.dispensedMass() + " n'est pas un multiple de " + pas);
                ok = false;
            }

        if (ok) succes(cas, resultat.dispensed().size() + " doses, résidu "
                + resultat.residual() + ", INV-01 et INV-02 tenus");
    }

    /**
     * INV-04 — invariance par permutation, testée sur des entrées GÉNÉRÉES.
     *
     * C'est le contrôle qui attrape une implémentation ayant « oublié » le
     * départage de RG-050 : elle passe tous les cas nominaux et se trompe le jour
     * où les composants arrivent dans un autre ordre. Aucune liste de cas ne
     * remplace une propriété testée sur des entrées engendrées.
     */
    static void proprieteInvarianceParPermutation() {
        List<MassBalance.Component> base = List.of(
                new MassBalance.Component("CMP-A", new BigDecimal("0.400000")),
                new MassBalance.Component("CMP-B", new BigDecimal("0.400000")),
                new MassBalance.Component("CMP-C", new BigDecimal("0.200000")));
        MassBalance.Request reference = new MassBalance.Request(
                new BigDecimal("1.001"), new BigDecimal("0.001"), base);
        MassBalance.Result attendu = MassBalance.compute(reference);

        Random alea = new Random(20260821L);   // graine fixe : la propriété est rejouable
        for (int essai = 0; essai < 200; essai++) {
            List<MassBalance.Component> melange = new ArrayList<>(base);
            java.util.Collections.shuffle(melange, alea);
            MassBalance.Result obtenu = MassBalance.compute(new MassBalance.Request(
                    reference.targetBatchMass(), reference.balanceStep(), melange));

            for (MassBalance.Dispensed a : attendu.dispensed()) {
                BigDecimal m = obtenu.dispensed().stream()
                        .filter(d -> d.componentId().equals(a.componentId()))
                        .findFirst().orElseThrow().dispensedMass();
                if (m.compareTo(a.dispensedMass()) != 0) {
                    echec("INV-04", "permutation " + melange.stream()
                            .map(MassBalance.Component::componentId).toList()
                            + " : " + a.componentId() + " vaut " + m
                            + " au lieu de " + a.dispensedMass());
                    return;
                }
            }
        }
        succes("INV-04", "200 permutations, résultat identique");
    }

    static void succes(String cas, String detail) {
        System.out.printf("  OK      %-8s %s%n", cas, detail);
    }

    static void echec(String cas, String detail) {
        System.out.printf("  ÉCART   %-8s %s%n", cas, detail);
        echecs++;
    }
}
