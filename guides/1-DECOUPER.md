# Guide 1 — Découper : identifier et nommer les fonctions

*À utiliser avant d'écrire la moindre règle. Sortie attendue : la liste des fonctions du
périmètre, nommées, décrites en une phrase, avec le valideur métier qui atteste qu'elles sont justes.*

---

## Pourquoi commencer par là

On ne spécifie pas un système. On spécifie **des fonctions**.

Sans découpage préalable, on produit un document fleuve : personne ne le relit en
entier, personne ne peut se le répartir, et personne ne sait dire si une règle manque.
Le découpage n'est pas une formalité d'architecte — c'est ce qui rend le travail
**divisible**, donc collaboratif, donc faisable.

C'est aussi ce qui permet d'avancer sur un existant sans tout réécrire : on cartographie
large et superficiel d'abord, on approfondit ensuite là où c'est rentable (§4).

## Qu'est-ce qu'une fonction, au sens de ce cadre

> Une **transformation qui produit un résultat identifiable à partir d'entrées
> identifiables**, et dont le métier sait nommer le résultat sans parler de mise en œuvre.

Le test tient en une question : **« peux-tu nommer ce que ça produit, sans dire
comment ? »**

| Candidat | Verdict |
|---|---|
| « Calculer l'énergie consommée sur un trajet » | ✅ le résultat est nommable : une énergie |
| « Prévoir l'instant du lever du Soleil » | ✅ un instant |
| « Décider si une lecture est exploitable » | ✅ une décision, avec son motif |
| « Gérer les relevés » | ❌ ne produit rien de nommable — c'est un paquet, pas une fonction |
| « Synchroniser le cache des stations » | ❌ pas de résultat métier — c'est de la technique |
| « Écran de saisie d'un lot » | ❌ c'est un endroit d'où l'on appelle des fonctions |

## Les trois façons de découper — et laquelle choisir

| Découpage | Quand c'est pertinent | Le piège |
|---|---|---|
| **Par résultat métier** ⭐ | par défaut, dans presque tous les cas | aucun — c'est le seul qui résiste aux réorganisations techniques |
| **Par événement** | systèmes réactifs, flux, traitements déclenchés | on confond le déclencheur avec le calcul ; plusieurs événements appellent souvent la même fonction |
| **Par écran ou cas d'usage** | jamais pour découper — utile seulement pour **vérifier** qu'on n'a rien oublié | on découpe l'interface, pas le métier ; le jour où l'écran change, la spécification devient fausse |

> **La règle qui tranche tous les cas douteux :** une fonction est définie par **ce
> qu'elle produit**, jamais par l'endroit d'où on l'appelle, ni par la couche technique
> où elle vit, ni par l'équipe qui la maintient.

Utilisez le découpage par cas d'usage **en fin d'atelier**, comme contrôle : pour chaque
parcours utilisateur connu, quelles fonctions sont mobilisées ? Un parcours qui
n'en mobilise aucune signale un trou.

## Nommer

**Verbe à l'infinitif + complément, dans la langue du domaine.**

Les verbes interdits, sans exception : *gérer, traiter, handle, process, manager,
administrer, prendre en charge*. Ce ne sont pas des verbes, ce sont des cache-misère.

> **Le test du verbe.** Si vous ne pouvez pas remplacer « gérer » par un verbe précis,
> c'est que la fonction n'en est pas une : c'est un paquet qui en contient plusieurs.
> Rouvrez-le.

| ❌ | ✅ |
|---|---|
| Gérer les aberrantes | Décider si une lecture est exploitable · Écarter les lectures aberrantes d'un relevé |
| Traiter les relevés | Valider un relevé · Calculer la moyenne du jour · Détecter les épisodes de gel |
| Module de dosage | *(ce n'est pas une fonction, c'est un endroit)* |

Les noms sont **des décisions collectives**, pas des préférences d'auteur : ils
deviendront le vocabulaire de la spécification, du code, des tests et des conversations.
C'est pour cela qu'on les fixe en atelier, à plusieurs (guide 2).

