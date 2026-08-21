# D-000 — <Nom de la donnée, tel qu'au glossaire>

| | |
|---|---|
| **Identifiant** | D-000 |
| **Nature** | mesurée / saisie / référentielle / dérivée |
| **Source de vérité** | *(une seule — système, capteur, fonction ou personne)* |
| **Garant** | |
| **Dernière modification** | AAAA-MM-JJ |

<!--
MODE D'EMPLOI — voir guides/3-DONNEES.md

Une fiche par donnée qui traverse plus d'une fonction.
Le nom est celui du glossaire, à l'identique — jamais une variante.
La source de vérité est UNIQUE. Deux sources = un conflit qui se règlera au hasard.
-->

## Type

```
<nom> : <Type>(<unité>, <précision>, <domaine>)
```

## Date qui fait foi
<!-- Laquelle des trois arrête la valeur ? La date de l'ÉVÉNEMENT (quand la chose
     s'est produite), la date d'OBSERVATION (quand on l'a mesurée ou saisie), ou la
     date de CALCUL (quand on s'en sert) ? Un rejeu correct utilise la valeur telle
     qu'elle était à la date retenue ici. -->

## Fraîcheur tolérée
<!-- Au-delà de quel âge la valeur n'est plus utilisable, et que fait-on alors ? -->

## Si absente ou invalide
<!-- Décision MÉTIER : rejet, valeur de repli, dégradation — et DANS QUEL SENS.
     Sans cette ligne, le développeur choisira le repli le plus commode,
     rarement le plus prudent. -->

## Consommée par

| Fonction | Ce qu'elle en fait |
|---|---|
| `FN-000` | |

## Si elle change

| Changement | Conséquence |
|---|---|
| Son unité change | |
| Elle devient indisponible | |
| Sa source change | |
