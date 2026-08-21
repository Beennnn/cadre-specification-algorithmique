#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Contrôles mécaniques d'une spécification fonctionnelle.

Met en œuvre les règles C-xx de REGLES-DE-CONTROLE.md qui sont mécanisables.
Les règles non mécanisables (C-05 à C-13 en partie, C-18, C-22) et les contrôles
humains H-xx relèvent de la relecture — voir guides/5-VALIDER.md.

Usage :
    python3 outils/verifier.py                  # tout le dépôt
    python3 outils/verifier.py <fichier.md>     # une spécification
Code de retour : 1 si au moins un ÉCHEC, 0 sinon.
"""
import os
import re
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
constats = []


def constat(niveau, regle, fichier, message):
    constats.append((niveau, regle, os.path.relpath(fichier, RACINE), message))


# ---------------------------------------------------------------- découpage
TITRES = {
    "entrees": "entrée", "sorties": "sortie", "parametres": "paramètre",
    "regles": "règle", "invariants": "invariant", "erreurs": "erreur",
    "essai": "jeu d'essai", "contraintes": "contrainte",
    "questions": "question", "historique": "historique",
}


def sections(texte):
    """Découpe le document en sections de niveau 2, indexées par mot-clé."""
    trouvees, courante, tampon = {}, None, []
    for ligne in texte.splitlines():
        if ligne.startswith("## "):
            if courante:
                trouvees.setdefault(courante, []).append("\n".join(tampon))
            titre = ligne[3:].lower()
            courante = next((c for c, m in TITRES.items() if m in titre), None)
            tampon = []
        elif courante:
            tampon.append(ligne)
    if courante:
        trouvees.setdefault(courante, []).append("\n".join(tampon))
    return {c: "\n".join(v) for c, v in trouvees.items()}


def blocs_code(texte):
    return re.findall(r"```[^\n]*\n(.*?)```", texte, re.S)


def champs(texte):
    """Noms de champs d'un contrat : lignes « nom : Type » dans un bloc de code."""
    noms = []
    for bloc in blocs_code(texte):
        for ligne in bloc.splitlines():
            m = re.match(r"\s{2,}([a-zà-ÿ_][a-zà-ÿ0-9_]*)\s*:\s*\S", ligne)
            if m:
                noms.append(m.group(1))
    return sorted(set(noms))


def version_tuple(v):
    try:
        return tuple(int(x) for x in v.split("."))
    except ValueError:
        return None


# ---------------------------------------------------------------- contrôles
def verifier_spec(chemin):
    texte = open(chemin, encoding="utf-8").read()
    # une spécification se reconnaît à ses règles numérotées ; les modèles
    # vierges de templates/ sont des squelettes incomplets par construction
    if not re.search(r"^###\s+RG-\d", texte, re.M) or os.sep + "templates" + os.sep in chemin:
        return False
    sec = sections(texte)
    corps_regles = sec.get("regles", "")

    # --- C-19 / C-20 : version et historique
    m = re.search(r"\|\s*\*\*Version\*\*\s*\|\s*([0-9.]+)\s*\|", texte)
    entete = m.group(1) if m else None
    lignes_hist = re.findall(r"^\|\s*\*{0,2}([0-9]+\.[0-9]+\.[0-9]+)\*{0,2}\s*\|(.*)$",
                             sec.get("historique", ""), re.M)
    if entete and lignes_hist:
        if entete != lignes_hist[-1][0]:
            constat("ÉCHEC", "C-19", chemin,
                    "en-tête %s ≠ dernière ligne d'historique %s"
                    % (entete, lignes_hist[-1][0]))
        precedente = None
        for version, reste in lignes_hist:
            cellules = [c.strip() for c in reste.split("|")]
            impact = cellules[2] if len(cellules) > 2 else ""
            notice = cellules[3] if len(cellules) > 3 else ""
            change = re.search(r"\boui\b", impact, re.I)
            # C-24 : un changement à impact renvoie à une notice existante
            if change:
                if not re.search(r"N-[0-9.]+", notice):
                    constat("ÉCHEC", "C-24", chemin,
                            "la version %s déclare un impact sans renvoyer "
                            "à une notice de changement" % version)
                else:
                    nom = re.search(r"N-[0-9.]+", notice).group(0)
                    if not re.search(r"^###\s+%s\b" % re.escape(nom), texte, re.M):
                        constat("ÉCHEC", "C-24", chemin,
                                "la notice %s est citée mais absente du document" % nom)
            v = version_tuple(version)
            if change and precedente and v and v[0] == precedente[0]:
                constat("ÉCHEC", "C-20", chemin,
                        "la version %s déclare un impact sur les résultats "
                        "mais n'incrémente pas le majeur" % version)
            if v:
                precedente = v
    elif not entete:
        constat("ÉCHEC", "C-19", chemin, "aucun numéro de version dans l'en-tête")

    # --- C-23 : glossaire et paramètres de référence nommés
    if not re.search(r"\*\*Glossaire de référence\*\*", texte):
        constat("AVERTIR", "C-23", chemin,
                "l'en-tête ne nomme pas le glossaire de référence et sa version")

    # --- C-04 : paramètre déclaré et jamais employé
    declares = re.findall(r"^\|\s*`(P-\d+)`", sec.get("parametres", ""), re.M)
    for p in declares:
        ailleurs = len(re.findall(re.escape(p), texte)) - len(
            re.findall(re.escape(p), sec.get("parametres", "")))
        if ailleurs == 0:
            constat("ÉCHEC", "C-04", chemin, "%s déclaré mais employé nulle part" % p)

    # --- C-14 / C-15 / C-17 : traçabilité
    definies = re.findall(r"^###\s+(RG-\d+)", corps_regles, re.M)
    for r in definies:
        if definies.count(r) > 1:
            constat("ÉCHEC", "C-17", chemin, "%s défini plusieurs fois" % r)
    couverture = ""
    m = re.search(r"###\s+Table de couverture(.*?)(?=\n##\s|\Z)", texte, re.S)
    if m:
        couverture = m.group(1)
        for r in sorted(set(definies)):
            if r not in couverture:
                constat("ÉCHEC", "C-14", chemin,
                        "%s absent de la table de couverture" % r)
        cites = set(re.findall(r"CT-\d+", couverture))
        existants = set(re.findall(r"CT-\d+", sec.get("essai", "")))
        for ct in sorted(cites - existants):
            constat("ÉCHEC", "C-15", chemin,
                    "%s cité en couverture mais défini nulle part" % ct)
    elif definies:
        constat("ÉCHEC", "C-14", chemin, "aucune table de couverture")

    # --- C-01 / C-02 : entrées et sorties orphelines
    aval = corps_regles + sec.get("invariants", "") + sec.get("essai", "")
    for cle, regle, quoi in (("entrees", "C-01", "entrée"),
                             ("sorties", "C-02", "sortie")):
        for champ in champs(sec.get(cle, "")):
            if len(champ) < 4:
                continue
            if not re.search(r"\b%s\b" % re.escape(champ), aval):
                constat("AVERTIR", regle, chemin,
                        "%s « %s » déclarée mais employée dans aucune règle"
                        % (quoi, champ))

    # --- C-26 : exigence de réalisation sans source, propriétaire ou vérification
    for ligne in re.findall(r"^\|\s*`(EX-[\d-]+)`\s*\|(.*)$",
                            sec.get("contraintes", ""), re.M):
        cellules = [c.strip() for c in ligne[1].split("|")]
        manquants = [nom for nom, i in (("énoncé", 0), ("source", 1),
                                        ("propriétaire", 2), ("vérification", 3))
                     if len(cellules) <= i or not cellules[i]]
        if manquants:
            constat("ÉCHEC", "C-26", chemin,
                    "%s sans %s" % (ligne[0], ", ".join(manquants)))

    # --- C-21 : question ouverte sans décideur ni échéance
    for ligne in re.findall(r"^\|\s*`(Q-[\d-]+)`\s*\|(.*)$",
                            sec.get("questions", ""), re.M):
        cellules = [c.strip() for c in ligne[1].split("|")]
        if len(cellules) >= 4 and "fermée" not in cellules[-2].lower():
            if not cellules[1] or not cellules[2]:
                constat("ÉCHEC", "C-21", chemin,
                        "%s sans décideur ou sans échéance" % ligne[0])
    return True


