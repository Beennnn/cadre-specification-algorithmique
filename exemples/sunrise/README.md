# Le lever du Soleil — l'équation sans solution

**Le régime qu'aucun autre exemple ne montre : une équation qui n'a pas toujours de
solution.** Au-delà des cercles polaires, le Soleil peut ne jamais se lever, ou ne jamais
se coucher. Ce n'est pas une erreur — c'est un résultat, qu'il faut nommer, typer et rendre.

| | |
|---|---|
| **La spécification** | [en anglais](spec/SPEC-AST-001.en.md) — fait foi · [en français](spec/SPEC-AST-001.fr.md) |
| **Jusqu'où va l'exemple** | la spécification seule : pas de code, pas de rapports |

---

## Ce qu'il apporte

### `arccos` n'est pas défini partout, et le signe dit lequel des deux cas

`RG-050` calcule un cosinus d'angle horaire qui **sort légitimement de `[−1, 1]`**. Hors de
cet intervalle, il n'existe aucun instant où le Soleil franchit la hauteur de référence — et
c'est le **signe** qui dit lequel des deux régimes on observe : jour polaire ou nuit polaire.

`CT-04` et `CT-05` ne diffèrent que par ce signe. Une implémentation qui testerait
`|cos ω| > 1` sans le regarder passerait les deux cas **en confondant jour et nuit
polaires** — l'erreur la plus grave que cette fonction puisse commettre.

C'est aussi pourquoi la grandeur interne `cos_hour_angle` est déclarée **sans borne** :
écrire un domaine `−1 .. 1` conduirait un développeur à valider l'entrée de l'arc cosinus
et à rejeter les cas polaires comme des erreurs.

### Le résultat n'est pas ce qu'on croit avoir demandé

« Le Soleil se lève » désigne **quatre instants différents** selon la hauteur de référence
retenue : centre du disque à l'horizon géométrique, centre avec réfraction, bord supérieur
avec réfraction, bord supérieur sans réfraction. Ils s'écartent de plusieurs minutes.

Le paramètre `P-01` porte donc **la définition même du résultat**. Un développeur qui
écrirait `0°` — le choix naturel — produirait un service dont toutes les valeurs sont
fausses, sans qu'aucun test ne le signale si les résultats attendus viennent du code.

### Un signe inversé donne un résultat plausible

Les deux conventions de longitude — est positive, ouest positive — coexistent dans la
littérature et dans les bases de données. Les confondre déplace le résultat de deux fois la
longitude : à Paris, **dix-neuf minutes**.

> **C'est arrivé pendant la rédaction de cet exemple.** L'erreur a été trouvée en
> confrontant le lever parisien aux éphémérides publiées, pas en relisant le calcul — un
> écart de dix-neuf minutes reste parfaitement plausible sur un affichage. C'est ce qui a
> motivé le paramètre `P-02`, l'invariant `INV-04` (symétrie par la longitude, testée sur
> des entrées engendrées) et le cas `CT-03`, à Quito, avec une longitude ouest.

### Un arrondi asymétrique, et pourquoi

`P-03` arrondit le lever **au supérieur** et le coucher **à l'inférieur** : la fenêtre de
jour annoncée est donc légèrement plus courte que la vraie. C'est le sens prudent pour les
usages qui en dépendent — extinction d'éclairage, fin d'un chantier extérieur. Un arrondi
au plus proche serait plus « juste » et moins sûr.

---

## Ce que l'exemple ne fait pas

Il s'arrête à la spécification. Ni code, ni jeu de données exécutable, ni rapport de
couverture — contrairement à [average-speed](../average-speed/),
[mass-balance](../mass-balance/) et [weather-summary](../weather-summary/). Son intérêt est
dans le document.

← [Retour aux exemples](../README.md)
