# Fondements

Cette méthode n'invente presque rien. Elle assemble des idées éprouvées, dont certaines
ont cinquante ans, et les met au service d'un objectif précis : **permettre à des experts
métier de spécifier sans coder**.

Cette page dit d'où vient chaque idée, **ce qu'on lui emprunte** et **ce qu'on écarte**.
Écarter explicitement est aussi utile qu'emprunter : cela évite qu'on rouvre le débat
tous les six mois.

---

## La frontière métier / technique

> **Michael Jackson, « The World and the Machine », ICSE 1995.**
> Un logiciel a un *domaine* — le monde, qui existe indépendamment de lui — et une
> *machine* — le programme. Les exigences appartiennent au monde ; la conception
> appartient à la machine. Confondre les deux est la source d'une grande partie des
> échecs de spécification.

**Ce qu'on emprunte** : toute la §1.4 du [CADRE.md](CADRE.md). La règle « si ça change un
résultat observable → métier ; si ça ne change que le temps, la mémoire ou le coût →
technique » est une reformulation opérationnelle de cette distinction.

**Ce qu'on écarte** : l'appareil formel des *problem frames*, trop lourd pour un usage
quotidien par des non-informaticiens.

---

## Le découpage en fonctions

> **David Parnas, « On the Criteria To Be Used in Decomposing Systems into Modules »,
> CACM, 1972.**
> On ne découpe pas un système selon les étapes du traitement, mais selon les
> **décisions susceptibles de changer** : chaque module encapsule une décision, de sorte
> qu'un changement reste local.

**Ce qu'on emprunte** : le [guide 1](guides/1-DECOUPER.md) découpe par *résultat métier*
— c'est-à-dire par décision métier — et non par étape technique. La colonne « fréquence
de changement » de la fiche de contraintes est directement l'idée de Parnas appliquée aux
paramètres.

---

## Écrire la spécification avant le code, sans prétendre que c'est linéaire

> **David Parnas et Paul Clements, « A Rational Design Process: How and Why to Fake It »,
> IEEE TSE, 1986.**
> Un processus de conception parfaitement rationnel est impossible : on découvre en
> faisant. Mais on peut, et on doit, **produire la documentation que ce processus aurait
> produite** — parce que c'est elle qui a de la valeur, pas la chronologie.

**Ce qu'on emprunte** : la réponse à l'objection « c'est du cycle en V » ([FAQ](FAQ.md)).
La spécification est écrite avant le code, révisée quand le code enseigne quelque chose,
et ce va-et-vient est **prévu** — c'est le rôle des questions ouvertes et du
versionnement.

---

## Le glossaire et la langue commune

> **Eric Evans, *Domain-Driven Design*, 2003.**
> La **langue omniprésente** (*ubiquitous language*) : un vocabulaire unique dans les
> conversations, les documents, le code et les tests. Le **contexte délimité** (*bounded
> context*) : le même terme peut légitimement avoir deux sens dans deux parties de
> l'organisation ; la frontière se nomme, elle ne se supprime pas.

**Ce qu'on emprunte** : tout le [guide 2](guides/2-GLOSSAIRE.md), y compris le traitement
des homonymes.

**Ce qu'on écarte** : les patrons tactiques (entités, agrégats, dépôts). Ce sont des
choix de conception, donc du ressort du développement — les mettre dans une spécification
métier violerait la frontière.

---

## Préconditions, postconditions, invariants

> **Bertrand Meyer, *Object-Oriented Software Construction*, 1988 et 1997 — la
> programmation par contrat.**
> Un composant se décrit par ce qu'il exige (préconditions), ce qu'il garantit
> (postconditions), et ce qui reste vrai en permanence (invariants).

**Ce qu'on emprunte** : la structure des §4, §5 et §8 du modèle de spécification. Les
invariants sont ce qui permet de tester une fonction sur des entrées qu'on n'avait pas
prévues.

---

## Les exemples comme oracle

> **Gojko Adzic, *Specification by Example*, 2011.**
> Des exemples concrets, choisis par le métier, valent mieux que des règles abstraites
> pour lever les ambiguïtés — et ils deviennent une documentation vivante, parce que leur
> exécution échoue quand ils cessent d'être vrais.

**Ce qu'on emprunte** : le §10 de chaque spécification, et l'exigence que **les résultats
attendus soient calculés à la main** et non produits par une implémentation.

**Ce qu'on écarte** : la syntaxe Gherkin (*Given / When / Then*). Elle excelle pour des
scénarios comportementaux, et devient illisible pour un calcul à douze étapes avec
arrondis et cas limites. Voir la [FAQ](FAQ.md).

---

## Les tests de propriété

