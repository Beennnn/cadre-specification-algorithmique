# FN-004 — Planifier les arrêts de recharge

| | |
|---|---|
| **Identifiant** | FN-004 |
| **Niveau de maturité** | **2 — contrat typé** *(voir [Guide 1 §4](../../guides/1-DECOUPER.md))* |
| **Propriétaire métier** | Expérience client |
| **Répondant technique** | Architecte embarqué |
| **Dernière modification** | 2026-02-24 |

> **Cette fiche s'arrête volontairement au niveau 2.** Les entrées, les sorties et
> l'objectif sont fixés ; les règles détaillées et le jeu d'essai ne sont pas écrits.
> **Ce n'est pas un travail inachevé, c'est un état assumé** : on sait ce que la fonction
> produit et on peut déjà s'en servir pour découper les responsabilités, estimer, et
> câbler les autres fonctions autour d'elle.

---

## 1. Rôle

**À quoi elle sert** — répondre à la question du conducteur *« où dois-je m'arrêter, et
combien de temps ? »*, une fois qu'il sait qu'il n'arrivera pas d'une traite.

**Ce qu'elle produit** — une suite ordonnée d'arrêts, chacun désignant une borne, un état
de charge visé et une durée ; et la durée totale du trajet qui en résulte.

*(Rôle et contrat sont renseignés ; l'algorithme ne l'est pas — c'est ce que signifie le
niveau 2. Voir [CADRE.md §3.0](../../CADRE.md).)*

## 2. Contrat

```
entrees :
    trajet              : liste ordonnée de Segment          — voir SPEC-NRG-001 §4
    etat_initial :
        etat_de_charge  : Fraction(0,000 .. 1,000, 3 décimales)
        temperature     : Température(°C, 1 décimale)
    bornes_candidates   : liste de Borne                      — produite par FN-006
    reserve_securite    : Énergie(kWh, 2 décimales, ≥ 0)      — produite par FN-010
    etat_de_charge_arrivee_minimal : Fraction(0,000 .. 1,000, 3 décimales)

Borne :
    identifiant         : Identifiant(texte, 6 à 24 caractères)
    position            : Distance(km depuis le départ, 3 décimales)
    puissance_maximale  : Puissance(kW, 1 décimale, > 0)
    standard            : Énuméré { CCS, CHADEMO, TYPE2 }

sorties :
    plan                : liste ordonnée d'Arret              — vide si aucun arrêt n'est nécessaire
    duree_totale        : Durée(min, 1 décimale)              — roulage + recharges
    faisable            : Booléen                             — faux si aucun plan ne satisfait les contraintes

Arret :
    borne                    : Identifiant
    etat_de_charge_arrivee   : Fraction(0,000 .. 1,000, 3 décimales)
    etat_de_charge_vise      : Fraction(0,000 .. 1,000, 3 décimales)
    duree_recharge           : Durée(min, 1 décimale)         — produite par FN-005
```

## 3. Objectif et contraintes

> **Ce que la spécification imposera : le résultat. Ce qu'elle n'imposera pas : la
> méthode.** Glouton, programmation dynamique, recherche heuristique — le choix
> appartiendra au développement, dès lors que le plan produit satisfait l'objectif et les
> contraintes ci-dessous, et que les règles de départage rendent le résultat unique.

**Objectif** — minimiser la durée totale : temps de roulage + temps de recharge.

**Contraintes** — un plan n'est recevable que si :

| # | Contrainte |
|---|---|
| C-1 | L'énergie disponible ne descend jamais sous la réserve de sécurité, en aucun point du trajet |
| C-2 | L'état de charge à l'arrivée est supérieur ou égal à `etat_de_charge_arrivee_minimal` |
| C-3 | Chaque borne retenue est atteignable depuis l'arrêt précédent en respectant C-1 |
| C-4 | Le standard de connecteur de chaque borne est compatible avec le véhicule |

**Départage** — à durée totale égale, le plan retenu sera, dans cet ordre :
le moins d'arrêts, puis l'arrêt le plus tardif possible, puis l'identifiant de borne le
plus petit dans l'ordre lexicographique.

## 4. Ce qui reste à trancher avant le niveau 3

| Id | Question | Décideur |
|---|---|---|
| `Q-04-1` | La puissance de charge décroît par paliers avec l'état de charge. Il en résulte que **deux arrêts courts sont souvent plus rapides qu'un seul arrêt long** — un résultat contre-intuitif pour le conducteur. Faut-il l'appliquer sans le dire, l'expliquer, ou plafonner le nombre d'arrêts pour respecter l'intuition ? | Expérience client |
| `Q-04-2` | Que fait-on lorsqu'**aucun plan recevable n'existe** ? On renvoie `faisable = faux` et rien d'autre, ou le meilleur plan infaisable avec le point de rupture ? | Expérience client |
| `Q-04-3` | La puissance réellement délivrée par une borne peut être inférieure à sa puissance affichée. Retient-on l'affichée, une valeur historique, ou un abattement forfaitaire ? | Partenariats réseau |
| `Q-04-4` | Un temps de trajet strictement minimal peut proposer un arrêt de 4 minutes. Existe-t-il une durée d'arrêt minimale acceptable pour un humain ? | Expérience client |

> `Q-04-1` est le genre de question qui justifie à elle seule la démarche : c'est un
> arbitrage entre **optimalité et acceptabilité**, il a des conséquences commerciales, et
> personne ne l'avait jamais posé. Dans le système actuel, la réponse existe — elle est
> enfouie dans une boucle, et elle a été choisie par un développeur en 2023.

## 5. Dépendances

| Consomme | Est consommée par |
|---|---|
| `FN-001` — estimer l'autonomie sur un trajet | `FN-012` — décider de la stabilité d'un plan recalculé |
| `FN-005` — estimer la durée d'une recharge | |
| `FN-006` — sélectionner les bornes candidates | |
| `FN-010` — déterminer la réserve de sécurité | |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `FN-004` | `f70a8d6f-84c8-406c-b40d-d662df8bb5f6` | document | FN-004 — Planifier les arrêts de recharge |
| `Q-04-1` | `e95e0c6c-6009-4388-a415-d41c76dac652` | question | La puissance de charge décroît par paliers avec l'état de charge. Il e |
| `Q-04-2` | `05ea43e3-893a-4bda-9086-8739be1e83d6` | question | Que fait-on lorsqu'**aucun plan recevable n'existe** ? On renvoie `fai |
| `Q-04-3` | `730f2923-d586-400b-9248-83ac8d297acf` | question | La puissance réellement délivrée par une borne peut être inférieure à  |
| `Q-04-4` | `19326885-4fa0-498d-923f-d1b44998f662` | question | Un temps de trajet strictement minimal peut proposer un arrêt de 4 min |
