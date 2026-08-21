# FN-000 — <Verbe à l'infinitif + complément>

| | |
|---|---|
| **Identifiant** | FN-000 |
| **Niveau de maturité** | 0 / 1 / 2 / 3 / 4 |
| **Propriétaire métier** | |
| **Répondant technique** | |
| **Dernière modification** | AAAA-MM-JJ |

<!--
MODE D'EMPLOI — voir guides/1-DECOUPER.md

Le niveau de maturité dit jusqu'où cette fiche est remplie. C'est un état ASSUMÉ,
pas un travail inachevé :
  0  la fonction existe et porte un nom          → §1 seulement
  1  + description et propriétaire               → §1
  2  + ancrage dans le code                      → §1, §5
  3  + contrat typé                              → §1, §2, §3, §5, §6
  4  + spécification complète                    → cette fiche + un SPEC-xxx

Nommage : verbe à l'infinitif + complément, dans la langue du domaine.
Verbes interdits : gérer, traiter, handle, process, administrer, prendre en charge.
Si vous ne pouvez pas remplacer « gérer » par un verbe précis, c'est un paquet
de plusieurs fonctions : rouvrez-le.
-->

## 1. Description

<!-- Une phrase : ce que la fonction produit, à partir de quoi.
     Test : « peut-on nommer ce que ça produit, sans dire comment ? » -->

## 2. Contrat
<!-- Niveau 3 et au-delà. Même notation que les §4 et §5 du modèle de spécification :
     unité, précision, domaine. Un contrat sans unité ne vaut rien. -->

```
entrees :
    <champ> : <Type>(<unité>, <précision>, <domaine>)

sorties :
    <champ> : <Type>(<unité>, <précision>, <domaine>)
```

## 3. Objectif et contraintes
<!-- Niveau 3. Ce que la fonction doit satisfaire, SANS dire comment y parvenir.
     Pour une optimisation : l'objectif, les contraintes, et les règles de départage
     qui rendent le résultat unique. -->

**Objectif** —

**Contraintes** —

| # | Contrainte |
|---|---|
| C-1 | |

**Départage** — à résultat équivalent, on retient dans cet ordre : …

## 4. Ce qui reste à trancher avant le niveau supérieur

| Id | Question | Décideur |
|---|---|---|
| `Q-000-1` | | |

## 5. Ancrage dans le code existant
<!-- Niveau 2 et au-delà. Chemin + symbole, vérifiable automatiquement.
     Plusieurs ancrages pour une même fonction est un signal précieux :
     deux implémentations de la même règle à deux endroits. -->

| Emplacement | Ce qu'on y trouve |
|---|---|
| `chemin/vers/fichier.ext#symbole` | |

## 6. Dépendances

| Consomme | Est consommée par |
|---|---|
| | |

## 7. Spécification
<!-- Niveau 4 uniquement : lien vers le SPEC-xxx correspondant. -->

*(aucune — cette fonction est au niveau <n>)*