## La bonne granularité

> **Une fonction = un calcul ou une décision métier dont on peut écrire le jeu d'essai.**

Le critère opérationnel : **si vous ne pouvez pas écrire trois cas de test qui la
caractérisent, elle est mal découpée.**

- Vous n'arrivez qu'à écrire des cas triviaux → elle est trop fine, c'est un détail
  d'implémentation.
- Vous n'arrivez pas à écrire un cas complet sans y mettre dix scénarios → elle est trop
  grosse, elle en contient plusieurs.

Ordre de grandeur utile : un périmètre bien découpé compte typiquement **quelques
dizaines de fonctions**, pas trois et pas huit cents.

## Le triptyque, et ce que l'échelle en gradue

Toute fonction se décrit par **trois choses** : son **rôle** (à quoi elle sert, ce qu'elle
produit), son **contrat** (ce qu'elle exige et garantit), son **algorithme** (les règles).

Chacune se lit sans la suivante — et l'échelle de maturité ci-dessous n'est rien d'autre
que **la part du triptyque qui est renseignée**. Voir [CADRE.md §3.0](../CADRE.md).

## L'échelle de maturité

C'est ce qui rend la démarche applicable à un système existant : **on ne monte pas
toutes les fonctions au même niveau.**

| Niveau | Ce qui est acquis | Coût typique |
|---|---|---|
| **0** | La fonction existe et porte un nom | minutes |
| **1** | + le **rôle** — à quoi elle sert, ce qu'elle produit — et son valideur métier | minutes |
| **2** | + le **contrat** typé d'entrées / sorties | heures |
| **3** | + l'**algorithme** : règles, contraintes, jeu d'essai | jours |

On peut cartographier deux cents fonctions au **niveau 1** en une semaine, et ne monter
au **niveau 3** que les dix qui le méritent — celles qui ont un impact financier,
contractuel ou réglementaire, qui vont vivre des années, ou qui changent souvent.

**Le niveau est une donnée de la fiche.** Une fonction au niveau 1 n'est pas un travail
inachevé : c'est un état assumé, qui dit « on sait qu'elle existe, on n'a pas encore eu
besoin d'en savoir plus ».

## Pourquoi cette méthode ne pointe pas vers le code

Une version antérieure de ce guide demandait d'**ancrer** chaque fonction dans le code
existant — un chemin de fichier et un symbole. **Cette exigence a été retirée**, et la
raison mérite d'être écrite, parce qu'elle dit quelque chose du cadre tout entier.

Le but de la démarche est **d'écrire un besoin**. Ce qu'il advient du code n'appartient pas
à celui qui écrit la spécification : il appartient à quelqu'un dont c'est le métier, qui
disposera de toutes les informations nécessaires et de **toute l'autonomie** pour produire
du code optimisé et maintenable — même s'il n'est pas expert du domaine.

Pointer depuis la spécification vers un fichier, c'était :

| | |
|---|---|
| **Inverser la dépendance** | La spécification doit survivre à l'implémentation. La faire pointer vers elle la rend caduque à la première refonte |
| **Entamer l'autonomie du développeur** | Un chemin dans une spécification se lit, qu'on le veuille ou non, comme « ta fonction doit vivre là » |
| **Violer notre propre frontière** | Un chemin de fichier est une décision technique dans un document métier ([CADRE §1.4](../CADRE.md)) |
| **Créer une carte qui ment** | Elle se périme silencieusement, et il faut alors un outil pour surveiller un lien dont on n'avait pas besoin |

### Le besoin réel, et sa bonne direction

Il reste une exigence légitime derrière tout cela : **« où est implémentée cette
règle ? »**, que l'auditabilité impose. Elle est déjà satisfaite, et **dans l'autre
sens** :

> **C'est le code qui cite la spécification, jamais la spécification qui cite le code.**

Le développeur mentionne `RG-010` en commentaire à l'endroit qu'il juge bon, et dans le
nom de ses tests ([guide 6](6-PASSER-AU-DEVELOPPEMENT.md)). Une recherche de `RG-010` dans
le dépôt de code répond à la question en dix secondes.

Cette direction a trois vertus que l'autre n'avait pas : elle **n'entame aucune
autonomie** — le développeur cite où il veut, autant de fois qu'il veut ; elle **ne se
périme pas**, puisque la citation voyage avec le code qu'elle annote ; et elle **ne
salit pas la spécification**, qui ignore jusqu'à l'existence du dépôt de code.

## Le contrat typé## Le contrat typé

Au **niveau 2**, la fiche porte les entrées et sorties, **avec la notation exacte des
§4 et §5 du [modèle de spécification](../templates/MODELE-SPECIFICATION.md)** — pas un
second langage à apprendre :

```
entrees :
    temperature  : Temperature(°C, −90.0 .. +60.0, 1 décimale)
    recorded_at  : DateAndTime(UTC, à la minute)
    quality_flag : Enumerated { VALID, SUSPECT, FAULTY }

sorties :
    usable       : Booleen
    motif        : Enumerated { ABSENTE, SIGNALEE, HORS_PLAGE, AUCUN }
```

> **Un contrat sans unité ne vaut rien.** `temperature : nombre` n'apporte aucune information
> que le code ne donnait déjà. Toute la valeur est dans l'unité, l'échelle, le référentiel, la
> précision et le domaine — c'est-à-dire dans ce que le typage du langage ne dit pas.

## Les dépendances

Notez **qui appelle qui**, mais restez sobre : l'objectif est de repérer les fonctions
centrales et les cycles suspects, pas de reconstituer un graphe d'appel exhaustif — que
l'outillage fait mieux que vous.

Deux signaux valent qu'on s'arrête : une fonction appelée par presque toutes les autres
(souvent un paquet mal découpé), et un cycle entre deux fonctions métier (souvent une
frontière mal placée).

## L'atelier de découpage

**Durée** 2 h · **Qui** 2 à 4 personnes du métier, 1 à 2 développeurs, 1 animateur ·
**Sortie** la liste des fonctions au niveau 1

| Temps | Ce qu'on fait |
|---|---|
| 15 min | Rappel du périmètre et de ce qui est **hors** périmètre |
| 40 min | Chacun liste les résultats métier produits dans le périmètre — **des résultats, pas des traitements**. On met en commun, on regroupe les doublons |
| 30 min | On nomme, verbe par verbe. C'est le moment le plus lent et le plus utile : chaque désaccord de nom cache un désaccord de fond |
| 20 min | Contrôle par les parcours utilisateurs connus : chacun mobilise-t-il des fonctions identifiées ? |
| 15 min | Valideur métier pour chaque fonction, et priorisation : lesquelles montent au niveau 3, et pourquoi |

**Deux règles d'animation :**

- Quand un débat sur un nom dure plus de trois minutes, c'est qu'il y a **deux fonctions**
  derrière, ou une notion absente du glossaire. Découpez ou nommez la notion.
- Rien n'est spécifié pendant l'atelier. Dès que quelqu'un commence à décrire une règle,
  on la note comme dette et on revient à la liste.

## Anti-patterns du découpage

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **Le découpage technique** | des fonctions qui portent le nom des couches : « Valider », « Persister », « Notifier » | on cartographie l'architecture actuelle, pas le métier — la carte meurt à la première refonte |
| **Le verbe fourre-tout** | « gérer », « traiter » | masque plusieurs fonctions sous un seul nom, donc plusieurs valideurs sous un seul |
| **Le découpage par écran** | une fonction par page de l'interface | la spécification devient fausse au premier changement d'interface |
| **Le grain unique** | tout au niveau 4, ou tout au niveau 1 | tout au niveau 4 : le projet n'aboutit jamais. Tout au niveau 1 : la carte ne sert à rien |
| **La carte exhaustive** | on veut cartographier tout le système avant de spécifier quoi que ce soit | l'effort est consommé avant le premier bénéfice, et la démarche est abandonnée |
