# État du chantier

*Dernière mise à jour : 2026-08-21 (9)*

---

## Où on en est

Le socle est **complet et cohérent** : la méthode est écrite, outillée par des modèles, et
illustrée de bout en bout par un fil rouge dont tous les chiffres sont vérifiés.

| Volet | État |
|---|---|
| Le principe et la frontière métier/technique | ✅ `CADRE.md` §1 |
| Le pseudo-langage | ✅ `CADRE.md` §2 |
| L'adaptation au calcul scientifique | ✅ `CADRE.md` §2.8 |
| L'anatomie d'une spécification | ✅ `CADRE.md` §3, `templates/MODELE-SPECIFICATION.md` |
| La fiche de contraintes | ✅ `CADRE.md` §4 |
| Le jeu d'essai et l'oracle | ✅ `CADRE.md` §5 |
| La gouvernance et le versionnement | ✅ `CADRE.md` §6 |
| L'adoption progressive et les anti-patterns | ✅ `CADRE.md` §7-8 |
| **Temps 1 — découper** | ✅ `guides/1-DECOUPER.md` |
| **Temps 2 — glossaire puis écriture collaborative** | ✅ `guides/2-GLOSSAIRE.md`, `guides/4-ECRIRE-A-PLUSIEURS.md` |
| **Temps 3 — passation au développement** | ✅ `guides/6-PASSER-AU-DEVELOPPEMENT.md` |
| Fondements théoriques et sources | ✅ `REFERENCES.md` |
| Objections fréquentes | ✅ `FAQ.md` |
| Fil rouge complet | ✅ `exemples/fil-rouge/` — 6 documents |
| Deux vignettes de contraste | ✅ `exemples/mass-balance/`, `SPEC-THM-001` |
| Une chaîne complète besoin → code → écarts | ✅ `exemples/mass-balance/`, code exécutable et qualifié |

## Priorité décidée pour la reprise

