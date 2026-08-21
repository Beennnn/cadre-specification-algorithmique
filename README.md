# Cadre de spécification fonctionnelle

> Le métier écrit la loi. L'IT écrit la machine.

**Ce dépôt est une méthode, pas un entrepôt de spécifications.** Il ne contient aucune
spécification de votre système : il contient la façon de les écrire, les modèles pour le
faire, et un exemple complet de bout en bout.

Les spécifications que vous produirez avec cette méthode vivent **dans le dépôt de votre
produit, à côté du code** — c'est ce qui leur permet d'être versionnées, relues en demande
de fusion, et de rester vraies.

---

## Le problème

Dans beaucoup d'organisations, la connaissance algorithmique appartient à des gens dont le
métier n'est pas de développer. Ils codent quand même — tableur, carnet de calcul, script
— parce que c'est le seul moyen qu'ils ont de vérifier que leur idée tient. Ce code finit
en production par accident, ou est réécrit avec dérive, ou est jeté avec ses quarante
décisions implicites.

Le point commun : **le code mélange indissociablement l'intention et la mise en œuvre.**
Et quand la spécification est floue, c'est le développeur qui décide du métier — sans le
savoir et sans mandat.

## Ce que la méthode produit

Une **spécification fonctionnelle** en deux volets :

1. **L'architecture fonctionnelle** — quelles fonctions existent, comment elles
   s'appellent, qui en répond, où elles vivent dans le code, quel est leur contrat.
2. **La spécification de chaque fonction** — les règles, en pseudo-langage, avec leurs
   paramètres, leurs invariants, leur jeu d'essai et leurs contraintes chiffrées.

Le tout assez précis pour qu'un développeur l'implémente sans deviner, et assez libre pour
qu'il choisisse le langage, les structures de données et l'architecture.

## Les trois temps

| | | Guide |
|---|---|---|
| **1. Découper** | Identifier les fonctions, les nommer, leur donner un propriétaire, les ancrer dans le code existant | **[guides/1-DECOUPER.md](guides/1-DECOUPER.md)** |
| **2. Écrire** | Le glossaire d'abord, puis les règles — à plusieurs, par lots, en revue croisée | **[guides/2-GLOSSAIRE.md](guides/2-GLOSSAIRE.md)** · **[guides/3-ECRIRE-A-PLUSIEURS.md](guides/3-ECRIRE-A-PLUSIEURS.md)** |
| **3. Passer au développement** | Transmettre, pour que l'équipe puisse choisir l'architecture et le langage, coder, optimiser et qualifier | **[guides/4-PASSER-AU-DEVELOPPEMENT.md](guides/4-PASSER-AU-DEVELOPPEMENT.md)** |

## Ce que contient le dépôt

| | |
|---|---|
| **[CADRE.md](CADRE.md)** | **Le document de référence.** Le principe, la frontière métier/technique, les huit faux amis, le pseudo-langage, l'adaptation au calcul scientifique, la fiche de contraintes, la gouvernance, les anti-patterns. À lire une fois, en entier |
| **[guides/](guides/)** | Quatre guides opérationnels, à ouvrir pendant qu'on fait |
| **[templates/](templates/)** | Les modèles vierges : spécification, fiche de fonction, glossaire, liste de vérification en revue |
| **[exemples/fil-rouge/](exemples/fil-rouge/)** | **Le fil rouge complet** : *l'autonomie d'un véhicule électrique*, du découpage jusqu'à la spécification détaillée |
| **[GLOSSAIRE.md](GLOSSAIRE.md)** | Le vocabulaire de la méthode elle-même |
| **[REFERENCES.md](REFERENCES.md)** | D'où viennent les idées, ce qu'on leur emprunte et ce qu'on écarte |
| **[FAQ.md](FAQ.md)** | Les objections fréquentes, et leurs réponses |

## Le fil rouge

Tous les guides s'appuient sur **le même scénario** : *[l'autonomie d'un véhicule
électrique](exemples/fil-rouge/)*. « Jusqu'où puis-je aller, et où dois-je m'arrêter ? »

Il a été choisi parce qu'il réunit quatre qualités rares ensemble : tout le monde comprend
la question, il y a de la vraie physique avec des équations, il est assez complexe pour
être intéressant, et il se découpe naturellement en une douzaine de fonctions réparties
sur cinq propriétaires métier.

On l'y suit de bout en bout : [le découpage](exemples/fil-rouge/1-DECOUPAGE.md) → [le
glossaire](exemples/fil-rouge/2-GLOSSAIRE.md) → [une fiche arrêtée au niveau
3](exemples/fil-rouge/3-FN-004-planifier-les-recharges.md) → [la spécification
complète](exemples/fil-rouge/4-SPEC-NRG-001-autonomie.md).

**Deux vignettes** complètent l'illustration, gardées parce qu'elles aboutissent à des
conclusions techniques **opposées** à partir de la même méthode :

| | [Le montant à payer d'une commande](exemples/SPEC-PRX-001-montant-a-payer.md) | [Le refroidissement d'une boisson](exemples/SPEC-THM-001-refroidissement.md) | [L'autonomie d'un véhicule](exemples/fil-rouge/4-SPEC-NRG-001-autonomie.md) |
|---|---|---|---|
| Exigence d'exactitude | exacte au centime | tolérance de 10⁻⁶ | reproductibilité de 10⁻⁹ entre deux implémentations |
| **Type numérique qui en découle** | **décimal exact obligatoire** | **double précision confortable** | **double précision indispensable** |
| Faux ami central | le centime résiduel d'une répartition | l'ordre d'ajout du lait | le sens de l'arrondi de l'autonomie |

*Trois exigences différentes, trois conclusions différentes.* C'est la démonstration que
c'est la spécification qui décide, et non l'habitude du développeur.

## Le critère d'acceptation

> **Le test de la double implémentation.** Deux développeurs qui ne se parlent pas, dans
> deux langages différents, produisent des programmes qui donnent **le même résultat** sur
> l'intégralité du jeu d'essai — et sur les cas auxquels personne n'avait pensé.

Si ce n'est pas vrai, la spécification n'est pas finie.

Sur des grandeurs continues, ce critère se dédouble en **reproductibilité** (deux
implémentations conformes donnent le même nombre), **justesse** (le nombre est la vraie
valeur des formules) et **validité du modèle** (les formules décrivent la réalité) — voir
[CADRE.md §2.8](CADRE.md).

## Par où commencer

1. Lisez **[CADRE.md](CADRE.md)** une fois, en entier. C'est le seul document long.
2. Parcourez le **[fil rouge](exemples/fil-rouge/)** pour voir à quoi ça ressemble.
3. Faites l'**exercice d'étalonnage** : prenez un algorithme existant, connu, moyennement
   complexe, écrivez-en la spécification *a posteriori*, et faites-la implémenter par
   quelqu'un qui ne connaît pas le domaine. Les écarts constatés convainquent — le
   discours, non.

> Ce cadre est un outil, pas un règlement. Une section qui ne sert jamais doit être
> supprimée ; une question qui revient toujours doit être ajoutée à la liste de
> vérification. La seule règle non négociable est la première : **la spécification s'écrit
> avant le code**.
