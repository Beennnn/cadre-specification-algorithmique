# Guide 9 — Ce que l'IA apporte, et ce qu'elle ne peut pas apporter

*Transverse. Une IA peut intervenir à presque toutes les étapes ; elle ne décide à aucune.*

---

## Le principe, qui ne change pas

> **Une IA n'est jamais un valideur.** Elle instruit, elle propose, elle signale. La
> signature reste humaine, datée, nominative — et **la personne qui a lancé la passe
> assume ce qu'elle en a retenu comme si elle l'avait écrit**.

C'est le principe de [l'humain aux commandes](../REFERENCES.md). Tout ce guide s'y
subordonne : aucun des usages ci-dessous ne déplace la responsabilité.

## Là où elle aide vraiment : le contrat

C'est son terrain le plus rentable, parce que c'est un travail **exhaustif et fastidieux**
que l'humain fait mal — non par incompétence, mais parce qu'il fatigue. Une IA ne se lasse
pas de vérifier que le vingt-troisième paramètre sert à quelque chose.

**Sur la cohérence entrées / sorties :**

| Ce qu'elle détecte | Pourquoi ça compte |
|---|---|
| Une **entrée déclarée qu'aucune règle n'emploie** | Soit une règle manque, soit on fait fournir une donnée pour rien |
| Une **sortie promise que rien ne produit** | Le contrat annonce un résultat que le calcul ne fabrique pas |
| Une **grandeur employée que rien ne déclare** | Un fantôme circule : le développeur devra inventer d'où il vient. C'est l'inverse exact du cas précédent — là on déclare sans employer, ici on emploie sans déclarer |
| Un **paramètre déclaré et jamais employé** | Maintenu, versionné, documenté — pour rien. Et un jour quelqu'un le changera en croyant agir |
| Une **grandeur sans unité, sans plage, sans précision** | La cause la plus fréquente d'écarts, et la plus tardive à se découvrir |
| Une **incohérence de vocabulaire** entre le contrat, les règles et le glossaire | Le lecteur cherche une nuance qui n'existe pas, et finit par en inventer une |

**Sur la complétude logique :** un `SI` sans `SINON`, une table de décision incomplète, un
arrondi sans sens déclaré, un superlatif sans règle de départage, une entrée facultative
dont l'absence n'est pas traitée, une itération sans nombre maximal.

**Sur ce qui n'a rien à faire là :** une **contrainte qui relève de l'implémentation**,
glissée dans la spécification — un nom de bibliothèque, une structure de données, « stocker
dans une table », « mettre en cache », « boucler sur la liste ». Ce sont des décisions qui
appartiennent au développement, et elles se repèrent bien mieux à la lecture qu'avec une
liste de mots-clés (`C-40`).

**Sur les besoins pas assez clairs :** les formulations qui repoussent la décision —
« etc. », « le cas échéant », « en général », « si nécessaire », « on gère les cas
particuliers », « comme d'habitude ». Chacune est une question que quelqu'un devra
trancher plus tard, sans mandat.

```bash
# la passe outillée d'abord, l'IA ensuite : voir guide 5
python3 outils/verifier.py <la spécification>
```

Puis la consigne prête à l'emploi : [`outils/PROMPT-RELECTURE-IA.md`](../outils/PROMPT-RELECTURE-IA.md).

## Ses apports, étape par étape

