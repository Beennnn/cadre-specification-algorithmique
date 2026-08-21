# SPEC-XXX-000 — <Titre : ce que ça calcule, en une ligne>

| | |
|---|---|
| **Identifiant** | SPEC-XXX-000 |
| **Version** | 0.1.0 |
| **Statut** | Brouillon / En revue / Acceptée / Abrogée |
| **Auteur métier** | |
| **Valideur métier** | |
| **Co-auteur technique** | |
| **Date d'effet** | AAAA-MM-JJ |
| **Glossaire de référence** | <lien> v0.0.0 |
| **Jeu de paramètres** | <référentiel> v0000.0 |
| **Dernière modification** | AAAA-MM-JJ |

<!--
MODE D'EMPLOI
- Copiez ce fichier, renommez-le, supprimez les commentaires au fur et à mesure.
- Aucune section n'est facultative. Une section sans contenu est un aveu utile :
  écrivez « néant » et pourquoi, plutôt que de la supprimer.
- Consultez CADRE.md §2 pour le pseudo-langage et §1.5 pour les pièges classiques.
-->

## 1. Objectif et contexte

<!-- Cinq lignes maximum : ce que ça calcule, pour qui, à quel moment.
     Le lecteur doit pouvoir décider en 30 secondes si ça le concerne. -->

## 2. Périmètre

**Dans le périmètre :**
-

**Hors périmètre :**
<!-- Aussi important que le précédent. Nommez ce que le lecteur pourrait
     raisonnablement croire inclus, et qui ne l'est pas. -->
-

## 3. Glossaire

| Terme | Définition |
|---|---|
| | |

<!-- Un terme = un sens, dans tout le document, dans le code et dans les tests. -->

## 4. Entrées

```
<nom> :
    <champ> : <Type>(<unité>, <précision>, <domaine>)      — facultatif ou non
```

**Préconditions :**
<!-- Ce qui est supposé vrai. Chaque précondition non tenue doit renvoyer à
     un cas d'erreur du §9. -->
-

## 5. Sorties

```
<nom> :
    <champ> : <Type>(<unité>, <précision>, <domaine>)
```

<!-- Pensez aux sorties exigées par l'auditabilité et l'explicabilité (§11) :
     un détail de calcul restituable est une sortie, pas un journal technique. -->

## 6. Paramètres

| Id | Libellé | Valeur | Unité | Qui peut le changer | Circuit de validation | Fréquence observée | Date d'effet |
|---|---|---|---|---|---|---|---|
| `P-01` | | | | | | | |

<!-- Un paramètre est une valeur qui peut changer sans que la logique change.
     Les trois colonnes « qui peut le changer / circuit / fréquence » sont lues par
     l'architecte : elles décident si la valeur vit dans le code ou dans un
     référentiel externe. Ne les laissez pas vides. -->

## 7. Règles

### 7.1 Chaîne de traitement
<!-- Le calcul vu comme une suite de boîtes. Chaque étape déclare ce qu'elle
     consomme, ce qu'elle produit (des noms NOUVEAUX, par immutabilité) et les
     règles qui la réalisent. Contrôlé par C-35 et C-36.
     Regrouper en `GR-xx` pour une vue de plus haut niveau.
     python3 outils/verifier.py --chaine <ce fichier> -->

| Étape | Consomme | Produit | Règles |
|---|---|---|---|
| `ET-01` <nom> | | | `RG-010` |

### 7.2 Grandeurs internes
<!-- Portée INTERNE : visibles dans le corps de cette fonction seulement.
     Jamais au contrat, jamais au catalogue des données. Décrites avec la même
     rigueur : famille de type, unité pivot, précision, plage.
     L'unicité du SENS reste globale : un nom ne désigne jamais deux notions. -->

```
<nom> : <Famille>(<unité pivot>, <précision>, <plage>)
```

### RG-010 — <titre de la règle>

```
<pseudo-langage>
```

<!-- Une règle = un identifiant stable, jamais réutilisé.
     Ajoutez une note « > Pourquoi » quand le choix n'est pas évident : c'est ce
     qui évite qu'on le remette en cause tous les six mois.
     Vérifiez : chaque SI a son SINON ; chaque table de décision est complète ;
     les arrondis, les ex æquo et les valeurs absentes sont traités. -->

### RG-020 — <titre>

```
```

## 8. Invariants

| Id | Propriété |
|---|---|
| `INV-01` | |

<!-- Ce qui reste vrai pour TOUTES les entrées, pas seulement celles du jeu d'essai.
     Ces propriétés se testent automatiquement sur des entrées générées. -->

## 9. Cas d'erreur métier

| Code | Condition | Conséquence | Message destiné à l'utilisateur |
|---|---|---|---|
| `E-XXX-001` | | | |

<!-- « Conséquence » : rien n'est retourné ? un résultat partiel ? lequel ? -->

## 10. Jeu d'essai

<!-- Résultats calculés À LA MAIN. Jamais produits par une implémentation. -->

### Vue d'ensemble

