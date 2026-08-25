# Guide 4 — Écrire à plusieurs

*À utiliser une fois le découpage et le glossaire faits. Sortie attendue : des
spécifications écrites, relues et acceptées, par lots de 5 à 8 fonctions.*

---

## Le problème

Une spécification écrite seule, dans un coin, est **relue par politesse**. Les
relecteurs approuvent en huit minutes, personne n'ouvre de question, et les vraies
ambiguïtés sortent trois mois plus tard, en développement.

Une spécification écrite à plusieurs est **vraie**, parce qu'elle a dû survivre à
plusieurs lectures adverses avant d'exister.

Tout ce guide vise un seul objectif : **rendre les désaccords visibles tôt et les traiter
explicitement**, au lieu de les laisser se résoudre en silence dans le code.

## La structure qui rend le travail parallèle possible

> **Une fonction = un fichier = un valideur métier.**

C'est la seule règle d'organisation qui compte, et elle a trois conséquences immédiates :

| Conséquence | Pourquoi ça change tout |
|---|---|
| Deux personnes peuvent écrire en même temps sans se gêner | Le document unique de 80 pages **sérialise** le travail : un seul auteur à la fois, et une file d'attente |
| Chaque relecture est courte | On relit sérieusement 8 pages ; on survole 80 |
| La responsabilité est traçable | « Qui a écrit cette règle, et qui l'a validée ? » a une réponse, ligne par ligne, dans l'historique |

**Le glossaire est la seule exception** : c'est un bien commun, donc le seul point de
contention structurel. Il se traite **en premier** ([guide 2](2-GLOSSAIRE.md)) et se
verrouille avant que l'écriture parallèle ne commence. Tout ajout ultérieur est traité
comme un changement structurant, pas comme une correction de détail.

## Les quatre moments, et qui est présent

| # | Moment | Durée | Qui | Sortie |
|---|---|---|---|---|
| 1 | **Atelier de découpage** | 2 h | métier + développeurs + animateur | les fonctions nommées, niveau 1 ([guide 1](1-DECOUPER.md)) |
| 2 | **Atelier de glossaire** | 1 h, + 1 h deux semaines après | les mêmes | 30 à 60 termes ([guide 2](2-GLOSSAIRE.md)) |
| 3 | **Écriture** | asynchrone, quelques jours | 1 auteur métier + 1 co-auteur technique, **en binôme** | une spécification par fonction |
| 4 | **Revue croisée** | asynchrone, 2 à 4 jours | 3 relecteurs, 3 casquettes | acceptation, ou questions ouvertes |

Et, en continu, un cinquième temps : **l'arbitrage des questions ouvertes**, en séance
courte, par le valideur métier.

## Le binôme métier / développeur pendant l'écriture

C'est le point le plus important de ce guide, et le plus souvent négligé.

> **Le développeur n'est pas le relecteur final d'une spécification : il en est le
> co-auteur.**

Concrètement, pendant l'écriture, il a deux rôles précis :

1. **Il pose les questions qui font mal.** « Et si la liste est vide ? », « Et si les deux
   ont le même score ? », « Que se passe-t-il à minuit ? ». Il ne conteste pas les règles :
   il exhibe les cas que l'auteur n'a pas vus. C'est un travail que le métier ne peut pas
   faire seul, parce qu'il connaît trop bien son domaine pour en voir les trous.
2. **Il co-écrit la fiche de contraintes (§11).** Le métier fournit les chiffres — volumes,
   latence acceptable, rejouabilité, fréquence de changement. Le développeur les traduit
   en questions précises et signale immédiatement ce qui coûtera cher. **Une contrainte
   irréaliste doit être discutée à l'écriture, pas découverte à la livraison.**

En revanche, il **n'écrit pas les règles** et ne propose pas de solution technique dans le
document. La frontière du [CADRE.md §1.4](../CADRE.md) s'applique aussi à la façon de
travailler.

> Sur le fil rouge, c'est un développeur qui a fait apparaître `FN-012` en demandant
> « et si on recalcule un kilomètre plus loin et que le plan change ? ». Personne n'avait
> la réponse. Voir [1-DECOUPAGE.md](../exemples/fil-rouge/1-DECOUPAGE.md).

