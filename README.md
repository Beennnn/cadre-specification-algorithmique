# Cadre de spécification fonctionnelle

> Le métier écrit la loi. L'IT écrit la machine.

**Ce dépôt est une méthode, pas un entrepôt de spécifications.** Il ne contient aucune
spécification de votre système : il contient la façon de les écrire, les modèles pour le
faire, et un exemple complet de bout en bout.

Les spécifications que vous produirez avec cette méthode vivent **dans le dépôt de votre
produit, à côté du code** — c'est ce qui leur permet d'être versionnées, relues en demande
de fusion, et de rester vraies.

---

## Le problème

Dans beaucoup d'organisations, la connaissance algorithmique appartient à des gens dont le
métier n'est pas de développer. Ils codent quand même — tableur, carnet de calcul, script —
parce que c'est le seul moyen de vérifier qu'une idée tient. Ce travail est légitime, et
souvent excellent : il fait exactement ce qu'on lui demande, démontrer qu'un raisonnement
fonctionne.

Mais un code qui part en production doit tenir autre chose : un niveau de qualité **assuré
dans la durée** — maintenabilité, testabilité, capacité à évoluer, performance,
exploitabilité, sécurité. Ces contraintes relèvent du **métier de développeur et
d'architecte logiciel**, et on ne peut pas raisonnablement en attendre l'expertise de gens
dont le métier est de concevoir et de maintenir les besoins algorithmiques — pas plus
qu'on n'attend d'un développeur qu'il maîtrise leur domaine.

Deux effets en découlent, et ils sont symétriques :

- le code mélange l'intention et la mise en œuvre, si bien qu'on ne distingue plus la
  règle de gestion du choix d'implémentation ;
- quand la spécification est floue, c'est le développeur qui tranche des questions
  métier — sans le savoir et sans mandat.

Dans les deux cas, **on demande à chacun de faire le métier de l'autre**.

## Ce que la méthode produit

Une **spécification fonctionnelle** en deux volets :

1. **L'architecture fonctionnelle** — quelles fonctions existent, comment elles
   s'appellent, **qui décide de ce qu'elles font**, et quel est leur contrat d'entrée et
   de sortie. Rien sur l'endroit où elles seront codées : ce découpage-là appartient au
   développeur et à l'architecte.
2. **La spécification de chaque fonction** — les règles, en pseudo-langage, avec leurs
   paramètres, leurs invariants, leur jeu d'essai et leurs contraintes chiffrées.

Le tout assez précis pour qu'un développeur l'implémente sans deviner, et assez libre pour
qu'il choisisse le langage, les structures de données et l'architecture.

## Les étapes

Douze étapes, dont la dernière referme la boucle sur le métier.

| | L'étape | Où c'est décrit |
|---|---|---|
| **1** | Définir les **conventions de rédaction** et les formats | [CADRE §2](CADRE.md) — *une fois pour l'organisation* |
| **2** | **Découper** les traitements en fonctions | [1-DECOUPER](guides/1-DECOUPER.md) |
| **3** | **Nommer et décrire** les fonctions et les données qu'elles véhiculent | [1-DECOUPER](guides/1-DECOUPER.md) · [2-GLOSSAIRE](guides/2-GLOSSAIRE.md) · [3-DONNEES](guides/3-DONNEES.md) |
| **4** | Décrire l'**algorithme** en pseudo-langage | [CADRE §2](CADRE.md) · [4-ECRIRE-A-PLUSIEURS](guides/4-ECRIRE-A-PLUSIEURS.md) |
| **5** | Identifier les **données de test et les résultats attendus** | [CADRE §5](CADRE.md) — calculés à la main, jamais produits par un programme |
| **6** | **Figer une version** pour relecture | [7-VERSIONNER](guides/7-VERSIONNER.md) — on ne relit pas un document qui bouge |
| **7** | Vérifier la **complétude et la cohérence** des informations métier | [5-VALIDER](guides/5-VALIDER.md) — étages 1 et 2 |
| **8** | Vérifier la **capacité à implémenter**, par la lecture d'un profil technique | [5-VALIDER](guides/5-VALIDER.md) — étage 3, verdict binaire |
| **9** | **Collecter les retours et itérer** jusqu'à un niveau de qualité suffisant | [5-VALIDER](guides/5-VALIDER.md) — questions `Q-xx` et suggestions `SM-xxx` |
| **10** | Passer à l'**architecture technique, au développement et aux tests** qui rejouent les entrées et comparent les résultats | [6-PASSER-AU-DEVELOPPEMENT](guides/6-PASSER-AU-DEVELOPPEMENT.md) |
| **11** | **Analyser les écarts** — par le métier, avec le support des développeurs, en lisant les valeurs propagées | [8-ANALYSER-LES-ECARTS](guides/8-ANALYSER-LES-ECARTS.md) |
| **12** | Décider si ces écarts sont **significatifs au regard des tolérances** | [8-ANALYSER-LES-ECARTS](guides/8-ANALYSER-LES-ECARTS.md) |

