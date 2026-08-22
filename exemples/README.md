# Les exemples

*Quatre exemples, délibérément différents. Aucun n'est une illustration décorative : chacun
existe pour montrer un régime que les autres ne montrent pas.*

---

| Exemple | Jusqu'où il va | Ce qu'il montre que les autres ne montrent pas |
|---|---|---|
| **[Le bilan de masse d'un lot](bilan-de-masse/)** | **Besoin → spécification → contrat → code → écarts** | La **chaîne complète**, seule du dépôt. Le code s'exécute, les données de référence sont rejouées, et l'analyse des écarts conclut que la spécification est en défaut |
| **[Le refroidissement d'une boisson](SPEC-THM-001-refroidissement.md)** | Spécification seule | Le régime **continu et approché** : une équation différentielle, une incertitude, et un résultat dont on discute la *justesse* — pas la justesse d'un calcul, celle d'un modèle |
| **[Le lever et le coucher du Soleil](SPEC-AST-001-lever-coucher-du-soleil.md)** | Spécification seule | Une **équation qui n'a pas toujours de solution** : au-delà du cercle polaire, le Soleil peut ne jamais se lever. Ce n'est pas une erreur, c'est un résultat. Plus les **conventions d'angle**, où un signe inversé donne un résultat plausible et faux |
| **[L'autonomie d'un véhicule](fil-rouge/)** | Besoin → découpage → glossaire → données → spécification | Le **passage à l'échelle** : plusieurs fonctions, un catalogue de données partagé, une chaîne de traitement en dix étapes, et des fils d'exécution parallélisables |

---

## Le contraste qui porte le plus

Le bilan de masse et le refroidissement traitent des grandeurs de nature opposée, et
**c'est délibéré** :

| | Bilan de masse | Refroidissement |
|---|---|---|
| Les grandeurs | **discrètes et exactes** — une masse pesée existe au pas de la balance, pas en deçà | **continues et approchées** — une température est mesurée à 2 °C près |
| Le résultat | juste ou faux ; la conservation en fait une propriété **vérifiable** | approché ; on discute reproductibilité, justesse numérique et validité du modèle |
| Le type numérique imposé | `Scaled` — décimal exact, le binaire flottant est exclu | `Real` — la double précision suffit très largement |
| Le faux ami central | l'arrondi et le **sort du résidu** | l'**ordre des opérations** : refroidir puis mélanger n'est pas mélanger puis refroidir |

> **La même méthode, appliquée à deux domaines, produit deux conclusions opposées sur le
> type numérique.** C'est le meilleur argument qu'elle ne présuppose rien : ce sont les
> exigences chiffrées qui décident, pas l'habitude du développeur.

---

## Par où commencer

**Si vous découvrez la méthode** — [le bilan de masse](bilan-de-masse/), en suivant le
parcours de bout en bout. C'est le seul qui montre ce que le document devient une fois
passé aux développeurs.

**Si vous voulez juger la rigueur d'une spécification** —
[le refroidissement](SPEC-THM-001-refroidissement.md), et en particulier son traitement de
l'ordre des opérations et de l'incertitude.

**Si votre domaine est scientifique et que les conventions vous inquiètent** —
[le lever du Soleil](SPEC-AST-001-lever-coucher-du-soleil.md), qui montre comment une
spécification traite un cas sans solution et pourquoi une convention de signe se déclare.

**Si vous vous demandez comment cela tient sur un vrai périmètre** —
[le fil rouge](fil-rouge/), qui part d'un cas métier et va jusqu'à une spécification
complète en passant par le découpage fonctionnel.

---

## Ajouter un exemple

Un exemple mérite d'exister s'il montre un **régime** que les autres ne montrent pas : une
autre nature de grandeur, un autre type de faux ami, une autre échelle. Un quatrième
exemple de calcul discret et exact n'apprendrait rien de plus que le premier.

Chaque exemple complet suit la même numérotation — `1-BESOIN`, `2-SPEC-…`, `3-CONTRAT`,
`4-code/`, `5-ECARTS` — et passe le vérificateur :

```bash
java outils/Verifier.java
```
