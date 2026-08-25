# Guide 0 — Raconter : les personas, les récits, et l'écran qui contraint

*Ce guide passe avant le découpage. Il répond à une question que les neuf autres
supposaient résolue : **d'où vient le besoin**, et qui a le droit de dire qu'une fonction
manque.*

---

## Pourquoi ce guide passe avant le découpage

La méthode commençait au [découpage](1-DECOUPER.md) : on liste les résultats métier, on
les nomme, on leur donne un valideur. C'est le bon geste, mais il répond à « **quelles
fonctions ?** » sans avoir répondu à « **pour qui, quand, et vu d'où ?** ».

Le fil rouge en administre la preuve, sans l'avoir cherché. Sur ses douze fonctions,
**quatre appartiennent à l'Expérience client** — et trois d'entre elles n'existent que
parce qu'il y a un écran :

| Fonction | Ce qu'elle décide | Ce qui la rend nécessaire |
|---|---|---|
| [`FN-007`](../exemples/fil-rouge/1-DECOUPAGE.md) | une décision **et son motif** | l'écran doit dire *pourquoi*, pas seulement *non* |
| `FN-011` | l'autonomie **affichée**, arrondie | un humain lit un nombre, pas un flottant |
| `FN-012` | conserver le plan courant, ou le remplacer | un plan qui change à chaque kilomètre est inutilisable |

Et le [découpage du fil rouge](../exemples/fil-rouge/1-DECOUPAGE.md) raconte comment
`FN-012` a été trouvée : *« et si on recalcule un kilomètre plus loin et que le plan
change, **on affiche quoi ?** »* — la question d'un développeur, en fin d'atelier.
Personne n'avait la réponse.

> **Elle a été trouvée par accident.** Mettre l'écran en premier, c'est cesser de compter
> sur l'accident. Le guide 1 emploie d'ailleurs déjà les parcours utilisateurs — mais
> **vingt minutes sur cent vingt, et comme contrôle final**. Ce qui sert à vérifier un
> découpage sert mieux à le produire.

## L'objection à lever tout de suite

Le guide 1 range « **le découpage par écran** » parmi les anti-patterns : une fonction par
page d'interface, et « la spécification devient fausse au premier changement
d'interface ». **Cet anti-pattern reste vrai.** Ce guide ne le contredit pas — il dit
autre chose.

| Ce que l'écran ne dicte **pas** | Ce que l'écran dicte |
|---|---|
| la **structure** du découpage — une fonction par page | les **décisions** qu'un humain doit prendre, et ce qu'il lui faut pour les prendre |
| le nom des fonctions | ce qui est **connu** au moment de l'appel, et ce qui est **consommable** en sortie |
| la disposition, le composant, la technologie | la **tolérance** : ce que le lecteur ne pardonnera pas |

`FN-011` en est l'illustration exacte. Ce n'est pas « la fonction de l'écran tableau de
bord » — ce serait l'anti-pattern. C'est **estimer l'autonomie affichée**, et elle
survivra à trois refontes du tableau de bord, parce qu'elle porte une décision métier :
*l'autonomie s'arrondit vers le bas, jamais au plus proche.* L'écran a révélé la
décision ; il ne la possède pas.

## Ce que l'écran contraint, et que rien d'autre ne révèle

| Ce que l'écran fixe | Ce que ça impose à la spécification |
|---|---|
| **le déclencheur et sa fréquence** — qui agit, quand, combien de fois | une exigence de réalisation `EX-xx` sur le temps de réponse, parfois le choix d'un résultat approché plutôt qu'exact |
| **ce qui est connu au moment de l'appel** | les **entrées du contrat**. Une entrée que l'écran n'a pas est une entrée que la fonction ne peut pas exiger — ou qu'il faut aller chercher, et c'est une décision |
| **ce qui est affichable, et dans quel état est le lecteur** | la **forme de la sortie** : l'unité, le nombre de décimales, l'arrondi et son sens |
| **ce qu'il faut pouvoir expliquer** | une **sortie de plus** — le motif. Une fonction qui ne renvoie qu'un verdict interdit à l'écran de justifier |
| **ce qui ne doit pas sauter sous les yeux** | une **fonction entière** : décider quand remplacer un résultat par un meilleur résultat |

