# La chaîne d'outils

*Proposition. Elle répond à une question que le dépôt laisse ouverte : avec quoi une
organisation qui adopte la méthode écrit-elle réellement à plusieurs, puis passe-t-elle
au développement ? Le substrat est tranché depuis longtemps ; la chaîne, non.*

---

## Ce qui est déjà tranché, et qu'on ne rouvre pas

| Décision | Où | Ce qu'elle ferme |
|---|---|---|
| La spécification vit dans **Git, en markdown**, dans le dépôt du produit | [`CADRE.md` §6.3](../CADRE.md) | le traitement de texte partagé, le tableur, le wiki |
| La revue se fait par **demande de fusion**, discussion attachée à la ligne | [`guides/4-ECRIRE-A-PLUSIEURS.md`](../guides/4-ECRIRE-A-PLUSIEURS.md) | la validation par courriel et par silence |
| **Pas de langage formel ni de langage contrôlé** | [`REFERENCES.md`](../REFERENCES.md) | B, TLA+, Alloy, SBVR, ACE |
| Le formalisme reste **léger** : markdown et conventions | [`README.md`](../README.md) | le langage dédié, le schéma, le métamodèle |

Ces quatre décisions sont argumentées et coûteuses à reprendre. Ce document les tient
pour acquises et ne traite que ce qui vient par-dessus.

## Le principe de sélection

Le dépôt a une règle pour ajouter un contrôle : **on l'ajoute quand son absence a causé
un incident réel**, jamais par précaution. La même règle s'applique à un outil, avec une
seconde entrée qui lui est propre :

> **Un outil se construit dans deux cas seulement : quand la méthode promet déjà un
> geste mécanique qu'elle n'outille pas, ou quand son absence a coûté quelque chose de
> constatable. Dans tous les autres cas, on prend ce que le marché donne — ou on ne fait
> rien.**

Trois catégories, donc, et la troisième est la plus importante à tenir.

| | Ce qui la remplit |
|---|---|
| **Le marché le fait mieux** | la forge, la revue, l'historique, les étiquettes, l'intégration continue, la publication d'un site lisible |
| **Personne ne le vend** | tout ce qui doit **connaître** `RG-xxx`, les identités durables, la grammaire des contrats, le pseudo-langage |
| **On refuse** | ce qui déplace le problème au lieu de le résoudre — voir la fin de ce document |

---

## La chaîne, moment par moment

Sept moments, du découpage aux écarts. Pour chacun : la friction réelle, et ce qui la
traite.

| Moment | La friction | L'outil | État |
|---|---|---|---|
| **Découper** | aucune — c'est une réunion, pas un outil | — | — |
| **Amorcer une spécification** | la page blanche, et un modèle qu'on recopie à la main | `Nouvelle.java` | **à construire** |
| **Écrire** | savoir si ce qu'on vient d'écrire est bien formé, sans lancer une commande | l'étage 1 **sur la demande de fusion** | **à construire** |
| **Relire à plusieurs** | un valideur métier qui lit du markdown brut avec des ancres et des tables d'UUID | un site publié depuis le dépôt | **marché** |
| **Faire tenir le vocabulaire** | deux experts, deux fonctions, un même terme qui dérive | index de glossaire + `C-18` | **à construire** |
| **Passer au développement** | un dossier de passation assemblé à la main | `Passation.java` | **à construire** |
| **Changer de version** | le test décisif du rejeu, annoncé mécanique, fait à la main | `Rejeu.java` | **à construire** |

---

## Ce qu'on construit, par ordre de rapport

### 1. L'étage 1 s'exécute sur la demande de fusion

**C'est le seul outil de cette liste dont l'absence rend tous les autres facultatifs.**

Aujourd'hui, `Verifier.java` s'exécute quand quelqu'un pense à le taper. Or l'expert
métier édite dans l'interface web de la forge — c'est la voie que
[`CADRE.md` §6.3](../CADRE.md) recommande explicitement pour lui — et il n'a ni Java, ni
terminal, ni raison d'y penser. Le résultat est déjà mesuré :
[l'analyse de l'outillage](ANALYSE-DE-L-OUTILLAGE.md) constate qu'il n'existe aucun
`.github/`, et que le code de retour 1, annoncé « utilisable tel quel en intégration
continue », ne sert à rien.

