# Cadre de spécification algorithmique métier

> Le métier écrit la loi. L'IT écrit la machine.

Ce dépôt décrit une méthode pour que les personnes qui **détiennent la connaissance
métier et algorithmique** cessent de produire du code, et produisent à la place des
**spécifications en pseudo-langage** : assez précises pour qu'un développeur les
implémente sans deviner, assez libres pour qu'il choisisse le langage, les structures
de données et l'architecture.

## Par où commencer

| Fichier | À qui | Quoi |
|---|---|---|
| **[CADRE.md](CADRE.md)** | tout le monde | Le document de référence : le principe, la frontière métier/technique, le pseudo-langage, la fiche de contraintes, le processus, les anti-patterns. **C'est le document à lire en premier.** |
| **[exemples/SPEC-PRX-001-montant-a-payer.md](exemples/SPEC-PRX-001-montant-a-payer.md)** | tout le monde | Exemple **gestion** : une spécification complète sur un cas que tout le monde comprend, *le calcul du montant à payer d'une commande*. |
| **[exemples/SPEC-THM-001-refroidissement.md](exemples/SPEC-THM-001-refroidissement.md)** | profils scientifiques | Exemple **scientifique** : *le refroidissement d'une boisson chaude* — un phénomène que tout le monde connaît, calculé avec une loi croisée au lycée. Illustre les trois adaptations du cadre au calcul numérique. |
| **[templates/MODELE-SPECIFICATION.md](templates/MODELE-SPECIFICATION.md)** | auteur métier | Le squelette vierge à copier pour écrire une nouvelle spécification. |
| **[templates/CHECKLIST-RELECTURE.md](templates/CHECKLIST-RELECTURE.md)** | relecteurs | Ce qu'on vérifie en revue, par casquette (métier, technique, test). |

## Le pari en trois phrases

1. Un expert métier qui code passe 80 % de son énergie sur des questions qui ne sont
   pas les siennes (langage, performance, industrialisation) et 20 % sur ce que lui
   seul sait — on inverse le ratio.
2. Une règle métier n'est pas ambiguë parce qu'elle est écrite en français : elle est
   ambiguë parce qu'il lui manque une unité, un arrondi, un « sinon » ou un cas de
   départage. Le cadre force à les écrire.
3. Le développeur ne peut choisir un langage et une architecture que si on lui donne
   les contraintes en *unités métier* : volumes, latence, précision, rejouabilité,
   fréquence de changement de la règle. Le cadre force à les chiffrer.

## Deux exemples, deux conclusions opposées

Les deux exemples sont issus de domaines volontairement éloignés, parce que **la même
méthode y conduit à des décisions techniques contraires** — la meilleure preuve que c'est
bien la spécification qui décide, et non l'habitude du développeur.

| | Gestion (`SPEC-PRX-001`) | Scientifique (`SPEC-THM-001`) |
|---|---|---|
| Exigence d'exactitude | exacte au centime | tolérance relative |
| Type numérique qui en découle | **décimal exact obligatoire** | **double précision suffisante** |
| Critère d'acceptation | égalité stricte | reproductibilité, justesse numérique, validité du modèle |
| Piège central traité | le centime résiduel d'une répartition | l'ordre d'ajout du lait — deux tiers de degré |
| Ce que la liberté permet | jointure, mémoire ou cache | forme fermée **ou** intégrateur — les deux conformes |

## Critère d'acceptation d'une spécification

> **Le test de la double implémentation.** Deux développeurs qui ne se parlent pas,
> dans deux langages différents, produisent des programmes qui donnent **le même
> résultat** sur l'intégralité du jeu d'essai — et sur les cas auxquels personne
> n'avait pensé.

Si ce n'est pas vrai, la spécification n'est pas finie.

Sur des grandeurs continues, ce critère se dédouble en **reproductibilité** (deux
implémentations conformes donnent le même nombre) et **justesse** (le nombre est proche
de la vérité physique) — voir `CADRE.md` §2.8.
