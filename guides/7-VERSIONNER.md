# Guide 7 — Versionner

*Transverse. Gouverne la vie d'une spécification après sa première validation.*

---

## Les trois questions auxquelles seul le versionnement répond

| Question | Qui la pose | Sans versionnement |
|---|---|---|
| **Quelle règle s'appliquait le 14 mars ?** | un auditeur, un client en litige | on ne sait pas |
| **Quelle version a produit ce chiffre ?** | la comptabilité, une analyse d'incident | on ne sait pas |
| **Ce changement casse-t-il quelque chose ?** | l'équipe qui s'apprête à modifier | on découvre en production |

## Ce qui se versionne — et ce n'est pas que la spécification

> **Une spécification seule ne suffit pas à rejouer un résultat.**

Il faut le **quadruplet** :

| Artefact | Pourquoi il est indispensable |
|---|---|
| **La spécification** | les règles |
| **Le glossaire** | le sens des termes. Changer le sens d'un mot change toutes les règles qui l'emploient **sans qu'aucune ne bouge** — c'est le changement le plus invisible qui soit |
| **Le jeu de paramètres** | les valeurs. Un seuil passé de 3 à 4 change les résultats sans toucher une règle |
| **Les contrats des fonctions dont elle dépend** | si `FN-002` change son contrat, `FN-001` change de comportement sans avoir été modifiée |

Un résultat n'est rejouable que s'il **enregistre les quatre versions**. C'est la raison
pour laquelle chaque spécification nomme, dans son en-tête, le glossaire et le jeu de
paramètres auxquels elle se réfère — avec leur version.

## L'incrément, en termes métier

| Incrément | Quand | Conséquence |
|---|---|---|
| **Majeur** — `2.0.0` | **Un résultat change** pour au moins un cas | Nouvelle date d'effet, information des consommateurs, décision de rejeu, **validation rejouée** |
| **Mineur** — `1.3.0` | Un cas nouveau est couvert, **sans changer** les résultats existants | Nouveaux cas de test, pas de rejeu |
| **Correctif** — `1.2.4` | Clarification de rédaction, **aucun** impact sur les résultats | Rien |

### Le test décisif, et il est mécanique

> **On rejoue le jeu d'essai de la version précédente sur la nouvelle version.
> Si un seul cas change de résultat attendu : c'est un majeur.**

Ce n'est pas une appréciation, c'est une exécution. Le jeu d'essai de la version N est
conservé ; il est rejoué contre la version N+1 ; le verdict tombe.

> « Ce n'est qu'une clarification » est la phrase à surveiller en revue. Neuf fois sur
> dix elle est vraie ; la dixième fois elle cache un changement de comportement — et le
> test ci-dessus la démasque en trente secondes.

Pour le **glossaire**, la même logique donne :

| Changement | Incrément |
|---|---|
| Ajouter un terme | mineur |
| Préciser une définition sans changer le sens | correctif |
| **Changer le sens** d'un terme | **majeur** |
| **Renommer** un terme | **majeur** — l'ancien nom devient un synonyme déprécié, daté |
| **Retirer** un terme | **majeur** — toute règle qui l'employait doit être reprise |

## La notice de changement

Une ligne d'historique dit *qu'il* y a eu un changement. Elle ne dit ni **pourquoi**, ni
**qui est touché**. Dès qu'un changement a un impact — sur un résultat ou sur un contrat —
il porte une **notice**, `N-<version>`, qui répond à trois questions et pas une de plus.

### 1. La raison

Deux phrases : ce qui n'allait pas, et ce que le changement corrige. Pas la solution
retenue — le problème. Si la raison est « on nous l'a demandé », la notice doit dire qui
et pourquoi, sinon personne ne saura, dans trois ans, s'il est encore légitime de la
maintenir.

### 2. Les fonctions impactées