Ce qu'il faut n'est pas un nouvel outil : c'est **un déclencheur et un format de
sortie**.

| Pièce | Ce que ça fait |
|---|---|
| Une action d'intégration continue | lance le vérificateur et le rejeu du corpus de défauts à chaque poussée |
| `--format=forge` | émet les constats au format d'annotation de la forge, pour qu'un **ÉCHEC devienne un commentaire sur la ligne fautive** |
| Un fichier de propriétaires (`CODEOWNERS`) | associe chaque fichier de spécification à **son** valideur métier, et le convoque automatiquement en revue |

> **Un prérequis mesuré, et il n'est pas facultatif.** `Verifier.java` construit ses
> constats sur `record Constat(niveau, regle, fichier, message)` : **il n'y a pas de
> numéro de ligne**. En l'état, une annotation ne peut se poser que sur le fichier
> entier, ce qui perd l'essentiel du bénéfice. Il faut donc d'abord ajouter la ligne au
> constat — la plupart des contrôles la connaissent déjà, puisqu'ils travaillent sur des
> `Matcher` dont on peut tirer la position. C'est la première tâche de la ligne 1, avant
> tout choix d'outil.

La troisième ligne mérite d'être remarquée : elle rend mécanique la règle « une
fonction = un fichier = un valideur », que le guide 4 énonce comme une propriété
structurelle et que rien ne fait respecter aujourd'hui. Elle est gratuite.

> **L'effet réel n'est pas de trouver plus de défauts** — le vérificateur les trouve
> déjà. Il est de les dire **dans le médium où le relecteur travaille**, au moment où il
> travaille. L'étage 1 cesse d'être une discipline pour devenir une propriété.

Effort : très faible pour l'action et `CODEOWNERS` ; faible pour `--format=forge`.

### 2. `Rejeu.java` — le test décisif du versionnement

[`guides/7-VERSIONNER.md`](../guides/7-VERSIONNER.md) écrit ceci, en encadré :

> « On rejoue le jeu d'essai de la version précédente sur la nouvelle version. Si un seul
> cas change de résultat attendu : c'est un majeur. Ce n'est pas une appréciation, c'est
> une exécution. »

Et plus loin, à propos de « ce n'est qu'une clarification » : « le test ci-dessus la
démasque en **trente secondes** ». Cette exécution n'existe pas. Le geste le plus mécanique de toute la méthode — celui qui départage un correctif
d'un majeur, donc qui décide s'il faut prévenir les consommateurs — est aujourd'hui fait
de tête.

L'outil compare le jeu d'essai de la version étiquetée précédente à celui de la version
courante et rend un verdict : **correctif**, **mineur** ou **majeur**, avec la liste des
cas dont le résultat attendu a bougé. Il lit ce qui existe déjà : le §10 des
spécifications et les `reference-data.csv`.

C'est la proposition la mieux justifiée de la liste, parce qu'elle n'invente rien : elle
**tient une promesse écrite**.

Effort : moyen — de l'ordre de `Couverture.java`.

### 3. `Nouvelle.java` — l'amorçage

Le remède à la page blanche est aujourd'hui « un modèle pré-rempli », c'est-à-dire un
fichier de `templates/` qu'on recopie. Recopier à la main, c'est produire d'emblée un
document sans annexe d'identités, sans en-tête complet, sans table de couverture — et
le corpus de défauts montre ce que ça donne : **douze constats `C-28` pour une seule
annexe manquante**.

`Nouvelle.java --domaine NRG --titre "…"` engendre le squelette, attribue les identités,
écrit l'en-tête, et crée du même coup l'ébauche de traduction portant **les mêmes
identités** — donc conforme à `C-42` dès la première minute.

> Ce n'est pas du confort. C'est la différence entre un outil qui **signale** une classe
> de défauts et un outil qui la **rend impossible**. La méthode préfère partout la
> seconde forme.

