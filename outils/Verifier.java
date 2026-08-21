// Contrôles mécaniques d'une spécification fonctionnelle — implémentation Java.
//
// Seconde implémentation du catalogue REGLES-DE-CONTROLE.md, à côté de verifier.py.
// Les deux doivent s'accorder sur le même corpus : c'est le test de la double
// implémentation appliqué à l'outillage lui-même.
//
// Programme à fichier unique : aucune dépendance, aucune construction préalable.
//     java outils/Verifier.java                  # tout le dépôt
//     java outils/Verifier.java <fichier.md>     # une spécification
// Code de retour : 1 si au moins un ÉCHEC, 0 sinon.

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class Verifier {

    record Constat(String niveau, String regle, String fichier, String message)
            implements Comparable<Constat> {
        public int compareTo(Constat o) {
            int c = niveau.compareTo(o.niveau);
            if (c != 0) return c;
            c = regle.compareTo(o.regle);
            if (c != 0) return c;
            c = fichier.compareTo(o.fichier);
            return c != 0 ? c : message.compareTo(o.message);
        }
    }

    static final List<Constat> constats = new ArrayList<>();
    static final Map<String, String[]> uuidsVus = new HashMap<>();
    static Path racine;

    static void constat(String niveau, String regle, Path f, String message) {
        constats.add(new Constat(niveau, regle, racine.relativize(f).toString(), message));
    }

    // --- découpage en sections de niveau 2, indexées par mot-clé -------------
    static final LinkedHashMap<String, String> TITRES = new LinkedHashMap<>();
    static {
        TITRES.put("entrees", "entrée");     TITRES.put("sorties", "sortie");
        TITRES.put("parametres", "paramètre"); TITRES.put("regles", "règle");
        TITRES.put("invariants", "invariant"); TITRES.put("erreurs", "erreur");
        TITRES.put("essai", "jeu d'essai");  TITRES.put("contraintes", "contrainte");
        TITRES.put("questions", "question"); TITRES.put("historique", "historique");
    }

    static Map<String, String> sections(String texte) {
        Map<String, StringBuilder> trouvees = new HashMap<>();
        String courante = null;
        for (String ligne : texte.split("\n", -1)) {
            if (ligne.startsWith("## ")) {
                String titre = ligne.substring(3).toLowerCase();
                courante = null;
                for (var e : TITRES.entrySet())
                    if (titre.contains(e.getValue())) { courante = e.getKey(); break; }
            } else if (courante != null) {
                trouvees.computeIfAbsent(courante, k -> new StringBuilder())
                        .append(ligne).append('\n');
            }
        }
        Map<String, String> r = new HashMap<>();
        trouvees.forEach((k, v) -> r.put(k, v.toString()));
        return r;
    }

    static List<String> blocsCode(String texte) {
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile("```[^\n]*\n(.*?)```", Pattern.DOTALL).matcher(texte);
        while (m.find()) r.add(m.group(1));
        return r;
    }

    /** Noms de champs d'un contrat : lignes « nom : Type » dans un bloc de code. */
    static SortedSet<String> champs(String texte) {
        SortedSet<String> noms = new TreeSet<>();
        Pattern p = Pattern.compile("^\\s{2,}([a-zà-ÿ_][a-zà-ÿ0-9_]*)\\s*:\\s*\\S");
        for (String bloc : blocsCode(texte))
            for (String ligne : bloc.split("\n", -1)) {
                Matcher m = p.matcher(ligne);
                if (m.find()) noms.add(m.group(1));
            }
        return noms;
    }

    static List<String> trouverTout(String motif, String texte, int flags) {
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile(motif, flags).matcher(texte);
        while (m.find()) r.add(m.group(1));
        return r;
    }

    // --- identités durables --------------------------------------------------
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
    static final String ANNEXE = "## Annexe — Identités";

    static String corps(String texte) {
        int i = texte.indexOf(ANNEXE);
        return i < 0 ? texte : texte.substring(0, i);
    }

    static LinkedHashMap<String, String> objets(String texte) {
        LinkedHashMap<String, String> r = new LinkedHashMap<>();
        Matcher m = Pattern.compile("\\|\\s*\\*\\*Identifiant\\*\\*\\s*\\|\\s*([A-Z]+-[A-Z]*-?\\d+)")
                .matcher(texte);
        if (m.find()) r.put(m.group(1), "document");
        String c = corps(texte);
        for (String[] motif : MOTIFS) {
            Matcher mm = Pattern.compile(motif[0], Pattern.MULTILINE).matcher(c);
            while (mm.find()) r.putIfAbsent(mm.group(1), motif[1]);
        }
        return r;
    }

    static Map<String, String> annexe(String texte) {
        Map<String, String> r = new HashMap<>();
        int i = texte.indexOf(ANNEXE);
        if (i < 0) return r;
        Matcher m = Pattern.compile("^\\|\\s*`([A-Z]+[-A-Z]*-[\\d-]+)`\\s*\\|\\s*`([0-9a-f-]{36})`",
                Pattern.MULTILINE).matcher(texte.substring(i));
        while (m.find()) r.put(m.group(1), m.group(2));
        return r;
    }

    // --- chaîne de traitement -----------------------------------------------
    record Etape(String id, String nom, List<String> consomme, List<String> produit) {}

    record Groupe(String id, String nom, List<String> membres, String role) {}

    static List<Etape> lireChaine(String texte) {
        List<Etape> r = new ArrayList<>();
        Matcher m = Pattern.compile(
                "^\\|\\s*`(ET-\\d+)`\\s*([^|]*)\\|([^|]*)\\|([^|]*)\\|([^|]*)\\|",
                Pattern.MULTILINE).matcher(texte);
        while (m.find())
            r.add(new Etape(m.group(1), m.group(2).trim(),
                    jetons(m.group(3)), jetons(m.group(4))));
        return r;
    }

    static List<Groupe> lireGroupes(String texte) {
        List<Groupe> r = new ArrayList<>();
        Matcher m = Pattern.compile("^\\|\\s*`(GR-\\d+)`\\s*([^|]*)\\|([^|]*)\\|([^|]*)\\|",
                Pattern.MULTILINE).matcher(texte);
        while (m.find())
            r.add(new Groupe(m.group(1), m.group(2).trim(),
                    trouverTout("(ET-\\d+)", m.group(3), 0), m.group(4).trim()));
        return r;
    }

    static List<String> jetons(String cellule) {
        return trouverTout("`([^`]+)`", cellule, 0);
    }

    /** Ce qu'un ensemble d'étapes consomme et produit vis-à-vis de l'extérieur. */
    static List<List<String>> frontiere(List<String> membres, List<Etape> etapes) {
        List<Etape> dedans = etapes.stream().filter(e -> membres.contains(e.id())).toList();
        Set<String> produitInterne = new LinkedHashSet<>();
        Set<String> consommeInterne = new LinkedHashSet<>();
        for (Etape e : dedans) {
            produitInterne.addAll(e.produit());
            consommeInterne.addAll(e.consomme());
        }
        List<String> consomme = consommeInterne.stream()
                .filter(g -> !produitInterne.contains(g)).sorted().toList();
        Set<String> externe = new LinkedHashSet<>();
        for (Etape e : etapes)
            if (!membres.contains(e.id())) externe.addAll(e.consomme());
        List<String> produit = produitInterne.stream()
                .filter(g -> externe.contains(g) || !consommeInterne.contains(g))
                .sorted().toList();
        return List.of(consomme, produit);
    }

    // --- contrôles -----------------------------------------------------------
    static boolean verifierSpec(Path chemin, String texte) {
        if (!Pattern.compile("^###\\s+RG-\\d", Pattern.MULTILINE).matcher(texte).find()
                || chemin.toString().contains(java.io.File.separator + "templates"
                        + java.io.File.separator))
            return false;
        Map<String, String> sec = sections(texte);
        String regles = sec.getOrDefault("regles", "");

        // C-19 / C-20 : version et historique
        Matcher mv = Pattern.compile("\\|\\s*\\*\\*Version\\*\\*\\s*\\|\\s*([0-9.]+)\\s*\\|")
                .matcher(texte);
        String entete = mv.find() ? mv.group(1) : null;
        Matcher mh = Pattern.compile("^\\|\\s*\\*{0,2}([0-9]+\\.[0-9]+\\.[0-9]+)\\*{0,2}\\s*\\|(.*)$",
                Pattern.MULTILINE).matcher(sec.getOrDefault("historique", ""));
        List<String[]> hist = new ArrayList<>();
        while (mh.find()) hist.add(new String[]{mh.group(1), mh.group(2)});
        if (entete == null) {
            constat("ÉCHEC", "C-19", chemin, "aucun numéro de version dans l'en-tête");
        } else if (!hist.isEmpty()) {
            String derniere = hist.get(hist.size() - 1)[0];
            if (!entete.equals(derniere))
                constat("ÉCHEC", "C-19", chemin, "en-tête " + entete
                        + " ≠ dernière ligne d'historique " + derniere);
            int[] precedent = null;
            for (String[] l : hist) {
                // split(-1) conserve les cellules vides finales, comme en Python :
                // sans cela on lit la colonne « Changement » au lieu de « Impact »
                String[] cellules = Arrays.stream(l[1].split("\\|", -1))
                        .map(String::trim).toArray(String[]::new);
                String impact = cellules.length > 2 ? cellules[2] : "";
                String notice = cellules.length > 3 ? cellules[3] : "";
                boolean change = Pattern.compile("\\boui\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(impact).find();
                // C-24 : un changement à impact renvoie à une notice existante
                if (change) {
                    Matcher mn = Pattern.compile("N-[0-9.]+").matcher(notice);
                    if (!mn.find()) {
                        constat("ÉCHEC", "C-24", chemin, "la version " + l[0]
                                + " déclare un impact sans renvoyer à une notice de changement");
                    } else {
                        String nom = mn.group(0);
                        if (!Pattern.compile("^###\\s+" + Pattern.quote(nom) + "\\b",
                                Pattern.MULTILINE).matcher(texte).find())
                            constat("ÉCHEC", "C-24", chemin, "la notice " + nom
                                    + " est citée mais absente du document");
                    }
                }
                int[] v = version(l[0]);
                if (change && precedent != null && v != null && v[0] == precedent[0])
                    constat("ÉCHEC", "C-20", chemin, "la version " + l[0]
                            + " déclare un impact sur les résultats mais n'incrémente pas le majeur");
                if (v != null) precedent = v;
            }
        }

        // C-23 : glossaire de référence
        if (!texte.contains("**Glossaire de référence**"))
            constat("AVERTIR", "C-23", chemin,
                    "l'en-tête ne nomme pas le glossaire de référence et sa version");

        // C-03 : grandeur employée dans une règle et déclarée nulle part (fantôme)
        //
        // L'inverse de C-01. On ne retient que les identifiants comportant un « _ » :
        // la convention snake_case (C-38) les distingue du français ordinaire qui
        // entoure le pseudo-code, et cette restriction évite de prendre « ligne » ou
        // « que » pour des grandeurs. Un nom est réputé déclaré s'il est introduit
        // dans les règles (SOIT, POUR CHAQUE, affectation) ou s'il apparaît ailleurs
        // dans le document — contrat, paramètres, glossaire, jeu d'essai.
        StringBuilder codeRegles = new StringBuilder();
        Matcher mFantome = Pattern.compile("^###\\s+(RG-\\d+)[^\\n]*\\n(.*?)(?=\\n###\\s|\\Z)",
                Pattern.DOTALL | Pattern.MULTILINE).matcher(sec.getOrDefault("regles", ""));
        while (mFantome.find())
            for (String b : blocsCode(mFantome.group(2))) codeRegles.append(b).append("\n");
        String codeRg = codeRegles.toString();

        Set<String> employes = new TreeSet<>(
                trouverTout("\\b([a-z][a-z0-9]*(?:_[a-z0-9]+)+)\\b", codeRg, 0));
        Set<String> introduits = new HashSet<>();
        introduits.addAll(trouverTout("\\bSOIT\\s+([a-z][a-z0-9_]*)", codeRg, 0));
        introduits.addAll(trouverTout("\\bPOUR\\s+CHAQUE\\s+([a-z][a-z0-9_]*)", codeRg, 0));
        // Une affectation introduit son membre gauche. « = » est aussi l'opérateur de
        // comparaison (§2.2) : on écarte donc les lignes de condition, et on tolère un
        // préfixe (branche de table de décision, puce) devant le nom affecté. Le nom
        // est celui qui précède le premier « = », champ éventuel retiré :
        // « boisson_melangee.masse = … » introduit « boisson_melangee ».
        Pattern conditionnelle = Pattern.compile(
                "\\s*(SI|SINON|TANT QUE|FILTRER|OÙ|POUR|IL EXISTE|AUCUN|TOUS)\\b");
        Pattern membreGauche = Pattern.compile("([a-z][a-z0-9_]*)(?:\\.[a-z][a-z0-9_]*)?\\s*$");
        for (String ligne : codeRg.split("\n", -1)) {
            if (conditionnelle.matcher(ligne).lookingAt()) continue;
            int i = ligne.indexOf('=');
            if (i < 0) continue;
            Matcher mg = membreGauche.matcher(ligne.substring(0, i));
            if (mg.find()) introduits.add(mg.group(1));
        }
        String ailleurs = texte.replace(sec.getOrDefault("regles", ""), "");
        for (String nom : employes) {
            if (introduits.contains(nom)) continue;
            if (!Pattern.compile("\\b" + Pattern.quote(nom) + "\\b").matcher(ailleurs).find())
                constat("ÉCHEC", "C-03", chemin,
                        "« " + nom + " » est employé dans une règle mais déclaré nulle part (fantôme)");
        }

        // C-39 : provenance et validation des résultats attendus
        if (!sec.getOrDefault("essai", "").contains("Provenance et validation"))
            constat("ÉCHEC", "C-39", chemin,
                    "le jeu d'essai ne trace pas la provenance ni la validation de ses résultats attendus");

        // C-04 : paramètre déclaré et jamais employé
        String params = sec.getOrDefault("parametres", "");
        for (String p : trouverTout("^\\|\\s*`(P-\\d+)`", params, Pattern.MULTILINE)) {
            int total = compter(texte, p), dansSection = compter(params, p);
            if (total - dansSection == 0)
                constat("ÉCHEC", "C-04", chemin, p + " déclaré mais employé nulle part");
        }

        // C-14 / C-15 / C-17 : traçabilité
        List<String> definies = trouverTout("^###\\s+(RG-\\d+)", regles, Pattern.MULTILINE);
        for (String r : new TreeSet<>(definies))
            if (Collections.frequency(definies, r) > 1)
                constat("ÉCHEC", "C-17", chemin, r + " défini plusieurs fois");
        Matcher mc = Pattern.compile("###\\s+Table de couverture(.*?)(?=\\n##\\s|\\Z)",
                Pattern.DOTALL).matcher(texte);
        if (mc.find()) {
            String couverture = mc.group(1);
            for (String r : new TreeSet<>(definies))
                if (!couverture.contains(r))
                    constat("ÉCHEC", "C-14", chemin, r + " absent de la table de couverture");
            Set<String> cites = new TreeSet<>(trouverTout("(CT-\\d+)", couverture, 0));
            Set<String> existants = new HashSet<>(
                    trouverTout("(CT-\\d+)", sec.getOrDefault("essai", ""), 0));
            for (String ct : cites)
                if (!existants.contains(ct))
                    constat("ÉCHEC", "C-15", chemin, ct + " cité en couverture mais défini nulle part");
        } else if (!definies.isEmpty()) {
            constat("ÉCHEC", "C-14", chemin, "aucune table de couverture");
        }

        // C-01 / C-02 : entrées et sorties orphelines
        String aval = regles + sec.getOrDefault("invariants", "") + sec.getOrDefault("essai", "");
        verifierOrphelins(chemin, sec.getOrDefault("entrees", ""), aval, "C-01", "entrée");
        verifierOrphelins(chemin, sec.getOrDefault("sorties", ""), aval, "C-02", "sortie");

        // C-38 : identifiants en ASCII snake_case
        for (String cle : new String[]{"entrees", "sorties"})
            for (String champ : champs(sec.getOrDefault(cle, "")))
                if (!champ.matches("[a-z][a-z0-9_]*"))
                    constat("ÉCHEC", "C-38", chemin,
                            "l'identifiant « " + champ + " » n'est pas en ASCII snake_case");

        // C-21 : question ouverte sans décideur ni échéance
        Matcher mq = Pattern.compile("^\\|\\s*`(Q-[\\d-]+)`\\s*\\|(.*)$", Pattern.MULTILINE)
                .matcher(sec.getOrDefault("questions", ""));
        while (mq.find()) {
            // split(-1) conserve les cellules vides finales, comme en Python : sans cela
            // une ligne dont la dernière cellule est vide est trop courte, et le contrôle
            // est silencieusement sauté.
            String[] c = Arrays.stream(mq.group(2).split("\\|", -1)).map(String::trim)
                    .toArray(String[]::new);
            if (c.length >= 4 && !c[c.length - 2].toLowerCase().contains("fermée")
                    && (c[1].isEmpty() || c[2].isEmpty()))
                constat("ÉCHEC", "C-21", chemin, mq.group(1) + " sans décideur ou sans échéance");
        }

        // C-26 : exigence de réalisation sans source, propriétaire ou vérification
        Matcher mx = Pattern.compile("^\\|\\s*`(EX-[\\d-]+)`\\s*\\|(.*)$", Pattern.MULTILINE)
                .matcher(sec.getOrDefault("contraintes", ""));
        while (mx.find()) {
            String[] c = Arrays.stream(mx.group(2).split("\\|", -1)).map(String::trim)
                    .toArray(String[]::new);
            String[] noms = {"énoncé", "source", "propriétaire", "vérification"};
            List<String> manquants = new ArrayList<>();
            for (int i = 0; i < noms.length; i++)
                if (c.length <= i || c[i].isEmpty()) manquants.add(noms[i]);
            if (!manquants.isEmpty())
                constat("ÉCHEC", "C-26", chemin,
                        mx.group(1) + " sans " + String.join(", ", manquants));
        }

        // C-28 / C-29 : identités durables
        Map<String, String> table = annexe(texte);
        objets(texte).forEach((ident, nature) -> {
            if (!table.containsKey(ident))
                constat("ÉCHEC", "C-28", chemin,
                        ident + " (" + nature + ") n'a pas d'identité dans l'annexe");
        });
        table.forEach((ident, uid) -> {
            String[] vu = uuidsVus.get(uid);
            if (vu != null && !(vu[0].equals(chemin.toString()) && vu[1].equals(ident)))
                constat("ÉCHEC", "C-29", chemin,
                        "UUID " + uid.substring(0, 8) + " déjà porté par " + vu[1]);
            uuidsVus.put(uid, new String[]{chemin.toString(), ident});
        });

        // C-08 / C-10 / C-13 / C-11 : analyse du pseudo-langage
        Matcher mr = Pattern.compile("^###\\s+(RG-\\d+)[^\n]*\n(.*?)(?=\n###\\s|\\Z)",
                Pattern.DOTALL | Pattern.MULTILINE).matcher(regles);
        while (mr.find()) {
            String titre = mr.group(1), bloc = mr.group(2);
            for (String code : blocsCode(bloc)) {
                long si = code.lines().filter(l -> l.matches("\\s*SI\\b.*")
                        && !l.matches(".*\\bFIN SI\\b.*")).count();
                long sinon = code.lines().filter(l -> l.matches("\\s*SINON\\b.*")
                        && !l.matches("\\s*SINON SI\\b.*")).count();
                if (si > sinon)
                    constat("ÉCHEC", "C-08", chemin,
                            titre + " : " + si + " bloc(s) SI pour " + sinon + " SINON");
                Matcher ma = Pattern.compile("ARRONDIR\\(([^()]*(?:\\([^()]*\\)[^()]*)*)\\)")
                        .matcher(code);
                while (ma.find())
                    if (compter(ma.group(1), ",") != 2)
                        constat("ÉCHEC", "C-10", chemin, titre + " : ARRONDIR("
                                + tronquer(ma.group(1).trim(), 40)
                                + ") — il faut valeur, décimales et mode");
                if (code.contains("TANT QUE") && !Pattern.compile("it[ée]ration",
                        Pattern.CASE_INSENSITIVE).matcher(bloc).find())
                    constat("ÉCHEC", "C-13", chemin,
                            titre + " : TANT QUE sans nombre maximal d'itérations déclaré");
            }
            boolean superlatif = Pattern.compile(
                    "le plus (petit|grand|élevé|faible)|le meilleur|\\bMINIMUM\\b|\\bMAXIMUM\\b|le premier",
                    Pattern.CASE_INSENSITIVE).matcher(bloc).find();
            boolean collection = Pattern.compile(
                    "\\bDES\\b|\\bDANS\\b|\\bTRIER\\b|\\bFILTRER\\b|lignes|segments|bornes|la ligne dont|celle dont")
                    .matcher(bloc).find();
            boolean departage = Pattern.compile("égalit|ex æquo|départage|alphabétiq|le plus précoce",
                    Pattern.CASE_INSENSITIVE).matcher(bloc).find();
            if (superlatif && collection && !departage)
                constat("AVERTIR", "C-11", chemin,
                        titre + " : superlatif sans règle de départage visible");
        }

        // C-35 / C-36 : cohérence de la chaîne
        List<Etape> etapes = lireChaine(texte);
        if (!etapes.isEmpty()) {
            Set<String> disponibles = new HashSet<>(champs(sec.getOrDefault("entrees", "")));
            disponibles.addAll(trouverTout("^\\|\\s*`(P-\\d+)`", params, Pattern.MULTILINE));
            int i = texte.indexOf("Grandeurs internes");
            if (i >= 0)
                disponibles.addAll(champs(texte.substring(i,
                        Math.min(i + 1200, texte.length()))));
            Set<String> produits = new HashSet<>(), consommes = new HashSet<>();
            for (Etape e : etapes) {
                for (String g : e.consomme()) {
                    consommes.add(g);
                    if (!disponibles.contains(g))
                        constat("ÉCHEC", "C-35", chemin, e.id() + " consomme « " + g
                                + " » qui n'est ni entrée, ni paramètre, ni produit d'une étape antérieure");
                }
                disponibles.addAll(e.produit());
                produits.addAll(e.produit());
            }
            Set<String> sorties = champs(sec.getOrDefault("sorties", ""));
            produits.stream().filter(g -> !consommes.contains(g) && !sorties.contains(g))
                    .sorted().forEach(g -> constat("ÉCHEC", "C-36", chemin,
                            "« " + g + " » est produit mais n'est ni consommé ni déclaré en sortie"));
        }
        return true;
    }

    static void verifierOrphelins(Path chemin, String bloc, String aval, String regle, String quoi) {
        for (String champ : champs(bloc)) {
            if (champ.length() < 4) continue;
            if (!Pattern.compile("\\b" + Pattern.quote(champ) + "\\b").matcher(aval).find())
                constat("AVERTIR", regle, chemin,
                        quoi + " « " + champ + " » déclarée mais employée dans aucune règle");
        }
    }

    static void verifierLiens(Path chemin, String texte) {
        String sansCode = texte.replaceAll("(?s)```.*?```", "");
        Matcher m = Pattern.compile("\\]\\(([^)#][^)]*)\\)").matcher(sansCode);
        while (m.find()) {
            String cible = m.group(1).split("#")[0];
            if (cible.isEmpty() || cible.startsWith("http")) continue;
            if (!Files.exists(chemin.getParent().resolve(cible).normalize()))
                constat("ÉCHEC", "LIEN", chemin, "lien cassé vers " + cible);
        }
    }

    static int[] version(String v) {
        try {
            return Arrays.stream(v.split("\\.")).mapToInt(Integer::parseInt).toArray();
        } catch (NumberFormatException e) { return null; }
    }

    static int compter(String texte, String motif) {
        int n = 0, i = 0;
        while ((i = texte.indexOf(motif, i)) >= 0) { n++; i += motif.length(); }
        return n;
    }

    static String tronquer(String s, int n) { return s.length() <= n ? s : s.substring(0, n); }

    // --- vue de la chaîne : qui crée, qui utilise, ce qui est parallélisable ---
    static int chaine(Path chemin) throws IOException {
        String texte = Files.readString(chemin);
        List<Etape> etapes = lireChaine(texte);
        List<Groupe> groupes = lireGroupes(texte);
        if (etapes.isEmpty()) {
            System.out.println("Aucune chaîne de traitement déclarée dans ce document.");
            return 2;
        }
        Map<String, String> sec = sections(texte);
        Set<String> connus = new LinkedHashSet<>(champs(sec.getOrDefault("entrees", "")));
        connus.addAll(trouverTout("^\\|\\s*`(P-\\d+)`", sec.getOrDefault("parametres", ""),
                Pattern.MULTILINE));
        Set<String> sorties = champs(sec.getOrDefault("sorties", ""));

        System.out.printf("Chaîne de %s — %d étape(s), %d groupe(s)%n%n",
                chemin.getFileName(), etapes.size(), groupes.size());

        // qui crée, qui utilise
        Map<String, List<String>> cree = new LinkedHashMap<>();
        Map<String, List<String>> utilise = new LinkedHashMap<>();
        for (Etape e : etapes) {
            for (String g : e.produit())
                cree.computeIfAbsent(g, k -> new ArrayList<>()).add(e.id());
            for (String g : e.consomme())
                utilise.computeIfAbsent(g, k -> new ArrayList<>()).add(e.id());
        }
        System.out.printf("%-30s %-14s %s%n", "GRANDEUR", "CRÉÉE PAR", "UTILISÉE PAR");
        Set<String> toutes = new TreeSet<>(cree.keySet());
        toutes.addAll(utilise.keySet());
        for (String g : toutes) {
            String origine = cree.containsKey(g) ? String.join(", ", cree.get(g))
                    : (connus.contains(g) ? "contrat" : "?");
            String aval = utilise.containsKey(g) ? String.join(", ", utilise.get(g))
                    : (sorties.contains(g) ? "sortie" : "—");
            System.out.printf("%-30s %-14s %s%n",
                    g.length() > 30 ? g.substring(0, 30) : g, origine, aval);
        }

        // C-35 / C-36
        System.out.println();
        Set<String> disponibles = new LinkedHashSet<>(connus);
        for (Etape e : etapes) {
            for (String g : e.consomme())
                if (!disponibles.contains(g))
                    System.out.println("ÉCHEC C-35  " + e.id() + " consomme « " + g
                            + " » qui n'est ni entrée, ni paramètre, ni produit d'une étape antérieure");
            disponibles.addAll(e.produit());
        }
        cree.forEach((g, origines) -> {
            if (!utilise.containsKey(g) && !sorties.contains(g))
                System.out.println("ÉCHEC C-36  « " + g + " » produit par "
                        + String.join(", ", origines) + " n'est ni consommé ni déclaré en sortie");
        });

        // fils d'exécution : niveaux topologiques
        Map<String, String> producteur = new LinkedHashMap<>();
        for (Etape e : etapes)
            for (String g : e.produit()) producteur.put(g, e.id());
        Map<String, Integer> niveau = new LinkedHashMap<>();
        for (Etape e : etapes) {
            int amont = 0;
            for (String g : e.consomme())
                if (producteur.containsKey(g))
                    amont = Math.max(amont, niveau.getOrDefault(producteur.get(g), 0));
            niveau.put(e.id(), 1 + amont);
        }
        Map<Integer, List<String>> vagues = new LinkedHashMap<>();
        niveau.forEach((id, n) -> vagues.computeIfAbsent(n, k -> new ArrayList<>()).add(id));
        System.out.printf("%nFils d'exécution — %d niveau(x)%n%n", vagues.size());
        for (Integer n : new TreeSet<>(vagues.keySet())) {
            List<String> etiquettes = new ArrayList<>(vagues.get(n));
            Collections.sort(etiquettes);
            System.out.println("  niveau " + n + " : " + String.join(", ", etiquettes)
                    + (etiquettes.size() > 1 ? "  ← indépendantes, parallélisables" : ""));
        }

        // chemin critique : on remonte par l'amont le plus tardif
        Map<String, String> precedent = new LinkedHashMap<>();
        for (Etape e : etapes) {
            int meilleurN = -1;
            String meilleur = null;
            for (String g : e.consomme()) {
                String p = producteur.get(g);
                if (p == null) continue;
                int n = niveau.getOrDefault(p, 0);
                // ordre des couples (niveau, identifiant), comme le max de Python
                if (n > meilleurN || (n == meilleurN && p.compareTo(meilleur) > 0)) {
                    meilleurN = n;
                    meilleur = p;
                }
            }
            if (meilleur != null) precedent.put(e.id(), meilleur);
        }
        String fin = null;
        for (var e : niveau.entrySet())  // max de Python : le PREMIER maximum rencontré
            if (fin == null || e.getValue() > niveau.get(fin)) fin = e.getKey();
        List<String> critique = new ArrayList<>(List.of(fin));
        String courant = fin;
        while (precedent.containsKey(courant)) {
            courant = precedent.get(courant);
            critique.add(courant);
        }
        Collections.reverse(critique);
        System.out.printf("%n  chemin critique : %s  (%d étapes)%n",
                String.join(" → ", critique), critique.size());
        System.out.println();
        System.out.println("  La spécification dit ce qui est INDÉPENDANT, pas ce qu'il faut");
        System.out.println("  paralléliser. La contrainte de déterminisme (§11) peut l'interdire.");

        // vue groupée
        if (!groupes.isEmpty()) {
            System.out.printf("%nVue de niveau supérieur%n%n");
            System.out.println("```mermaid\nflowchart LR");
            for (Groupe gr : groupes) {
                List<List<String>> f = frontiere(gr.membres(), etapes);
                System.out.printf("    %s[\"%s %s<br/><small>%s</small>\"]%n",
                        gr.id().replace("-", ""), gr.id(), gr.nom(), gr.role());
                System.out.println("    %% consomme : " + String.join(", ", f.get(0)));
                System.out.println("    %% produit  : " + String.join(", ", f.get(1)));
            }
            for (Groupe a : groupes) {
                List<String> pa = frontiere(a.membres(), etapes).get(1);
                for (Groupe b : groupes) {
                    if (a == b) continue;
                    List<String> cb = frontiere(b.membres(), etapes).get(0);
                    List<String> liens = pa.stream().filter(cb::contains).sorted().toList();
                    if (!liens.isEmpty())
                        System.out.printf("    %s -->|%s| %s%n", a.id().replace("-", ""),
                                String.join(", ", liens), b.id().replace("-", ""));
                }
            }
            System.out.println("```");
        }
        return 0;
    }

    // --- parcours d'une grandeur : où elle est déclarée, produite, consommée ---
    static int tracer(String nom, List<Path> fichiers) throws IOException {
        Pattern motif = Pattern.compile("\\b" + Pattern.quote(nom) + "\\b");
        System.out.printf("Parcours de « %s »%n%n", nom);
        boolean trouve = false;
        for (Path chemin : fichiers) {
            String texte = Files.readString(chemin);
            if (!motif.matcher(texte).find()) continue;
            Map<String, String> sec = sections(texte);
            List<String> roles = new ArrayList<>();
            if (champs(sec.getOrDefault("entrees", "")).contains(nom)) roles.add("ENTRÉE");
            if (champs(sec.getOrDefault("sorties", "")).contains(nom)) roles.add("SORTIE");
            Set<String> regles = new TreeSet<>(trouverTout(
                    "###\\s+(RG-\\d+)[^\\n]*\\n(?:(?!###)[\\s\\S])*?" + Pattern.quote(nom),
                    sec.getOrDefault("regles", ""), Pattern.DOTALL));
            if (!regles.isEmpty()) roles.add("employée par " + String.join(", ", regles));
            if (roles.isEmpty()) roles.add("citée");
            trouve = true;
            System.out.printf("  %-46s %s%n", racine.relativize(chemin),
                    String.join(" · ", roles));
        }
        if (!trouve) System.out.println("  (aucune occurrence)");
        return 0;
    }

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(
                java.io.FileDescriptor.out), true, java.nio.charset.StandardCharsets.UTF_8));
        racine = Paths.get(Verifier.class.getProtectionDomain().getCodeSource()
                .getLocation().getPath()).getParent();
        if (!Files.exists(racine.resolve("CADRE.md"))) racine = Paths.get("").toAbsolutePath();

        if (args.length > 0 && args[0].equals("--chaine")) {
            if (args.length < 2) {
                System.out.println("usage : Verifier.java --chaine <fichier>");
                System.exit(2);
            }
            System.exit(chaine(Paths.get(args[1]).toAbsolutePath()));
        }
        if (args.length > 0 && args[0].equals("--tracer")) {
            if (args.length < 2) {
                System.out.println("usage : Verifier.java --tracer <nom de grandeur>");
                System.exit(2);
            }
            try (Stream<Path> s = Files.walk(racine)) {
                System.exit(tracer(args[1], s.filter(x -> x.toString().endsWith(".md"))
                        .filter(x -> !x.toString().contains(".git")).sorted().toList()));
            }
        }

        List<Path> fichiers;
        if (args.length > 0) {
            fichiers = Arrays.stream(args).map(a -> Paths.get(a).toAbsolutePath()).toList();
        } else {
            try (Stream<Path> s = Files.walk(racine)) {
                fichiers = s.filter(p -> p.toString().endsWith(".md"))
                        .filter(p -> !p.toString().contains(".git"))
                        .filter(p -> !p.toString().contains("jeu-d-essai"))
                        .sorted().toList();
            }
        }
        int specs = 0;
        for (Path f : fichiers) {
            String texte = Files.readString(f);
            verifierLiens(f, texte);
            if (verifierSpec(f, texte)) specs++;
        }
        long echecs = constats.stream().filter(c -> c.niveau().equals("ÉCHEC")).count();
        constats.stream().sorted().forEach(c -> System.out.printf("%-8s %-5s %-46s %s%n",
                c.niveau(), c.regle(), c.fichier(), c.message()));
        System.out.printf("%n%d fichier(s) examiné(s), dont %d spécification(s) — "
                + "%d échec(s), %d avertissement(s)%n",
                fichiers.size(), specs, echecs, constats.size() - echecs);
        if (constats.isEmpty())
            System.out.println("Aucun défaut mécanique. 23 des 39 contrôles sont mécanisés ; "
                    + "les seize restants et les contrôles humains H-01 à H-07 relèvent de la relecture.");
        System.exit(echecs > 0 ? 1 : 0);
    }
}
