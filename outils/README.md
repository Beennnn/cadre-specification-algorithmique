# Outils

| Fichier | À quoi ça sert |
|---|---|
| **[REGLES-DE-CONTROLE.md](REGLES-DE-CONTROLE.md)** | Le catalogue des règles de contrôle `C-01` à `C-23` (mécaniques) et `H-01` à `H-06` (humaines). Écrit une fois, lu par un humain, exécuté par le script, donné en consigne à une IA |
| **[verifier.py](verifier.py)** | Met en œuvre les règles `C-xx` mécanisables. Aucune dépendance : Python 3 seul |
| **[PROMPT-RELECTURE-IA.md](PROMPT-RELECTURE-IA.md)** | La consigne prête à l'emploi pour une pré-relecture par un modèle |

## Utilisation

```bash
python3 outils/verifier.py                                    # tout le dépôt
python3 outils/verifier.py exemples/fil-rouge/5-SPEC-*.md      # une spécification
```

Code de retour `1` si au moins un **échec** — utilisable tel quel en intégration continue.

## Lire la sortie

| Niveau | Signification |
|---|---|
| **ÉCHEC** | Un défaut mécanique certain. Il bloque le passage en développement |
| **AVERTIR** | Un signalement à trancher à la main. Il n'est jamais « faux » : ou bien le défaut est réel, ou bien il révèle une divergence de vocabulaire entre le contrat et les règles |

## Ce que le script ne fait pas

Il vérifie ce qui est **mécanisable**. Les contrôles `C-05` à `C-13`, `C-18` et `C-22`
demandent une lecture du pseudo-langage que le script ne fait pas, et les contrôles `H-xx`
demandent un jugement.

C'est exactement le partage décrit au [guide 5](../guides/5-VALIDER.md) : le script
d'abord, l'IA ensuite, les humains en dernier — chacun sur ce qu'il sait faire.

## Faire évoluer

Une règle s'ajoute au catalogue **quand son absence a causé un incident réel**. Si elle
est mécanisable, elle est implémentée ici ; sinon, c'est un `H-xx`.

Un avertissement récurrent qui se révèle toujours être un faux positif ne se fait pas
taire : il signale une règle **mal formulée**. C'est la boucle d'amélioration du catalogue.
