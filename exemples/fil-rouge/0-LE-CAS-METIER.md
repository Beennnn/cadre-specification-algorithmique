# Le cas métier — « Est-ce que j'arrive ? »

*Point d'entrée du fil rouge. À lire avant tout le reste, y compris par ceux qui ne
liront rien d'autre.*

---

# Partie 1 — Le cas, simplement

## La scène

Vendredi soir. 340 km à faire. La voiture est chargée à bloc et annonce **320 km
d'autonomie**.

On part, ou on ne part pas ?

La question paraît triviale — il manque 20 km, donc il faudra s'arrêter une fois. Sauf
que ce « 320 » n'est pas une caractéristique du véhicule. C'est **une prédiction**, et
elle dépend de ce qui va se passer pendant les trois heures qui suivent.

## Le même véhicule, la même batterie pleine

| Conditions | Autonomie réelle |
|---|---|
| 110 km/h, plat, 20 °C | **320 km** |
| 130 km/h, plat, 20 °C | 255 km |
| 110 km/h, plat, −5 °C | 250 km |
| 130 km/h, plat, −5 °C | **199 km** |

**320 km ou 199 km.** Même voiture, même batterie, même charge. Un écart de 38 %, produit
par deux facteurs que le conducteur maîtrise ou subit : sa vitesse, et la météo.

Ajoutez le relief et l'écart s'aggrave : la même voiture qui consomme 17 kWh aux 100 km à
110 km/h sur le plat en consomme **44** dans une montée à 5 %. Deux fois et demie plus.

> C'est tout le problème. **L'autonomie n'est pas une donnée, c'est un calcul** — et un
> calcul qui doit être refait en permanence, parce que ses entrées changent tout le temps.

## Les cinq questions auxquelles il faut répondre

En langage de conducteur :

1. **Jusqu'où puis-je aller** avant de devoir m'arrêter ?
2. **Est-ce que j'arrive** sans m'arrêter ?
3. Si non, **où m'arrêter, et combien de temps** ?
4. **Qu'est-ce qui change** si je roule moins vite ? s'il fait froid ? si je coupe le
   chauffage ?
5. **Puis-je faire confiance** au chiffre affiché ?

La cinquième est la plus importante, et c'est celle qu'on oublie. Un conducteur qui a été
surpris une fois ne fera plus jamais confiance au chiffre — et il gardera 80 km de marge
dans sa tête, ce qui annule l'intérêt du calcul.

## Ce qui se passe quand on se trompe

| Erreur | Conséquence |
|---|---|
| **Trop optimiste** | Le conducteur tombe en panne sur une voie rapide. C'est un incident de sécurité, une dépanneuse, un client perdu, parfois la presse |
| **Trop prudent** | Le conducteur s'arrête pour rien, perd 25 minutes, et raconte partout que « cette voiture ne tient pas ses promesses » |

Les deux erreurs coûtent cher, et elles ne sont **pas symétriques** : la première est
grave, la seconde est agaçante. Cette asymétrie n'est pas un détail d'implémentation —
c'est une décision d'entreprise, et elle doit apparaître **dans la spécification**.

Elle y apparaît, sous une forme qui surprend toujours : *l'autonomie s'arrondit toujours
vers le bas, jamais au plus proche.*

---

# Partie 2 — Le même cas, un cran plus technique

## Ce qui freine la voiture

Rien de mystérieux : trois forces, connues depuis le lycée.

| Force | Formule | Ce qu'elle représente |
|---|---|---|
| **Traînée aérodynamique** | `½ · ρ · Cx·S · v²` | l'air qu'il faut écarter. Elle croît avec le **carré** de la vitesse — c'est elle qui punit les 130 km/h |
| **Résistance au roulement** | `Crr · m · g · cos α` | la déformation des pneus. Quasi constante |
| **Pente** | `m · g · sin α` | la gravité. **Signée** : elle freine en montée, elle pousse en descente |

L'énergie à fournir aux roues sur un segment de longueur `d` :

```
E_mécanique = ( F_traînée + F_roulement + F_pente ) × d
```

## Les trois complications, dans l'ordre

**1. Les rendements ne sont pas symétriques.** La chaîne de traction perd environ 10 % à
l'aller. Mais en descente, quand la gravité fournit plus que ce que les résistances
consomment, la voiture **récupère** — et elle ne récupère qu'environ 60 % de ce
qu'elle pourrait.