> **Koen Claessen et John Hughes, « QuickCheck: A Lightweight Tool for Random Testing of
> Haskell Programs », ICFP 2000.**
> Plutôt que d'énumérer des cas, on énonce des **propriétés** que le programme doit
> satisfaire pour toute entrée, et un générateur cherche des contre-exemples.

**Ce qu'on emprunte** : les invariants du §8 sont écrits pour être testés ainsi. Les
propriétés de symétrie — invariance par changement d'unité, par translation de l'origine
des temps, additivité — attrapent des classes entières d'erreurs qu'aucune liste de cas
ne trouverait.

---

## Les tables de décision

> **OMG, *Decision Model and Notation* (DMN), à partir de 2015.**
> Une norme de représentation des décisions métier, dont les tables de décision, avec des
> règles de complétude et de non-recouvrement vérifiables mécaniquement.

**Ce qu'on emprunte** : la forme des tables du [CADRE.md §2.5](CADRE.md) et le contrôle de
complétude — toute combinaison des entrées apparaît dans exactement une ligne.

**Ce qu'on écarte** : la norme complète, son métamodèle et son outillage. On garde la
discipline, pas la machinerie.

---

## La revue à plusieurs casquettes

> **Michael Fagan, « Design and code inspections to reduce errors in program
> development », IBM Systems Journal, 1976.**
> L'inspection formelle, avec des rôles distincts et des listes de vérification
> explicites, trouve significativement plus de défauts qu'une relecture libre.

**Ce qu'on emprunte** : les trois casquettes de la [liste de
vérification](templates/CHECKLIST-RELECTURE.md) et le fait que chacune ait **sa** liste.

**Ce qu'on écarte** : le cérémonial des réunions d'inspection. La revue se fait en
demande de fusion, de façon asynchrone.

---

## Les caractéristiques de qualité

> **ISO/IEC 25010 — modèles de qualité des systèmes et des logiciels.**
> Une taxonomie des caractéristiques non fonctionnelles : performance, fiabilité,
> sécurité, maintenabilité, portabilité.

**Ce qu'on emprunte** : l'idée qu'il existe une **liste** de dimensions à passer en revue,
et qu'on ne peut pas se contenter de « il faut que ce soit rapide ». La fiche de
contraintes ([CADRE.md §4](CADRE.md)) en est une version courte, exprimée en questions
posées au métier plutôt qu'en catégories.

**Ce qu'on écarte** : la taxonomie elle-même, trop abstraite pour être remplie par un
expert métier.

---

## Les standards d'expression des exigences

> **ISO/IEC/IEEE 29148** (qui a succédé à IEEE 830) — ingénierie des exigences.

**Ce qu'on emprunte** : l'exigence de traçabilité, l'identification stable des exigences,
et le principe qu'une exigence doit être vérifiable.

**Ce qu'on écarte** : la forme « le système doit… », répétée sur des centaines de lignes.
Elle décrit un comportement observable de l'extérieur, ce qui convient à un cahier des
charges contractuel mais ne dit **pas** comment un résultat est calculé. C'est justement
ce que cette méthode cherche à produire.

---

## Les méthodes formelles

> **B, Z, Alloy, TLA+.**
> Des langages de spécification formels, avec preuve ou vérification de modèle.

**Ce qu'on écarte, et pourquoi.** Ces langages sont très supérieurs sur la non-ambiguïté
et disqualifiés sur un critère qui, ici, prime : **la relecture par un pair métier**. Une
spécification que le métier ne peut pas relire est une spécification que le métier
n'écrit pas — et l'objectif de toute la démarche est précisément qu'il l'écrive.

Ils restent le bon outil pour un noyau critique restreint : protocole de sûreté,
cohérence d'un algorithme distribué, propriété de sécurité. Rien n'interdit de spécifier
formellement une fonction et d'employer cette méthode pour les quarante autres.

---

## Ce que cette méthode ajoute

Peu de choses, et elles sont assumées :

1. **La liste des faux amis** ([CADRE.md §1.5](CADRE.md)) — arrondis, ex æquo, ordre des
   opérations, valeurs absentes, bornes, modes dégradés. Ce n'est pas une théorie, c'est
   un constat de terrain : ce sont les points qui, en pratique, font diverger deux
   implémentations honnêtes.
2. **La fiche de contraintes exprimée en questions au métier**, avec en face ce que l'IT
   en déduit. Le pont entre l'expression métier et la décision d'architecture.
3. **Les trois niveaux d'exactitude** — reproductibilité, justesse, validité du modèle —
   pour le calcul scientifique ([CADRE.md §2.8](CADRE.md)). Leur confusion est l'erreur la
   plus commune sur un logiciel scientifique.
4. **L'échelle de maturité par fonction** ([guide 1](guides/1-DECOUPER.md)). C'est elle qui
   rend la démarche applicable à un système existant : on ne spécifie pas tout, on
   spécifie ce qui le mérite, et on assume le reste.
