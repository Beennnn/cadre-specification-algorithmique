# Cadre de spécification fonctionnelle

> Le métier écrit la loi. L'IT écrit la machine.

**Ce dépôt est une méthode, pas un entrepôt de spécifications.** Il ne contient aucune
spécification de votre système : il contient la façon de les écrire, les modèles pour le
faire, les outils pour les contrôler, et un exemple complet déroulé de bout en bout.

Les spécifications que vous produirez vivent **dans le dépôt de votre produit, à côté du
code** — c'est ce qui leur permet d'être versionnées, relues en demande de fusion, et de
rester vraies.

---

## Le problème

Dans beaucoup d'organisations, la connaissance algorithmique appartient à des gens dont le
métier n'est pas de développer. Ils codent quand même — tableur, carnet de calcul, script —
parce que c'est le seul moyen de vérifier qu'une idée tient. **Ce travail est légitime, et
souvent excellent** : il fait exactement ce qu'on lui demande, démontrer qu'un raisonnement
fonctionne.

Mais un code qui part en production doit tenir autre chose : un niveau de qualité **assuré
dans la durée** — maintenabilité, testabilité, capacité à évoluer, performance,
exploitabilité, sécurité. Ces contraintes relèvent du **métier de développeur et
d'architecte logiciel**, et on ne peut pas raisonnablement en attendre l'expertise de gens
dont le métier est de concevoir et de maintenir les besoins algorithmiques — pas plus qu'on
n'attend d'un développeur qu'il maîtrise leur domaine.

Deux effets en découlent, et ils sont symétriques :

- le code mélange l'intention et la mise en œuvre, si bien qu'on ne distingue plus la règle
  de gestion du choix d'implémentation ;
- quand la spécification est floue, c'est le développeur qui tranche des questions métier —
  sans le savoir et sans mandat.

Dans les deux cas, **on demande à chacun de faire le métier de l'autre**.

## Ce que la méthode produit

Une **spécification fonctionnelle** en deux volets :

1. **L'architecture fonctionnelle** — quelles fonctions existent, comment elles s'appellent,
   qui valide qu'elles sont justes, quelles données elles véhiculent, et quel est leur
   contrat d'entrée et de sortie.
2. **La spécification de chaque fonction** — son rôle, son contrat, son algorithme en
   pseudo-langage, ses paramètres, ses invariants, son jeu d'essai et ses contraintes
   chiffrées.

Assez précise pour qu'un développeur l'implémente sans deviner. Assez libre pour qu'il
choisisse seul le langage, les structures de données et l'architecture.

---

## Les douze étapes

| | L'étape | Où c'est décrit |
|---|---|---|
| **1** | Définir les **conventions de rédaction** et les formats | [CADRE §2](CADRE.md) — *une fois pour l'organisation* |
| **2** | **Découper** les traitements en fonctions | [1-DECOUPER](guides/1-DECOUPER.md) |
| **3** | **Nommer et décrire** les fonctions et les données qu'elles véhiculent | [1-DECOUPER](guides/1-DECOUPER.md) · [2-GLOSSAIRE](guides/2-GLOSSAIRE.md) · [3-DONNEES](guides/3-DONNEES.md) |
| **4** | Décrire l'**algorithme** en pseudo-langage | [CADRE §2](CADRE.md) · [4-ECRIRE-A-PLUSIEURS](guides/4-ECRIRE-A-PLUSIEURS.md) |
| **5** | Identifier les **données de test et les résultats attendus** | [CADRE §5](CADRE.md) |
| **6** | **Figer une version** pour relecture | [5-VALIDER](guides/5-VALIDER.md) — on ne relit pas un document qui bouge |
| **7** | Vérifier la **complétude et la cohérence** des informations métier | [5-VALIDER](guides/5-VALIDER.md) — étages 1 et 2 |
| **8** | Vérifier la **capacité à implémenter**, par la lecture d'un profil technique | [5-VALIDER](guides/5-VALIDER.md) — étage 3, verdict binaire |
| **9** | **Collecter les retours et itérer** jusqu'à un niveau de qualité suffisant | [5-VALIDER](guides/5-VALIDER.md) |
| **10** | Passer à l'**architecture technique, au développement et aux tests** | [6-PASSER-AU-DEVELOPPEMENT](guides/6-PASSER-AU-DEVELOPPEMENT.md) |
| **11** | **Analyser les écarts** — par le métier, en lisant les valeurs propagées | [8-ANALYSER-LES-ECARTS](guides/8-ANALYSER-LES-ECARTS.md) |
| **12** | Décider si ces écarts sont **significatifs au regard des tolérances** | [8-ANALYSER-LES-ECARTS](guides/8-ANALYSER-LES-ECARTS.md) |

