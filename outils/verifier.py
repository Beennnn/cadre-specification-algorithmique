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
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import identites  # noqa: E402  — même dossier

constats = []
_uuids_vus = {}


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

    # --- C-08 / C-10 / C-11 / C-13 : analyse du pseudo-langage
    for titre, bloc in re.findall(r"^###\s+(RG-\d+)[^\n]*\n(.*?)(?=\n###\s|\Z)",
                                  corps_regles, re.S | re.M):
        for code in blocs_code(bloc):
            lignes = code.splitlines()
            multi = [l for l in lignes if re.match(r"\s*SI\b", l)
                     and not re.search(r"\bFIN SI\b", l)]
            sinon = [l for l in lignes if re.match(r"\s*SINON\b", l)
                     and not re.match(r"\s*SINON SI\b", l)]
            if len(multi) > len(sinon):
                constat("ÉCHEC", "C-08", chemin,
                        "%s : %d bloc(s) SI pour %d SINON"
                        % (titre, len(multi), len(sinon)))
            for appel in re.findall(r"ARRONDIR\(([^()]*(?:\([^()]*\)[^()]*)*)\)", code):
                if appel.count(",") != 2:
                    constat("ÉCHEC", "C-10", chemin,
                            "%s : ARRONDIR(%s) — il faut valeur, décimales et mode"
                            % (titre, appel.strip()[:40]))
            if re.search(r"\bTANT QUE\b", code) and not re.search(
                    r"it[ée]ration", bloc, re.I):
                constat("ÉCHEC", "C-13", chemin,
                        "%s : TANT QUE sans nombre maximal d'itérations déclaré" % titre)
        # un superlatif sur deux scalaires (« le plus petit de a et b ») est sans
        # ambiguïté ; seul un superlatif choisissant un ÉLÉMENT dans une collection
        # exige un départage
        superlatif = re.search(r"le plus (petit|grand|élevé|faible)|le meilleur|"
                               r"\bMINIMUM\b|\bMAXIMUM\b|le premier", bloc, re.I)
        collection = re.search(r"\bDES\b|\bDANS\b|\bTRIER\b|\bFILTRER\b|"
                               r"lignes|segments|bornes|la ligne dont|celle dont",
                               bloc)
        if superlatif and collection and not re.search(
                r"égalit|ex æquo|départage|alphabétiq|le plus précoce", bloc, re.I):
            constat("AVERTIR", "C-11", chemin,
                    "%s : superlatif sans règle de départage visible" % titre)

    # --- C-35 / C-36 : cohérence de la chaîne, dans la passe globale
    etapes, _ = lire_chaine(texte)
    if etapes:
        disponibles = set(champs(sec.get("entrees", ""))) | set(
            re.findall(r"^\|\s*`(P-\d+)`", sec.get("parametres", ""), re.M))
        internes = set(champs(texte.split("Grandeurs internes")[1][:1200])
                       ) if "Grandeurs internes" in texte else set()
        disponibles |= internes
        produits, consommes = set(), set()
        for e in etapes:
            for g in e["consomme"]:
                consommes.add(g)
                if g not in disponibles:
                    constat("ÉCHEC", "C-35", chemin,
                            "%s consomme « %s » qui n'est ni entrée, ni paramètre, "
                            "ni produit d'une étape antérieure" % (e["id"], g))
            disponibles |= set(e["produit"])
            produits |= set(e["produit"])
        for g in sorted(produits - consommes - set(champs(sec.get("sorties", "")))):
            constat("ÉCHEC", "C-36", chemin,
                    "« %s » est produit mais n'est ni consommé ni déclaré en sortie" % g)

    # --- C-38 : identifiants en ASCII strict, snake_case
    for cle in ("entrees", "sorties"):
        for champ in champs(sec.get(cle, "")):
            if not re.fullmatch(r"[a-z][a-z0-9_]*", champ):
                constat("ÉCHEC", "C-38", chemin,
                        "l'identifiant « %s » n'est pas en ASCII snake_case" % champ)

    # --- C-28 / C-29 : identités durables
    table = identites.annexe_existante(texte)
    for ident, nature, _ in identites.objets(texte):
        if ident not in table:
            constat("ÉCHEC", "C-28", chemin,
                    "%s (%s) n'a pas d'identité dans l'annexe" % (ident, nature))
    for ident, uid in table.items():
        if uid in _uuids_vus and _uuids_vus[uid] != (chemin, ident):
            constat("ÉCHEC", "C-29", chemin,
                    "UUID %s déjà porté par %s" % (uid[:8], _uuids_vus[uid][1]))
        _uuids_vus[uid] = (chemin, ident)

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
    texte = open(chemin, encoding="utf-8").read()
    # les blocs de code contiennent des notations du type Liste[X](1 .. 200)
    # qui ressemblent à des liens sans en être
    texte = re.sub(r"```.*?```", "", texte, flags=re.S)
    for m in re.finditer(r"\]\(([^)#][^)]*)\)", texte):
        cible = m.group(1).split("#")[0]
        if not cible or cible.startswith("http"):
            continue
        if not os.path.exists(os.path.normpath(os.path.join(dossier, cible))):
            constat("ÉCHEC", "LIEN", chemin, "lien cassé vers %s" % cible)