## Comment on converge quand deux experts ne sont pas d'accord

> **Un désaccord ne se résout jamais par un compromis de rédaction.**

Écrire « en général, le résidu va au plus gros composant » pour satisfaire deux personnes
qui pensaient l'inverse ne résout rien : ça transmet le désaccord au développeur, qui le
tranchera sans le savoir.

La règle est mécanique :

```
désaccord non résolu en séance
        ↓
Q-xx ouverte, avec :
   — la question, formulée de façon à admettre une réponse
   — les deux positions et ce qui les motive
   — UN décideur nommé (une personne, pas un comité)
   — UNE date d'échéance
        ↓
arbitrage, tracé dans le tableau des questions (le Q-xx reste, avec sa décision)
```

Trois principes qui font que ça marche :

- **Un décideur, pas un comité.** « La direction tranchera » ne trancherait jamais.
- **La question reste au document une fois fermée**, avec sa réponse et sa date. C'est
  ainsi qu'on évite de rouvrir le même débat tous les six mois.
- **Une question ouverte n'empêche pas d'avancer** sur le reste de la spécification — elle
  empêche seulement de la déclarer prête à développer.

## Le rythme

**Des lots de 5 à 8 fonctions.** Pas deux, pas cinquante.

- En dessous, on ne construit pas d'habitude : chaque lot ressemble à un cas particulier.
- Au-dessus, la revue devient un survol, le glossaire bouge sous les pieds des auteurs, et
  le premier bénéfice arrive trop tard pour que la démarche tienne.

Un lot dure typiquement **trois à quatre semaines** : écriture, revue, arbitrage,
acceptation. Le lot suivant démarre pendant que le précédent part en développement — ce
n'est pas un cycle en V, c'est un flux ([FAQ](../FAQ.md)).

## Les signaux qu'on collabore mal

| Signal | Ce qu'il révèle |
|---|---|
| Une spécification n'a **qu'un seul auteur** dans l'historique | Le binôme n'a pas eu lieu ; les cas limites n'ont pas été cherchés |
| Les revues sont approuvées **en moins de dix minutes** | Personne n'a lu ; l'approbation est un rite social |
| **Aucune `Q-xx` n'est jamais ouverte** | Soit le domaine est trivial, soit les désaccords se règlent hors trace — la seconde hypothèse est la bonne |
| Les questions ouvertes apparaissent **en développement** plutôt qu'en revue | La revue est trop faible, ou trop tardive |
| Le glossaire n'a **pas bougé depuis trois mois** sur un domaine actif | Il a cessé d'être lu, donc d'être vrai |
| Les commentaires de revue portent sur **le style** et non sur le fond | Les relecteurs n'ont pas les bonnes casquettes, ou pas la [liste de vérification](../templates/CHECKLIST-RELECTURE.md) |

## Trois pratiques qui tuent la collaboration

| Pratique | Pourquoi elle échoue |
|---|---|
| **Le document unique de 80 pages** | Un seul auteur à la fois ; relu en diagonale ; impossible d'attribuer une responsabilité |
| **Le tableur partagé** | Pas d'historique exploitable, pas de revue ligne à ligne, pas de discussion attachée au texte |
| **La validation par silence** (« sans retour sous 8 jours, c'est approuvé ») | Transforme la relecture en formalité, et supprime précisément ce qu'on cherchait |

L'outil recommandé est le dépôt de code du produit lui-même, en markdown, avec revue par
demande de fusion : historique, discussion attachée à la ligne, comparaison entre
versions, étiquettes de version. Voir [CADRE.md §6.3](../CADRE.md).

## L'inconfort de Git pour un non-développeur

Il est réel, et il se traite. Il dure environ deux semaines.

- Édition directe dans l'interface web de la forge — aucune installation, aucune ligne de
  commande.
- Un modèle pré-rempli, de sorte que la première spécification ne parte jamais d'une page
  blanche.
- Un accompagnement en binôme sur les deux premières.

> Ne cédez pas sur ce point pour « faciliter l'adoption ». Un traitement de texte
> partagé coûte, dès la troisième spécification, plus cher que la semaine
> d'apprentissage qu'il prétendait éviter.