L'étape 1 se fait **une seule fois**, au démarrage. Les étapes 2 à 12 se répètent à chaque
lot de fonctions. Le [versionnement](guides/7-VERSIONNER.md) court en travers de toutes.

> **Les étapes 11 et 12 sont ce qui distingue cette méthode d'une simple discipline de
> rédaction.** Elle ne s'arrête pas quand le code est écrit, mais quand le métier a
> confirmé, chiffres en main, que ce qui tourne calcule bien ce qu'il avait décrit.

---

*Les quatre sections qui suivent disent qui fait quoi, et pourquoi ce partage tient.*

## Qui décide quoi

> **Le métier écrit la loi, la technique écrit la machine.**
> Le métier décrit *ce qui doit être calculé, et à partir de quoi*. La technique décide
> *comment*, et en assume la tenue dans la durée.

Ce n'est pas un partage de tâches, c'est un partage de **souveraineté** : aucun des deux
ne se prononce dans le champ de l'autre. Une spécification qui impose une table indexée
est aussi fautive qu'un développeur qui décide seul du sens d'un arrondi.

**La question qui tranche :**

> Si modifier ce point change **un résultat observable** → c'est du **métier**.
> Si ça ne change que le temps, la mémoire, le coût ou le déploiement → c'est de la
> **technique**.

Elle fait basculer côté métier toute une série de points qu'on croit techniques : les
arrondis, l'ordre des opérations non commutatives, le départage des ex æquo, le
traitement des valeurs absentes, les bornes, le mode dégradé. Ce sont les **huit faux
amis**, et ils causent l'essentiel des écarts entre deux implémentations honnêtes.

> **Quand la technique rencontre un cas non prévu — et elle en rencontrera — elle ne
> tranche pas : elle ouvre une question ou une suggestion de modification.** Le
> développeur propose, le valideur métier dispose. Une décision métier prise dans un
> commit est une décision perdue.

→ Le partage détaillé, les deux dérives qu'il corrige et les huit faux amis, un par un :
**[CADRE §1.2 à §1.5](CADRE.md)**.

## La place de l'IA

Elle intervient à presque toutes les étapes et **ne décide à aucune**. Son terrain le plus
rentable est la **cohérence du contrat** : entrée déclarée que personne n'emploie, sortie promise
que rien ne produit, grandeur employée que rien ne déclare, unité absente, contrainte d'implémentation glissée
dans une règle, formulation qui repousse la décision. Un travail exhaustif et fastidieux
que l'humain fait mal — non par incompétence, mais parce qu'il fatigue.

Elle aide ensuite à coder — en appliquant les règles de codage de l'équipe, en écrivant des
commentaires qui disent le **pourquoi** plutôt que de répéter le code, et en posant les
annotations qui relient le code à la spécification puis en vérifiant leur cohérence dans
les deux sens. Puis à dériver les tests du jeu d'essai, à comparer les résultats poste par
poste, et — **une fois que le métier les a jugés valides** — à figer les jeux de données
qui deviennent alors des données de référence. Mais elle ne dit pas si la règle est **la
bonne** règle, ne juge pas qu'un écart est acceptable, et **ne voit pas ce dont personne
n'a jamais parlé** : elle repère une incohérence entre deux choses écrites, jamais
l'absence d'une troisième. Un trou ne se voit que s'il a des bords.

> **Une IA n'est jamais un valideur.** La signature reste humaine, datée, nominative, et la
> personne qui a lancé la passe assume ce qu'elle en a retenu comme si elle l'avait écrit.
> Voir [guide 9](guides/9-L-APPORT-DE-L-IA.md).

---

## La validation, en trois étages et dans cet ordre

| Étage | Qui relit | Ce qu'on y cherche | Exemple de ce qui est attrapé |
|---|---|---|---|
| **1** | Un **script**, puis une **IA** | Les incohérences **formelles** — celles qui se constatent sans rien connaître au métier | Une entrée déclarée qu'aucune règle n'emploie ; un `IF` sans `ELSE` ; un numéro de version qui ne correspond pas à l'historique |
| **2** | Le **métier** et le **test** | La **justesse et la complétude** des règles | Une règle **impeccablement écrite mais fausse** : le document dit que la remise démarre à 3 articles, la politique commerciale en vigueur dit 4. Ou un cas que le métier connaît et que personne n'a pensé à écrire |
| **3** | Un **développeur extérieur** au domaine et à l'écriture | La **capacité à implémenter**, par oui ou par non | Une question à laquelle le document ne répond pas, et qu'il trancherait donc **seul, sans mandat** : « deux lignes ont exactement le même montant — sur laquelle j'impute le centime restant ? » |

