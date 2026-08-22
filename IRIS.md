# Ce dépôt et les projets Iris

*Pourquoi une méthode d'expression du besoin fonctionnel a sa place dans un programme
d'architecture — et ce qu'elle lui apporte concrètement.*

> **À compléter avec le contexte Iris.** Ce document pose le lien de principe, qui ne
> dépend pas du programme. Les éléments propres à Iris — périmètre, équipes, jalons,
> dépôts, référentiel d'architecture existant — restent à renseigner : ils sont signalés
> par ⟨…⟩ dans le texte. Je ne les ai pas inventés.

---

## Le lien, en une phrase

> **On ne choisit pas une architecture à partir d'un besoin flou.** On la choisit à partir
> d'exigences chiffrées — et ce sont précisément celles que l'expression du besoin
> fonctionnel doit produire, ou personne ne les produira.

L'architecture n'est pas un préalable au besoin : elle en est une **conséquence**. Chaque
décision structurante — le langage, le type numérique, le découpage en composants, le
stockage, la parallélisation, le mode dégradé — se déduit d'une exigence métier chiffrée.
Quand cette exigence n'est pas écrite, la décision est quand même prise : par le
développeur, au moment de coder, sans mandat et sans trace.

---

## Ce que l'architecture attend du besoin, point par point

C'est le contenu de la **fiche de contraintes** ([CADRE §4](CADRE.md)), et chaque ligne
commande une décision d'architecture :

| Ce que le métier chiffre | Ce que l'architecture en déduit |
|---|---|
| L'**exactitude** exigée, en unités métier | Le type numérique : décimal exact ou flottant. Ce n'est pas un détail de codage |
| Les **volumes** et la **latence** acceptable | Le dimensionnement, la stratégie de cache, le traitement par lots ou au fil de l'eau |
| La **rejouabilité** et la durée de conservation | Le stockage, l'immutabilité des entrées, la stratégie d'archivage |
| La **fréquence de changement** d'une règle | Ce qui doit être paramètre plutôt que code, et ce qui justifie un moteur de règles |
| Le **mode dégradé** quand une dépendance tombe | La résilience, les délais, les reprises |
| L'**auditabilité** de ce qui est calculé | La traçabilité, les journaux, ce qu'on conserve et sous quelle forme |
| Les **dépendances entre étapes** | Ce qui est parallélisable, et ce qui ne l'est pas |

**Aucune de ces lignes ne peut être remplie par un architecte seul.** Ce sont des décisions
métier : combien de temps un résultat doit rester reproductible, quelle erreur est
acceptable, ce qu'il faut faire quand une donnée manque.

---

## La démonstration que le dépôt en donne

Elle tient dans un contraste, et c'est pour cela que les exemples ont été choisis ainsi.

| | [Bilan de masse](exemples/mass-balance/) | [Refroidissement](exemples/SPEC-THM-001-refroidissement.md) |
|---|---|---|
| L'exigence d'exactitude | conservation **exacte** de la masse | justesse relative, incertitude de mesure ±2 °C |
| Le type numérique qui en découle | **décimal exact obligatoire** — le binaire flottant est exclu | **double précision, largement suffisante** |

> **La même méthode, deux domaines, deux conclusions opposées.** Ce n'est pas l'habitude du
> développeur qui décide, ni une règle d'architecture générale : c'est l'exigence chiffrée,
> écrite par le métier. Sans elle, les deux composants auraient reçu le même type numérique
> — et l'un des deux aurait été faux.

Deux autres sorties d'architecture que le dépôt produit **mécaniquement**, à partir du
document et sans qu'on les écrive :

- **Les niveaux d'exécution et le chemin critique.** `java outils/Verifier.java --chaine`
  dérive de la chaîne de traitement ce qui est indépendant, donc parallélisable. Sur
  [le lever du Soleil](exemples/SPEC-AST-001-lever-coucher-du-soleil.md), deux étapes le
  sont ; sur [le bilan de masse](exemples/mass-balance/), aucune — et c'est un résultat, pas
  une omission.
- **Le rapport de couverture spec ↔ code.** `java outils/Couverture.java` relie chaque point
  de la spécification au fichier et à la ligne qui l'implémentent, et signale l'inverse : du
  code qui se réclame d'une exigence que la spécification ignore.

---

## Ce que cela change dans la conduite d'un programme

| Sans expression du besoin organisée | Avec |
|---|---|
| L'architecture est arbitrée sur des intentions, puis reprise quand les vrais chiffres arrivent | Elle est arbitrée une fois, sur des exigences écrites et opposables |
| Les décisions métier implicites sont prises dans le code, dispersées et non tracées | Elles sont dans un document versionné, avec un valideur nommé |
| Deux équipes qui implémentent le même besoin produisent deux résultats | Le **test de la double implémentation** est le critère d'acceptation |
| Un écart constaté en recette devient une dispute | Il devient une analyse : le code est-il conforme, ou la spécification est-elle en défaut ? |
| La reprise d'un composant, cinq ans plus tard, part d'une relecture de code | Elle part du document, et le code y renvoie par ses annotations |

> **Le point le plus souvent sous-estimé, c'est le dernier.** Un composant se reprend
> plusieurs fois dans sa vie. Ce qui coûte n'est pas de l'écrire, c'est de **retrouver
> pourquoi il fait ce qu'il fait** — et cette information n'est jamais dans le code.

---

## Comment le rattacher concrètement à Iris

⟨*À trancher avec l'équipe Iris — je n'ai pas les éléments pour le faire seul.*⟩

| Point à trancher | Ce qu'il faut décider |
|---|---|
| **Périmètre d'application** | Quels composants d'Iris relèvent de cette méthode. Elle vise les traitements **algorithmiques** ; elle n'apporte rien à un CRUD ou à une intégration |
| **Rattachement documentaire** | Ce dépôt reste-t-il autonome et référencé par le référentiel d'architecture d'Iris ⟨lequel ?⟩, ou est-il absorbé dedans |
| **Point d'entrée dans le cycle** | À quel jalon la fiche de contraintes est exigée. Elle doit précéder l'arbitrage d'architecture, sinon elle ne sert à rien |
| **Rôles** | Qui tient le rôle de **valideur métier** et de **co-auteur technique** ⟨nommément⟩ sur les premiers composants |
| **Outillage en intégration continue** | `Verifier.java` et `Couverture.java` rendent 1 en cas d'échec : ils sont utilisables tels quels en CI ⟨sur quelle chaîne ?⟩ |
| **Premier composant pilote** | Le meilleur candidat est un calcul **déjà écrit deux fois** ou **déjà source d'écarts** : la méthode s'y démontre en quelques jours |

### Ce que je recommande pour démarrer

**Ne pas commencer par la méthode, commencer par un écart.** Prendre un désaccord réel et
récent entre deux implémentations, ou un résultat que personne n'arrive à expliquer, et lui
appliquer la démarche sur ce seul périmètre. Les écarts constatés convainquent ; le discours,
non — c'est d'ailleurs ce que dit le [guide d'adoption](CADRE.md).

---

## Pour aller voir

| | |
|---|---|
| **[README.md](README.md)** | La méthode en une lecture |
| **[CADRE.md §4](CADRE.md)** | La fiche de contraintes — c'est **la** section qui intéresse l'architecture |
| **[exemples/](exemples/)** | Les six exemples, et ce que chacun démontre |
| **[outils/](outils/)** | Les contrôles mécanisés, utilisables en intégration continue |
