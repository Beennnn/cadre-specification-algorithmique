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

## Les deux dérives

Elles sont symétriques, et aucune n'est volontaire.

| Le métier fait le travail du développeur | Le développeur fait le travail du métier |
|---|---|
| Il écrit du code qui finit en production | Il choisit un arrondi, et son sens |
| Il choisit des structures de données, optimise | Il décide quoi faire d'une donnée absente |
| Il impose une technologie dans la spécification | Il départage deux valeurs ex æquo |
| Il fige un format de stockage, un ordre d'exécution | Il fixe un seuil, une valeur par défaut |
| Il se prononce sur la performance | Il décide du comportement en mode dégradé |

À gauche, on demande une expertise logicielle à qui ne l'a pas. À droite, on laisse
trancher des questions **comptables, contractuelles ou réglementaires** à quelqu'un qui
n'a ni le mandat ni les éléments — et le plus souvent sans qu'il s'en aperçoive.

## La question qui tranche

> Si modifier ce point change un résultat observable par un client, un comptable, un
> régulateur ou un opérateur → **c'est du métier**.
> Si ça ne change que le temps, la mémoire, le coût ou la façon de déployer → **c'est de la
> technique**.

Elle fait basculer du côté métier toute une série de points qu'on croit techniques : les
arrondis, l'ordre des opérations non commutatives, le départage des ex æquo, le traitement
des valeurs absentes, les bornes, le mode dégradé. Ce sont les [huit faux amis](CADRE.md),
et ils causent l'essentiel des écarts entre deux implémentations honnêtes.

## Le rôle du métier

> **Il décrit ce qui doit être calculé, et à partir de quoi.**

| Il décide | Il ne décide pas |
|---|---|
| Les règles, et leur ordre quand il change le résultat | Le langage, l'architecture, les structures de données |
| Les paramètres, les seuils, les plafonds, les barèmes | La parallélisation, le cache, le déploiement |
| Les arrondis, leur sens, le sort du reste | Les optimisations |
| Le départage des ex æquo | L'endroit du code où la fonction vivra |
| Le comportement sur donnée absente ou invalide | Le format de stockage et de sérialisation |
| Le mode dégradé quand une dépendance tombe | |
| Les contraintes **chiffrées en unités métier** : volumes, latence acceptable, exactitude, rejouabilité, fréquence de changement | |
| Les données de référence, et leur validation | |
| Si un écart constaté est significatif | |

Et il **atteste** que l'expression fonctionnelle est juste, puis tranche les questions
ouvertes qui s'y rapportent.

## Le rôle de la technique

> **Elle décide comment c'est calculé, et elle en assume la tenue dans la durée.**

| Elle décide | Elle ne décide pas |
|---|---|
| Le langage, l'architecture, les structures de données | Une règle, un seuil, un arrondi |
| L'algorithme retenu, les optimisations, la parallélisation | Le départage d'un ex æquo |
| Le stockage, le cache, le déploiement, l'observabilité | Le sort d'une donnée absente |
| Le découpage du code et son organisation | Le comportement en mode dégradé |
| Comment la qualité tient dans la durée : maintenabilité, testabilité, performance, sécurité | Si un écart est acceptable |

Et elle **co-écrit la fiche de contraintes**, signale ce qui coûtera cher, vérifie qu'elle
peut coder sans reposer de question, implémente, teste contre les données de référence, et
fournit au métier les moyens de voir — les valeurs propagées, l'analyse statique et
dynamique.

> **Quand elle rencontre un cas non prévu — et elle en rencontrera — elle ne tranche pas :
> elle ouvre une question ou une suggestion de modification.** Le développeur propose, le
> valideur métier dispose. Une décision métier prise dans un commit est une décision
> perdue.

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
| **1** | Un **script**, puis une **IA** | Les incohérences **formelles** — celles qui se constatent sans rien connaître au métier | Une entrée déclarée qu'aucune règle n'emploie ; un `SI` sans `SINON` ; un numéro de version qui ne correspond pas à l'historique |
| **2** | Le **métier** et le **test** | La **justesse et la complétude** des règles | Un seuil qui devrait valoir 4 articles et non 3 ; un cas limite que personne n'avait envisagé |
| **3** | Un **développeur extérieur** au domaine et à l'écriture | La **capacité à implémenter**, par oui ou par non | « Et si les deux ont le même score, on prend lequel ? » |