```
si E_mécanique > 0 :  E_batterie = E_mécanique ÷ 0,90     ← on DIVISE
sinon              :  E_batterie = E_mécanique × 0,60     ← on MULTIPLIE
```

Écrire une seule formule pour les deux cas — l'erreur classique — donne une voiture qui
récupère 111 % de l'énergie de la descente. Un mouvement perpétuel.

**2. Les auxiliaires consomment par unité de temps, pas de distance.** Le chauffage tire
ses 500 W que la voiture avance ou non. Conséquence contre-intuitive : **il existe une
vitesse en dessous de laquelle ralentir coûte plus cher**. Avec ce véhicule, elle vaut
**29,9 km/h**.

**3. L'énergie disponible n'est pas la capacité de la batterie.** Une batterie pleine à
−5 °C ne rend que 80 % de ce qu'elle rend à 20 °C. Et on s'interdit de descendre à zéro :
on garde une réserve.

```
énergie_disponible = capacité × état_de_charge × facteur_température
budget_utilisable  = énergie_disponible − réserve_de_sécurité
```

## Un exemple chiffré

Un trajet de 125 km en quatre segments, avec un véhicule de 1 800 kg :

| # | Distance | Vitesse | Pente | Énergie | Cumul |
|---|---|---|---|---|---|
| 1 | 100 km | 110 km/h | 0 % | 17,2005 kWh | 17,2005 |
| 2 | 10 km | 90 km/h | **+3 %** | 2,9907 kWh | 20,1912 |
| 3 | 10 km | 90 km/h | **−3 %** | **−0,1244 kWh** | 20,0668 |
| 4 | 5 km | 50 km/h | 0 % | 0,4392 kWh | 20,5060 |

Deux choses à voir dans ce tableau :

- **10 km de montée à 3 % coûtent 2,99 kWh** — soit autant que 17 km de plat. La pente
  domine tout le reste.
- **Le cumul diminue** entre le segment 2 et le segment 3. La descente rend de l'énergie.
  Anodin ? Non : cela signifie que la consommation cumulée **n'est pas une suite
  croissante**, ce qui interdit un certain nombre d'optimisations qu'un développeur ferait
  spontanément.

## Le point d'autonomie

Reste à répondre à la question du conducteur. On parcourt les segments, on cumule, et on
cherche où le budget s'épuise :

| Conditions | Où le budget s'épuise |
|---|---|
| 20 °C, batterie à 40 % | **106,017 km** — au milieu du segment 2 |
| −5 °C, batterie à 40 % | **82,555 km** — dans le segment 1 |

**23,5 km de moins, pour 25 degrés d'écart, sans que rien d'autre ne change.** Le point
tombe rarement sur une frontière de segment : il faut interpoler à l'intérieur.

## Et ensuite : la recharge

Une fois qu'on sait où l'on s'arrête, il faut savoir **combien de temps**. Et là, une
seconde non-linéarité apparaît : la puissance de charge **décroît** à mesure que la
batterie se remplit — 150 kW jusqu'à la moitié, 35 kW au-delà de 80 %.

Il en découle un résultat que personne ne devine : **deux arrêts courts sont souvent plus
rapides qu'un seul arrêt long.** Faut-il l'appliquer sans le dire ? l'expliquer au
conducteur ? ou respecter son intuition au prix de quelques minutes ?

Ce n'est pas une question d'algorithme. C'est un arbitrage entre **optimalité et
acceptabilité**, il a des conséquences commerciales, et personne dans l'organisation ne
l'a jamais posé explicitement. Dans le système actuel, la réponse existe pourtant : elle
est enfouie dans une boucle, et elle a été choisie par un développeur.

---

# Partie 3 — Pourquoi ce cas, et ce qu'on va en faire

## C'est le détail de cet algorithme qu'on va formaliser

Pas le survoler : **le formaliser complètement**, jusqu'au dernier arrondi et au dernier
cas limite, jusqu'à ce que deux développeurs qui ne se parlent pas puissent l'implémenter
dans deux langages différents et obtenir exactement le même résultat.

Ce n'est pas un exercice de physique. **C'est un révélateur.**

## Ce que ce cas nous force à inventer

L'intérêt n'est pas dans la réponse — c'est dans les questions qu'on ne peut pas éviter en
chemin. Chacune impose un outil de méthode, et ces outils, eux, resservent partout.

