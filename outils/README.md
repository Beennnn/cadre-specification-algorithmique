# Outils

| Fichier | À quoi ça sert |
|---|---|
| **[REGLES-DE-CONTROLE.md](REGLES-DE-CONTROLE.md)** | Le catalogue des règles de contrôle `C-01` à `C-23` (mécaniques) et `H-01` à `H-06` (humaines). Écrit une fois, lu par un humain, exécuté par le script, donné en consigne à une IA |
| **[verifier.py](verifier.py)** | Met en œuvre les règles `C-xx` mécanisables. Aucune dépendance : Python 3 seul |
| **[PROMPT-RELECTURE-IA.md](PROMPT-RELECTURE-IA.md)** | La consigne prête à l'emploi pour une pré-relecture par un modèle |

## Utilisation

```bash
python3 outils/verifier.py                                   # tout le dépôt
python3 outils/verifier.py exemples/fil-rouge/5-SPEC-*.md     # une spécification
python3 outils/verifier.py --tracer montant_net_ligne         # parcours d'une grandeur
```

Le mode `--tracer` répond à « **où passe cette grandeur ?** » : dans quels documents elle
est déclarée en entrée ou en sortie, et quelles règles l'emploient. Il repose sur la
portée globale des noms et sur l'immutabilité ([CADRE §2.4](../CADRE.md)) — sans elles, un
nom désignerait plusieurs choses et le parcours n'aurait pas de sens.

Code de retour `1` si au moins un **échec** — utilisable tel quel en intégration continue.

## Lire la sortie

| Niveau | Signification |
|---|---|
| **ÉCHEC** | Un défaut mécanique certain. Il bloque le passage en développement |
| **AVERTIR** | Un signalement à trancher à la main. Il n'est jamais « faux » : ou bien le défaut est réel, ou bien il révèle une divergence de vocabulaire entre le contrat et les règles |

## Ce que le script ne fait pas

Il vérifie ce qui est **mécanisable** : `C-01` à `C-04`, `C-14` à `C-17`, `C-19` à `C-21`,
`C-23`, `C-24`, `C-26`. Les contrôles `C-05` à `C-13`, `C-18`, `C-22` et `C-25` demandent
une lecture du pseudo-langage que le script ne fait pas, et les contrôles `H-xx` demandent
un jugement.

`H-07` (immutabilité) est le cas le plus net : deux branches d'un même `SI` qui affectent
le même nom sont légitimes, une réaffectation séquentielle ne l'est pas. Un modèle lit
correctement la structure des branches ; un script non. **C'est là que la passe IA apporte
le plus.**

C'est exactement le partage décrit au [guide 5](../guides/5-VALIDER.md) : le script
d'abord, l'IA ensuite, les humains en dernier — chacun sur ce qu'il sait faire.

## Pourquoi ces outils sont en Python — et pourquoi une version Java est légitime

La question se pose dès qu'on sort du dépôt de méthode : **en contexte industriel Java, on
préférera du Java.** C'est justifié, et la réponse tient dans une distinction que la méthode
elle-même enseigne.

> **Le catalogue de règles est l'actif. Le vérificateur n'en est qu'une implémentation.**

[`REGLES-DE-CONTROLE.md`](REGLES-DE-CONTROLE.md) joue exactement le rôle d'une
spécification : il énonce **ce qui doit être vérifié**, sans dire comment. `verifier.py` en
est une réalisation possible ; une réalisation en Java en serait une autre, tout aussi
conforme. C'est le [test de la double implémentation](../CADRE.md) appliqué à l'outillage
lui-même.

**Deux artefacts, deux finalités différentes :**

| | **Le vérificateur de démonstration** — ici | **Le vérificateur industriel** — chez vous |
|---|---|---|
| Sa vertu | se lire et **s'exécuter sans rien installer** : `python3 outils/verifier.py`, aucune dépendance, aucun build | être **maintenu comme le reste du code de l'organisation** : mêmes conventions, même revue, même chaîne de construction |
| Son public | quiconque découvre la méthode et veut la voir tourner en trente secondes | les équipes, en intégration continue |
| Le bon langage | un langage de script, parce qu'il n'y a rien à installer et que le texte s'y manipule bien | **celui de l'organisation** — en contexte Java, du Java, packagé et testé comme le reste |

> **Attention à ne pas confondre avec l'arbitrage sur les identifiants** ([CADRE
> §2.4](../CADRE.md)). Là, on refuse la convention d'un langage parce que **la
> spécification doit survivre au langage**. Ici, c'est l'inverse : un vérificateur est du
> code ordinaire, soumis aux mêmes exigences de maintenabilité que le reste — et le §1.1
> s'applique à lui comme à n'importe quel autre composant.

## Réimplémenter le vérificateur

Trois choses suffisent, et elles existent déjà :

1. **Le catalogue** — [`REGLES-DE-CONTROLE.md`](REGLES-DE-CONTROLE.md), qui dit quelles
   règles sont mécanisables et ce qu'un échec révèle.
2. **Un corpus de référence** — les documents de ce dépôt, dont on connaît le verdict :
   `0 échec, 0 avertissement`. Toute réimplémentation doit retrouver ce verdict.
3. **Un corpus de défauts connus** — des documents volontairement fautifs, un par règle,
   avec le constat attendu. *(À constituer : voir `CHANTIER.md`.)*

Les deux implémentations doivent alors **s'accorder sur le même corpus**. C'est la seule
façon de savoir qu'elles vérifient bien la même chose — et c'est très exactement ce que la
méthode demande de tout composant.

## Faire évoluer

Une règle s'ajoute au catalogue **quand son absence a causé un incident réel**. Si elle
est mécanisable, elle est implémentée ici ; sinon, c'est un `H-xx`.

Un avertissement récurrent qui se révèle toujours être un faux positif ne se fait pas
taire : il signale une règle **mal formulée**. C'est la boucle d'amélioration du catalogue.