### Pourquoi deux passes au premier étage

Elles n'attrapent pas la même chose, et l'une ne remplace pas l'autre.

| | Ce qu'elle attrape | Son verdict |
|---|---|---|
| **Le script** — [`verifier.py`](outils/verifier.py) | ce qui se **compte** : entrées orphelines, paramètres morts, règles non couvertes, identifiants dupliqués, versions incohérentes | **certain**, en quelques secondes, sans faux positif |
| **L'IA** | ce qui se **lit** : une règle floue, une contrainte d'implémentation glissée dans le texte, un « etc. » qui repousse une décision, un vocabulaire qui dérive | **à vérifier** — chaque constat doit citer un passage exact |

Le script vient en premier parce qu'il est gratuit et sûr. L'IA vient ensuite, sur un
document déjà propre : elle n'a plus à signaler ce qu'un contrôle mécanique aurait dû
trouver, et peut se consacrer à ce que seule une lecture repère.

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
| **[exemples/fil-rouge/](exemples/fil-rouge/)** | Le fil rouge complet : *l'autonomie d'un véhicule électrique* |
| **[GLOSSAIRE.md](GLOSSAIRE.md)** | Le vocabulaire de la méthode elle-même |
| **[REFERENCES.md](REFERENCES.md)** | D'où viennent les idées, ce qu'on leur emprunte et ce qu'on écarte |
| **[FAQ.md](FAQ.md)** | Les objections fréquentes et leurs réponses |
| **[CHANTIER.md](CHANTIER.md)** | L'état des travaux, les décisions prises, ce qui reste ouvert |

## L'outillage

```bash
python3 outils/verifier.py                    # 30 des 38 contrôles, sur tout le dépôt
python3 outils/verifier.py --chaine <spec>    # qui crée / qui utilise, fils d'exécution, vue groupée
python3 outils/verifier.py --tracer <nom>     # où passe cette grandeur
python3 outils/identites.py --registre        # le registre des identités durables
```

Sans dépendance, code de retour `1` sur échec — utilisable en intégration continue tel
quel. Les règles sont écrites **une fois** dans
[`REGLES-DE-CONTROLE.md`](outils/REGLES-DE-CONTROLE.md), pour trois lecteurs : un humain
qui les applique, le script qui les exécute, une IA à qui on les donne en consigne.

---

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
complète](exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md)

**Deux vignettes** complètent l'illustration, gardées parce qu'elles aboutissent à des
conclusions techniques **opposées** à partir de la même méthode :

| | [Le montant à payer](exemples/SPEC-PRX-001-montant-a-payer.md) | [Le refroidissement d'une boisson](exemples/SPEC-THM-001-refroidissement.md) | [L'autonomie d'un véhicule](exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md) |
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

Sur des grandeurs continues, ce critère se dédouble en **reproductibilité** (deux
implémentations conformes donnent le même nombre), **justesse numérique** (le nombre est la
vraie valeur des formules) et **validité du modèle** (les formules décrivent la réalité).
Confondre les trois transforme chaque écart de mesure en rapport de bogue — voir
[CADRE §2.9](CADRE.md).

## Par où commencer

1. Lisez **[CADRE.md](CADRE.md)** une fois, en entier. C'est le seul document long.
2. Lisez **[le cas métier du fil rouge](exemples/fil-rouge/0-LE-CAS-METIER.md)** — la page à
   montrer à quelqu'un qui n'a que dix minutes.
3. Parcourez le reste du [fil rouge](exemples/fil-rouge/) pour voir la méthode déroulée.
4. Faites l'**exercice d'étalonnage** : prenez un algorithme existant, connu, moyennement
   complexe, écrivez-en la spécification *a posteriori*, et faites-la implémenter par
   quelqu'un qui ne connaît pas le domaine. Les écarts constatés convainquent — le
   discours, non.

> Ce cadre est un outil, pas un règlement. Une section qui ne sert jamais doit être
> supprimée ; une question qui revient toujours doit être ajoutée à la liste de
> vérification. La seule règle non négociable est la première : **la spécification s'écrit
> avant le code**.
