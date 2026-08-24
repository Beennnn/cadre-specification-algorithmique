# L'outillage

*Avec quoi le métier écrit à plusieurs, et comment la spécification arrive au
développement. Un choix par besoin, pas un panorama. Le raisonnement qui a conduit à
ces choix est dans [`outils/CHAINE-D-OUTILS.md`](outils/CHAINE-D-OUTILS.md) ; la mesure
de l'outillage déjà en place est dans
[`outils/ANALYSE-DE-L-OUTILLAGE.md`](outils/ANALYSE-DE-L-OUTILLAGE.md).*

---

## La règle qui a servi à choisir

> **Un outil se construit dans deux cas seulement : quand la méthode promet déjà un
> geste mécanique qu'elle n'outille pas, ou quand son absence a coûté quelque chose de
> constatable. Dans tous les autres cas, on prend ce que le marché donne — ou on ne
> fait rien.**

C'est la règle du catalogue de contrôles, appliquée à l'outillage : *on ajoute quand
l'absence a causé un incident réel, jamais par précaution.*

## Ce qu'on prend au marché

| | Outil | Ce qu'il fait ici |
|---|---|---|
| 🐙 | **[GitHub](https://github.com)** | La forge : historique, comparaison, discussion attachée à la ligne, étiquettes de version. `CADRE.md` §6.3 s'appuie déjà dessus. Sur [GitLab](https://docs.gitlab.com/ee/user/project/codeowners/) tout existe aussi, mais **`CODEOWNERS` y est une fonction payante** |
| ⌨️ | **[github.dev](https://docs.github.com/en/codespaces/the-githubdev-web-based-editor)** | **Écrire sans rien installer.** La touche `.` sur le dépôt ouvre un VS Code complet dans le navigateur, qui écrit sur une branche et ouvre la demande de fusion. C'est la réponse à « le métier n'a ni Java ni terminal ». L'éditeur au crayon reste bon pour une correction d'une ligne |
| ⚙️ | **[GitHub Actions](https://docs.github.com/en/actions)** | Lance l'étage 1 à chaque poussée, avec [`actions/setup-java`](https://github.com/actions/setup-java) et [Temurin](https://adoptium.net) 21. `java outils/Verifier.java` tourne tel quel, sans construction |
| 📍 | **[Commandes d'atelier](https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions)** | `::error file=…,line=…::` transforme un constat en annotation sur la ligne. Zéro dépendance — c'est un format de sortie, pas un outil. [reviewdog](https://github.com/reviewdog/reviewdog) est l'alternative si vous visez GitHub **et** GitLab avec le même outillage |
| 👥 | **[CODEOWNERS](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners)** | Associe chaque spécification à **son** valideur métier et le convoque en revue. Rend mécanique le « une fonction = un fichier = un valideur » du [guide 4](guides/4-ECRIRE-A-PLUSIEURS.md) |
| 🛡️ | **[Rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets)** | La protection de branche : demande de fusion obligatoire, approbation du propriétaire, étage 1 au vert. C'est ce qui rend l'ordre des étages non négociable ([guide 5](guides/5-VALIDER.md)) |
| 📖 | **[MkDocs](https://www.mkdocs.org)** + **[Material](https://squidfunk.github.io/mkdocs-material/)** | Publie le dépôt en site lisible et cherchable, servi par [GitHub Pages](https://pages.github.com). Un valideur métier ne relit pas des tables d'UUID en markdown brut. [`mike`](https://github.com/jimporter/mike) ajoute les versions publiées ; [Docusaurus](https://docusaurus.io) est l'alternative si vous acceptez Node |
| 🔤 | **[Vale](https://vale.sh)** | Linter de prose à règles de terminologie. Mécanise `C-41` — « etc. », « le cas échéant », « en général » — et donne le numéro de ligne, ce que le vérificateur ne fait pas encore |
| 📈 | **[Mermaid](https://mermaid.js.org)** | Les schémas de chaîne, rendus nativement par GitHub **et** par Material. Déjà employé dans ce dépôt |
| 🤖 | **[Claude](https://claude.com)** — `claude-opus-5` | La passe de relecture de l'étage 2, soit en action sur la demande de fusion avec [Claude Code](https://claude.com/claude-code), soit par l'[API Messages](https://docs.claude.com). Ce qui compte n'est pas l'appel, c'est **la trace** — sans opérateur, date et constats, `C-27` reste faux |
| 🔗 | **[lychee](https://github.com/lycheeverse/lychee)** | *Facultatif.* Le vérificateur couvre déjà les liens relatifs ; lychee ajoute les URL sortantes |
| 🧹 | **[Prettier](https://prettier.io)** | *Facultatif, et avec précaution.* Il réaligne les tableaux que le métier désaligne — mais le vérificateur lit le markdown par expressions régulières. À essayer sur une copie avant de l'imposer |

## Ce qu'on écrit soi-même

Rien de tout cela ne s'achète : ces outils doivent **connaître** `RG-xxx`, les identités
durables, la grammaire des contrats et le pseudo-langage.

| | Outil | État |
|---|---|---|
| ✅ | [`Verifier.java`](outils/Verifier.java) | En place — 25 des 41 contrôles |
| ✅ | [`Identites.java`](outils/Identites.java) | En place — identités durables et registre |
| ✅ | [`Couverture.java`](outils/Couverture.java) | En place — spécification ↔ code, dans les deux sens |
| 🔜 | `Rejeu.java` | À écrire. Le [guide 7](guides/7-VERSIONNER.md) annonce le test décisif de version comme « une exécution » qui « démasque en trente secondes » un majeur déguisé en correctif. **Cette exécution n'existe pas** |
| 🔜 | `Nouvelle.java` | À écrire. Engendre le squelette d'une spécification, ses identités, et l'ébauche de traduction qui les porte |
| 🔜 | `Passation.java` | À écrire. Le paquet de passation, reproductible, au lieu d'un dossier assemblé à la main |

## Les cinq fichiers de réglage

Aucun ne demande d'écrire du Java. C'est ce qui ferme le **défaut 8** de l'analyse : le
dépôt annonçait un code de retour « utilisable tel quel en intégration continue », et
personne ne s'en servait.

*Ces cinq-là sont des fichiers de **configuration**, pas des pages : ils ne figurent pas
sur le site publié, et les liens ci-dessous pointent donc vers le dépôt.*

| Fichier | Ce qu'il installe |
|---|---|
| [`.github/workflows/verifier.yml`](https://github.com/Beennnn/cadre-specification-algorithmique/blob/main/.github/workflows/verifier.yml) | L'étage 1 sur chaque poussée : le vérificateur, **le verdict figé du corpus de défauts**, la fraîcheur du registre et des rapports engendrés, Vale, et la construction du site |
| [`.github/CODEOWNERS`](https://github.com/Beennnn/cadre-specification-algorithmique/blob/main/.github/CODEOWNERS) | Le valideur métier par fichier |
| [`.github/rulesets/specifications.json`](https://github.com/Beennnn/cadre-specification-algorithmique/blob/main/.github/rulesets/specifications.json) | La protection de branche, à importer dans les réglages |
| [`mkdocs.yml`](https://github.com/Beennnn/cadre-specification-algorithmique/blob/main/mkdocs.yml) | Le site, publiable depuis le dépôt tel quel — sans dossier `docs/` ni copie |
| [`.vale.ini`](https://github.com/Beennnn/cadre-specification-algorithmique/blob/main/.vale.ini) + [`styles/Methode/`](styles/Methode/) | La règle de vocabulaire, portée aux seules spécifications |

Le premier fichier va plus loin que le seul étage 1 : il ferme aussi le **défaut 3**, en
régénérant `registre.json` et les rapports de couverture puis en échouant sur tout écart.
Ces artefacts étaient commités sans que rien ne garantisse qu'ils correspondaient encore
aux documents.

## Trois pièges, trouvés en exécutant

*Ces trois-là ne se lisent pas dans une documentation : ils se découvrent en lançant la
commande. Ils sont corrigés dans les fichiers livrés, et notés ici pour qu'on ne les
réintroduise pas.*

| Piège | Ce qui se passe | Le correctif |
|---|---|---|
| **`docs_dir: .`** | MkDocs **refuse** un `docs_dir` qui soit le dossier parent du fichier de configuration. La configuration évidente ne démarre pas | le greffon [`mkdocs-same-dir`](https://github.com/oprypin/mkdocs-same-dir), fait exactement pour ça |
| **Les ancres GitHub** | MkDocs engendre par défaut des ancres qui ne sont pas celles de GitHub. Les liens du type `SPEC-WTH-001.en.md#rg-030--iterative-outlier-rejection` cessent **tous** de résoudre — sans erreur, ce qui est pire | `slugify` de `pymdownx` dans l'extension `toc`. Vérifié : on passe de dizaines d'ancres cassées à **zéro** |
| **`etc.` invisible pour Vale** | Vale encadre chaque motif de limites de mot. `\betc\.\b` exige un caractère de mot **après** le point, qui ne vient jamais en fin de phrase : la règle paraissait active et laissait filer le cas le plus fréquent | `nonword: true`, et les limites écrites à la main |

> **Le troisième est le plus instructif** : un contrôle qui ne se déclenche jamais se lit
> exactement comme un contrôle qui marche. C'est la conclusion de
> [l'analyse de l'outillage](outils/ANALYSE-DE-L-OUTILLAGE.md), et elle vaut aussi pour
> les outils qu'on n'a pas écrits.

## Un prérequis, avant de croire aux annotations

`Verifier.java` construit ses constats sur `record Constat(niveau, regle, fichier,
message)` : **il n'y a pas de numéro de ligne**. Une annotation ne peut donc se poser
aujourd'hui que sur le fichier entier, ce qui perd l'essentiel du bénéfice. Ajouter la
ligne au constat est la première tâche à faire — la plupart des contrôles la connaissent
déjà, puisqu'ils travaillent sur des `Matcher` dont la position se lit.

## Ce qu'on refuse

| Ce qu'on refuse | L'argument |
|---|---|
| Une **suite de gestion d'exigences** | Elle vend la traçabilité et l'identité durable, que le dépôt tient déjà avec une annexe d'UUID et `registre.json`, pour zéro licence. Elle coûte en échange ce qui compte le plus : la spécification cesse d'être **un fichier à côté du code** |
| Un **éditeur dédié**, en formulaire | Il supprime la page blanche, et supprime avec elle la possibilité d'écrire une phrase que le formulaire n'a pas prévue. Le formalisme est léger **par décision** |
| Un **langage dédié** exécutable | « Une spécification exécutable serait un programme — écrit dans un langage de plus, sans écosystème, sans outillage, et que le métier ne pourrait plus relire » ([FAQ](FAQ.md)) |
| Un **wiki** | Pas de comparaison de versions exploitable, pas d'étiquette qui gèle, pas de revue attachée à la ligne. C'est le traitement de texte partagé sous un autre nom |
| Un **second vérificateur** | Le dépôt l'a fait, puis l'a retiré : deux fois la surface de bug pour un bénéfice ponctuel. Ce que la double implémentation apportait est tenu par le [corpus de défauts](outils/jeu-d-essai/) |

## Démarrer

```bash
# Ce que fait l'intégration continue, en local
java outils/Verifier.java

# Le site, en aperçu
pip install mkdocs-material mkdocs-same-dir
mkdocs serve

# Le vocabulaire
pip install vale && vale sync
vale exemples/
```

Côté forge, trois réglages une fois pour toutes : activer **Pages** sur la branche
principale, importer le **ruleset**, et vérifier que les noms des contrôles exigés
correspondent à ceux du fichier d'atelier.