Effort : faible — l'attribution d'identités existe déjà dans `Identites.java`.

### 4. Le glossaire outillé, et `C-18`

C'est le point proprement **collaboratif**, et le seul de cette liste que la taille
aggrave. Deux experts écrivent deux fonctions ; le même terme métier y prend deux sens,
ou deux termes y désignent la même chose. Aucun contrôle ne le voit : `C-18` — tout terme
employé figure au glossaire, tout terme du glossaire est employé — n'est pas mécanisé.

Le dépôt a pourtant déjà le germe de l'outil. `--tracer` répond à « où passe cette
grandeur ? » à travers tout le corpus. Il manque le pendant côté vocabulaire : un index
qui, pour chaque terme du glossaire, dit quelles spécifications l'emploient — et signale
les termes employés que le glossaire ignore.

Le catalogue avertit déjà du cas grave, à propos de `C-01` : « soit le champ est
réellement inutile — on le retire ; soit les règles le désignent **par un autre nom** que
le contrat — et c'est alors le vocabulaire qui diverge, ce qui est un défaut plus
sérieux ». À l'échelle d'une équipe, c'est le mode de panne principal.

Effort : moyen. Ordre suggéré : l'index d'abord, qui se lit ; `C-18` ensuite, une fois
qu'on aura vu sur quoi il se déclenche vraiment.

### 5. `Passation.java` — le paquet de passation

[`guides/6-PASSER-AU-DEVELOPPEMENT.md`](../guides/6-PASSER-AU-DEVELOPPEMENT.md) décrit un
« dossier de passation », assemblé à la main. Ses pièces sont toutes déjà dans le dépôt,
et toutes déjà engendrables : la spécification à l'étiquette gelée, le jeu de données de
référence, la table de couverture, les questions ouvertes restantes, les notices de
changement depuis la version précédente.

Un paquet engendré est **reproductible** : deux passations de la même version donnent le
même contenu, et on peut prouver ce qui a été remis. Un dossier assemblé à la main ne
l'est pas.

Effort : faible — c'est de l'assemblage, la matière existe.

### 6. L'étage 2 devient un outil, pas un copier-coller

[`PROMPT-RELECTURE-IA.md`](PROMPT-RELECTURE-IA.md) est une consigne qu'un humain
assemble à la main avec le catalogue, le glossaire et la spécification. Résultat mesuré :
**aucune spécification du dépôt ne déclare de passe IA**, alors que `C-27` l'exige et que
le guide 5 la présente comme la seconde passe de l'étage 1.

Un outil qui assemble le contexte, lance la passe et **enregistre son en-tête** —
opérateur, date, constats retenus et écartés — rend `C-27` satisfaisable, donc
mécanisable. Tant que c'est un copier-coller, la trace ne s'écrit pas, et un contrôle du
catalogue reste lettre morte.

Effort : faible pour l'assemblage et la trace. Attention : l'outil ne **conclut** pas —
le guide 5 est net, la validation reste un acte humain daté.

---

## Ce qu'on prend au marché, sans le construire

| Besoin | Ce qu'on prend | Pourquoi ne rien écrire |
|---|---|---|
| Historique, comparaison, discussion à la ligne, étiquettes | la **forge** (GitHub, GitLab) | c'est exactement le métier de l'outil, et `CADRE.md` §6.3 s'appuie déjà dessus |
| Convocation du bon valideur | **`CODEOWNERS`** de la forge | gratuit, et mécanise « un fichier, un valideur » |
| Déclenchement de l'étage 1 | **l'intégration continue** de la forge | un fichier de vingt lignes |
| Lecture confortable par un non-rédacteur | un **générateur de site statique** (MkDocs Material, Docusaurus) | il rend le markdown lisible, cherchable, avec les liens internes qui fonctionnent — sans toucher aux fichiers |
| Édition sans installation | **l'éditeur web de la forge** | déjà la recommandation du cadre |

### La même chaîne, avec des noms

