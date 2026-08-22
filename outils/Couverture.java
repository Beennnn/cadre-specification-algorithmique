// Rapport de couverture : ce que la spécification exige, et où le code le met en œuvre.
//
// Il répond à la question qu'une suite de tests verte ne répond jamais d'elle-même :
// « le code couvre-t-il tout ce que la spécification demande, et tout le code se
// rattache-t-il à quelque chose que le métier a demandé ? »
//
// Il croise deux sources et n'en invente aucune :
//   - la SPÉCIFICATION : règles RG-xxx (titres), invariants, cas d'erreur, paramètres
//     et fonctions (lignes de tableau) ;
//   - le CODE : les annotations @ImplementsSpec, lues dans les sources.
//
// Le rapport produit des liens dans les deux sens — vers l'ancre du point de spécification
// et vers le fichier et la ligne de code — pour qu'un relecteur puisse vérifier lui-même
// au lieu de croire l'outil sur parole.
//
//     java outils/Couverture.java <dossier-exemple> [> rapport.md]
//
// Code de retour : 1 s'il reste un point non couvert ou une annotation orpheline.

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

public class Couverture {

    /** Un point de la spécification : son identifiant, son énoncé, son ancre. */
    record Point(String id, String enonce, String ancre, String fichier) {}

    /** Une citation dans le code : le fichier et la ligne qui déclarent la couvrir. */
    record Citation(String fichier, int ligne) {}

    static final List<String> sortie = new ArrayList<>();
    static void dire(String s) { sortie.add(s); }

    /**
     * Ancre GitHub d'un titre : minuscules, ponctuation retirée sauf tiret et
     * soulignement, espaces en tirets. Le tiret cadratin disparaît, ce qui produit le
     * double tiret caractéristique de « RG-010 — Durée » → « rg-010--duree ».
     */
    static String ancreDeTitre(String titre) {
        StringBuilder b = new StringBuilder();
        for (char c : titre.trim().toLowerCase().toCharArray())
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') b.append(c);
            else if (c == ' ') b.append('-');
        return b.toString();
    }

    /** Points exigés par la spécification, dans l'ordre où elle les énonce. */
    static LinkedHashMap<String, Point> lireSpec(Path spec) throws IOException {
        String texte = Files.readString(spec);
        int i = texte.indexOf("## Annexe — Identités");
        if (i >= 0) texte = texte.substring(0, i);
        String nom = spec.getFileName().toString();
        LinkedHashMap<String, Point> points = new LinkedHashMap<>();

        // Les règles sont des titres de niveau 3 : elles ont une ancre naturelle.
        Matcher mr = Pattern.compile("^###\\s+(RG-\\d+)\\s+—\\s+(.+)$", Pattern.MULTILINE)
                .matcher(texte);
        while (mr.find())
            points.put(mr.group(1), new Point(mr.group(1), mr.group(2).trim(),
                    ancreDeTitre(mr.group(1) + " — " + mr.group(2)), nom));

        // Les autres vivent en tableau : pas d'ancre naturelle, d'où l'ancre explicite
        // posée dans le document. Sans elle, on ne saurait pointer que la section.
        Matcher mt = Pattern.compile(
                "^\\|\\s*(?:<a id=\"([^\"]+)\"></a>)?\\s*`((?:INV|FN|P|E)-[A-Z]*-?\\d+)`\\s*\\|\\s*([^|]+)",
                Pattern.MULTILINE).matcher(texte);
        while (mt.find()) {
            String id = mt.group(2);
            String ancre = mt.group(1) != null ? mt.group(1) : id.toLowerCase();
            points.putIfAbsent(id, new Point(id, mt.group(3).trim(), ancre, nom));
        }
        return points;
    }

    /** Ce que le code déclare couvrir, par identifiant. */
    static TreeMap<String, List<Citation>> lireCode(Path racineCode) throws IOException {
        TreeMap<String, List<Citation>> cite = new TreeMap<>();
        if (!Files.isDirectory(racineCode)) return cite;
        List<Path> sources;
        try (Stream<Path> s = Files.walk(racineCode)) {
            sources = s.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
        // L'annotation est lue sur le TEXTE ENTIER, pas ligne à ligne : une liste
        // d'identifiants un peu longue se replie sur deux lignes, et une lecture ligne
        // à ligne n'y trouve alors aucune parenthèse fermante. Elle ne signalait rien :
        // l'annotation disparaissait, et le point passait pour vérifié par personne.
        Pattern annotation = Pattern.compile("@ImplementsSpec\\s*\\(([^)]*)\\)",
                Pattern.DOTALL);
        Pattern identifiant = Pattern.compile("\"([A-Z]+-[A-Z]*-?\\d+)\"");
        for (Path source : sources) {
            String texte = Files.readString(source, StandardCharsets.UTF_8);
            Matcher ma = annotation.matcher(texte);
            while (ma.find()) {
                int ligne = 1 + (int) texte.substring(0, ma.start()).chars()
                        .filter(c -> c == '\n').count();
                Matcher mi = identifiant.matcher(ma.group(1));
                while (mi.find())
                    cite.computeIfAbsent(mi.group(1), k -> new ArrayList<>())
                        .add(new Citation(racineCode.relativize(source).toString(), ligne));
            }
        }
        return cite;
    }

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(
                java.io.FileDescriptor.out), true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.out.println("usage : java outils/Couverture.java <dossier-exemple>");
            System.exit(2);
        }
        Path exemple = Paths.get(args[0]).toAbsolutePath().normalize();
        Path dossierSpec = exemple.resolve("spec");
        Path code = exemple.resolve("code/src/main/java");
        Path tests = exemple.resolve("code/src/test/java");

