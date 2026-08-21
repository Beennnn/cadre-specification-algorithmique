# Guide 2 — Le glossaire : le premier livrable

*À produire juste après le découpage, avant d'écrire la moindre règle. Sortie attendue :
30 à 60 termes définis, verrouillés, partagés.*

---

## Pourquoi le glossaire passe avant tout le reste

Le glossaire est le **seul artefact partagé par toutes les fonctions**. C'est donc le
seul point de contention structurel de la démarche — et la seule chose qu'il faut fixer
*avant* de paralléliser l'écriture entre plusieurs auteurs.

Écrit après coup, il ne sert à rien : chacun aura déjà employé ses propres mots, et le
glossaire ne fera que constater le désordre. Écrit d'abord, il **rend l'écriture
parallèle possible**.

Il a aussi un effet mécanique sous-estimé : **chaque terme défini est une phrase qu'on
n'écrit plus**. Une spécification adossée à un bon glossaire est deux fois plus courte,
et deux fois plus précise.

### Le fondement théorique

> **Eric Evans, *Domain-Driven Design* (2003)** — la **langue omniprésente**
> (*ubiquitous language*) : un vocabulaire unique, employé à l'identique dans les
> conversations, les documents, le code et les tests. Toute traduction entre le langage
> du métier et celui du code est une occasion de perdre du sens ; le glossaire supprime
> la traduction.

> **Michael Jackson, « The World and the Machine » (ICSE, 1995)** — la distinction entre
> **le monde** (le domaine, qui existe indépendamment du logiciel) et **la machine** (le
> programme). Les termes du glossaire décrivent le monde. Un terme qui n'a de sens que
> dans le programme — « cache produit », « file de traitement », « identifiant technique »
> — n'a rien à faire dans un glossaire de domaine.

C'est le même partage que celui du [CADRE.md §1.4](../CADRE.md), appliqué au vocabulaire.

## Les deux règles — et la seconde qu'on oublie toujours

**1. Un terme = un sens.** Celle-là, tout le monde la connaît.

**2. Un sens = un terme.** Celle-là s'oublie systématiquement, et coûte aussi cher.

Trois mots pour la même notion — « client », « acheteur », « commanditaire » — fabriquent
des règles qui *semblent* parler de choses différentes. Le lecteur cherche la nuance,
ne la trouve pas, et finit par en inventer une. C'est ainsi que naissent les divergences
d'implémentation les plus difficiles à diagnostiquer : personne n'a écrit d'erreur, tout
le monde a lu un texte différent.

## Ce qu'on met, ce qu'on ne met pas

| On met | On ne met pas |
|---|---|
| Les notions du domaine employées dans les règles | Les termes techniques (cache, file, index, service) |
| Les états et leurs transitions autorisées | Les mots du français courant que personne ne conteste |
| Les grandeurs, **avec leur unité, leur devise, leur référentiel** | Les synonymes ajoutés « pour la variété » |
| Les rôles et qui les tient | Les définitions circulaires |
| Les objets et documents métier | Les termes que personne n'emploie à l'oral |
| Les notions réglementaires ou contractuelles, **avec leur source** | Les acronymes internes non expliqués |

## Écrire une bonne définition

Six critères, tous vérifiables en relecture :

1. **Une phrase.** Si vous en écrivez trois, c'est probablement deux notions.
2. **Sans employer le mot défini.** « Panier : un panier de commande » ne définit rien.
3. **Elle dit ce que la chose *est***, pas ce qu'elle fait, ni comment on l'obtient.
4. **Elle porte l'unité, la devise, le fuseau** quand il s'agit d'une grandeur.
5. **Elle dit ce que ce n'est pas**, quand une confusion est fréquente.
6. **Elle cite sa source** quand le terme est réglementaire, contractuel ou normatif.

### Le même terme, mal puis bien défini

| | |
|---|---|
| ❌ | **Montant net** — le montant après remises. |
| ✅ | **Montant net** — le montant hors taxes d'une ligne ou d'un panier, une fois toutes les remises appliquées et avant frais de livraison. Exprimé en euros à 2 décimales. À ne pas confondre avec le *montant à payer*, qui inclut la TVA, les frais de livraison et l'imputation des points de fidélité. |

La mauvaise définition n'est pas fausse : elle est **insuffisante**. Elle ne dit pas les
taxes, pas les frais de port, pas l'unité, et elle laisse le lecteur croire qu'elle
désigne ce qu'on doit payer.

| | |
|---|---|
| ❌ | **Remise** — on applique 10 % quand le client commande 3 articles ou plus. |
| ✅ | **Remise** — réduction consentie sur un montant, exprimée en taux ou en montant fixe. Une remise de *quantite_commandee* porte sur une ligne, une remise de *panier* porte sur l'ensemble de la commande. |

La mauvaise définition décrit **un traitement** au lieu d'une notion : elle enferme dans
le glossaire une règle qui devrait vivre en `RG-xxx`, avec son identifiant, son
propriétaire et sa date d'effet. Le jour où le seuil passe à 4 articles, il faudra
modifier le glossaire — c'est-à-dire le socle partagé — pour un changement de paramètre.

> **Le test.** Si une définition contient un chiffre, une condition ou un « si », c'est
> presque toujours une règle déguisée. Sortez-la.

## Les homonymes : une frontière, pas une erreur