# ---------------------------------------------------------------- exécution
def lire_chaine(texte):
    """Étapes et groupes déclarés dans le document."""
    etapes, groupes = [], []
    for ident, nom, cons, prod, regles in re.findall(
            r"^\|\s*`(ET-\d+)`\s*([^|]*)\|([^|]*)\|([^|]*)\|([^|]*)\|",
            texte, re.M):
        jetons = lambda c: [x for x in re.findall(r"`([^`]+)`", c)]
        etapes.append({"id": ident, "nom": nom.strip(),
                       "consomme": jetons(cons), "produit": jetons(prod),
                       "regles": jetons(regles)})
    for ident, nom, membres, role in re.findall(
            r"^\|\s*`(GR-\d+)`\s*([^|]*)\|([^|]*)\|([^|]*)\|", texte, re.M):
        groupes.append({"id": ident, "nom": nom.strip(),
                        "membres": re.findall(r"ET-\d+", membres),
                        "role": role.strip()})
    return etapes, groupes


def frontiere(membres, etapes):
    """Ce qu'un ensemble d'étapes consomme et produit vis-à-vis de l'extérieur."""
    dedans = [e for e in etapes if e["id"] in membres]
    produit_interne = {g for e in dedans for g in e["produit"]}
    consomme = sorted({g for e in dedans for g in e["consomme"]} - produit_interne)
    consomme_interne = {g for e in dedans for g in e["consomme"]}
    externe = {g for e in etapes if e["id"] not in membres for g in e["consomme"]}
    produit = sorted(g for g in produit_interne
                     if g in externe or g not in consomme_interne)
    return consomme, produit


