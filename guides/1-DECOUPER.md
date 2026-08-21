# Guide 1 — Découper : identifier et nommer les fonctions

*À utiliser avant d'écrire la moindre règle. Sortie attendue : la liste des fonctions du
périmètre, nommées, décrites en une phrase, avec un propriétaire.*

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
| « Calculer le montant à payer d'une commande » | ✅ le résultat est nommable : un montant |
| « Prévoir la date de livraison » | ✅ une date |
| « Décider de l'éligibilité à une remise » | ✅ une décision, avec son motif |
| « Gérer les commandes » | ❌ ne produit rien de nommable — c'est un paquet, pas une fonction |
| « Synchroniser le cache produit » | ❌ pas de résultat métier — c'est de la technique |
| « Écran de validation du panier » | ❌ c'est un endroit d'où l'on appelle des fonctions |

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
| Gérer les remises | Calculer la remise applicable à une ligne · Décider de la cumulabilité d'un code promotionnel |
| Traiter les commandes | Valider un panier · Calculer le montant à payer · Réserver le stock |
| Module de tarification | *(ce n'est pas une fonction, c'est un endroit)* |

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

## L'échelle de maturité

C'est ce qui rend la démarche applicable à un système existant : **on ne monte pas
toutes les fonctions au même niveau.**

| Niveau | Ce qui est acquis | Coût typique |
|---|---|---|
| **0** | La fonction existe et porte un nom | minutes |
| **1** | + une description en une phrase et un propriétaire métier | minutes |
| **2** | + un ancrage dans le code existant | dizaines de minutes |
| **3** | + un contrat typé d'entrées / sorties | heures |
| **4** | + une spécification complète : règles, contraintes, jeu d'essai | jours |

On peut cartographier deux cents fonctions au **niveau 1** en une semaine, et ne monter
au **niveau 4** que les dix qui le méritent — celles qui ont un impact financier,
contractuel ou réglementaire, qui vont vivre des années, ou qui changent souvent.

**Le niveau est une donnée de la fiche.** Une fonction au niveau 1 n'est pas un travail
inachevé : c'est un état assumé, qui dit « on sait qu'elle existe, on n'a pas encore eu
besoin d'en savoir plus ».

## Ancrer dans le code existant

C'est le point qui décide si la cartographie vit ou meurt. **Une cartographie qui ment
est pire que pas de cartographie** : elle donne une fausse confiance et personne ne la
rouvre après six mois.

| Option | Forme | Robustesse | Effort |
|---|---|---|---|
| **1. Chemin + symbole** ⭐ | `src/pricing/Cart.java#computeTotal` | se périme lentement ; **vérifiable automatiquement** (le symbole existe-t-il encore ?) | faible |
| **2. Marqueur dans le code** | un commentaire `@fonction FN-012` posé à l'ancre | survit aux déplacements et renommages ; lien **bidirectionnel** | moyen, touche au code |
| **3. Registre généré depuis le code** | le code est la source, la carte est produite | maximale | fort, rigide au démarrage |

**Recommandation : commencer par l'option 1, passer à la 2 quand la carte sert
vraiment.** Le critère qui compte n'est pas la précision du pointeur, c'est que
**l'incohérence soit détectable mécaniquement**. Dans les deux cas, une vérification
automatique doit répondre à deux questions :

- toute fiche de niveau ≥ 2 pointe-t-elle vers un symbole qui existe encore ?
- tout marqueur posé dans le code référence-t-il une fiche existante ?

Ce contrôle a sa place dans l'intégration continue, au même titre qu'un test.

> Une fonction peut avoir **plusieurs ancrages** — c'est même un signal précieux : deux
> implémentations de la même règle à deux endroits est exactement le genre de chose que
> la cartographie doit faire remonter.

## Le contrat typé

Au **niveau 3**, la fiche porte les entrées et sorties, **avec la notation exacte des
§4 et §5 du [modèle de spécification](../templates/MODELE-SPECIFICATION.md)** — pas un
second langage à apprendre :

```
entrees :
    montant_ht    : Montant(EUR, 2 décimales, ≥ 0)
    date_commande : Horodatage(fuseau Europe/Paris)
    quantite      : Entier(≥ 1)

sorties :
    remise_ht     : Montant(EUR, 2 décimales, ≥ 0)
    motif         : Énuméré { QUANTITE, CODE_PROMO, AUCUNE }
```

> **Un contrat sans unité ne vaut rien.** `montant : nombre` n'apporte aucune information
> que le code ne donnait déjà. Toute la valeur est dans l'unité, la devise, le fuseau, la
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
| 15 min | Propriétaire pour chaque fonction, et priorisation : lesquelles montent au niveau 4, et pourquoi |

**Deux règles d'animation :**

- Quand un débat sur un nom dure plus de trois minutes, c'est qu'il y a **deux fonctions**
  derrière, ou une notion absente du glossaire. Découpez ou nommez la notion.
- Rien n'est spécifié pendant l'atelier. Dès que quelqu'un commence à décrire une règle,
  on la note comme dette et on revient à la liste.

## Anti-patterns du découpage

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **Le découpage technique** | des fonctions qui portent le nom des couches : « Valider », « Persister », « Notifier » | on cartographie l'architecture actuelle, pas le métier — la carte meurt à la première refonte |
| **Le verbe fourre-tout** | « gérer », « traiter » | masque plusieurs fonctions sous un seul nom, donc plusieurs propriétaires sous un seul |
| **Le découpage par écran** | une fonction par page de l'interface | la spécification devient fausse au premier changement d'interface |
| **Le grain unique** | tout au niveau 4, ou tout au niveau 1 | tout au niveau 4 : le projet n'aboutit jamais. Tout au niveau 1 : la carte ne sert à rien |
| **L'ancrage non vérifié** | des chemins de fichiers écrits une fois, jamais contrôlés | la carte ment au bout de six mois, et on ne le sait pas |
| **La carte exhaustive** | on veut cartographier tout le système avant de spécifier quoi que ce soit | l'effort est consommé avant le premier bénéfice, et la démarche est abandonnée |