Le même mot a souvent deux sens légitimes dans deux parties de l'organisation.

> « **Commande** », pour le commerce, est l'intention d'achat validée par le client.
> « **Commande** », pour la logistique, est l'ensemble des colis à préparer — une commande
> commerciale peut en produire trois.

**La mauvaise réponse est d'inventer un mot unique et artificiel** que personne
n'emploiera. Personne ne dira jamais « commande-commerciale-agrégée » à la machine à
café, et le glossaire redeviendra une fiction.

La bonne réponse, documentée par Evans sous le nom de **contexte délimité** (*bounded
context*) : **nommer la frontière**. On indique pour chaque terme le contexte où il vaut,
et on documente la correspondance au point de passage.

| Terme | Contexte | Définition | Correspondance |
|---|---|---|---|
| Commande | Commerce | Intention d'achat validée par le client, à un instant donné | 1 commande commerciale → 1 à n expéditions |
| Commande | Logistique | Ensemble des colis à préparer pour une même adresse et une même date | — |

> **Une frontière de vocabulaire est presque toujours une frontière d'équipe, de
> système, ou de responsabilité.** Quand vous en trouvez une, vous venez de découvrir
> quelque chose d'important sur votre organisation — notez-le.

## Le glossaire vit et se versionne

| Changement | Incrément | Conséquence |
|---|---|---|
| Ajouter un terme | **mineur** | rien à reprendre |
| Préciser une définition sans changer le sens | **correctif** | rien à reprendre |
| **Changer le sens d'un terme existant** | **majeur** | **toutes les règles qui l'emploient sont potentiellement affectées** — l'impact s'analyse avant de valider |
| Renommer un terme | **majeur** | l'ancien nom est conservé comme synonyme déprécié, daté |

Le changement de sens d'un terme est le plus dangereux de tous les changements d'une
spécification, parce qu'il est **invisible** : aucune règle n'a bougé, et pourtant elles
ont toutes changé de signification. C'est la raison pour laquelle le glossaire est
versionné avec le même sérieux que les règles.

## L'atelier de glossaire

**Durée** 1 h, à répéter une fois deux semaines plus tard · **Qui** les mêmes personnes
que l'atelier de découpage · **Sortie** 30 à 60 termes

| Temps | Ce qu'on fait |
|---|---|
| 10 min | On relit les noms des fonctions issues du découpage et on **surligne chaque terme métier** qu'ils contiennent. C'est l'amorce : ces termes-là sont obligatoires |
| 25 min | Chacun définit à l'écrit, seul, cinq termes. En silence. **L'écrit d'abord évite que le plus fort en réunion impose son sens** |
| 20 min | Mise en commun. Les définitions identiques passent. **Les définitions divergentes sont le vrai produit de l'atelier** : chacune signale soit un homonyme, soit un désaccord de fond |
| 5 min | Ce qui n'est pas tranché devient une question `Q-xx` avec un décideur et une date — **jamais un compromis de rédaction** |

> **Le moment qui compte, c'est la divergence.** Un atelier de glossaire où tout le monde
> est d'accord dès le premier tour n'a rien produit : soit les termes étaient triviaux,
> soit les gens n'ont pas écrit avant de parler.

## Le contrôle qui vaut tous les autres

Deux passes, cinq minutes, à faire à chaque revue :

1. Prendre **trois règles au hasard** : tout terme métier qu'elles emploient figure-t-il
   au glossaire ?
2. Prendre **trois termes du glossaire au hasard** : sont-ils réellement employés dans au
   moins une règle ?

Un terme jamais employé est soit une notion qui manque aux règles, soit un terme mort.
Les deux méritent une discussion de trente secondes.

## Anti-patterns

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **Le glossaire écrit à la fin** | il apparaît dans le dernier commit, « pour la forme » | il constate le désordre au lieu de l'empêcher |
| **La définition circulaire** | le mot défini apparaît dans sa définition | elle donne l'illusion d'avoir tranché |
| **La règle déguisée en définition** | la définition contient un chiffre, un « si », une condition | un changement de paramètre oblige à modifier le socle partagé |
| **Le dictionnaire technique** | on y trouve « service », « cache », « identifiant technique » | on décrit la machine, pas le monde |
| **La grandeur sans unité** | « Poids : le poids du colis » | cause classique d'écarts, découverts tard |
| **Le mot artificiel** | un terme inventé pour éviter un homonyme, que personne n'emploie à l'oral | le glossaire devient une fiction et cesse d'être la langue commune |
| **Le glossaire figé** | aucune modification depuis six mois sur un domaine actif | il a cessé d'être lu, donc d'être vrai |

---

## Voir sur le fil rouge

Le glossaire complet du scénario *L'autonomie d'un véhicule électrique*, avec ses deux
homonymes assumés (*autonomie*, *charge*) et ses termes explicitement écartés, est ici :
**[exemples/fil-rouge/2-GLOSSAIRE.md](../exemples/fil-rouge/2-GLOSSAIRE.md)**

Regardez en particulier sa ligne d'historique `1.1.0` : le retrait d'un seul terme mal
défini — *consommation moyenne* — a obligé à reprendre deux règles déjà écrites. C'est
exactement l'effet recherché.

Et le [modèle vierge](../templates/MODELE-GLOSSAIRE.md) est prêt à copier.