### Pourquoi deux passes au premier étage

Elles n'attrapent pas la même chose, et l'une ne remplace pas l'autre.

| | Ce qu'elle attrape | Son verdict |
|---|---|---|
| **Le script** — [`Verifier.java`](outils/Verifier.java) | ce qui se **compte** : entrées orphelines, paramètres morts, règles non couvertes, identifiants dupliqués, versions incohérentes | **certain**, en quelques secondes, sans faux positif |
| **L'IA** | ce qui se **lit** : une règle floue, une contrainte d'implémentation glissée dans le texte, un « etc. » qui repousse une décision, un vocabulaire qui dérive | **à vérifier** — chaque constat doit citer un passage exact |

Le script vient en premier parce qu'il est gratuit et sûr. L'IA vient ensuite, sur un
document déjà propre : elle n'a plus à signaler ce qu'un contrôle mécanique aurait dû
trouver, et peut se consacrer à ce que seule une lecture repère.

**Chaque étage voit ce que les autres ne peuvent pas voir**, et c'est pour cela qu'aucun ne
remplace les autres :

- un document peut être **formellement irréprochable et matériellement faux** — aucun
  script, aucune IA ne sait qu'une remise démarre en réalité à 4 articles ; seul quelqu'un
  qui connaît la politique commerciale le sait ;
- et il peut être **juste, complet du point de vue métier, et malgré tout inimplémentable
  sans deviner** — parce que le métier, qui connaît trop bien son domaine, n'a pas vu qu'il
  laissait une question sans réponse. Seul quelqu'un qui doit écrire le code s'y heurte.

L'ordre des trois étages n'est pas négociable : faire relire par des humains un document
qui contient encore un paramètre inutilisé gaspille la ressource la plus rare de la
démarche. Le verdict de l'étage 3 est **binaire**, et le critère de sortie est **zéro
question**.

## La donnée de référence

> **Un résultat validé cesse d'être un exemple : il devient un engagement.**

Quand le métier a examiné et accepté les sorties par rapport aux entrées, le couple cesse
d'être un exemple : il devient un **engagement opposable**, attaché à une fonction, une
version, un valideur et une date. Il sert à qualifier le code, à **tester la
non-régression** — c'est ce qui rend l'optimisation sans peur, donc possible — et à
comparer deux implémentations.

Ce qui rend un tel résultat valide n'est pas sa provenance mais **l'examen** qui précède son
acceptation. Il peut donc venir d'une solution analytique, d'un calcul à la main, d'une
maquette technique, ou du composant lui-même — sous réserve, dans ce dernier cas, d'une
analyse statique et dynamique. Le seul cas jamais valide : **accepter un résultat parce
qu'un programme l'a produit, sans l'examiner**.

---

## Ce que contient le dépôt