def chaine(chemin):
    texte = open(chemin, encoding="utf-8").read()
    etapes, groupes = lire_chaine(texte)
    if not etapes:
        print("Aucune chaîne de traitement déclarée dans ce document.")
        return 2
    sec = sections(texte)
    connus = set(champs(sec.get("entrees", ""))) | set(
        re.findall(r"^\|\s*`(P-\d+)`", sec.get("parametres", ""), re.M))
    sorties = set(champs(sec.get("sorties", "")))

    print("Chaîne de %s — %d étape(s), %d groupe(s)\n"
          % (os.path.basename(chemin), len(etapes), len(groupes)))

    # --- qui crée, qui utilise
    cree, utilise = {}, {}
    for e in etapes:
        for g in e["produit"]:
            cree.setdefault(g, []).append(e["id"])
        for g in e["consomme"]:
            utilise.setdefault(g, []).append(e["id"])
    print("%-30s %-14s %s" % ("GRANDEUR", "CRÉÉE PAR", "UTILISÉE PAR"))
    for g in sorted(set(cree) | set(utilise)):
        origine = ", ".join(cree.get(g, [])) or ("contrat" if g in connus else "?")
        print("%-30s %-14s %s" % (g[:30], origine,
                                  ", ".join(utilise.get(g, [])) or
                                  ("sortie" if g in sorties else "—")))

    # --- C-35 / C-36
    print()
    disponibles = set(connus)
    for e in etapes:
        for g in e["consomme"]:
            if g not in disponibles:
                print("ÉCHEC C-35  %s consomme « %s » qui n'est ni entrée, "
                      "ni paramètre, ni produit d'une étape antérieure" % (e["id"], g))
        disponibles |= set(e["produit"])
    for g, origines in cree.items():
        if g not in utilise and g not in sorties:
            print("ÉCHEC C-36  « %s » produit par %s n'est ni consommé "
                  "ni déclaré en sortie" % (g, ", ".join(origines)))

    # --- fils d'exécution
    niveau, producteur = {}, {}
    for e in etapes:
        for g in e["produit"]:
            producteur[g] = e["id"]
    for e in etapes:
        amont = [niveau.get(producteur[g], 0) for g in e["consomme"]
                 if g in producteur]
        niveau[e["id"]] = 1 + max(amont or [0])
    vagues = {}
    for ident, n in niveau.items():
        vagues.setdefault(n, []).append(ident)
    print("\nFils d'exécution — %d niveau(x)\n" % len(vagues))
    for n in sorted(vagues):
        etiquettes = sorted(vagues[n])
        marque = "  ← indépendantes, parallélisables" if len(etiquettes) > 1 else ""
        print("  niveau %d : %s%s" % (n, ", ".join(etiquettes), marque))
    # chemin critique
    precedent = {}
    for e in etapes:
        amont = [(niveau.get(producteur[g], 0), producteur[g]) for g in e["consomme"]
                 if g in producteur]
        if amont:
            precedent[e["id"]] = max(amont)[1]
    fin = max(niveau, key=lambda k: niveau[k])
    chemin, courant = [fin], fin
    while courant in precedent:
        courant = precedent[courant]
        chemin.append(courant)
    print("\n  chemin critique : %s  (%d étapes)"
          % (" → ".join(reversed(chemin)), len(chemin)))
    print("\n  La spécification dit ce qui est INDÉPENDANT, pas ce qu'il faut")
    print("  paralléliser. La contrainte de déterminisme (§11) peut l'interdire.")

    # --- vue groupée
    if groupes:
        print("\nVue de niveau supérieur\n")
        print("```mermaid\nflowchart LR")
        for gr in groupes:
            c, pr = frontiere(gr["membres"], etapes)
            print("    %s[\"%s %s<br/><small>%s</small>\"]"
                  % (gr["id"].replace("-", ""), gr["id"], gr["nom"], gr["role"]))
            print("    %%%% consomme : %s" % ", ".join(c))
            print("    %%%% produit  : %s" % ", ".join(pr))
        for a in groupes:
            _, pa = frontiere(a["membres"], etapes)
            for b in groupes:
                if a is b:
                    continue
                cb, _ = frontiere(b["membres"], etapes)
                liens = sorted(set(pa) & set(cb))
                if liens:
                    print("    %s -->|%s| %s" % (a["id"].replace("-", ""),
                                                 ", ".join(liens),
                                                 b["id"].replace("-", "")))
        print("```")
    return 0


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
    if cibles and cibles[0] == "--chaine":
        if len(cibles) < 2:
            print("usage : verifier.py --chaine <fichier>")
            return 2
        return chaine(os.path.abspath(cibles[1]))
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
        print("Aucun défaut mécanique. 30 des 38 contrôles sont mécanisés ; les huit "
              "restants et les contrôles humains H-01 à H-07 relèvent de la relecture.")
    return 1 if echecs else 0


if __name__ == "__main__":
    sys.exit(main())