L'étape 1 se fait **une seule fois**, au démarrage. Les étapes 2 à 12 se répètent à chaque
lot de fonctions. Le versionnement ([7-VERSIONNER](guides/7-VERSIONNER.md)) court en
permanence, en travers de toutes.

> **Les étapes 11 et 12 sont ce qui distingue cette méthode d'une simple discipline de
> rédaction.** La spécification ne s'arrête pas quand le code est écrit : elle s'arrête
> quand le métier a confirmé, chiffres en main, que ce qui tourne calcule bien ce qu'il
> avait décrit.

## Ce que contient le dépôt

| | |
|---|---|
| **[CADRE.md](CADRE.md)** | **Le document de référence.** Le principe, la frontière métier/technique, les huit faux amis, le pseudo-langage, l'adaptation au calcul scientifique, la fiche de contraintes, la gouvernance, les anti-patterns. À lire une fois, en entier |
| **[guides/](guides/)** | Huit guides opérationnels, à ouvrir pendant qu'on fait |
| **[outils/](outils/)** | Le catalogue des règles de contrôle, le vérificateur, et la consigne de relecture par une IA |
| **[templates/](templates/)** | Les modèles vierges : spécification, fiche de fonction, fiche de donnée, glossaire, liste de vérification |
| **[exemples/fil-rouge/](exemples/fil-rouge/)** | **Le fil rouge complet** : *l'autonomie d'un véhicule électrique*, du découpage jusqu'à la spécification détaillée |
| **[GLOSSAIRE.md](GLOSSAIRE.md)** | Le vocabulaire de la méthode elle-même |
| **[REFERENCES.md](REFERENCES.md)** | D'où viennent les idées, ce qu'on leur emprunte et ce qu'on écarte |
| **[FAQ.md](FAQ.md)** | Les objections fréquentes, et leurs réponses |
| **[CHANTIER.md](CHANTIER.md)** | L'état des travaux : ce qui est fait, les décisions déjà prises, ce qui reste ouvert |

## Le fil rouge

Tous les guides s'appuient sur **le même scénario** : *[l'autonomie d'un véhicule
électrique](exemples/fil-rouge/)*. « Jusqu'où puis-je aller, et où dois-je m'arrêter ? »

Il a été choisi parce qu'il réunit quatre qualités rares ensemble : tout le monde comprend
la question, il y a de la vraie physique avec des équations, il est assez complexe pour
être intéressant, et il se découpe naturellement en une douzaine de fonctions réparties
sur cinq valideurs métier différents.

