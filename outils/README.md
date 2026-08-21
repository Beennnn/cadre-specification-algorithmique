# Outils

| Fichier | À quoi ça sert |
|---|---|
| **[REGLES-DE-CONTROLE.md](REGLES-DE-CONTROLE.md)** | Le catalogue des règles de contrôle `C-01` à `C-23` (mécaniques) et `H-01` à `H-06` (humaines). Écrit une fois, lu par un humain, exécuté par le script, donné en consigne à une IA |
| **[Verifier.java](Verifier.java)** | Met en œuvre les règles `C-xx` mécanisables. Fichier unique, aucune dépendance, aucune construction : `java outils/Verifier.java` |
| **[Identites.java](Identites.java)** | Attribue et maintient les identités durables (UUID), et produit `registre.json`. Même forme : `java outils/Identites.java --registre` |
| **[jeu-d-essai/](jeu-d-essai/)** | Le corpus de défauts connus et le verdict attendu — l'oracle de non-régression du vérificateur lui-même |
| **[PROMPT-RELECTURE-IA.md](PROMPT-RELECTURE-IA.md)** | La consigne prête à l'emploi pour une pré-relecture par un modèle |

## Utilisation

```bash
java outils/Verifier.java                                   # tout le dépôt
java outils/Verifier.java exemples/fil-rouge/5-SPEC-*.md     # une spécification
java outils/Verifier.java --tracer dispensed_mass            # parcours d'une grandeur
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

## Ce qui rend ces contrôles possibles

Le vérificateur ne peut contrôler **que ce que le formalisme rend contrôlable**. La
grammaire des contrats, les préfixes d'identifiants, les colonnes fixes des tables, la
chaîne écrite en table et l'annexe des identités ne sont pas des contraintes de rédaction
gratuites : ce sont les **points d'accroche** qui permettent à une machine de lire ce
document.

Le compromis est assumé : formalisme **léger** — markdown et conventions — pour que le
métier puisse écrire et relire, donc contrôle **partiel** — 31 règles sur 41. Le détail
est dans le [README du dépôt](../README.md).

## Ce que le script ne fait pas

Il vérifie ce qui est **mécanisable** : `C-01` à `C-04`, `C-14` à `C-17`, `C-19` à `C-21`,
`C-23`, `C-24`, `C-26`. Les contrôles `C-05` à `C-13`, `C-18`, `C-22` et `C-25` demandent
une lecture du pseudo-langage que le script ne fait pas, et les contrôles `H-xx` demandent
un jugement.

`H-07` (immutabilité) est le cas le plus net : deux branches d'un même `IF` qui affectent
le même nom sont légitimes, une réaffectation séquentielle ne l'est pas. Un modèle lit
correctement la structure des branches ; un script non. **C'est là que la passe IA apporte
le plus.**

C'est exactement le partage décrit au [guide 5](../guides/5-VALIDER.md) : le script
d'abord, l'IA ensuite, les humains en dernier — chacun sur ce qu'il sait faire.

## Pourquoi ces outils sont en Java

L'outillage a d'abord existé en Python, puis a été porté en Java et Python a été retiré.
L'arbitrage rendu : **un outil dont on attend de la fiabilité se maintient dans le langage
de l'organisation.** Il tient dans une distinction que la méthode elle-même enseigne.

> **Le catalogue de règles est l'actif. Le vérificateur n'en est qu'une implémentation.**

[`REGLES-DE-CONTROLE.md`](REGLES-DE-CONTROLE.md) joue exactement le rôle d'une
spécification : il énonce **ce qui doit être vérifié**, sans dire comment. `Verifier.java`
en est une réalisation possible ; une autre, dans un autre langage, serait tout aussi
conforme — à condition de retrouver le même verdict sur le même corpus.

**Ce que le choix de Java règle :**

| | |
|---|---|
| **Fiabilité** | l'outil est maintenu comme le reste du code de l'organisation : mêmes conventions, même revue, même chaîne de construction, mêmes relecteurs |
| **Rien à installer** | l'argument qui plaidait pour un langage de script a disparu : depuis Java 11, un fichier source se lance directement — `java outils/Verifier.java`, sans build ni dépendance |
| **Une seule implémentation** | deux outils à maintenir pour un seul catalogue, c'était deux fois la surface de bug pour un bénéfice ponctuel. Ce que la double implémentation apportait est désormais tenu par le [jeu d'essai](jeu-d-essai/) |

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
3. **Un corpus de défauts connus** — [`jeu-d-essai/`](jeu-d-essai/), un document
   volontairement fautif dont le verdict attendu est figé : **28 échecs, 3 avertissements**.

> **Ce corpus a déjà servi, et c'est pour cela qu'on le garde.** Le dépôt a porté un temps
> deux implémentations du même catalogue, en Python et en Java. Confrontées sur ce corpus,
> elles divergeaient sur six points — et **chacune avait tort quelque part** : Python
> signalait le même identifiant dupliqué une fois par occurrence ; Java lisait la mauvaise
> colonne de l'historique et la mauvaise cellule de statut d'une question fermée ; `C-24`
> et `C-26` manquaient entièrement à Java. C'est très exactement ce que le test de la
> double implémentation est censé produire.
>
> Python a ensuite été retiré. Le verdict figé lui survit : il est devenu **l'oracle de
> non-régression** de l'implémentation restante. C'est la même donnée de référence qui a
> servi à valider le portage, ligne à ligne, avant la suppression.

Une réimplémentation doit **retrouver ce verdict**. C'est la seule façon de savoir qu'elle
vérifie bien la même chose — et c'est très exactement ce que la méthode demande de tout
composant.

## Faire évoluer

Une règle s'ajoute au catalogue **quand son absence a causé un incident réel**. Si elle
est mécanisable, elle est implémentée ici ; sinon, c'est un `H-xx`.

Un avertissement récurrent qui se révèle toujours être un faux positif ne se fait pas
taire : il signale une règle **mal formulée**. C'est la boucle d'amélioration du catalogue.