def verifier_liens(chemin):
    dossier = os.path.dirname(chemin)
    for m in re.finditer(r"\]\(([^)#][^)]*)\)", open(chemin, encoding="utf-8").read()):
        cible = m.group(1).split("#")[0]
        if not cible or cible.startswith("http"):
            continue
        if not os.path.exists(os.path.normpath(os.path.join(dossier, cible))):
            constat("ÉCHEC", "LIEN", chemin, "lien cassé vers %s" % cible)


# ---------------------------------------------------------------- exécution
def tracer(nom, fichiers):
    """Parcours d'une grandeur : où elle est déclarée, produite, consommée."""
    motif = re.compile(r"\b%s\b" % re.escape(nom))
    print("Parcours de « %s »\n" % nom)
    trouve = False
    for chemin in sorted(fichiers):
        texte = open(chemin, encoding="utf-8").read()
        if not motif.search(texte):
            continue
        sec = sections(texte)
        roles = []
        if nom in champs(sec.get("entrees", "")):
            roles.append("ENTRÉE")
        if nom in champs(sec.get("sorties", "")):
            roles.append("SORTIE")
        regles = sorted(set(re.findall(r"###\s+(RG-\d+)[^\n]*\n(?:(?!###).)*?"
                                       + re.escape(nom),
                                       sec.get("regles", ""), re.S)))
        if regles:
            roles.append("employée par " + ", ".join(regles))
        if not roles:
            roles.append("citée")
        trouve = True
        print("  %-46s %s" % (os.path.relpath(chemin, RACINE), " · ".join(roles)))
    if not trouve:
        print("  (aucune occurrence)")
    return 0


def main():
    cibles = sys.argv[1:]
    if cibles and cibles[0] == "--tracer":
        if len(cibles) < 2:
            print("usage : verifier.py --tracer <nom de grandeur>")
            return 2
        return tracer(cibles[1],
                      [os.path.join(r, f) for r, d, fs in os.walk(RACINE)
                       if ".git" not in r for f in fs if f.endswith(".md")])
    if cibles:
        fichiers = [os.path.abspath(c) for c in cibles]
    else:
        fichiers = [os.path.join(r, f)
                    for r, d, fs in os.walk(RACINE) if ".git" not in r
                    for f in fs if f.endswith(".md")]
    specs = 0
    for f in sorted(fichiers):
        verifier_liens(f)
        if verifier_spec(f):
            specs += 1

    echecs = [c for c in constats if c[0] == "ÉCHEC"]
    for niveau, regle, fichier, message in sorted(constats):
        print("%-8s %-5s %-46s %s" % (niveau, regle, fichier, message))
    print("\n%d fichier(s) examiné(s), dont %d spécification(s) — "
          "%d échec(s), %d avertissement(s)"
          % (len(fichiers), specs, len(echecs), len(constats) - len(echecs)))
    if not constats:
        print("Aucun défaut mécanique. Les contrôles C-05 à C-13, C-18, C-22, C-25 et "
              "les contrôles humains H-01 à H-07 relèvent de la relecture.")
    return 1 if echecs else 0


if __name__ == "__main__":
    sys.exit(main())
