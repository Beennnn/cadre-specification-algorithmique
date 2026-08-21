# Défauts attendus sur le corpus

*Toute implémentation du catalogue doit trouver exactement ces constats sur
[SPEC-FAUX-001](SPEC-FAUX-001-corpus-de-defauts.md) — ni plus, ni moins.*

C'est le **jeu de données de référence du vérificateur lui-même**, et il sert deux fois :
comme **oracle de non-régression** de l'implémentation en place, et comme critère de
conformité de toute réimplémentation — dans un autre langage, ou après une refonte.

**Verdict attendu : 28 échecs, 3 avertissements.**

| Règle | Nb | Ce qui doit être signalé |
|---|---|---|
| `LIEN` | 1 | lien cassé vers `CE-FICHIER-N-EXISTE-PAS.md` |
| `C-03` | 1 | `taux_inconnu` employé dans `RG-030`, déclaré nulle part (**fantôme**) |
| `C-04` | 1 | `P-02` déclaré mais employé nulle part |
| `C-08` | 1 | `RG-010` : un bloc `SI` pour zéro `SINON` |
| `C-10` | 1 | `RG-010` : `ARRONDIR` à deux arguments au lieu de trois |
| `C-14` | 2 | `RG-020` et `RG-030` absents de la table de couverture |
| `C-17` | 1 | `RG-020` défini plusieurs fois — **une seule fois**, pas une par occurrence |
| `C-19` | 1 | en-tête `1.4.0` ≠ dernière ligne d'historique `1.3.0` |
| `C-20` | 1 | la version `1.3.0` déclare un impact sans incrémenter le majeur |
| `C-21` | 1 | `Q-01` sans décideur ni échéance |
| `C-24` | 1 | la version `1.3.0` déclare un impact sans renvoyer à une notice |
| `C-26` | 2 | `EX-02` sans vérification ; `EX-03` sans source, propriétaire ni vérification |
| `C-28` | **12** | l'annexe des identités est absente : un constat **par objet identifié** — le document, `RG-010`, `RG-020`, `RG-030`, `CT-01`, `P-01`, `P-02`, `Q-01`, `Q-02`, `EX-01`, `EX-02`, `EX-03` |
| `C-38` | 1 | `quantité_livrée` n'est pas en ASCII `snake_case` |
| `C-39` | 1 | le jeu d'essai ne trace ni provenance ni validation |
| `C-01` | 2 | `devise_facturation` et `quantite_livree` déclarées, employées dans aucune règle *(avertissement)* |
| `C-23` | 1 | l'en-tête ne nomme pas le glossaire de référence *(avertissement)* |

**`Q-02` ne produit aucun `C-21`** : c'est une question **fermée**, dont le décideur et
l'échéance n'ont plus à être renseignés. Le cas est là exprès — il distingue une
implémentation qui lit la bonne cellule de statut d'une qui lit la précédente.

**`C-15` n'apparaît pas**, bien que `CT-99` soit cité en couverture : `RG-020` étant absent
de la table, celle-ci n'est pas atteinte pour ce cas. C'est un **effet de bord assumé** du
contrôle, et le noter ici évite qu'on le prenne un jour pour une régression.

## Provenance et validation de ce jeu de référence

| | |
|---|---|
| **Provenance** | Fixture **construite à dessein** : chaque défaut a été introduit volontairement, un par règle mécanisée |
| **Comment il a été examiné** | Les deux implémentations ont été exécutées ; chaque constat a été confronté au défaut qu'il était censé révéler, et les trois divergences initiales ont été instruites une par une |
| **Ce que l'examen a produit** | Six corrections réelles. Premier tour : `C-17` signalait le même défaut une fois par occurrence côté Python ; `C-20` lisait la mauvaise colonne côté Java ; `C-24` manquait entièrement à Java. Second tour, après élargissement du corpus : `C-26` manquait à Java, `C-21` y lisait la mauvaise cellule quand la dernière était vide, et le décompte de couverture annoncé par le dépôt (« 31 des 41 ») était faux |
| **Verdict figé le** | 2026-08-21 |

## Comment s'en servir

```bash
java outils/Verifier.java outils/jeu-d-essai/SPEC-FAUX-001-corpus-de-defauts.md > /tmp/obtenu.txt
diff /tmp/attendu.txt /tmp/obtenu.txt && echo "verdict conforme"
```

Un écart avec le verdict figé est **toujours** à instruire, jamais à absorber : soit le
contrôle a régressé, soit le corpus a changé sans que le verdict suive. Et si deux
implémentations sont un jour confrontées, une divergence ne dit pas laquelle a tort : elle
dit que **le catalogue est ambigu** sur ce point. Dans tous les cas, c'est le catalogue
qu'on précise — jamais le verdict qu'on aligne sur ce que produit l'outil.