> **La dernière ligne est la plus contre-intuitive.** « Le plan ne doit pas changer tout le
> temps » ressemble à du confort d'interface. C'est en réalité un arbitrage métier —
> *vaut-il mieux un plan juste qui change, ou un plan stable qui vieillit ?* — avec des
> conséquences chiffrables, et il lui faut un valideur. C'est `FN-012`.

---

## Les personas

### Ce qu'un persona est, et ce qu'il n'est pas

Un persona n'est **ni un titre de poste, ni un profil marketing**. C'est quelqu'un défini
par trois choses, et par elles seules :

1. **un but** — ce qu'il cherche à décider ;
2. **un contexte d'usage** — où il est, de combien de temps et d'attention il dispose ;
3. **une tolérance** — ce qu'il pardonne, et ce qu'il ne pardonne pas.

« L'utilisateur » n'est pas un persona : il n'a ni contexte ni tolérance, donc il ne
contraint rien, donc il ne sert à rien.

> **À ne pas confondre avec le valideur métier.** Le persona **se sert** de la fonction ;
> le valideur **tranche** ce qu'elle doit faire. Dans le fil rouge, le conducteur est un
> persona ; l'Expérience client est un valideur. Ils ne figurent pas dans les mêmes
> tables et ne s'arbitrent pas de la même façon — mais c'est le persona qui donne au
> valideur la matière de son arbitrage.

### Ce qu'un persona déclare

| Champ | Pourquoi il y est |
|---|---|
| **Identifiant** `PE-xx` | pour que les récits le citent sans ambiguïté |
| **Nom du rôle** | jamais un prénom inventé : un rôle se discute, un prénom se défend |
| **Ce qu'il cherche à décider** | s'il ne décide rien, il n'a pas besoin du calcul |
| **Son contexte d'usage** | mains libres ? bruit ? combien de secondes ? seul ou devant un tiers ? |
| **Ce qu'il sait déjà** | ce qu'il peut fournir en entrée sans aller le chercher |
| **Ce qu'il ne pardonne pas** | la contrainte la plus forte, et la plus souvent oubliée |
| **Son valideur métier** | qui arbitre quand deux personas s'opposent |

### Les quatre familles

Tout produit algorithmique servant des humains rencontre les mêmes quatre familles. Elles
ne sont pas des catégories esthétiques : **chacune impose une contrainte différente à la
spécification**.

| Famille | Contexte | Ce qu'elle impose |
|---|---|---|
| **Celui qui décide dans l'instant** | attention minimale, action en cours | un chiffre unique, un arrondi assumé, une **stabilité** du résultat |
| **Celui qui prépare** | assis, du temps, veut comparer | des **variantes**, et des hypothèses rendues explicites |
| **Celui qui doit expliquer** | face à un tiers qui conteste | un **motif**, une trace, et la **rejouabilité** ([guide 7](7-VERSIONNER.md)) |
| **Celui qui surveille en masse** | *n* objets, cherche les exceptions | la **cohérence entre exécutions**, et des agrégats qui ne mentent pas |

### Les personas du fil rouge

Tirés du [cas métier](../exemples/fil-rouge/0-LE-CAS-METIER.md), et de lui seul.

