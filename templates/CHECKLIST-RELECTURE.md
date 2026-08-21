# Liste de vérification en revue

> Trois relecteurs, trois casquettes. Une spécification relue par une seule population
> n'est relue par personne.

**Avant d'ouvrir cette liste**, l'étage 1 de la validation doit être vert :

```bash
python3 outils/verifier.py <la spécification>
```

Les contrôles mécaniques `C-01` à `C-23` attrapent les entrées mortes, les paramètres
fantômes, les règles non couvertes et les versions incohérentes. Cette liste-ci ne sert
qu'à ce qu'eux ne savent pas voir — voir [guide 5](../guides/5-VALIDER.md).

---

## Relecteur métier — « est-ce juste et complet ? »

- [ ] Le glossaire couvre tous les termes du domaine employés dans le document, et un
      terme n'a qu'un seul sens.
- [ ] Le hors-périmètre nomme explicitement ce qu'un lecteur pourrait raisonnablement
      croire inclus.
- [ ] Chaque règle correspond à une décision réellement prise, par quelqu'un
      d'identifiable — pas à une habitude ou à ce que fait le système actuel.
- [ ] Les choix non évidents portent une note « pourquoi ».
- [ ] Chaque paramètre nomme qui peut le changer, et selon quel circuit de validation.
- [ ] Les questions ouvertes ont un décideur et une échéance.
- [ ] Les cas d'erreur décrivent une **décision métier** (refuser, dégrader, valeur par
      défaut), pas un comportement technique.

## Relecteur métier — les huit pièges (CADRE.md §1.5)

- [ ] **Arrondis** — à quelle étape, combien de décimales, quel sens, et **où va le
      centime résiduel** ?
- [ ] **Ordre des opérations** — si l'ordre change le résultat, est-il écrit ? justifié ?
- [ ] **Ex æquo** — chaque « le plus petit », « le meilleur », « le premier » a-t-il une
      règle de départage ?
- [ ] **Valeurs absentes** — pour chaque entrée facultative : rejet, défaut, ignorée ou
      dégradation ?
- [ ] **Temps** — fuseau, calendrier, heure de bascule, date qui fait foi ; et lors d'un
      rejeu, règles d'aujourd'hui ou de l'époque ?
- [ ] **Unités et devises** — toute grandeur porte la sienne ; les conversions ont une
      source, une date et un arrondi.
- [ ] **Bornes** — `≥` ou `>` ? un plafond écrête-t-il ou rejette-t-il ? une itération
      a-t-elle un maximum, et que se passe-t-il si on l'atteint ?
- [ ] **Indisponibilité** — que fait-on si une dépendance tombe ? (c'est une décision
      commerciale, pas d'exploitation)

## Relecteur métier — trois pièges supplémentaires en calcul scientifique

<!-- À ne cocher que si la spécification manipule des grandeurs continues,
     un traitement du signal, un calcul itératif ou une simulation. -->

- [ ] **Interpolation** — pour toute grandeur intégrée, dérivée ou interpolée sur des
      données échantillonnées : la convention est-elle déclarée ? (« l'aire sous la
      courbe » n'existe pas tant qu'on n'a pas dit ce que vaut le signal entre deux points)
- [ ] **Convergence** — pour tout calcul itératif : critère d'arrêt, nombre maximal
      d'itérations, **et ce qui est rapporté en cas de non-convergence** ?
- [ ] **Aléatoire** — pour toute étape stochastique : l'algorithme du générateur et la
      graine figurent-ils dans les entrées de la spécification ?
- [ ] **Traitements spécifiés par leur méthode** — un filtre, un ajustement ou une
      régularisation nommés explicitement sont des choix d'implémentation déguisés en
      règles : sont-ils reformulés en propriétés vérifiables, avec leur protocole de
      vérification ?
- [ ] **Trois exactitudes** — la spécification distingue-t-elle la **reproductibilité**
      (entre implémentations, contre sa propre définition), la **justesse numérique**
      (contre une solution analytique) et la **validité du modèle** (contre des mesures) ?
      Chacune a-t-elle son chiffre et son moment de contrôle ?
- [ ] **Asymptotes et domaines de validité** — une grandeur qui tend vers une limite sans
      l'atteindre rend certaines questions solubles et absurdes : la marge qui tranche
      est-elle posée par le métier ?
- [ ] **Invariants de symétrie** — translation de l'origine, changement d'unité,
      permutation des entrées : sont-ils écrits ?

## Co-auteur technique — « est-ce implémentable, et à quel coût ? »

- [ ] Toutes les entrées et sorties sont typées, avec unité, précision et domaine.
- [ ] **Aucun** nom de langage, de bibliothèque, de base de données, de structure de
      données technique n'apparaît dans le document.
- [ ] Aucune performance exprimée en prose : tout est chiffré au §11.
- [ ] Chaque `SI` a son `SINON` ; chaque table de décision couvre toutes les
      combinaisons, et chacune exactement une fois.
- [ ] Les boucles explicites sont justifiées, avec critère d'arrêt et nombre maximal
      d'itérations. Partout ailleurs, la spécification décrit un résultat, pas un parcours.
- [ ] La fiche de contraintes est **complète et chiffrée** — en particulier :
      exactitude, déterminisme, rejouabilité, fréquence de changement, qui modifie.
- [ ] Les contraintes sont réalistes : si l'une d'elles multiplie le coût par dix, c'est
      dit **maintenant**, avec une alternative chiffrée.
- [ ] Rien dans le document ne présuppose une solution technique.

## Relecteur test — « est-ce vérifiable ? »

- [ ] Le jeu d'essai contient au moins : un cas nominal, un cas riche avec trace de
      calcul détaillée, les cas aux limites, les cas d'erreur.
- [ ] Chaque résultat attendu a été **examiné et accepté** par une personne du métier, et
      la façon dont il l'a été est écrite : calcul analytique, calcul à la main, maquette,
      ou analyse statique et dynamique du composant. Aucun résultat n'a été accepté
      **parce qu'un programme l'a produit**.
- [ ] La table de couverture existe, et chaque `RG-xxx` y figure — les cases vides sont
      nommées et suivies.
- [ ] Les invariants sont écrits sous une forme testable sur des entrées quelconques.
- [ ] Les cas aux limites incluent au moins : la valeur exactement égale à chaque seuil,
      un ex æquo, un arrondi à la demi-unité, une collection à un seul élément.
- [ ] Les messages d'erreur sont utilisables tels quels, ou explicitement marqués comme
      indicatifs.

---

## Le test final, à faire mentalement avant d'approuver

> **Je suis développeur, je ne connais pas ce domaine, et je dois coder ça demain
> matin sans pouvoir poser de question à personne. Combien de fois vais-je devoir
> deviner ?**

Chaque « je devinerais ici » est un commentaire de revue.
Si la réponse est zéro, la spécification est acceptée.
