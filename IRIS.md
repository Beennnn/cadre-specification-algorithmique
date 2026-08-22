# Ce dépôt et Iris

*Iris documente déjà **pourquoi** chaque choix technique a été fait. Ce dépôt fait la même
chose pour l'autre moitié : les décisions métier.*

---

## Iris, en deux lignes

[Iris](https://github.com/iris7-app) est un système de gestion client mené comme un vrai
système industriel : un backend Spring Boot 4 / Java 25, un jumeau Python en FastAPI, une
interface Angular 21, sur GKE Autopilot éphémère, avec une chaîne GitLab complète —
Testcontainers, SBOM, signature cosign, Sonar, Semgrep, Trivy, tests de mutation PIT — et
un GitOps Argo CD.

> **La source de vérité est [GitLab](https://gitlab.com/iris-7).** Les dépôts GitHub sont
> des miroirs en lecture seule. Ce qui suit vaut pour la méthode, quel que soit l'hôte.

---

## Trois points d'accroche, et ils sont déjà là

### 1. Iris fait tourner le test de la double implémentation. En production.

`iris-service-java` et `iris-service-python` sont deux implémentations **des mêmes
contrats**, dans deux langages, par deux chaînes de construction distinctes.

> **C'est très exactement le critère d'acceptation de cette méthode** ([CADRE
> §1.3](CADRE.md)) : *deux développeurs qui ne se parlent pas, dans deux langages
> différents, produisent des programmes qui donnent le même résultat — y compris sur les
> cas auxquels personne n'avait pensé.*

Iris a donc déjà le dispositif. Ce qui lui manque, c'est **l'oracle** : le couple (entrées,
sorties) validé par le métier, contre lequel les deux implémentations se confrontent. Sans
lui, un écart entre Java et Python se tranche par une discussion — avec lui, il se tranche
par une donnée de référence datée et opposable ([CADRE §5](CADRE.md)).

Et si les deux divergent un jour, l'expérience de ce dépôt est instructive : en confrontant
ses **propres** outils, vérificateur Python et vérificateur Java, sur un corpus de défauts
connus, les deux divergeaient sur six points — et **chacun avait tort sur certains**. C'est
exactement ce que le test est censé produire.

### 2. Iris a 64 ADR. Il n'a aucun équivalent pour les décisions métier.

Le principe est déjà admis chez vous, et il est écrit noir sur blanc dans le profil de
l'organisation : *« every non-obvious choice has a written justification and revisit
conditions »*, pour que **le « pourquoi » survive au mainteneur suivant**.

Mais un ADR répond à *« pourquoi Kustomize plutôt que Helm »*. Aucun ne répond à :

- à quelle étape arrondit-on, dans quel sens, et **où va le reste** ?
- deux enregistrements sont à égalité — lequel retient-on, et pourquoi celui-là ?
- que fait-on d'une donnée absente : rejet, valeur par défaut, ligne ignorée, dégradation ?
- ce seuil est-il atteint à `≥` ou à `>` ?

| | **ADR** | **Spécification algorithmique** |
|---|---|---|
| Répond à | pourquoi cette **technique** | pourquoi ce **résultat** |
| Décidé par | l'architecte | le valideur métier |
| Se périme quand | le contexte technique change | la règle métier change |
| Sans lui | on refait le débat technique tous les six mois | **le développeur tranche à la place du métier, sans le savoir** |

**Les deux artefacts sont symétriques, et ce dépôt est celui qui manque.** La discipline
est la même : un identifiant stable, une justification écrite, des conditions de révision.

### 3. Sonar et PIT mesurent si le code est testé. Rien ne mesure si la spec est couverte.

La chaîne Iris est sérieuse sur la qualité du code : couverture à 90 %, mutation testing
PIT, Sonar, Semgrep. Toutes ces mesures répondent à *« le code est-il éprouvé ? »*.

Aucune ne répond à *« ce que le métier a demandé est-il implémenté ? »* — une exigence que
personne n'a codée n'apparaît dans aucun rapport de couverture, parce qu'il n'y a pas de
ligne à couvrir.

C'est ce que produit [`outils/Couverture.java`](outils/Couverture.java) : il croise les
points de la spécification et les annotations `@ImplementsSpec` du code, et signale les deux
sens — l'exigence que rien n'implémente, et le code qui se réclame d'une exigence que la
spécification ignore. Il rend `1` en cas d'échec : utilisable en CI tel quel, à côté de
Sonar.

---

## Ce que l'architecture attend du besoin

C'est la **fiche de contraintes** ([CADRE §4](CADRE.md)), et chaque ligne commande une
décision qu'Iris a déjà eu à prendre :

| Ce que le métier chiffre | Ce que l'architecture en déduit | Chez Iris |
|---|---|---|
| L'**exactitude** exigée, en unités métier | décimal exact ou flottant | le type des montants et des agrégats |
| Les **volumes** et la **latence** acceptable | dimensionnement, cache Redis, lots ou fil de l'eau | le dimensionnement GKE, la stratégie Kafka |
| La **rejouabilité** et la durée de conservation | stockage, immutabilité, archivage | la stratégie PostgreSQL, la rétention des événements |
| La **fréquence de changement** d'une règle | paramètre plutôt que code | ce qui mérite un *feature flag* Unleash plutôt qu'un déploiement |
| Le **mode dégradé** quand une dépendance tombe | résilience, délais, reprises | ce que Chaos Mesh doit précisément valider |
| Les **dépendances entre étapes** | ce qui est parallélisable | les niveaux d'exécution, dérivés de la chaîne |

**Aucune de ces lignes ne peut être remplie par un architecte seul.** Combien de temps un
résultat doit rester reproductible, quelle erreur est acceptable, ce qu'on fait quand une
donnée manque : ce sont des décisions métier.

> **La démonstration que ce dépôt en donne.** Le [bilan de masse](exemples/mass-balance/) et
> le [refroidissement](exemples/cooling/spec/SPEC-THM-001.en.md) aboutissent à des
> conclusions **opposées** sur le type numérique — décimal exact obligatoire d'un côté,
> double précision largement suffisante de l'autre. Ce n'est pas l'habitude du développeur
> qui décide : c'est l'exigence chiffrée. Sans elle, les deux auraient reçu le même type, et
> l'un des deux aurait été faux.

---

## Comment le brancher, concrètement

La méthode ne s'applique qu'aux traitements **algorithmiques** : elle n'apporte rien à un
CRUD, à une intégration ou à un mapping. Sur Iris, les candidats sont les calculs qui
existent **en double**, Java et Python, et dont un écart serait coûteux à trancher.

| Étape | Ce qu'il y a à faire | Coût |
|---|---|---|
| **1 — Un pilote** | Prendre **un** calcul déjà présent dans les deux services, et écrire sa spécification. Le [gabarit](templates/MODELE-SPECIFICATION.md) et l'[exemple de prise en main](exemples/average-speed/) suffisent | une demi-journée |
| **2 — L'oracle** | Figer le jeu d'essai en donnée de référence, rejouée par les deux services. C'est ce qui transforme la double implémentation en **preuve** | quelques heures |
| **3 — La CI** | `Verifier.java` et `Couverture.java` rendent `1` en cas d'échec. Ils s'ajoutent à la chaîne GitLab comme n'importe quel *linter* | une étape de pipeline |
| **4 — L'index** | Un régénérateur d'index existe déjà pour les ADR (`bin/dev/regen-adr-index.sh` dans `iris-common`, avec son `adr-drift.yml`). Le même dispositif vaut pour les spécifications | par analogie |

### Par où démarrer

**Ne pas commencer par la méthode, commencer par un écart.** S'il existe un cas où
`iris-service-java` et `iris-service-python` ne donnent pas le même résultat — ou un
résultat que personne n'arrive à expliquer — c'est le pilote idéal : la démarche s'y
démontre en quelques jours, et l'écart constaté convainc là où le discours échoue.

À défaut, prendre le calcul le plus exposé aux [huit faux amis](CADRE.md) : celui qui
arrondit, qui trie, ou qui traite des valeurs absentes.

---

## Pour aller voir

| | |
|---|---|
| **[README.md](README.md)** | La méthode en une lecture |
| **[CADRE.md §4](CADRE.md)** | La fiche de contraintes — **la** section qui intéresse l'architecture |
| **[exemples/](exemples/)** | Six exemples, et ce que chacun démontre |
| **[outils/](outils/)** | Les 25 contrôles mécanisés, et le rapport de couverture |

Ce dépôt est sous [0BSD](LICENSE), comme
[runtime-xray](https://github.com/Beennnn/runtime-xray) : reprenez-le, renommez-le,
absorbez-le dans le référentiel Iris. Aucune attribution requise.