| | Rôle | Cherche à décider | Contexte | Ne pardonne pas | Valideur |
|---|---|---|---|---|---|
| `PE-01` | **Le conducteur en trajet** | s'arrêter maintenant, ou continuer | au volant, en mouvement, une seconde d'attention | la panne, et le chiffre qui saute | Expérience client |
| `PE-02` | **Le conducteur qui prépare** | partir ou ne pas partir, et à quelle vitesse | avant le départ, assis, du temps devant lui | qu'on lui cache les hypothèses | Expérience client |
| `PE-03` | **Le conseiller après-vente** | répondre à un client qui conteste son autonomie | au téléphone, face à un mécontent | de ne pas pouvoir rejouer le calcul du jour même | Expérience client |

> **Une quatrième famille est volontairement absente.** Un gestionnaire de flotte serait
> un persona légitime — et il ferait apparaître des fonctions de cohérence entre
> véhicules qui n'existent pas ici. Il est laissé dehors parce que **le périmètre du fil
> rouge est un véhicule**. C'est la discipline qui compte : un persona s'ajoute quand le
> périmètre le porte, jamais parce que la liste paraîtrait plus complète.

---

## Le récit

### La forme canonique, et ce que chaque case fait vraiment

```
En tant que <persona>, je veux <ce que je fais à l'écran>, afin de <la décision que ça me permet>.
```

| Case | Ce qu'elle apporte | Si elle est vide ou fausse |
|---|---|---|
| **En tant que** | le contexte et la tolérance, par renvoi au persona | on écrit pour personne, donc sans contrainte |
| **Je veux** | l'action **observable**, du point de vue de celui qui agit | on décrit un traitement, pas un besoin |
| **Afin de** | le **pourquoi** — le seul juge de ce qu'est une bonne réponse | on ne saura pas arbitrer les cas limites |

> **Le « afin de » est la case qui décide de tout.** C'est lui qui dit si arrondir vers le
> bas est prudent ou mensonger, si trois secondes d'attente sont acceptables, si un
> résultat approché suffit. Un récit dont le « afin de » se déduit du « je veux » n'a pas
> de « afin de ».

### Les trois pièges de la forme

| Piège | Exemple | Pourquoi c'est fatal |
|---|---|---|
| **Le persona générique** | « en tant qu'utilisateur… » | aucun contexte, aucune tolérance : le récit ne contraint rien |
| **Le « afin de » tautologique** | « je veux voir l'autonomie **afin de** connaître l'autonomie » | la case existe, la question n'a pas été posée |
| **Le récit qui décrit le calcul** | « je veux que le système applique la formule de traînée » | ce n'est pas un récit, c'est une règle — elle appartient à la spécification, pas ici |

### Les récits du fil rouge

Les [cinq questions du conducteur](../exemples/fil-rouge/0-LE-CAS-METIER.md), mises en
forme. Elles étaient déjà là ; il leur manquait un persona et un « afin de ».

| | Le récit | Fonctions mobilisées |
|---|---|---|
| `R-01` | En tant que **conducteur en trajet**, je veux voir si j'arrive à destination sans m'arrêter, afin de décider **maintenant** si je dois prévoir une pause | `FN-001` `FN-002` `FN-011` |
| `R-02` | En tant que **conducteur en trajet**, je veux comprendre **pourquoi** le système annonce que je n'arrive pas, afin de choisir entre ralentir et m'arrêter | `FN-007` `FN-003` |
| `R-03` | En tant que **conducteur qui prépare**, je veux comparer l'effet de ma vitesse et de la météo, afin de partir avec une marge **que j'ai choisie** | `FN-001` `FN-008` `FN-010` |
| `R-04` | En tant que **conducteur en trajet**, je veux qu'on me dise où m'arrêter et combien de temps, afin de ne pas improviser à 15 % de batterie | `FN-004` `FN-005` `FN-006` |
| `R-05` | En tant que **conducteur en trajet**, je veux que le plan affiché ne change pas à chaque kilomètre, afin de pouvoir m'y fier | `FN-012` |
| `R-06` | En tant que **conseiller après-vente**, je veux rejouer le calcul tel qu'il a été fait ce jour-là, afin de répondre à un client qui conteste | *quadruplet de rejouabilité* ([guide 7](7-VERSIONNER.md)) |