| Étape | Ce qu'elle peut faire | Qui décide |
|---|---|---|
| **2–3 Découper, nommer** | Proposer un découpage candidat à partir de documents existants ; repérer les verbes fourre-tout (« gérer », « traiter ») et les noms qui désignent un endroit plutôt qu'un résultat ; proposer des termes de glossaire | Le métier — elle ignore qui décide de quoi dans l'organisation |
| **3 Données** | Repérer les données citées sans source, sans unité, sans comportement d'absence | Le métier |
| **4 Écrire** | Reformuler de la prose en pseudo-langage ; transformer des `SI` imbriqués en table de décision ; signaler une boucle qui pourrait être une opération d'ensemble | Le métier — **elle ne fournit jamais la règle** |
| **5 Jeu d'essai** | **Proposer les cas aux limites qu'on n'a pas pensés** : zéro, vide, un seul élément, valeur exactement au seuil, ex æquo, demi-centime, bascule de date, dépassement de plafond. Calculer les résultats attendus | Le métier — les résultats restent des **candidats jusqu'à examen** |
| **7–8 Valider** | Tout ce qui précède, en une passe exhaustive | Les trois relecteurs |
| **10 Coder et tester** | Produire une implémentation candidate ; appliquer les **règles de codage** de l'équipe ; écrire **commentaires et documentation** ; poser les **annotations de traçabilité** vers la spécification et en vérifier la cohérence ; transformer chaque `CT-xx` en test nommé ; dériver des tests de propriété depuis les invariants — voir ci-dessous | Le développeur, qui assume l'architecture, la qualité dans la durée et ce qu'il livre |
| **11 Écarts** | Comparer obtenu et attendu **poste par poste le long de la chaîne**, localiser le premier point de divergence, proposer des hypothèses de cause | Le métier — **elle ne juge pas si l'écart est significatif** |
| **Figer les jeux** | **Une fois que le métier les a jugés valides** : figer les jeux de données, les formater, les versionner, générer la trace de provenance et de validation | Le métier juge **avant** ; l'IA fige **après**. C'est ce jugement, et lui seul, qui transforme un jeu de données en donnée de référence |

> **L'ordre compte, sur la dernière ligne.** L'IA ne fige rien de sa propre initiative :
> elle attend que le métier ait examiné et accepté les résultats, puis elle exécute le
> travail mécanique qui s'ensuit — formater, versionner, écrire la trace. Figer avant le
> jugement reviendrait à graver dans le marbre ce qui n'a pas encore été validé, et à
> donner à un jeu de données l'autorité d'un engagement qu'il n'a pas.

> **Le meilleur apport de la liste est celui de l'étape 5.** Proposer les cas aux limites
> est exactement ce qu'un expert fait mal : il connaît trop bien son domaine pour en voir
> les trous. Une IA n'a pas ce biais — elle n'a pas d'intuition à protéger.

## Sur le code : conventions, commentaires, et lien avec la spécification

Quatre usages, tous du côté du **dépôt de code** — jamais dans la spécification, qui ignore
jusqu'à l'existence du code.

### Faire respecter les règles de codage

Appliquer la convention de l'équipe de façon **uniforme** : nommage, structure, style,
organisation des fichiers. Et en particulier la **correspondance des noms** depuis le
`snake_case` de la spécification vers la convention du langage — `montant_net_ht` →
`montantNetHt` en Java, `MontantNetHt` en C#, inchangé en C ou en Python
([CADRE §2.4](../CADRE.md)).

C'est un travail systématique, sans jugement à porter : exactement ce qu'une IA fait bien
et qu'un relecteur humain fait de moins en moins bien à mesure que le fichier s'allonge.

### Écrire des commentaires et une documentation qui servent

Un bon commentaire ne répète pas le code : il dit **pourquoi**. Et le « pourquoi », dans
cette méthode, est déjà écrit — ce sont les notes de justification des règles
(« *Pourquoi « le plus petit » et pas « le prix promotionnel »* »).

> **L'IA fait ici un transfert, pas une invention** : elle reporte dans le code la
> justification que le métier a écrite dans la spécification. C'est ce qui évite qu'un
> développeur, dans trois ans, « simplifie » une règle dont il ne voit plus la raison.

Elle aide de même à produire la documentation d'usage du composant — ce qu'il calcule, ce
qu'il exige, ce qu'il garantit — en la dérivant du **rôle** et du **contrat**, sans les
paraphraser.

### Poser les annotations de traçabilité

Le code cite la spécification, **jamais l'inverse** ([guide 1](1-DECOUPER.md)) :

- l'identifiant `RG-xxx` en commentaire, à l'endroit qui l'implémente ;
- l'identifiant `CT-xx` dans le nom ou la description du test correspondant ;
- la **version de spécification** que le composant déclare implémenter.

L'IA sait poser ces annotations à grande échelle, et surtout les **maintenir** quand le
code bouge — c'est là qu'elles se perdent habituellement.

### Vérifier la cohérence de ces liens

Le contrôle se fait dans les **deux sens**, et il tourne dans l'intégration continue du
dépôt de code :