On l'y suit de bout en bout : **[le cas métier](exemples/fil-rouge/0-LE-CAS-METIER.md)**
→ [le découpage](exemples/fil-rouge/1-DECOUPAGE.md) → [le
glossaire](exemples/fil-rouge/2-GLOSSAIRE.md) → [le cheminement des
données](exemples/fil-rouge/3-DONNEES.md) → [une fiche arrêtée au niveau
3](exemples/fil-rouge/4-FN-004-planifier-les-recharges.md) → [la spécification
complète](exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md).

**Deux vignettes** complètent l'illustration, gardées parce qu'elles aboutissent à des
conclusions techniques **opposées** à partir de la même méthode :

| | [Le montant à payer d'une commande](exemples/SPEC-PRX-001-montant-a-payer.md) | [Le refroidissement d'une boisson](exemples/SPEC-THM-001-refroidissement.md) | [L'autonomie d'un véhicule](exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md) |
|---|---|---|---|
| Exigence d'exactitude | exacte au centime | tolérance de 10⁻⁶ | reproductibilité de 10⁻⁹ entre deux implémentations |
| **Type numérique qui en découle** | **décimal exact obligatoire** | **double précision confortable** | **double précision indispensable** |
| Faux ami central | le centime résiduel d'une répartition | l'ordre d'ajout du lait | le sens de l'arrondi de l'autonomie |

*Trois exigences différentes, trois conclusions différentes.* C'est la démonstration que
c'est la spécification qui décide, et non l'habitude du développeur.

## La validation, en deux étages

Une spécification ne part pas en développement parce qu'elle a l'air prête, mais parce
qu'elle a été **validée** — et la validation se fait dans cet ordre :

| | Qui | Ce qu'on cherche |
|---|---|---|
| **Étage 1** | un script, puis une IA | des incohérences **formelles** : entrée déclarée jamais employée, paramètre fantôme qui circule, paramètre mort, règle non couverte, version incohérente |
| **Étage 2** | trois relecteurs humains | de la **compréhensibilité** et de la justesse métier : « combien de fois aurais-je dû deviner ? » |

L'ordre n'est pas négociable : faire relire par des humains un document qui contient
encore un paramètre inutilisé gaspille la ressource la plus rare de la démarche.

```bash
python3 outils/verifier.py exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md
```

## Le critère d'acceptation

> **Le test de la double implémentation.** Deux développeurs qui ne se parlent pas, dans
> deux langages différents, produisent des programmes qui donnent **le même résultat** sur
> l'intégralité du jeu d'essai — et sur les cas auxquels personne n'avait pensé.

Si ce n'est pas vrai, la spécification n'est pas finie.

Sur des grandeurs continues, ce critère se dédouble en **reproductibilité** (deux
implémentations conformes donnent le même nombre), **justesse** (le nombre est la vraie
valeur des formules) et **validité du modèle** (les formules décrivent la réalité) — voir
[CADRE.md §2.8](CADRE.md).

## Par où commencer

1. Lisez **[CADRE.md](CADRE.md)** une fois, en entier. C'est le seul document long.
2. Lisez **[le cas métier du fil rouge](exemples/fil-rouge/0-LE-CAS-METIER.md)** : le
   scénario expliqué simplement puis techniquement, et ce que sa formalisation va nous
   forcer à construire. C'est la page à montrer à quelqu'un qui n'a que dix minutes.
3. Parcourez le reste du **[fil rouge](exemples/fil-rouge/)** pour voir à quoi ça
   ressemble une fois déroulé.
4. Faites l'**exercice d'étalonnage** : prenez un algorithme existant, connu, moyennement
   complexe, écrivez-en la spécification *a posteriori*, et faites-la implémenter par
   quelqu'un qui ne connaît pas le domaine. Les écarts constatés convainquent — le
   discours, non.

> Ce cadre est un outil, pas un règlement. Une section qui ne sert jamais doit être
> supprimée ; une question qui revient toujours doit être ajoutée à la liste de
> vérification. La seule règle non négociable est la première : **la spécification s'écrit
> avant le code**.