*Un choix par besoin, pas un panorama. Les offres et les niveaux de licence bougent :
vérifier `CODEOWNERS` et la protection de branche contre votre propre plan avant de
promettre quoi que ce soit.*

| Besoin | Le choix | Pourquoi celui-là |
|---|---|---|
| Forge | **GitHub** | `CODEOWNERS`, protection de branche, Actions et Pages dans le même produit. Sur **GitLab**, tout existe aussi, mais `CODEOWNERS` est une fonction payante — à vérifier avant de bâtir la gouvernance dessus |
| Écrire sans rien installer | **`github.dev`** — la touche `.` sur le dépôt | un VS Code complet dans le navigateur, qui écrit sur une branche et ouvre la demande de fusion. C'est la réponse concrète à « le métier n'a ni Java ni terminal ». L'éditeur au crayon reste bien pour une correction d'une ligne |
| Lancer l'étage 1 | **GitHub Actions** + `actions/setup-java` (Temurin 21) | `java outils/Verifier.java` tourne tel quel, sans construction |
| Rendre un échec visible sur la ligne | **les commandes d'atelier** `::error file=…,line=…,title=C-08::…` | zéro dépendance — c'est un simple format de sortie, fidèle à la doctrine « un fichier, aucune dépendance ». **reviewdog** est l'alternative si vous visez GitHub *et* GitLab avec le même outil |
| Convoquer le valideur | **`CODEOWNERS`** + protection de branche exigeant son approbation | une ligne par spécification |
| Publier un site lisible | **MkDocs + Material for MkDocs**, servi par **GitHub Pages** | markdown pur, recherche intégrée, ancres qui fonctionnent. **`mike`** ajoute les versions publiées, le greffon **i18n** apparie `.en.md` et `.fr.md`. **Docusaurus** si vous voulez versions et langues nativement et acceptez Node |
| Tenir le vocabulaire | **Vale** | linter de prose à règles de terminologie : « ce terme, pas celui-là », exécutable en intégration continue. C'est la moitié marché de `C-18`/`C-25` ; le reste demande de lire le glossaire du domaine |
| Aligner les tableaux markdown | **Prettier** — avec précaution | il réaligne les tableaux que le métier désaligne. **Mais le vérificateur lit le markdown par expressions régulières** : essayez-le sur une copie avant de l'imposer, un reformatage peut déplacer une ancre |
| Liens externes | **lychee** | le vérificateur couvre déjà les liens relatifs ; lychee ajoute les URL sortantes |
| Diagrammes | **Mermaid** | déjà employé dans ce dépôt, et rendu nativement par GitHub comme par MkDocs Material |
| Passe de relecture par IA (étage 2) | **Claude**, modèle `claude-opus-5` | soit Claude Code en action sur la demande de fusion, soit un appel à l'API Messages avec le catalogue, le glossaire et la spécification. Ce qui compte n'est pas l'appel : c'est **la trace** — opérateur, date, constats retenus et écartés — sans quoi `C-27` reste faux |

> **Cinq fichiers suffisent à démarrer**, et aucun ne demande d'écrire du Java :
> `.github/workflows/verifier.yml`, `CODEOWNERS`, `mkdocs.yml`, `.vale.ini`, et une
> règle de protection de branche. Le reste de ce document est du développement ; ceci
> est du réglage, et c'est ce qui ferme le défaut 8.

> **La publication d'un site est le seul ajout que le dépôt n'a jamais mentionné et qui
> change quelque chose pour le métier.** Un valideur qui relit `| <a id="p-01"></a>`P-01`
> | …` dans un tableau markdown brut ne relit pas dans de bonnes conditions. C'est un
> réglage, pas un développement.

## Ce qu'on refuse, et pourquoi

Refuser demande un argument, sinon c'est un préjugé.