        List<Path> specs;
        try (Stream<Path> s = Files.list(dossierSpec)) {
            specs = s.filter(p -> p.toString().endsWith(".en.md")).sorted().toList();
        }
        if (specs.isEmpty()) {
            System.out.println("aucune spécification normative (.en.md) dans " + dossierSpec);
            System.exit(2);
        }
        Path spec = specs.get(0);

        LinkedHashMap<String, Point> exiges = lireSpec(spec);
        TreeMap<String, List<Citation>> cites = lireCode(code);
        TreeMap<String, List<Citation>> verifies = lireCode(tests);
        String versSpec = "../spec/" + spec.getFileName();

        dire("# Coverage report — " + exemple.getFileName());
        dire("");
        dire("*Generated by `outils/Couverture.java`. It cross-checks two sources and invents");
        dire("neither: the specification `" + spec.getFileName() + "`, and the `@ImplementsSpec`");
        dire("annotations found in `code/src/main/java`.*");
        dire("");

        int nonCouverts = 0;
        dire("## What the specification requires");
        dire("");
        dire("An item is **implemented** in the production code, **verified** by a test, or");
        dire("both. An invariant is normally only verified: it is a property to check, not code");
        dire("to write. An item that is neither is a hole.");
        dire("");
        dire("| Item | Statement | Implemented in | Verified by |");
        dire("|---|---|---|---|");
        for (Point p : exiges.values()) {
            String impl = liens(cites.get(p.id()), "../code/src/main/java/");
            String verif = liens(verifies.get(p.id()), "../code/src/test/java/");
            if (impl == null && verif == null) nonCouverts++;
            dire("| [`" + p.id() + "`](" + versSpec + "#" + p.ancre() + ") | "
                    + tronquer(p.enonce()) + " | " + (impl == null ? "—" : impl)
                    + " | " + (verif == null ? "—" : verif) + " |");
        }

        // L'inverse : du code qui se réclame d'un point que la spécification ignore.
        List<String> orphelines = new ArrayList<>();
        for (String id : cites.keySet()) if (!exiges.containsKey(id)) orphelines.add(id);
        for (String id : verifies.keySet())
            if (!exiges.containsKey(id) && !orphelines.contains(id)) orphelines.add(id);

        dire("");
        dire("## Verdict");
        dire("");
        dire("| | |");
        dire("|---|---|");
        dire("| Items required by the specification | " + exiges.size() + " |");
        dire("| Covered — implemented, verified, or both | " + (exiges.size() - nonCouverts) + " |");
        dire("| **Neither implemented nor verified** | "
                + (nonCouverts == 0 ? "0" : "**" + nonCouverts + "**") + " |");
        dire("| **Orphan annotations** | "
                + (orphelines.isEmpty() ? "0" : "**" + String.join(", ", orphelines) + "**") + " |");

        if (!orphelines.isEmpty()) {
            dire("");
            dire("> **An orphan annotation is a defect, not a detail.** The code claims to");
            dire("> implement something the specification does not state — either the item was");
            dire("> removed and the code was not, or someone invented a requirement.");
        }
        if (nonCouverts == 0 && orphelines.isEmpty()) {
            dire("");
            dire("**Every item of the specification is covered, and every annotation traces");
            dire("back to the specification.**");
        }

        Path rapport = exemple.resolve("reports/COVERAGE-REPORT.md");
        Files.createDirectories(rapport.getParent());
        Files.writeString(rapport, String.join("\n", sortie) + "\n");
        sortie.forEach(System.out::println);
        System.exit(nonCouverts > 0 || !orphelines.isEmpty() ? 1 : 0);
    }

    /** Liens vers les fichiers et lignes qui portent une annotation, ou null s'il n'y en a pas. */
    static String liens(List<Citation> citations, String prefixe) {
        if (citations == null || citations.isEmpty()) return null;
        StringBuilder b = new StringBuilder();
        for (Citation c : citations) {
            if (b.length() > 0) b.append(", ");
            b.append("[`").append(c.fichier()).append(":").append(c.ligne())
             .append("`](").append(prefixe).append(c.fichier())
             .append("#L").append(c.ligne()).append(")");
        }
        return b.toString();
    }

    static String tronquer(String s) {
        s = s.replaceAll("\\s+", " ").replace("|", "/").trim();
        return s.length() > 58 ? s.substring(0, 58) + "…" : s;
    }
}