> **`R-02` et `R-05` n'existent que parce qu'il y a un écran.** Sans eux, `FN-007` ne
> renverrait qu'un verdict, et `FN-012` n'existerait pas. C'est toute la thèse de ce
> guide, sur deux lignes.

---

## De l'écran à la spécification — la charnière

Le récit ne remplace pas la spécification : **il l'oriente, puis il s'efface**. Voici où
va chaque chose.

| Ce que l'écran révèle | Où ça atterrit |
|---|---|
| ce que l'utilisateur fournit ou que le contexte connaît | les **entrées** du contrat (§4) |
| ce qu'il faut afficher, et sous quelle forme | les **sorties**, avec unité et décimales (§5) |
| l'arrondi et son sens | une **règle** `RG-xxx`, jamais un détail de rendu |
| le besoin d'expliquer un refus | une **sortie de plus**, et un **cas d'erreur** `E-xxx` |
| le temps de réponse acceptable | une **exigence** `EX-xx`, avec sa source et sa vérification |
| ce que le persona ne pardonne pas | un **invariant** `INV-xx`, ou un cas de test qui le garde |
| la maquette, la disposition, le composant | **nulle part** — voir plus bas |

### Sur `R-01`, en entier

| Étape | Ce que ça donne |
|---|---|
| Le récit | *conducteur en trajet · voir si j'arrive · décider maintenant* |
| L'écran, décrit en mots | un bandeau permanent, un chiffre en kilomètres, une couleur, mis à jour en roulant |
| Ce que l'écran **a** | la destination, la position, l'état de charge, la température extérieure |
| Ce que l'écran **n'a pas** | la météo à l'arrivée → ou bien on va la chercher, ou bien le contrat ne peut pas l'exiger. **C'est une décision, et elle se prend ici** |
| Ce que l'écran **montre** | une distance en km, entière → `FN-011`, et l'arrondi **vers le bas** |
| Ce que « maintenant » impose | un temps de réponse compatible avec la conduite → une `EX-xx` |
| Ce que « décider » impose | si la réponse est non, un motif → `R-02`, donc `FN-007` |

## Les critères d'acceptation, et où ils ne vont pas

Un récit sans critères d'acceptation n'est pas fini. Mais **ces critères ne sont pas les
cas de test des fonctions**, et les confondre casse la traçabilité dans les deux sens.

| | Critère d'acceptation d'un récit | Cas de test `CT-xx` |
|---|---|---|
| Le niveau | l'écran, du point de vue du persona | une fonction, du point de vue du contrat |
| L'énoncé | « quand je roule à 130 et qu'il reste 40 km de marge, le bandeau passe à l'orange » | « pour ce jeu d'entrées, la sortie vaut 106,017 km » |
| Qui le juge | le valideur métier, en regardant | le vérificateur, en comparant |
| Qui le porte | le récit | la spécification, §10 |

Un seul critère de récit mobilise souvent **plusieurs `CT-xx` répartis sur plusieurs
fonctions**. C'est normal, et c'est exactement pourquoi les deux listes restent séparées.

## Le contrôle qui manquait : quelle fonction n'a pas de récit ?

Une fois les récits écrits, la table « récit → fonctions » se lit **à l'envers**. Une
fonction qu'aucun récit ne mobilise est dans l'un de deux états :

