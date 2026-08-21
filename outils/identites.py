#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Attribution et maintenance des identités durables (UUID).

Chaque objet identifié — spécification, fonction, règle, paramètre, donnée,
exigence, invariant, cas de test, cas d'erreur, question — porte un UUID attribué
une fois et jamais modifié. Le UUID est l'identité ; l'identifiant lisible
(`RG-010`) et le libellé sont des étiquettes, qui peuvent changer.

Les UUID vivent dans une annexe « Identités » en fin de document, pour que le
corps du texte reste lisible.

Usage :
    python3 outils/identites.py --attribuer [fichier ...]   # complète les manquants
    python3 outils/identites.py --registre                  # produit registre.json
"""
import json
import os
import re
import sys
import uuid

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ANNEXE = "## Annexe — Identités"

# Les objets susceptibles d'être cités depuis l'extérieur de leur document.
MOTIFS = [
    (r"^###\s+(RG-\d+)\s+—\s+(.+)$", "règle"),
    (r"^###\s+(CT-\d+)\s+—\s+(.+)$", "cas de test"),
    (r"^\|\s*`(FN-\d+)`\s*\|\s*\*{0,2}([^|*]+)", "fonction"),
    (r"^\|\s*`(P-\d+)`\s*\|\s*([^|]+)", "paramètre"),
    (r"^\|\s*`(D-\d+)`\s*\|\s*([^|]+)", "donnée"),
    (r"^\|\s*`(EX-\d+)`\s*\|\s*([^|]+)", "exigence"),
    (r"^\|\s*`(INV-\d+)`\s*\|\s*([^|]+)", "invariant"),
    (r"^\|\s*`(E-[A-Z]+-\d+)`\s*\|\s*([^|]+)", "cas d'erreur"),
    (r"^\|\s*`(Q-[\d-]+)`\s*\|\s*([^|]+)", "question"),
]


def corps(texte):
    return texte.split(ANNEXE)[0]


def objets(texte):
    """Objets identifiés du document, hors annexe, dans l'ordre d'apparition."""
    trouves, vus = [], set()
    m = re.search(r"\|\s*\*\*Identifiant\*\*\s*\|\s*([A-Z]+-[A-Z]*-?\d+)", texte)
    if m:
        titre = re.search(r"^#\s+(.+)$", texte, re.M)
        trouves.append((m.group(1), "document", titre.group(1) if titre else ""))
        vus.add(m.group(1))
    for motif, nature in MOTIFS:
        for ident, libelle in re.findall(motif, corps(texte), re.M):
            if ident in vus:
                continue
            vus.add(ident)
            trouves.append((ident, nature, re.sub(r"\s+", " ", libelle).strip(" *|")))
    return trouves


def annexe_existante(texte):
    if ANNEXE not in texte:
        return {}
    bloc = texte.split(ANNEXE)[1]
    return {i: u for i, u in re.findall(
        r"^\|\s*`([A-Z]+[-A-Z]*-[\d-]+)`\s*\|\s*`([0-9a-f-]{36})`", bloc, re.M)}


def attribuer(chemin):
    texte = open(chemin, encoding="utf-8").read()
    liste = objets(texte)
    if not liste:
        return 0
    connus = annexe_existante(texte)
    nouveaux = 0
    lignes = ["", ANNEXE, "",
              "*Chaque objet porte un UUID attribué une fois et jamais modifié. "
              "L'identifiant lisible et le libellé sont des étiquettes : ils peuvent "
              "changer, l'identité non. Voir [CADRE.md §2.8](../../CADRE.md).*"
              if "fil-rouge" in chemin else
              "*Chaque objet porte un UUID attribué une fois et jamais modifié. "
              "L'identifiant lisible et le libellé sont des étiquettes : ils peuvent "
              "changer, l'identité non. Voir [CADRE.md §2.8](../CADRE.md).*",
              "", "| Identifiant | UUID | Nature | Libellé |", "|---|---|---|---|"]
    for ident, nature, libelle in liste:
        if ident not in connus:
            connus[ident] = str(uuid.uuid4())
            nouveaux += 1
        lignes.append("| `%s` | `%s` | %s | %s |"
                      % (ident, connus[ident], nature, libelle[:70]))
    retires = sorted(set(connus) - {i for i, _, _ in liste})
    if retires:
        lignes += ["", "### Identités retirées", "",
                   "*Un objet supprimé conserve son UUID : il n'est jamais réattribué, "
                   "pour qu'une référence ancienne reste résoluble.*", "",
                   "| Identifiant | UUID |", "|---|---|"]
        lignes += ["| `%s` | `%s` |" % (i, connus[i]) for i in retires]
    open(chemin, "w", encoding="utf-8").write(corps(texte).rstrip() + "\n" +
                                              "\n".join(lignes) + "\n")
    return nouveaux


def registre():
    entrees, doublons = {}, []
    for r, d, fs in os.walk(RACINE):
        if ".git" in r or "templates" in r or "jeu-d-essai" in r:
            continue
        for f in sorted(fs):
            if not f.endswith(".md"):
                continue
            chemin = os.path.join(r, f)
            texte = open(chemin, encoding="utf-8").read()
            table = annexe_existante(texte)
            natures = {i: (n, l) for i, n, l in objets(texte)}
            for ident, uid in table.items():
                if uid in entrees:
                    doublons.append((uid, entrees[uid]["identifiant"], ident))
                nature, libelle = natures.get(ident, ("retiré", ""))
                entrees[uid] = {"uuid": uid, "identifiant": ident,
                                "nature": nature, "libelle": libelle,
                                "document": os.path.relpath(chemin, RACINE)}
    chemin = os.path.join(RACINE, "registre.json")
    open(chemin, "w", encoding="utf-8").write(
        json.dumps(sorted(entrees.values(), key=lambda e: e["uuid"]),
                   ensure_ascii=False, indent=2) + "\n")
    print("registre.json : %d identités" % len(entrees))
    for uid, a, b in doublons:
        print("DOUBLON %s partagé par %s et %s" % (uid, a, b))
    return 1 if doublons else 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if args and args[0] == "--registre":
        sys.exit(registre())
    if args and args[0] == "--attribuer":
        cibles = args[1:] or [os.path.join(r, f)
                              for r, d, fs in os.walk(RACINE)
                              if ".git" not in r and "templates" not in r
                              and "jeu-d-essai" not in r
                              for f in fs if f.endswith(".md")]
        total = sum(attribuer(os.path.abspath(c)) for c in cibles)
        print("%d identité(s) attribuée(s)" % total)
        sys.exit(0)
    print(__doc__)
    sys.exit(2)