| | |
|---|---|
| **[CADRE.md](CADRE.md)** | **Le document de référence.** Le principe, la frontière, les faux amis, le pseudo-langage, le contrat, la chaîne de traitement, l'oracle, la gouvernance, les anti-patterns. À lire une fois, en entier |
| **[guides/](guides/)** | Neuf guides opérationnels, à ouvrir pendant qu'on fait — dont [l'apport de l'IA](guides/9-L-APPORT-DE-L-IA.md), transverse |
| **[templates/](templates/)** | Les modèles vierges : [spécification](templates/MODELE-SPECIFICATION.md), [fiche de fonction](templates/MODELE-FICHE-FONCTION.md), [fiche de donnée](templates/MODELE-FICHE-DONNEE.md), [glossaire](templates/MODELE-GLOSSAIRE.md), [liste de vérification](templates/CHECKLIST-RELECTURE.md) |
| **[outils/](outils/)** | Le catalogue des règles de contrôle, le vérificateur, la gestion des identités, la consigne de relecture par une IA |
| **[IRIS.md](IRIS.md)** | Le rattachement à [Iris](https://github.com/iris7-app) : il fait déjà tourner deux implémentations des mêmes contrats et documente 64 décisions techniques en ADR — il lui manque l'artefact symétrique pour les décisions métier |
| **[exemples/](exemples/)** | Six exemples, et ce que chacun montre que les autres ne montrent pas |
| **[exemples/mass-balance/](exemples/mass-balance/)** | La **chaîne complète** : besoin → spécification → contrat → code exécutable → analyse des écarts |
| **[exemples/fil-rouge/](exemples/fil-rouge/)** | Le fil rouge complet : *l'autonomie d'un véhicule électrique* |
| **[GLOSSAIRE.md](GLOSSAIRE.md)** | Le vocabulaire de la méthode elle-même |
| **[REFERENCES.md](REFERENCES.md)** | D'où viennent les idées, ce qu'on leur emprunte et ce qu'on écarte |
| **[FAQ.md](FAQ.md)** | Les objections fréquentes et leurs réponses |
| **[CHANTIER.md](CHANTIER.md)** | L'état des travaux, les décisions prises, ce qui reste ouvert |

## L'outillage

### Ce que fait le vérificateur

Il lit les spécifications du dépôt et rend deux services : il **contrôle**, et il
**donne à voir**.

**Il contrôle** — 25 des 41 règles du catalogue, réparties en quatre familles :

| Famille | Exemples de ce qu'il attrape |
|---|---|
| **Cohérence du contrat** | une entrée déclarée que personne n'emploie (*morte*) ; une grandeur employée que rien ne déclare (*fantôme*) ; un paramètre jamais utilisé ; une grandeur sans unité, sans plage ou sans famille de type |
| **Complétude logique** | un `IF` sans `ELSE` ; un `ROUND` sans mode ni décimales ; un superlatif sans règle de départage ; un `WHILE` sans nombre maximal d'itérations |
| **Traçabilité et version** | une règle absente de la table de couverture ; un cas de test cité qui n'existe pas ; un identifiant défini deux fois ; un en-tête de version qui contredit l'historique ; un impact déclaré sur les résultats sans incrément majeur ; une identité manquante ou partagée par deux objets |
| **Cohérence de la chaîne** | une grandeur consommée par une étape qui n'est ni entrée, ni paramètre, ni produite en amont ; un produit que rien ne consomme et que le contrat n'annonce pas |

**Il donne à voir** — des vues qu'aucune lecture ne fournit :

```bash
java outils/Verifier.java                     # tous les contrôles, sur tout le dépôt
java outils/Verifier.java --chaine <spec>     # qui crée / qui utilise chaque grandeur,
                                              # fils d'exécution, chemin critique, vue groupée
java outils/Verifier.java --tracer <nom>      # où passe cette grandeur, dans tout le corpus
java outils/Identites.java --registre         # le registre des identités durables
java outils/Couverture.java <exemple>         # le rapport de couverture spécification ↔ code
```

Sans dépendance, code de retour `1` sur échec — utilisable en intégration continue tel
quel.

**Le catalogue est l'actif ; le vérificateur n'en est qu'une réalisation.** Elle est en
Java, en **fichier unique** — `java outils/Verifier.java`, sans construction ni
dépendance : un outil dont on attend de la fiabilité se maintient dans le langage de
l'organisation, avec ses conventions et ses relecteurs.

Le dépôt a porté un temps deux implémentations, en Python et en Java, confrontées sur le
[corpus de défauts connus](outils/jeu-d-essai/). Elles divergeaient sur six points, et
**chacune avait tort quelque part** — c'est très exactement ce que le test de la double
implémentation est censé produire. Python a ensuite été retiré, après validation du
portage contre ce même corpus ; le verdict figé lui survit comme **oracle de
non-régression**.

### Pourquoi c'est possible : le formalisme

Le vérificateur ne fait aucun miracle. **Il ne peut contrôler que ce que le formalisme rend
contrôlable** — et c'est précisément la raison d'être des conventions de l'étape 1.

| La convention | Ce qu'elle rend vérifiable |
|---|---|
| Les contrats sont déclarés en blocs, avec une **grammaire** : `nom : Famille(unité pivot ▸ unité d'usage, précision, plage)` | Comparer ce qui est **déclaré** à ce qui est **employé** — d'où les morts, les fantômes, les unités manquantes |
| Les identifiants ont des **préfixes fixes** : `RG-`, `P-`, `CT-`, `D-`, `ET-`… | Suivre une règle du contrat jusqu'au cas de test, et détecter les doublons |
| Les tables ont des **colonnes fixes** | Lire les paramètres, les questions ouvertes, l'historique de version |
| La chaîne de traitement est une **table**, pas un dessin | Dériver le graphe, les fils d'exécution, les produits morts |
| Les identités vivent dans une **annexe normalisée** | Garantir qu'aucun objet n'en manque et qu'aucune n'est partagée |

> Chacune de ces conventions ressemble, à la lecture, à une contrainte de rédaction. Ce
> sont en réalité des **points d'accroche pour une machine**. Sans elles, le document
> resterait compréhensible par un humain et **totalement opaque à un outil**.

**Le compromis est assumé, et il explique le chiffre.** On a choisi la lisibilité par le
métier — donc un formalisme **léger** : du markdown et des conventions, pas un langage
dédié. Donc un contrôle **partiel** : 25 règles sur 41. Un formalisme plus strict — une
grammaire formelle, un schéma — permettrait d'en mécaniser davantage, au prix de ce qui
compte le plus : que le métier puisse écrire et relire lui-même.

> **Le chiffre se recompte, il ne se relit pas.** Une version antérieure de cette page
> annonçait « 31 règles sur 41 » en citant deux contrôles qui n'existaient pas. Le nombre
> se vérifie en comptant les constats émis par le vérificateur, pas en reprenant la phrase
> précédente.

### Les vues globales, et celles que le code peut produire

Les vues ci-dessus sont produites **à partir des spécifications** : elles disent ce qui est
décrit. Une seconde famille de vues se produit **à partir du code**, en moissonnant les
annotations `@ImplementsSpec` que les développeurs y ont posées. C'est ce que fait
[`outils/Couverture.java`](outils/Couverture.java), et le rapport qu'il produit relie les
deux sens — de l'ancre du point de spécification vers le fichier et la ligne, et retour :

| Ce qu'on obtient | Ce que ça révèle |
|---|---|
| Quelles règles sont implémentées, et où | La carte de couverture réelle |
| Quelles règles ne sont citées **nulle part** | Une règle non implémentée, ou implémentée sans être reconnue |
| Quelles citations pointent vers une règle **qui n'existe plus** | Une citation périmée, après abrogation ou renommage |
| Quelle version de spécification chaque composant déclare | Un composant resté sur une version antérieure |
| Quels **paramètres** aucun cas de test n'arbitre | Une valeur validée, datée, implémentée — et vérifiée par rien. C'est le défaut le plus fréquent qu'ait produit ce dépôt |

> **Cette seconde famille vit du côté du code, dans son intégration continue** — jamais
> dans la spécification, qui ignore l'existence du dépôt de code. C'est la règle de
> direction : le code cite la spécification, jamais l'inverse.
>
> Trois exemples du dépôt la font tourner :
> [average-speed](exemples/average-speed/reports/COVERAGE-REPORT.md),
> [mass-balance](exemples/mass-balance/reports/COVERAGE-REPORT.md) et
> [weather-summary](exemples/weather-summary/reports/COVERAGE-REPORT.md). Les deux premiers
> rapports ont **immédiatement désigné des trous réels** dans nos propres spécifications.

---

## La chaîne complète, de bout en bout

**Un seul exemple va du besoin jusqu'au code exécuté** : *[le bilan de masse d'un
lot](exemples/mass-balance/)*. Doser les composants d'un lot sur une balance de
résolution finie — assez petit pour être lu en entier, assez piégeux pour porter cinq faux
amis.