| Ce qu'on refuse | L'argument |
|---|---|
| Une **suite de gestion d'exigences** (type DOORS, Polarion, Jira étendu) | Elle apporte la traçabilité et l'identité durable — que le dépôt tient déjà avec une annexe d'UUID et `registre.json`, pour zéro licence. Elle coûte en échange ce qui compte le plus ici : la spécification cesse d'être **un fichier à côté du code**, et la citation `code → RG-xxx` devient impraticable |
| Un **éditeur dédié** à la méthode, en formulaire | Il supprime la page blanche, et supprime avec elle la possibilité d'écrire une phrase que le formulaire n'a pas prévue. Le formalisme est léger **par décision** ; un éditeur le durcirait par la porte de derrière |
| Un **langage dédié** exécutable | Déjà écarté, et l'argument tient : « une spécification exécutable serait un programme — écrit dans un langage de plus, sans écosystème, sans outillage, et que le métier ne pourrait plus relire » ([`FAQ.md`](../FAQ.md)) |
| Un **wiki** (Confluence et apparentés) | Pas de comparaison de versions exploitable, pas d'étiquette qui gèle, pas de revue attachée à la ligne. C'est le « traitement de texte partagé » sous un autre nom |
| Un **second vérificateur** dans un autre langage | Le dépôt l'a fait, puis l'a retiré : deux fois la surface de bug pour un bénéfice ponctuel. Ce que la double implémentation apportait est tenu par le corpus de défauts ([`README.md`](README.md)) |

---

## Ce que ça donne bout en bout

Une spécification, de sa création à sa livraison, avec ce qui la touche à chaque pas :

| Pas | Qui agit | L'outil |
|---|---|---|
| 1 | l'expert métier | `Nouvelle.java` crée le squelette et ses identités |
| 2 | l'expert métier | il écrit dans l'éditeur web de la forge, sur une branche |
| 3 | la forge | l'étage 1 s'exécute ; les échecs reviennent **en commentaires sur les lignes fautives** |
| 4 | le binôme métier / développeur | corrige jusqu'au vert ; `CODEOWNERS` a convoqué le bon valideur |
| 5 | une IA | passe de relecture, **trace enregistrée** — `C-27` devient vrai |
| 6 | les relecteurs | étages 2 et 3, sur le site publié, discussion attachée à la ligne |
| 7 | l'auteur | fusion, étiquette de version |
| 8 | l'équipe | `Passation.java` produit le paquet ; l'auteur déroule le cas riche à voix haute |
| 9 | le développement | code, annote `@ImplementsSpec` ; `Couverture.java` boucle dans les deux sens |
| 10 | à la version suivante | `Rejeu.java` rend le verdict : correctif, mineur ou majeur |

Six outils à construire, tous dans la forme déjà retenue par le dépôt — **un fichier
Java, aucune dépendance, aucune construction**. Aucun ne dépasse en taille le
vérificateur existant.

## Ce que cette proposition ne tranche pas

| # | Question | Ce qui en dépend |
|---|---|---|
| i | L'étage 1 doit-il **bloquer** la fusion, ou seulement commenter ? | Bloquer tient la promesse « l'ordre n'est pas négociable » ; commenter ménage l'adoption. Les deux se défendent, pas en même temps |
| ii | Le site publié doit-il l'être **par version étiquetée**, ou seulement sur la branche principale ? | Par version, il devient l'archive consultable qui rend `guides/7` réel ; c'est plus de réglage |
| iii | `Rejeu.java` lit-il les jeux d'essai **des documents** ou **des `reference-data.csv`** ? | Les documents font foi ; les CSV sont exécutables. Il faudra dire lequel arbitre en cas d'écart — et c'est peut-être un contrôle de plus |
| iv | Les six outils restent-ils **dans ce dépôt**, ou deviennent-ils un paquet installable ? | Le dépôt est une méthode, pas un produit. Un outillage qui grossit finit par demander sa propre maison — la question d'empaquetage est déjà ouverte dans [`CHANTIER.md`](../CHANTIER.md) |

> **Ordre suggéré si l'on ne fait qu'une chose : la ligne 1.** Sans l'étage 1 sur la
> demande de fusion, les cinq autres outils restent des commandes que quelqu'un doit
> penser à taper — et le dépôt sait déjà ce que valent les gestes qui dépendent de ce
> qu'un relecteur pense à faire.