| Id | Ce qu'il exerce | Résultat attendu |
|---|---|---|
| `CT-01` | cas nominal | |
| `CT-02` | cas aux limites | |
| `CT-03` | cas riche, avec trace de calcul détaillée | |
| `CT-04` | cas d'erreur | |

### CT-01 — <titre>

<!-- Entrées, puis résultat. -->

### CT-03 — <titre> (trace de calcul complète)

<!-- Étape par étape, chaque valeur intermédiaire écrite, avec la règle citée.
     C'est le document auquel toute l'équipe reviendra en cas de doute. -->

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-010` | |

<!-- Une règle non couverte est soit inutile, soit un test manquant.
     Dans les deux cas, nommez-le plutôt que de le cacher. -->

## 11. Contraintes et exigences

### 11.1 Contraintes métier

<!-- Chiffré, en unités métier. C'est ce qui permet à l'IT de choisir le langage
     et l'architecture. « Il faut que ce soit rapide » n'est pas une contrainte. -->

| Dimension | Contrainte métier |
|---|---|
| **Volumétrie** | nombre d'exécutions par jour ; taille typique et maximale d'une entrée ; croissance à 3 ans |
| **Profil de charge** | charge moyenne, charge de pointe (facteur et durée), saisonnalité |
| **Mode d'appel** | à la demande / par lots à heure fixe / sur événement |
| **Latence** | temps acceptable, **et ce qui se passe si on le dépasse** |
| **Fraîcheur** | à quelle date les données sont arrêtées ; délai toléré |
| **Exactitude** | précision requise, mode d'arrondi, tolérance sur un écart |
| **Déterminisme** | deux exécutions identiques doivent-elles donner exactement le même résultat ? |
| **Rejouabilité** | faut-il recalculer un résultat passé à l'identique ? sur quelle profondeur ? |
| **Auditabilité** | faut-il justifier le résultat pas à pas ? à qui ? pendant combien de temps ? |
| **Explicabilité** | faut-il pouvoir dire à l'utilisateur *pourquoi* il obtient ce résultat ? |
| **Criticité et mode dégradé** | que fait-on si une donnée ou un service dont dépend le calcul est indisponible ? |
| **Confidentialité** | données personnelles ou sensibles manipulées ? lesquelles ? |
| **Conformité** | textes ou normes applicables, obligations de conservation |
| **Fréquence de changement** | combien de fois par an la règle change-t-elle ? et les paramètres ? |
| **Qui modifie** | le métier doit-il pouvoir changer une valeur sans livraison logicielle ? |
| **Durée de vie** | combien de temps ce calcul est-il censé vivre ? |

### 11.2 Exigences de réalisation

<!-- Contraintes IMPOSÉES par l'organisation, qui restreignent le choix du
     développement — à distinguer des contraintes métier ci-dessus, qui l'éclairent.
     L'expert métier ne les invente pas : il les reçoit et les intègre.

     Une exigence SANS SOURCE NOMMÉE n'est pas une exigence, c'est une préférence
     d'équipe déguisée. Une exigence sans moyen de vérification est décorative.

     Familles : classification et segmentation des données · langages et bibliothèques
     · règles de codage · plate-forme cible · intégration et exploitation
     · conformité et certification. -->

| Id | Exigence | Source | Qui la valide | Vérification |
|---|---|---|---|---|
| `EX-01` | | | | |

<!-- Un conflit entre une exigence de réalisation et une contrainte métier ne se
     règle pas ici : il devient une question ouverte, arbitrée par les deux
     valideurs. -->

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| `Q-01` | | | | Ouverte |

<!-- Une question tranchée reste dans le tableau, avec sa décision et sa date.
     Une question qui disparaît sans trace est une décision perdue. -->

## 13. Historique et notices de changement

| Version | Date | Changement | Impact sur les résultats | Notice |
|---|---|---|---|---|
| 0.1.0 | | Version initiale | — | — |

<!-- Toute ligne déclarant un impact — sur un résultat OU sur un contrat — renvoie
     à une notice N-<version> ci-dessous. Contrôlé par C-24. -->

### N-0.0.0 — <titre du changement>

**Raison.** <deux phrases : ce qui n'allait pas, et ce que le changement corrige.
Le problème, pas la solution. Si c'est une demande, dire de qui et pourquoi.>

**Fonctions impactées**

| Fonction | Nature de l'impact |
|---|---|
| `FN-000` | comportement / contrat / **aucun** *(une fonction réexaminée sans impact figure quand même)* |

**Impacts sur les contrats**

| Fonction | Élément | Nature | Détail | Compatibilité |
|---|---|---|---|---|
| `FN-000` | `<entrée ou sortie>` | ajout / modification / suppression | | compatible / **rupture** |

**Conséquences**

| | |
|---|---|
| **Rejeu** | nécessaire / non nécessaire, et pourquoi |
| **Date d'effet** | |
| **Consommateurs à prévenir** | |

<!-- MAJEUR : un résultat change. MINEUR : un cas nouveau est couvert sans
     changer l'existant. CORRECTIF : clarification sans impact. -->