[Le besoin](exemples/mass-balance/NEED.md) → [la
spécification](exemples/mass-balance/spec/SPEC-MAS-001.en.md) → [le
contrat](exemples/mass-balance/CONTRACT.md) → [le
code](exemples/mass-balance/code/) → [les
écarts](exemples/mass-balance/DEVIATIONS.md)

Le code s'exécute en deux commandes, sans dépendance, et se qualifie contre les données de
référence de la spécification :

```bash
cd exemples/mass-balance/code && javac -encoding UTF-8 -d /tmp/mb $(find src -name '*.java')
cd .. && java -cp /tmp/mb method.massbalance.MassBalanceTest \
    code/src/test/resources/reference-data.csv spec/SPEC-MAS-001.en.md reports/TEST-REPORT.md
```

Le harnais **ne contient aucune valeur attendue** : il rejoue le §10 de la spécification.
C'est ce qui en fait une qualification et non un test écrit par celui qui a codé.

Et il se termine par ce que la plupart des démarches oublient : [l'analyse des
écarts](exemples/mass-balance/DEVIATIONS.md), qui conclut ici que **le code est conforme et
que la spécification est en défaut** — la correction remonte donc au document, versionnée
et motivée.

→ [Les six exemples et ce que chacun montre](exemples/)

## Le fil rouge

