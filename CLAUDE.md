# Contexte du dépôt

## Ce qu'est ce dépôt

Une **méthode**, pas un entrepôt de spécifications. Il décrit comment des experts métier
écrivent une spécification fonctionnelle sans coder, et comment elle est transmise aux
développeurs. Les spécifications réelles d'un produit vivent dans le dépôt de ce produit,
à côté du code — jamais ici.

**Erreur à ne pas refaire** : ne pas transformer ce dépôt en catalogue des fonctions d'un
système réel. Tout ce qui est ici est soit de la méthode, soit un modèle, soit un exemple
pédagogique.

## Langue et style

- **La méthode est en français** : CADRE, guides, glossaire, README, gabarits.
- **Les spécifications sont bilingues** : `<nom>.en.md` fait foi, `<nom>.fr.md` est la
  traduction, et `C-42` vérifie qu'elles ne dérivent pas.
- **Le reste est en anglais seul** : pseudo-langage, identifiants, code, jeux de données,
  tests, rapports. Ce sont des artefacts d'exécution, pas de relecture.
- Guillemets français « … », espaces insécables avant `: ; ! ?` non requis en markdown.
- Nombres à la française : `17,2005 kWh`, `0,601702`, `−5 °C` (signe moins U+2212).
- Ton : direct, dense, sans remplissage. Chaque affirmation doit être défendable.
- Les tableaux sont préférés aux listes à puces quand il y a deux dimensions.
- Les encadrés `>` servent aux points saillants, pas à la décoration.

## Conventions d'identifiants

| Préfixe | Objet |
|---|---|
| `FN-xxx` | fonction |
| `SPEC-XXX-nnn` | spécification d'une fonction (niveau 4) |
| `RG-xxx` | règle de gestion |
| `P-xx` | paramètre |
| `E-XXX-nnn` | cas d'erreur métier |
| `INV-xx` | invariant |
| `CT-xx` | cas de test |
| `Q-xx` | question ouverte |
| `D-xxx` | donnée du catalogue |
| `EX-xxx` | exigence de réalisation |
| `N-<version>` | notice de changement |
| `C-xx` / `H-xx` | contrôle mécanique / humain |

Un identifiant n'est **jamais** réutilisé, même après suppression.

## Une règle de direction, absolue

**La spécification ne pointe jamais vers le code.** C'est le code qui cite les `RG-xxx`
qu'il implémente. Toute tentative de réintroduire un chemin de fichier ou un symbole dans
une fiche de fonction ou une spécification est une régression — voir `CHANTIER.md`,
décision 18.

## Forme des identifiants

**`snake_case`, ASCII strict** — ni accent, ni symbole grec, ni majuscule (`C-38`).
Le corps du texte, lui, reste en français accentué : la contrainte ne porte que sur ce qui
est cité, tracé et comparé mécaniquement.

Pas la convention du langage cible : la spécification survit au langage, elle a des
lecteurs qui ne codent pas, et il y a **deux cibles** dans le fil rouge. La correspondance
vers Java, C, Python ou C# est mécanique et déclarée une fois — même principe que
*protobuf*. Voir `CADRE.md` §2.4.

## Trois règles d'écriture qui ne se négocient pas

1. **Les règles emploient les identifiants du contrat, à l'identique** — pas le terme
   français équivalent. C'est ce qui rend `C-01` à `C-03` vérifiables.
2. **Un nom valorisé ne change plus.** Toute transformation crée un nouveau nom qui la
   porte (`nominal_mass` → `rounded_mass` → `dispensed_mass`). Pour une accumulation,
   on indexe : `energie_cumulee(i)`.
3. **On ne code dans le nom que ce qui n'est déclaré nulle part ailleurs** : ni la portée,
   ni le type, ni l'unité — tous trois déjà déclarés et vérifiables. Mais **oui** au stade
   de transformation, que rien d'autre ne distingue.

Voir `CADRE.md` §2.3 et §2.4.

## Après toute modification

```bash
java outils/Identites.java --attribuer   # UUID des nouveaux objets
java outils/Identites.java --registre    # regénère registre.json
java outils/Verifier.java                # doit finir à 0 échec
java    outils/Verifier.java              # seconde implémentation : même verdict exigé
```

## Règle absolue sur les chiffres

**Tout nombre publié dans un exemple doit avoir été calculé et vérifié**, jamais estimé.
Les jeux d'essai des exemples ont tous été recalculés indépendamment (Python, `Decimal`
décimal exact quand une conservation doit être stricte, flottant quand la grandeur est
elle-même incertaine). Si vous modifiez un exemple,
**recalculez** — un chiffre faux dans un dépôt qui prêche la rigueur le disqualifie.

Valeurs de référence du fil rouge (véhicule 1 800 kg, Cx·S 0,64 m², Crr 0,010,
η 0,90 / 0,60, auxiliaires 500 W, batterie 60 kWh) :

| Grandeur | Valeur |
|---|---|
| Trajet de référence (125 km, 4 segments) | 20,5060 kWh — 16,405 kWh/100 km |
| Consommation à plat 90 / 110 / 130 km/h | 13,567 / 17,200 / 21,612 kWh/100 km |
| Vitesse de consommation minimale | 29,92 km/h |
| Point d'autonomie à 20 °C / −5 °C (SoC 40 %, réserve 5 kWh) | 106,017 km / 82,555 km |
| Autonomie batterie pleine, 110 km/h 20 °C vs 130 km/h −5 °C | 320 km vs 199 km |
| Consommation en montée à 5 %, 110 km/h | 44,410 kWh/100 km |

## Structure

```
README.md          point d'entrée : ce qu'est le dépôt, les trois temps
OUTILLAGE.md       la chaîne d'outils, nommée : marché, à écrire, refusé
CADRE.md           le document de référence, long, à lire une fois
GLOSSAIRE.md       le vocabulaire de la méthode elle-même
REFERENCES.md      les sources ; ce qu'on emprunte et ce qu'on écarte
FAQ.md             les objections fréquentes
guides/            0-RACONTER · 1-DECOUPER · 2-GLOSSAIRE · 4-ECRIRE-A-PLUSIEURS
                   · 6-PASSER-AU-DEVELOPPEMENT
templates/         MODELE-SPECIFICATION · MODELE-FICHE-FONCTION · MODELE-GLOSSAIRE · CHECKLIST-RELECTURE
exemples/
  fil-rouge/       le scénario déroulé : 0-LE-CAS-METIER → 1-DECOUPAGE → 2-GLOSSAIRE
                   → 4-FN-004 (niveau 3) → 5-SPEC-NRG-001 (niveau 4)
  SPEC-MAS-001…    vignette discrète et exacte (bilan de masse d'un lot)
  SPEC-THM-001…    vignette scientifique courte (refroidissement d'une boisson)
```

## Vérifier avant de pousser

**Toujours** lancer le vérificateur. Code de retour 1 s'il reste un échec :

```bash
java outils/Verifier.java
```

Il met en œuvre les règles `C-xx` de `outils/REGLES-DE-CONTROLE.md`. Un **ÉCHEC** est un
défaut certain et bloque ; un **AVERTIR** se tranche à la main et ne se fait jamais taire
en modifiant le script.

Le contrôle des liens relatifs cassés est inclus dans le vérificateur :
aucun outil séparé à lancer.

## Git

Branche de travail : `main`. Commits en français, sujet à l'impératif, corps expliquant
**pourquoi** et non seulement quoi.