| La question qu'on va rencontrer | Ce qu'elle nous force à construire |
|---|---|
| « Autonomie » veut dire deux choses selon l'interlocuteur. Comment on s'en sort ? | **Le glossaire**, les homonymes et les contextes délimités — [guide 2](../../guides/2-GLOSSAIRE.md) |
| Il y a douze calculs là-dedans. Lesquels sont des *fonctions* ? Comment on les nomme ? | **Le découpage** et les règles de nommage — [guide 1](../../guides/1-DECOUPER.md) |
| L'arrondi de l'autonomie : au plus proche ou vers le bas ? Qui décide ? | **Les faux amis** : ces points qui ressemblent à de la technique et n'en sont pas — [CADRE §1.5](../../CADRE.md) |
| On veut laisser le développeur choisir son algorithme d'optimisation. Comment sans perdre le contrôle du résultat ? | **Décrire un résultat, pas un parcours** — plus l'objectif, les contraintes et les règles de départage |
| « Précis à combien ? » n'a pas une réponse mais trois. | **Les trois niveaux d'exactitude** : reproductibilité, justesse, validité du modèle |
| Comment prouver que l'implémentation est juste, sans se comparer à elle-même ? | **L'oracle** : des formules fermées, calculables à la main, et un jeu d'essai qui en découle |
| Comment le développeur choisit-il son langage et son architecture ? | **La fiche de contraintes**, chiffrée en unités métier |
| Cinq valideurs métier différents doivent écrire en parallèle. Comment ? | **Une fonction = un fichier = un valideur métier** — [guide 3](../../guides/4-ECRIRE-A-PLUSIEURS.md) |
| Que remet-on exactement à l'équipe de développement ? | **Le dossier de passation** et son guide de lecture — [guide 4](../../guides/6-PASSER-AU-DEVELOPPEMENT.md) |

> **C'est là tout le pari du fil rouge.** On ne cherche pas à produire une bonne
> spécification d'autonomie de véhicule électrique — cela n'intéresse que les
> constructeurs. On cherche à ce que **l'effort de la produire fasse apparaître la
> méthode**, et les outils qui vont avec. Un cas réel, mené jusqu'au bout, enseigne ce
> qu'aucun exposé général n'enseigne.

## Pourquoi celui-là plutôt qu'un autre

| Critère | |
|---|---|
| **Tout le monde comprend la question** | « Est-ce que j'arrive ? » — aucune connaissance de domaine ne peut masquer une ambiguïté de rédaction |
| **Il y a de la vraie science** | Trois forces, deux rendements, une interpolation. Les équations sont celles du lycée, mais elles sont réelles et les chiffres sont justes |
| **C'est assez complexe** | Deux non-linéarités, un cumul non monotone, une optimisation sous contrainte |
| **Ça se découpe** | Douze fonctions, cinq valideurs métier — de quoi illustrer le travail collaboratif, pas seulement la rédaction |
| **Les enjeux sont clairs** | Une erreur laisse quelqu'un au bord de la route. On n'a pas besoin d'expliquer pourquoi la rigueur est nécessaire |

## Le chemin

| Étape | Ce qu'on y fait | Où |
|---|---|---|
| **0** | *Vous êtes ici* — comprendre le cas | cette page |
| **1** | Identifier et nommer les fonctions, désigner qui valide qu'elles sont justes | [1-DECOUPAGE.md](1-DECOUPAGE.md) |
| **2** | Fixer le vocabulaire commun | [2-GLOSSAIRE.md](2-GLOSSAIRE.md) |
| **3** | Voir une fonction arrêtée au niveau 3 — un état assumé, pas inachevé | [4-FN-004](4-FN-004-planifier-les-recharges.md) |
| **4** | La spécification complète, celle que le développement reçoit | [5-SPEC-NRG-001](5-SPEC-NRG-001.en.md) |

## Ce qu'on obtient à l'arrivée

**Deux choses, et la seconde vaut plus que la première.**

La première est une spécification que deux développeurs implémentent à l'identique, dans
deux langages, sur deux cibles — et le dépôt montre que ces deux cibles aboutissent
réellement à deux implémentations différentes, toutes deux conformes.

La seconde est **une méthode et un outillage réutilisables** : un modèle de spécification,
un modèle de fiche de fonction, un modèle de glossaire, une liste de vérification en
revue, un processus de revue à trois casquettes. Le prochain calcul à formaliser — dans
n'importe quel domaine — ne repartira pas d'une page blanche.

> Le véhicule électrique est le prétexte. La méthode est le produit.