- **légitime** — elle est interne, mobilisée par une autre fonction et jamais par un
  humain. Dans le fil rouge, `FN-009` (correction par l'historique du conducteur) est dans
  ce cas, et c'est écrit ;
- **suspecte** — personne ne s'en sert, et personne ne l'avait remarqué.

C'est le pendant exact de `C-14` (« chaque règle figure dans la table de couverture »),
un cran plus haut. Voir la fin de ce guide pour ce qu'il resterait à mécaniser.

## La frontière ne bouge pas

`C-40` interdit toute contrainte d'implémentation dans une spécification. **Ce guide n'y
fait pas exception, et n'en demande pas.**

| | |
|---|---|
| **Ce qui entre dans la spécification** | les entrées disponibles, la forme et l'unité des sorties, l'arrondi et son sens, le besoin d'un motif, une exigence de temps de réponse, une exigence de stabilité |
| **Ce qui n'y entre jamais** | la maquette, le nom du composant, la disposition, la technologie, le nombre de clics |

> **La maquette est un instrument d'élicitation, pas un artefact de spécification.** Elle
> sert à faire dire au métier ce qu'il n'aurait pas dit autrement ; elle ne se range pas
> dans le dépôt, et rien ne la cite. Ce qu'elle a révélé, oui — sous la forme d'une
> entrée, d'une règle ou d'une exigence.

---

## L'atelier de récits

**Durée** 1 h 30 · **Qui** 2 à 4 personnes du métier, 1 développeur, 1 personne qui
connaît les usages réels · **Sortie** les personas et les récits, avant tout découpage

| Temps | Ce qu'on fait |
|---|---|
| 15 min | Les personas. Trois questions chacun : que décide-t-il, dans quel contexte, que ne pardonne-t-il pas |
| 30 min | Les récits, à la forme canonique. Un par décision, pas un par écran |
| 20 min | Pour chaque récit : **que voit-on, et qu'a-t-on sous la main au moment où on le voit ?** L'écran se décrit en mots, au tableau |
| 15 min | Les critères d'acceptation du récit — au niveau de l'écran, pas du calcul |
| 10 min | Le valideur métier de chaque récit |

**Deux règles d'animation :**

- Dès que quelqu'un dessine un composant, on revient au « afin de ». Un désaccord sur le
  composant cache presque toujours un désaccord sur la décision à prendre.
- Quand un récit mobilise une fonction que personne n'a nommée, **on ne la spécifie pas
  ici** : on la note, et elle entre dans l'atelier de découpage avec un récit qui la
  justifie déjà.

## Anti-patterns

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **Le récit-écran** | un récit par page, un persona par écran | on retombe sur l'anti-pattern du guide 1, par une autre porte |
| **Le persona sans tolérance** | trois lignes de biographie, rien sur ce qu'il ne pardonne pas | il n'apporte aucune contrainte, donc rien à la spécification |
| **Le récit sans valideur** | tout le monde l'approuve, personne ne l'arbitre | au premier cas limite, la décision retombera sur un développeur |
| **La maquette normative** | la spécification renvoie à un écran | la spécification meurt à la première refonte — et `C-40` est enfreint |
| **Le catalogue de récits** | on écrit tous les récits avant de spécifier quoi que ce soit | même faute que la carte exhaustive du guide 1 : l'effort passe avant le premier bénéfice |

## Ce qui reste à arbitrer

| # | Question | Ce qui en dépend |
|---|---|---|
| i | Les préfixes **`R-xx`** et **`PE-xx`** entrent-ils dans les conventions d'identifiants ? | `PE-xx` est visuellement proche de `P-xx` (paramètre). `AC-xx` (acteur) ou `PERS-xx` sont les autres candidats. Un identifiant n'étant **jamais réutilisé**, ce choix se fait une fois |
| ii | Les récits reçoivent-ils une **identité durable** (UUID), comme les autres objets ? | s'ils sont cités depuis les spécifications, oui — et il faut alors les faire connaître de `Identites.java` |
| iii | Faut-il un contrôle mécanique « toute fonction est mobilisée par un récit, ou déclarée interne » ? | c'est le pendant de `C-14`, et il demande que la table récit → fonctions soit lisible par le vérificateur |
| iv | Les récits vivent-ils **avec les spécifications**, ou dans le dépôt du produit à part ? | la règle du dépôt est que la spécification vit à côté du code. Le récit précède la spécification : il n'a pas nécessairement le même cycle de vie |