| Sens | Ce qu'on vérifie | Ce qu'un échec révèle |
|---|---|---|
| **Spécification → code** | Toute `RG-xxx` est citée au moins une fois dans le code **et** dans un test | Une règle non implémentée, ou implémentée sans être testée |
| **Code → spécification** | Toute citation référence une règle qui **existe encore** dans la version courante | Une citation périmée : règle abrogée, ou identifiant qui a changé |
| **Version** | La version déclarée par le composant est bien celle qu'on croit implémenter | Un composant qui applique une version antérieure sans que personne ne le sache |
| **Couverture** | Chaque `CT-xx` du jeu d'essai a un test qui le rejoue | Une donnée de référence qui ne protège plus rien |

> **Une annotation est un pointeur, pas une preuve.** Qu'un `RG-010` soit cité à côté d'un
> bloc de code n'établit en rien que ce bloc applique la règle : seuls les tests contre les
> **données de référence** l'établissent. L'annotation sert à *retrouver*, pas à *garantir*
> — et il faut le savoir, sinon elle donne une fausse confiance.

## Ce qu'elle ne peut pas faire

Ces limites ne sont pas des défauts de maturité. Elles sont **structurelles**.

| | |
|---|---|
| **Dire si la règle est la bonne règle** | Une règle peut être parfaitement claire, cohérente et complète — **et fausse**. Que la remise démarre à 3 articles quand la politique commerciale en vigueur dit 4 ne se lit dans aucun document : cela ne se sait que du métier (`H-06`) |
| **Juger qu'un écart est acceptable** | C'est un arbitrage entre des conséquences, pas un calcul |
| **Voir ce dont personne n'a jamais parlé** | **C'est la limite la plus importante** — voir l'explication ci-dessous |
| **Assumer** | Elle n'a pas de mandat et ne rend de comptes à personne |

### La limite qui compte : un trou ne se voit que s'il a des bords

Une IA repère une **incohérence entre deux choses écrites**. Elle ne repère pas l'absence
d'une troisième dont personne n'a jamais parlé.

| | |
|---|---|
| **Un trou avec des bords — elle le voit** | Un `SI` sans `SINON` : le `SI` est écrit, son incomplétude est visible dans le texte. Une entrée déclarée qu'aucune règle n'emploie : les deux bouts sont là, c'est leur rapport qui cloche |
| **Un trou sans bords — elle ne le voit pas** | Personne ne s'est jamais demandé *« et si deux produits ont exactement le même prix ? »*. Alors aucune règle ne parle d'égalité, le glossaire n'a pas de terme pour ça, aucun cas de test n'en approche. **Le document est parfaitement cohérent** — il lui manque simplement une question que personne n'a posée |

C'est structurel, et aucun progrès des modèles n'y changera rien : **on ne peut pas
détecter l'absence de ce qui n'a jamais existé nulle part**. L'IA travaille sur ce qui est
écrit.

Ce qui trouve les trous sans bords, ce sont deux choses, et aucune n'est automatisable :

- **l'atelier de découpage**, où un développeur demande « et si… ? » et où personne n'a la
  réponse — c'est ainsi que `FN-012` est apparue sur le fil rouge ;
- **la relecture technique** de l'étage 3, où quelqu'un qui doit coder se heurte au vide et
  compte les fois où il aurait dû deviner.

## Trois pièges, et ce qui les désamorce

| Piège | Ce qui le désamorce |
|---|---|
| **La plausibilité** — elle est convaincante même quand elle a tort | Exiger que **chaque constat cite un passage exact** du document, et le vérifier avant de le traiter |
| **Le biais d'automatisation** — à force qu'elle ait raison, on cesse de vérifier | Le même exigence, appliquée sans exception. Et compter les faux positifs : leur disparition totale est suspecte |
| **Le fusible moral** — faire « valider » par un humain une sortie qu'il ne peut pas réellement examiner | Lui donner le temps, la compétence et le **droit de dire non**. Sans ces trois conditions, on n'a pas créé de responsabilité : on a désigné un coupable |

## Ce qu'on trace

Une passe d'IA se déclare, comme tout le reste :

| | |
|---|---|
| **Quel modèle**, quelle version du catalogue de règles | pour pouvoir rejouer et comparer |
| **Qui l'a lancée**, et quand | c'est la personne qui assume ce qui en a été retenu |
| **Combien de constats** retenus, écartés, et pourquoi | les faux positifs récurrents signalent une **règle mal formulée**, pas une IA défaillante |

C'est l'objet du contrôle `C-27`. Une relecture par IA non tracée n'a pas eu lieu.