| Fonction | Nature de l'impact |
|---|---|
| `FN-001` | **comportement** — un résultat change, le contrat ne bouge pas |
| `FN-004` | **contrat** — voir le détail ci-dessous |
| `FN-011` | **aucun** — réexaminée, sans impact *(la ligne reste : elle évite de refaire l'analyse)* |

Une fonction réexaminée sans impact **figure quand même**. C'est cette ligne qui, dans
trois ans, évitera de recommencer le travail d'analyse.

### 3. Les impacts sur les contrats

Le cœur de la notice, et la raison pour laquelle elle existe.

| Fonction | Élément | Nature | Détail | Compatibilité |
|---|---|---|---|---|
| `FN-004` | `etat_de_charge_arrivee_minimal` | **ajout** | entrée, `Fraction(0,000..1,000)`, obligatoire | **rupture** |
| `FN-001` | `point_autonomie` | **modification** | sens de l'arrondi : au plus proche → vers le bas | compatible |
| `FN-002` | `facteur_temperature_brut` | **suppression** | sortie retirée | **rupture** |

Trois natures, et trois seulement : **ajout**, **modification**, **suppression** — d'une
entrée ou d'une sortie.

**Ce qui est une rupture, et ce qui ne l'est pas :**

| Changement de contrat | Effet sur les appelants |
|---|---|
| Ajout d'une entrée **facultative** | compatible |
| Ajout d'une entrée **obligatoire** | **rupture** — tous les appelants doivent changer |
| Ajout d'une sortie | compatible |
| Suppression d'une sortie | **rupture** |
| Suppression d'une entrée | compatible, mais les appelants la fournissent pour rien |
| Modification d'un type, d'une **unité**, d'une précision | **rupture** |
| Restriction d'un domaine d'entrée | **rupture** |
| Élargissement d'un domaine d'entrée | compatible |
| **Changement de sens** d'un nom, à type inchangé | **rupture silencieuse** — la pire de toutes : rien ne casse, et tout devient faux |
| **Renommage** d'un champ, à sens et type inchangés | **compatible**, à condition que les références passent par l'identité durable ([CADRE §2.8](../CADRE.md)). Sans UUID, un renommage est indistinguable d'une suppression suivie d'un ajout : **deux ruptures pour un changement d'étiquette** |

### Deux axes indépendants, qu'on confond toujours

|  | **Le contrat change** | **Le contrat ne bouge pas** |
|---|---|---|
| **Un résultat change** | majeur **et** rupture — le cas le plus lourd | **majeur** |
| **Aucun résultat ne change** | **rupture** (ou compatible, si simple ajout facultatif) | mineur ou correctif |

Le numéro de version suit la règle du **résultat**. La notice porte, en plus, le verdict
de **compatibilité** — parce qu'un ajout de sortie ne change aucun résultat et oblige
pourtant les consommateurs à se mettre à jour pour en profiter.

### La notice se prépare mécaniquement

Le registre des identités étant versionné, sa comparaison entre deux versions donne
directement la matière de la notice :

```bash
java outils/Identites.java --registre && git diff registre.json
```

| Ce que montre la comparaison | Ce qu'on en déduit |
|---|---|
| Une entrée **nouvelle** | un ajout |
| Une entrée dont le **libellé ou l'identifiant** change, UUID identique | un **renommage** — pas une suppression |
| Une entrée dont le **document** change, UUID identique | un **déplacement** |
| Une entrée passée en « retiré » | une suppression |

C'est le seul moyen fiable de distinguer un renommage d'une suppression suivie d'un ajout.
De mémoire, six mois plus tard, personne n'y arrive.

### 4. Les conséquences

Trois lignes, toujours les mêmes : **rejeu** nécessaire ou non, **date d'effet**,
**consommateurs à prévenir**.

> **Le contrôle `C-24`** vérifie mécaniquement que toute ligne d'historique déclarant un
> impact renvoie à une notice existante. Un changement sans notice ne passe pas.

## Version et date d'effet sont deux axes indépendants

C'est la confusion la plus fréquente, et elle produit des rejeux faux.

| | Ce que ça date |
|---|---|
| **La version** | quand le **document** a changé |
| **La date d'effet** | à partir de quand la **règle** s'applique |

Une `2.0.0` publiée le 12 mars avec une date d'effet au 1ᵉʳ juin : pendant onze semaines,
la `1.x` fait foi pour les calculs, et la `2.0.0` existe, validée, en attente.

**Deux stratégies, et laquelle choisir :**

| Stratégie | Comment | Quand |
|---|---|---|
| **Une version par période d'effet** ⭐ | la `2.0.0` remplace la `1.x` à sa date d'effet ; on retrouve l'ancienne par son étiquette | par défaut — les changements de règles sont rares |
| **Des règles datées dans un même document** | chaque `RG-xxx` porte sa période de validité | uniquement pour les **paramètres**, qui changent souvent |

Mélanger les deux dans un même document le rend illisible en deux ans.

## Geler une version

| Mécanisme | Rôle |
|---|---|
| Le champ **Version** de l'en-tête | la source déclarative — c'est lui qui fait foi |
| Une **étiquette Git** `SPEC-NRG-001/v2.0.1` | le gel technique : elle rend le document retrouvable à l'octet près |
| Un **contrôle automatique** | vérifie que l'en-tête, la dernière ligne de l'historique et l'étiquette coïncident (`C-19`) |

**Le numéro de version ne va jamais dans le nom du fichier.** `SPEC-NRG-001-v2.md`
crée un second document qui divergera. Le fichier est unique ; c'est Git qui porte
l'histoire.

## Le code déclare la version qu'il implémente

Deux obligations, et la seconde est celle qu'on oublie :

1. Le code **déclare** la version de spécification qu'il implémente — une constante, un
   manifeste, peu importe la forme.
2. **Tout résultat persisté embarque les versions** qui l'ont produit : spécification,
   glossaire, jeu de paramètres, date qui fait foi ([guide 3](3-DONNEES.md)).

Sans le second point, l'audit est impossible et le rejeu est faux : le programme relira
sagement les paramètres **courants** et produira un chiffre différent de l'original.

## Les dépendances entre spécifications

Une spécification déclare les fonctions dont elle dépend **et la version de leur contrat**.

**Règle de propagation :** un majeur chez une dépendance oblige à **réexaminer**, pas
forcément à changer. Le réexamen est tracé — même s'il conclut « aucun impact », il doit
laisser une ligne dans l'historique. C'est cette ligne qui, dans trois ans, évitera de
refaire l'analyse.

## Ce qu'on ne supprime jamais

| | Pourquoi |
|---|---|
| Une **règle abrogée** | reste, barrée et datée : un rejeu antérieur s'y réfère |
| Un **identifiant** | jamais réutilisé, même après suppression |
| Une **question fermée** | reste avec sa décision et sa date : c'est ce qui évite de rouvrir le débat |
| Un **terme retiré** du glossaire | devient un synonyme déprécié, daté |

Conséquence assumée : **le document ne rétrécit jamais.** C'est acceptable parce qu'on lit
la version courante et que l'historique ne sert qu'à l'audit — à condition que les
éléments périmés soient visuellement marqués, jamais mélangés aux vivants.

## Anti-patterns

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **La version qui ne bouge jamais** | `1.0.0` depuis deux ans sur un domaine actif | le document a cessé d'être tenu, donc d'être vrai |
| **« Ce n'est qu'une clarification »** | un correctif qui change un résultat | les consommateurs ne sont pas prévenus, le rejeu n'est pas fait |
| **Le document versionné, les paramètres non** | l'historique ne mentionne jamais de changement de valeur | on ne peut pas rejouer, alors qu'on croit pouvoir |
| **La date d'effet absente** | seule la date de publication figure | on ne sait pas quelle règle s'appliquait quand |
| **Le rejeu qui relit le courant** | aucune capture des paramètres à la date de l'événement | les chiffres de l'an dernier ne se retrouvent pas — **le défaut le plus fréquent** |
| **La version dans le nom du fichier** | `SPEC-…-v2.md` | deux documents, qui divergeront |
| **Le glossaire non versionné** | il n'a pas d'en-tête de version | le changement le plus invisible devient intraçable |

---

## Voir sur le fil rouge

L'historique de [`SPEC-NRG-001`](../exemples/fil-rouge/5-SPEC-NRG-001.en.md)
montre les trois incréments dans l'ordre :

- `1.0.0` → **`2.0.0`** : l'arrondi du point d'autonomie passe vers le bas. Le résultat
  change, fût-ce d'un mètre → **majeur**, sans discussion.
- `2.0.0` → **`2.0.1`** : deux règles reformulées après le retrait d'un terme au
  glossaire. Aucun résultat ne change → **correctif**.

Et le [glossaire du fil rouge](../exemples/fil-rouge/2-GLOSSAIRE.md) est passé en
**`2.0.0`** parce qu'un terme en a été **retiré** — ce qui a forcé la reprise de deux
règles de la spécification. Un glossaire qui passe en majeur, c'est une spécification qui
bouge.

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `FN-001` | `e4d8a75d-7fd9-4d69-af3f-606974ae99be` | fonction | comportement |
| `FN-004` | `6b1bee5a-76dd-47f0-8a3a-4517eb13bd91` | fonction | contrat |
| `FN-011` | `0e341e9d-e746-4d42-b831-5e31f3f9a6fb` | fonction | aucun |
| `FN-002` | `5a2012a7-8dc7-40df-8f81-b8ba67b1884a` | fonction | `facteur_temperature_brut` |