Tous les guides s'appuient sur **le même scénario** : *[l'autonomie d'un véhicule
électrique](exemples/fil-rouge/)*. « Jusqu'où puis-je aller, et où dois-je m'arrêter ? »

Choisi parce qu'il réunit quatre qualités rarement ensemble : tout le monde comprend la
question, il y a de la vraie physique avec des équations, il est assez complexe pour être
intéressant, et il se découpe en une douzaine de fonctions réparties sur cinq valideurs
métier.

[Le cas métier](exemples/fil-rouge/0-LE-CAS-METIER.md) → [le
découpage](exemples/fil-rouge/1-DECOUPAGE.md) → [le
glossaire](exemples/fil-rouge/2-GLOSSAIRE.md) → [le cheminement des
données](exemples/fil-rouge/3-DONNEES.md) → [une fiche arrêtée au niveau
2](exemples/fil-rouge/4-FN-004-planifier-les-recharges.md) → [la spécification
complète](exemples/fil-rouge/5-SPEC-NRG-001.en.md)

**Deux vignettes** complètent l'illustration, gardées parce qu'elles aboutissent à des
conclusions techniques **opposées** à partir de la même méthode :

| | [Le bilan de masse d'un lot](exemples/mass-balance/spec/SPEC-MAS-001.en.md) | [Le refroidissement d'une boisson](exemples/cooling/spec/SPEC-THM-001.en.md) | [L'autonomie d'un véhicule](exemples/fil-rouge/5-SPEC-NRG-001.en.md) |
|---|---|---|---|
| Exigence d'exactitude | exacte au centime | tolérance de 10⁻⁶ | reproductibilité de 10⁻⁹ entre deux implémentations |
| **Type numérique qui en découle** | **décimal exact obligatoire** | **double précision confortable** | **double précision indispensable** |
| Faux ami central | le centime résiduel d'une répartition | l'ordre d'ajout du lait | le sens de l'arrondi de l'autonomie |

*Trois exigences, trois conclusions.* C'est la démonstration que c'est la spécification qui
décide, et non l'habitude du développeur.

---

## Le critère d'acceptation

> **Le test de la double implémentation.** Deux développeurs qui ne se parlent pas, dans
> deux langages différents, produisent des programmes qui donnent **le même résultat** sur
> l'intégralité du jeu d'essai — et sur les cas auxquels personne n'avait pensé.

Si ce n'est pas vrai, la spécification n'est pas finie.

→ Ce qu'il devient sur des grandeurs continues, où il se dédouble en reproductibilité,
justesse numérique et validité du modèle : **[CADRE §1.3 et §2.9](CADRE.md)**.

## Par où commencer

1. Lisez **[CADRE.md](CADRE.md)** une fois, en entier. C'est le seul document long.
2. Lisez **[le cas métier du fil rouge](exemples/fil-rouge/0-LE-CAS-METIER.md)** — la page à
   montrer à quelqu'un qui n'a que dix minutes.
3. Parcourez le reste du [fil rouge](exemples/fil-rouge/) pour voir la méthode déroulée.
4. Faites l'**exercice d'étalonnage** : prenez un algorithme existant, connu, moyennement
   complexe, écrivez-en la spécification *a posteriori*, et faites-la implémenter par
   quelqu'un qui ne connaît pas le domaine. Les écarts constatés convainquent — le
   discours, non.

> La seule règle non négociable : **la spécification s'écrit avant le code.** Tout le
> reste est un outil, pas un règlement — [CADRE.md](CADRE.md) dit comment l'adapter.

---

## Licence

**[0BSD](LICENSE)** — *BSD Zero Clause License*, la moins contraignante des licences
libres reconnues.

Prenez ce cadre, ses gabarits, ses exemples et ses outils ; adaptez-les à votre domaine,
renommez-les, intégrez-les à votre référentiel interne, publiez-les ou gardez-les. **Aucune
attribution n'est requise, aucune condition n'est attachée.**

> Ce choix n'est pas neutre, il découle de l'objet du dépôt. Une méthode ne vaut que si on
> se l'approprie : une licence qui obligerait à citer la source freinerait exactement ce
> qu'on cherche — qu'une organisation s'en empare et la fasse sienne.

Même licence que [runtime-xray](https://github.com/Beennnn/runtime-xray).
