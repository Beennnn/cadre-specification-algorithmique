# FN-000 — <Verbe à l'infinitif + complément>

| | |
|---|---|
| **Identifiant** | FN-000 |
| **Niveau de maturité** | 0 / 1 / 2 / 3 |
| **Valideur métier** | |
| **Co-auteur technique** | |
| **Dernière modification** | AAAA-MM-JJ |

<!--
MODE D'EMPLOI — voir guides/1-DECOUPER.md

Le niveau de maturité dit jusqu'où cette fiche est remplie. C'est un état ASSUMÉ,
pas un travail inachevé :
  0  la fonction existe et porte un nom          → §1 seulement
  1  + rôle et valideur métier                   → §1
  2  + contrat typé                              → §1, §2, §3
  3  + algorithme complet                        → cette fiche + un SPEC-xxx

Cette fiche ne pointe JAMAIS vers le code : c'est le code qui cite les RG-xxx
qu'il implémente, jamais l'inverse. Voir guides/1-DECOUPER.md.

Nommage : verbe à l'infinitif + complément, dans la langue du domaine.
Verbes interdits : gérer, traiter, handle, process, administrer, prendre en charge.
Si vous ne pouvez pas remplacer « gérer » par un verbe précis, c'est un paquet
de plusieurs fonctions : rouvrez-le.
-->

## 1. Rôle

<!-- Deux phrases : à quoi elle sert, et CE QU'ELLE PRODUIT. Jamais « comment ».
     Test : « peut-on nommer ce que ça produit, sans dire comment ? »
     Le rôle doit se lire SANS le contrat, et le contrat SANS l'algorithme.
     Une fonction dont le rôle ne s'explique qu'en déroulant ses règles est
     mal découpée — voir CADRE.md §3.0. -->

**À quoi elle sert** —

**Ce qu'elle produit** —

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

## 5. Dépendances

| Consomme | Est consommée par |
|---|---|
| | |

## 6. Algorithme

<!-- Niveau 4 uniquement. Les règles vivent dans le SPEC-xxx correspondant :
     cette section n'en porte que le lien. Le triptyque est alors complet —
     rôle (§1), contrat (§2), algorithme (§7). -->

## 7. Spécification
<!-- Niveau 4 uniquement : lien vers le SPEC-xxx correspondant. -->

*(aucune — cette fonction est au niveau <n>)*
