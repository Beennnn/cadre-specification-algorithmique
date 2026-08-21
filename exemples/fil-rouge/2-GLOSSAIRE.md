# Fil rouge — Glossaire du domaine

| | |
|---|---|
| **Version** | 1.1.0 |
| **Valideur métier** | R&D Énergie |
| **Dernière modification** | 2026-02-17 |

*Produit lors de l'atelier de glossaire du 2026-02-10, révisé le 2026-02-17.
Méthode : [Guide 2 — Le glossaire](../../guides/2-GLOSSAIRE.md)*

---

## Termes

| Terme | Définition | Contexte | Source |
|---|---|---|---|
| **Trajet** | Suite ordonnée de segments reliant un point de départ à une destination, fournie en entrée. Le trajet ne change pas pendant le calcul. | | |
| **Segment** | Portion de trajet sur laquelle la vitesse praticable et la pente sont considérées constantes. Longueur typique : 0,5 à 10 km. | | |
| **Pente** | Rapport entre le dénivelé et la distance horizontale d'un segment, exprimé en pourcentage signé : positif en montée, négatif en descente. | | |
| **Vitesse praticable** | Vitesse à laquelle le véhicule est supposé parcourir un segment, en km/h. Elle tient compte de la limitation réglementaire et des conditions de circulation. À ne pas confondre avec la vitesse maximale autorisée. | | |
| **Énergie mécanique** | Énergie qu'il faut fournir aux roues pour parcourir un segment, en joules. Elle est **négative en descente prononcée**, lorsque la gravité fournit plus que ce que les résistances consomment. | | |
| **Récupération** | Conversion en électricité d'une partie de l'énergie mécanique excédentaire en descente ou au freinage. | | |
| **Rendement de traction** | Fraction de l'énergie prélevée sur la batterie qui parvient effectivement aux roues. Sans unité, entre 0 et 1. | | |
| **Auxiliaires** | Ensemble des consommateurs électriques indépendants du déplacement : chauffage, climatisation, éclairage, électronique de bord. Exprimé en watts. | | |
| **État de charge** | Fraction de la capacité nominale actuellement stockée dans la batterie, entre 0 et 1. Abrégé *SoC* dans la littérature ; **on écrit « état de charge » en toutes lettres dans les spécifications**. | | |
| **Capacité nominale** | Énergie que la batterie peut stocker à l'état neuf, à 25 °C, en kWh. C'est une caractéristique du véhicule, pas une mesure. | | Fiche technique constructeur |
| **Énergie disponible** | Énergie réellement mobilisable à l'instant du calcul, en kWh. Elle vaut la capacité nominale multipliée par l'état de charge et par le facteur de température. **Ce n'est pas la capacité nominale**, et l'écart peut atteindre 30 % par grand froid. | | |
| **Facteur de température** | Coefficient sans unité, entre 0 et 1, traduisant la fraction de l'énergie stockée effectivement mobilisable à une température donnée. | | Essais R&D Batterie, campagne 2025 |
| **Réserve de sécurité** | Énergie que le calcul s'interdit de consommer, en kWh, afin que le conducteur ne se retrouve jamais à zéro. C'est un **arbitrage d'expérience client**, pas une contrainte physique. | | |
| **Point d'autonomie** | Distance depuis le départ, en km, à laquelle l'énergie disponible diminuée de la réserve de sécurité est épuisée. **Ce n'est pas le point de panne** : le véhicule roule encore, sur la réserve. | | |
| **Borne de recharge** | Point de charge public identifié, caractérisé par sa position sur le trajet, sa puissance maximale et son standard de connecteur. | | |
| **Puissance de charge** | Puissance instantanée acceptée par la batterie pendant une recharge, en kW. Elle **décroît quand l'état de charge augmente** : c'est une propriété de la batterie, pas de la borne. | | |
| **Palier de charge** | Intervalle d'état de charge sur lequel la puissance de charge est considérée constante. | | |
| **Plan de recharge** | Suite ordonnée d'arrêts proposés au conducteur, chacun décrivant une borne, un état de charge visé et une durée. | | |
| **Autonomie affichée** | Distance restante présentée au conducteur au tableau de bord, en km entiers. Elle dérive du point d'autonomie mais **n'a pas la même valeur** : elle est arrondie vers le bas et bornée (`FN-011`). | | |

## Homonymes assumés

| Terme | Contexte | Définition | Correspondance à la frontière |
|---|---|---|---|
| **Autonomie** | Commercial / réglementaire | Distance homologuée sur un cycle normalisé, dans des conditions de référence. C'est une valeur de catalogue, identique pour tous les exemplaires d'un modèle. | Aucune correspondance directe : les deux valeurs n'ont ni la même méthode, ni le même objet. **Ne jamais comparer l'une à l'autre dans un message destiné au conducteur.** |
| **Autonomie** | Embarqué / ce document | Distance que *ce* véhicule, dans *son* état et sur *ce* trajet, peut parcourir avant d'entamer sa réserve. | |
| **Charge** | Énergie | Quantité d'électricité stockée, ou l'action de la reconstituer. | Aucune : ce sont deux notions sans rapport. Le contexte lève l'ambiguïté à l'oral, **pas à l'écrit** — d'où la règle ci-dessous. |
| **Charge** | Mécanique | Masse transportée par le véhicule, passagers et bagages compris, en kg. | |

> **Décision de rédaction, prise en atelier :** dans les spécifications, on n'écrit jamais
> « charge » seul. On écrit **« état de charge »**, **« recharge »** ou **« masse
> transportée »**. L'homonymie est réelle et légitime dans les deux métiers ; on ne la
> supprime pas, on l'évite à l'écrit.

## Synonymes dépréciés

| Ancien terme | Remplacé par | Depuis le |
|---|---|---|
| Consommation moyenne | *(supprimé — voir ci-dessous)* | 2026-02-17 |
| SoC | État de charge | 2026-02-10 |

## Termes explicitement écartés

| Terme écarté | Pourquoi | Employer à la place |
|---|---|---|
| **Consommation moyenne** | Trois personnes lui donnaient trois sens : par 100 km sur le trajet, depuis le dernier plein, ou depuis la mise en service. Aucun n'était faux, et c'est bien le problème | Nommer explicitement l'assiette : « consommation du trajet », « consommation sur les 100 derniers kilomètres » |
| **Range** | Anglicisme qui recouvre les deux sens d'« autonomie », donc réintroduit l'homonymie qu'on vient de traiter | Autonomie *(en précisant le contexte)* |
| **Batterie pleine** | Ne dit pas si l'on parle de 100 % ou de 80 % — or les deux existent selon le mode de charge | « État de charge de 100 % » ou « état de charge visé » |

## Historique

| Version | Date | Changement | Impact |
|---|---|---|---|
| 1.0.0 | 2026-02-10 | Version initiale, 17 termes | — |
| 1.1.0 | 2026-02-17 | Ajout de *palier de charge* et *autonomie affichée* ; **suppression de *consommation moyenne*** ; homonyme *charge* documenté | Ajouts : aucun impact. La suppression de *consommation moyenne* a nécessité la reprise de deux règles de `SPEC-NRG-001`, qui l'employaient sans préciser l'assiette |

> **Ce que la ligne 1.1.0 raconte.** Un terme retiré du glossaire a obligé à reprendre
> deux règles déjà écrites. C'est exactement l'effet qu'on cherche : le glossaire n'est
> pas un lexique décoratif, c'est le socle sur lequel les règles s'appuient. Quand il
> bouge, elles bougent — et on le voit.