**L'outillage.** Le dépôt affirme que « l'incohérence doit être détectable mécaniquement »
et que l'étage 1 nettoie avant les humains : cette promesse doit être tenue entièrement.
**25 des 41 contrôles sont désormais mécanisés** — `C-03` (fantôme) l'a été en dernier,
et a immédiatement trouvé deux défauts réels dans nos propres exemples. Restent `C-05`
(valeur magique), `C-09` (complétude d'une table de décision), `C-12` (entrée facultative
sans comportement d'absence), et ceux qui demandent un jugement.

## Décisions structurantes déjà prises

À ne pas rouvrir sans raison — elles ont chacune coûté une itération.

| # | Décision | Pourquoi |
|---|---|---|
| 1 | **Le dépôt est une méthode, pas un entrepôt** | Les specs réelles vivent dans le dépôt du produit. Une première version cataloguait des fonctions réelles : abandonnée |
| 2 | **Un seul fil rouge**, décliné dans tous les guides | Mieux qu'une collection d'exemples jetables : le lecteur capitalise |
| 3 | **Le fil rouge est l'autonomie d'un véhicule électrique** | Universel, avec de vraies équations, assez complexe, et il se découpe en 12 fonctions sur 5 valideurs métier. Deux fils rouges antérieurs écartés : la commande en ligne (pas d'équations), la détection de pics (trop proche de l'instrumentation) |
| 4 | **Les deux vignettes sont conservées** | Elles aboutissent à trois conclusions opposées sur le type numérique — c'est la démonstration que la spécification décide, pas l'habitude |
| ~~5~~ | ~~Ancrage dans le code~~ — **annulée par la décision 18** : la spécification ne pointe jamais vers le code | |
| 6 | **Pas de langage formel** (B, TLA+, Alloy) | Une spec que le métier ne peut pas relire est une spec que le métier n'écrit pas. Argumenté dans `REFERENCES.md` |
| 7 | **Échelle de maturité en 5 niveaux** | C'est ce qui rend la démarche applicable à un existant sans tout spécifier |

## Ce qui reste ouvert

| # | Sujet | Nature |
|---|---|---|
| ~~A~~ | ~~L'option d'ancrage~~ — **tranché : l'ancrage est RETIRÉ du cadre.** Le but est d'écrire un besoin ; le code appartient à celui dont c'est le métier, avec toute son autonomie. La citation va **du code vers la spécification**, jamais l'inverse | fait |
| ~~B~~ | ~~Le contrôle des ancrages~~ — sans objet depuis le retrait de A | sans objet |
| C | **Monter `FN-004` (planifier les recharges) au niveau 4** | la fonction est au niveau 3 ; c'est le meilleur terrain pour illustrer « décrire un résultat, pas un parcours » sur une optimisation sous contrainte, et le résultat contre-intuitif des deux arrêts courts |
| D | **Un exemple « avant / après »** : une même règle mal spécifiée puis bien spécifiée | très pédagogique, pas encore écrit |
| E | **Le nom du dépôt** — `cadre-specification-algorithmique` est étroit depuis que l'architecture fonctionnelle est couverte | renommage GitHub indolore ; `atlas-fonctionnel` avait été proposé |
| F | **Protéger `main`** en exigeant une revue, et créer un label `question-ouverte` | cohérence : le dépôt doit s'appliquer à lui-même sa propre gouvernance (`CADRE.md` §6.3) |
| G | **Une version diaporama** pour présenter la démarche en interne | évoqué, non commencé |
| ~~H~~ | ~~Mécaniser `C-05` à `C-13`~~ — **fait pour `C-08`, `C-10`, `C-11`, `C-13`**, ainsi que `C-35`/`C-36` dans la passe globale. Restent `C-05`, `C-09`, `C-12` | partiellement fait |
| H2 | ~~Mécaniser `C-05` à `C-13`~~ (ancienne ligne) (valeur magique, `IF` sans `ELSE`, arrondi sans sens, superlatif sans départage) — demande une analyse du pseudo-langage, pas seulement du markdown | à construire |
| I | ~~Les avertissements `C-01`/`C-02`~~ — **tranché** : le vocabulaire des règles a été aligné sur les identifiants du contrat. Zéro avertissement | fait |
| J | **Mécaniser `C-25`** (orthographe identique entre contrat et règles) — proche de `C-01`, réalisable | à construire |
| ~~O~~ | ~~Un corpus de défauts connus~~ — **fait** : `outils/jeu-d-essai/`, verdict figé à 19 échecs et 3 avertissements | fait |
| ~~P~~ | ~~Le vérificateur en Java~~ — **fait** : `outils/Verifier.java`, fichier unique sans dépendance ni construction. Reste à décider s'il doit être empaqueté en module Maven et intégré à la chaîne de construction de l'organisation | fait ; empaquetage à arbitrer |
| L | **Enrichir les glossaires d'illustrations** — chaque concept nommé, décrit **et illustré**. Les entrées existent, les illustrations manquent | à écrire |
| M | **Appliquer la chaîne de traitement `ET-xx` aux deux vignettes** — seul `SPEC-NRG-001` la porte | à écrire |
| ~~N~~ | ~~Mécaniser `C-35`/`C-36` dans la passe globale~~ — **fait** | fait |
| ~~K~~ | ~~Les fiches de données en fichiers séparés~~ — **tranché : un catalogue unique.** La valeur est dans la vue d'ensemble ; le diagramme de cheminement et le tableau d'impact n'ont de sens que rassemblés | fait |

## Reste dans l'autre dépôt

Une PR de transit, désormais obsolète, subsiste sur `Beennnn/scratch` :
[#1](https://github.com/Beennnn/scratch/pull/1). Elle contenait la première version du
contenu avant que ce dépôt n'existe, et décrit encore un exemple qui n'existe plus.
**Elle peut être fermée** — décision du titulaire du compte.
