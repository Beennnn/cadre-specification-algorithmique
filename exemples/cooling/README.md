# Le refroidissement d'une boisson — le continu et l'approché

**Le régime qu'aucun autre exemple ne montre : une grandeur physique intrinsèquement
incertaine.** Une équation différentielle, une mesure à ±2 °C près, et un résultat dont on
discute la *justesse du modèle* — pas celle du calcul.

| | |
|---|---|
| **La spécification** | [en anglais](spec/SPEC-THM-001.en.md) — fait foi · [en français](spec/SPEC-THM-001.fr.md) |
| **Jusqu'où va l'exemple** | la spécification seule : pas de code, pas de rapports |

---

## Ce qu'il apporte

### Trois niveaux d'exactitude, à ne jamais confondre

C'est le §8.2 de la spécification, et c'est ce qu'elle a de plus transposable :

| | Reproductibilité | Justesse numérique | Validité du modèle |
|---|---|---|---|
| La question | Deux implémentations conformes donnent-elles le même nombre ? | Le nombre est-il la vraie solution de l'équation ? | L'équation décrit-elle la réalité ? |
| Se juge contre | la spécification | la solution analytique | des mesures réelles |
| Tolérance | 10⁻⁹ | 10⁻⁶ | ± 2 °C à 60 min |

> Un écart de 1,5 °C entre la prévision et un thermomètre réel peut être parfaitement
> normal — c'est le modèle qui est approché, pas le programme qui est faux. Un écart de
> 10⁻⁶ entre deux implémentations, lui, ne l'est jamais. **Sans cette distinction, chaque
> écart de mesure devient un ticket de bogue.**

### L'ordre des opérations change le résultat

Ajouter le lait **tôt** puis attendre, ou attendre puis ajouter le lait **tard**, ne donne
pas la même température finale — alors que la quantité de lait, sa température et la durée
totale sont identiques. `CT-02` le chiffre : **55,18 °C** contre **54,52 °C** à quinze
minutes.

L'explication tient en une phrase : le refroidissement est proportionnel à l'écart à la
température ambiante, donc une boisson déjà refroidie par le lait perd ensuite sa chaleur
**plus lentement**. Aucun développeur ne peut le deviner, et aucun ne doit avoir à en
décider.

> `CT-02` est aussi un excellent détecteur d'implémentation paresseuse : celle qui applique
> le mélange en fin de calcul « parce que c'est plus simple » donne 54,5160 dans les deux
> variantes, et **passe tous les autres cas de test**.

### Une seule spécification, deux implémentations opposées, toutes deux conformes

L'usage embarqué (moins d'une milliseconde, sans réseau, contrainte d'énergie) impose la
solution en forme fermée. L'usage laboratoire, qui veut une température ambiante variable,
impose un intégrateur numérique à pas adaptatif. **Le même jeu d'essai valide les deux** —
et cela n'est possible que parce que la spécification n'a imposé aucune méthode de
résolution.

C'est la meilleure démonstration du dépôt que la frontière métier / technique a été tracée
au bon endroit.

---

## Le contraste avec le bilan de masse

Les deux exemples traitent des grandeurs de nature opposée, et c'est délibéré :

| | [Bilan de masse](../mass-balance/) | Refroidissement |
|---|---|---|
| Les grandeurs | **discrètes et exactes** | **continues et approchées** |
| Le résultat | juste ou faux ; la conservation en fait une propriété vérifiable | approché ; on discute reproductibilité, justesse, validité |
| Le type numérique imposé | `Scaled` — décimal exact, binaire flottant exclu | `Real` — la double précision suffit largement |
| Le faux ami central | l'arrondi et le sort du résidu | l'ordre des opérations |

> **La même méthode, appliquée à deux domaines, produit deux conclusions opposées sur le
> type numérique.** C'est le meilleur argument qu'elle ne présuppose rien : ce sont les
> exigences chiffrées qui décident, pas l'habitude du développeur.

---

## Ce que l'exemple ne fait pas

Il s'arrête à la spécification. Ni code, ni jeu de données exécutable, ni rapport de
couverture — contrairement à [average-speed](../average-speed/),
[mass-balance](../mass-balance/) et [weather-summary](../weather-summary/).

Il assume aussi **deux trous nommés plutôt que cachés** : `RG-060` (non-convergence) et
`E-HORIZON-001` ne sont couverts par aucun cas de test, et `Q-03` demande s'il faut les
conserver. Une couverture incomplète et déclarée vaut mieux qu'une couverture complète et
fausse.

← [Retour aux exemples](../README.md)
