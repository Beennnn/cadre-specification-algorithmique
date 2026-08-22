# Fil rouge — L'autonomie d'un véhicule électrique

Tous les guides de ce dépôt s'appuient sur **le même scénario**. L'idée est simple :
plutôt que dix exemples jetables, un seul cas qu'on approfondit à mesure qu'on avance,
pour que chaque notion de la méthode se voie **appliquée sur un terrain déjà familier**.

## Pourquoi ce scénario

| Critère | Pourquoi celui-ci le remplit |
|---|---|
| **Ça parle à tout le monde** | « Est-ce que j'arrive à destination, et où dois-je m'arrêter ? » — pas besoin de connaître le domaine pour juger si la spécification est claire |
| **Il y a de la vraie science** | Bilan des forces, traînée aérodynamique, énergie, rendements, effet de la température. Les équations sont celles du lycée, mais elles sont réelles |
| **C'est assez complexe** | Non-linéarités (la récupération change la formule de signe), interpolation, condition d'arrêt, courbe de charge par paliers, optimisation sous contrainte |
| **Ça se découpe naturellement** | Une douzaine de fonctions, cinq valideurs métier différents — exactement ce qu'il faut pour illustrer le découpage et le travail collaboratif |
| **Les faux amis y sont spectaculaires** | Le sens de l'arrondi de l'autonomie, la réserve de sécurité, la stabilité d'un plan recalculé, deux arrêts courts contre un long |

## Ce que vous trouverez ici

| Fichier | Ce que ça illustre | Guide associé |
|---|---|---|
| **[0-LE-CAS-METIER.md](0-LE-CAS-METIER.md)** | **Le point d'entrée** : le cas expliqué simplement, puis techniquement, et pourquoi c'est en le formalisant qu'on construit la méthode | — |
| **[1-DECOUPAGE.md](1-DECOUPAGE.md)** | La sortie d'un atelier de découpage : 12 fonctions nommées, avec leur valideur métier et leur niveau de maturité | [Guide 1 — Découper](../../guides/1-DECOUPER.md) |
| **[2-GLOSSAIRE.md](2-GLOSSAIRE.md)** | Un glossaire de domaine réel, avec ses homonymes et ses contextes | [Guide 2 — Le glossaire](../../guides/2-GLOSSAIRE.md) |
| **[3-DONNEES.md](3-DONNEES.md)** | Le catalogue des données, leur source, leur cheminement — et les trois constats que l'exercice a produits | [Guide 3 — Les données](../../guides/3-DONNEES.md) |
| **[4-FN-004-planifier-les-recharges.md](4-FN-004-planifier-les-recharges.md)** | Une fiche de fonction arrêtée au **niveau 3** : contrat typé, pas encore de spécification. Un état assumé, pas un travail inachevé | [Guide 1 §4](../../guides/1-DECOUPER.md) |
| **[5-SPEC-NRG-001-autonomie.md](5-SPEC-NRG-001-autonomie.md)** | Une fonction montée au **niveau 4** : la spécification complète, avec ses équations et son jeu d'essai | [CADRE.md](../../CADRE.md) |

## Le parcours conseillé

0. Lisez **[le cas métier](0-LE-CAS-METIER.md)**. Tout part de là, et c'est la seule page
   à lire si vous n'en lisez qu'une.
1. Lisez le **découpage** : vous verrez à quoi ressemble un périmètre cartographié, et
   surtout que **toutes les fonctions ne sont pas au même niveau**.
2. Lisez le **glossaire** : c'est le socle partagé, et le seul artefact que tout le monde
   doit approuver.
3. Regardez la **fiche de niveau 3** : elle montre qu'on peut s'arrêter là, utilement.
4. Lisez la **spécification complète** : c'est le livrable final, celui que le
   développement reçoit.

> Les deux autres exemples du dépôt — [le montant à payer d'une
> lot](../mass-balance/spec/SPEC-MAS-001.en.md) et [le refroidissement d'une
> boisson](../SPEC-THM-001-refroidissement.md) — sont des **vignettes** : des
> spécifications courtes et complètes, gardées parce qu'elles aboutissent à des
> conclusions techniques opposées sur le type numérique. Elles ne font pas partie du
> fil rouge.
