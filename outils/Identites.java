// Attribution et maintenance des identités durables (UUID).
//
// Chaque objet identifié — spécification, fonction, règle, paramètre, donnée,
// exigence, invariant, cas de test, cas d'erreur, question — porte un UUID attribué
// une fois et jamais modifié. Le UUID est l'identité ; l'identifiant lisible
// (RG-010) et le libellé sont des étiquettes, qui peuvent changer.
//
// Les UUID vivent dans une annexe « Identités » en fin de document, pour que le
// corps du texte reste lisible.
//
// Programme à fichier unique : aucune dépendance, aucune construction préalable.
//     java outils/Identites.java --attribuer [fichier ...]   # complète les manquants
//     java outils/Identites.java --registre                  # produit registre.json

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class Identites {

    static Path racine;
    static final String ANNEXE = "## Annexe — Identités";

    // Les objets susceptibles d'être cités depuis l'extérieur de leur document.
    static final String[][] MOTIFS = {
        {"^###\\s+(RG-\\d+)\\s+—\\s+(.+)$", "règle"},
        {"^###\\s+(CT-\\d+)\\s+—\\s+(.+)$", "cas de test"},
        {"^\\|\\s*`(FN-\\d+)`\\s*\\|\\s*\\*{0,2}([^|*]+)", "fonction"},
        {"^\\|\\s*`(P-\\d+)`\\s*\\|\\s*([^|]+)", "paramètre"},
        {"^\\|\\s*`(D-\\d+)`\\s*\\|\\s*([^|]+)", "donnée"},
        {"^\\|\\s*`(EX-\\d+)`\\s*\\|\\s*([^|]+)", "exigence"},
        {"^\\|\\s*`(INV-\\d+)`\\s*\\|\\s*([^|]+)", "invariant"},
        {"^\\|\\s*`(E-[A-Z]+-\\d+)`\\s*\\|\\s*([^|]+)", "cas d'erreur"},
        {"^\\|\\s*`(Q-[\\d-]+)`\\s*\\|\\s*([^|]+)", "question"},
    };

    record Objet(String identifiant, String nature, String libelle) {}

    static String corps(String texte) {
        int i = texte.indexOf(ANNEXE);
        return i < 0 ? texte : texte.substring(0, i);
    }

    /** Objets identifiés du document, hors annexe, dans l'ordre d'apparition. */
    static List<Objet> objets(String texte) {
        List<Objet> trouves = new ArrayList<>();
        Set<String> vus = new HashSet<>();
        Matcher m = Pattern.compile("\\|\\s*\\*\\*Identifiant\\*\\*\\s*\\|\\s*([A-Z]+-[A-Z]*-?\\d+)")
                .matcher(texte);
        if (m.find()) {
            Matcher t = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE).matcher(texte);
            trouves.add(new Objet(m.group(1), "document", t.find() ? t.group(1) : ""));
            vus.add(m.group(1));
        }
        String c = corps(texte);
        for (String[] motif : MOTIFS) {
            Matcher mm = Pattern.compile(motif[0], Pattern.MULTILINE).matcher(c);
            while (mm.find()) {
                if (!vus.add(mm.group(1))) continue;
                String libelle = mm.group(2).replaceAll("\\s+", " ");
                // équivalent de strip(" *|") en Python : retrait des deux côtés
                libelle = libelle.replaceAll("^[ *|]+", "").replaceAll("[ *|]+$", "");
                trouves.add(new Objet(mm.group(1), motif[1], libelle));
            }
        }
        return trouves;
    }

    /**
     * Le fichier dont ce document est la traduction, ou null. Une traduction porte
     * délibérément les mêmes objets que sa source : elle n'entre donc pas au registre,
     * qui recense les objets une fois et une seule. C'est C-42, côté vérificateur, qui
     * garantit qu'elle n'a pas dérivé.
     */
    static boolean estUneTraduction(String texte) {
        return Pattern.compile("\\|\\s*\\*\\*(?:Traduction de|Translation of)\\*\\*\\s*\\|")
                .matcher(texte).find();
    }

    static LinkedHashMap<String, String> annexeExistante(String texte) {
        LinkedHashMap<String, String> r = new LinkedHashMap<>();
        int i = texte.indexOf(ANNEXE);
        if (i < 0) return r;
        Matcher m = Pattern.compile("^\\|\\s*`([A-Z]+[-A-Z]*-[\\d-]+)`\\s*\\|\\s*`([0-9a-f-]{36})`",
                Pattern.MULTILINE).matcher(texte.substring(i + ANNEXE.length()));
        while (m.find()) r.put(m.group(1), m.group(2));
        return r;
    }

    static int attribuer(Path chemin) throws IOException {
        String texte = Files.readString(chemin);
        List<Objet> liste = objets(texte);
        if (liste.isEmpty()) return 0;
        LinkedHashMap<String, String> connus = annexeExistante(texte);
        int nouveaux = 0;
        // Le renvoi vers CADRE.md est relatif au document : on CALCULE sa profondeur
        // depuis la racine du dépôt. Deviner à partir d'un morceau de chemin — ce que
        // faisait la version précédente — casse dès qu'un exemple change de niveau.
        int profondeur = racine.relativize(chemin).getNameCount() - 1;
        String renvoi = profondeur == 0 ? "CADRE.md" : "../".repeat(profondeur) + "CADRE.md";
        List<String> lignes = new ArrayList<>(List.of("", ANNEXE, "",
                "*Chaque objet porte un UUID attribué une fois et jamais modifié. "
                + "L'identifiant lisible et le libellé sont des étiquettes : ils peuvent "
                + "changer, l'identité non. Voir [CADRE.md §2.8](" + renvoi + ").*",
                "", "| Identifiant | UUID | Nature | Libellé |", "|---|---|---|---|"));
        for (Objet o : liste) {
            if (!connus.containsKey(o.identifiant())) {
                connus.put(o.identifiant(), UUID.randomUUID().toString());
                nouveaux++;
            }
            String libelle = o.libelle();
            if (libelle.length() > 70) libelle = libelle.substring(0, 70);
            lignes.add("| `" + o.identifiant() + "` | `" + connus.get(o.identifiant())
                    + "` | " + o.nature() + " | " + libelle + " |");
        }
        Set<String> vivants = liste.stream().map(Objet::identifiant).collect(Collectors.toSet());
        List<String> retires = connus.keySet().stream()
                .filter(i -> !vivants.contains(i)).sorted().toList();
        if (!retires.isEmpty()) {
            lignes.addAll(List.of("", "### Identités retirées", "",
                    "*Un objet supprimé conserve son UUID : il n'est jamais réattribué, "
                    + "pour qu'une référence ancienne reste résoluble.*", "",
                    "| Identifiant | UUID |", "|---|---|"));
            for (String i : retires) lignes.add("| `" + i + "` | `" + connus.get(i) + "` |");
        }
        Files.writeString(chemin, corps(texte).stripTrailing() + "\n"
                + String.join("\n", lignes) + "\n");
        return nouveaux;
    }

    /** Échappement JSON équivalent à json.dumps(..., ensure_ascii=False). */
    static String json(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                case '\b' -> b.append("\\b");
                case '\f' -> b.append("\\f");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }

    record Entree(String uuid, String identifiant, String nature, String libelle,
                  String document) {}

    static int registre() throws IOException {
        Map<String, Entree> entrees = new LinkedHashMap<>();
        List<String[]> doublons = new ArrayList<>();
        List<Path> fichiers;
        try (Stream<Path> s = Files.walk(racine)) {
            fichiers = s.filter(p -> p.toString().endsWith(".md"))
                    .filter(p -> !p.toString().contains(".git"))
                    .filter(p -> !p.toString().contains("templates"))
                    .filter(p -> !p.toString().contains("jeu-d-essai"))
                    .sorted().toList();
        }
        for (Path chemin : fichiers) {
            String texte = Files.readString(chemin);
            if (estUneTraduction(texte)) continue;
            Map<String, String> table = annexeExistante(texte);
            Map<String, Objet> natures = new HashMap<>();
            for (Objet o : objets(texte)) natures.put(o.identifiant(), o);
            for (var e : table.entrySet()) {
                if (entrees.containsKey(e.getValue()))
                    doublons.add(new String[]{e.getValue(),
                            entrees.get(e.getValue()).identifiant(), e.getKey()});
                Objet o = natures.get(e.getKey());
                entrees.put(e.getValue(), new Entree(e.getValue(), e.getKey(),
                        o != null ? o.nature() : "retiré", o != null ? o.libelle() : "",
                        racine.relativize(chemin).toString()));
            }
        }
        List<Entree> triees = entrees.values().stream()
                .sorted(Comparator.comparing(Entree::uuid)).toList();
        StringBuilder b = new StringBuilder("[\n");
        for (int i = 0; i < triees.size(); i++) {
            Entree e = triees.get(i);
            b.append("  {\n")
             .append("    \"uuid\": ").append(json(e.uuid())).append(",\n")
             .append("    \"identifiant\": ").append(json(e.identifiant())).append(",\n")
             .append("    \"nature\": ").append(json(e.nature())).append(",\n")
             .append("    \"libelle\": ").append(json(e.libelle())).append(",\n")
             .append("    \"document\": ").append(json(e.document())).append("\n")
             .append("  }").append(i < triees.size() - 1 ? ",\n" : "\n");
        }
        b.append("]\n");
        Files.writeString(racine.resolve("registre.json"), b.toString());
        System.out.println("registre.json : " + entrees.size() + " identités");
        for (String[] d : doublons)
            System.out.println("DOUBLON " + d[0] + " partagé par " + d[1] + " et " + d[2]);
        return doublons.isEmpty() ? 0 : 1;
    }

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(
                java.io.FileDescriptor.out), true, java.nio.charset.StandardCharsets.UTF_8));
        racine = Paths.get(Identites.class.getProtectionDomain().getCodeSource()
                .getLocation().getPath()).getParent();
        if (!Files.exists(racine.resolve("CADRE.md"))) racine = Paths.get("").toAbsolutePath();

        if (args.length > 0 && args[0].equals("--registre")) System.exit(registre());

        if (args.length > 0 && args[0].equals("--attribuer")) {
            List<Path> cibles;
            if (args.length > 1) {
                cibles = Arrays.stream(args).skip(1)
                        .map(a -> Paths.get(a).toAbsolutePath()).toList();
            } else {
                try (Stream<Path> s = Files.walk(racine)) {
                    cibles = s.filter(p -> p.toString().endsWith(".md"))
                            .filter(p -> !p.toString().contains(".git"))
                            .filter(p -> !p.toString().contains("templates"))
                            .filter(p -> !p.toString().contains("jeu-d-essai"))
                            .sorted().toList();
                }
            }
            int total = 0;
            for (Path c : cibles) total += attribuer(c);
            System.out.println(total + " identité(s) attribuée(s)");
            System.exit(0);
        }

        System.out.println("""
                Attribution et maintenance des identités durables (UUID).

                    java outils/Identites.java --attribuer [fichier ...]
                    java outils/Identites.java --registre""");
        System.exit(2);
    }
}
