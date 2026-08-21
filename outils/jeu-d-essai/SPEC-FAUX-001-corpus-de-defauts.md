# SPEC-FAUX-001 — Corpus de défauts connus

| | |
|---|---|
| **Identifiant** | SPEC-FAUX-001 |
| **Version** | 1.4.0 |
| **Statut** | Fixture — **ne pas corriger** |

> **Ce document est volontairement fautif.** Il sert à vérifier qu'une implémentation du
> catalogue de contrôles trouve bien ce qu'elle doit trouver. Les défauts attendus sont
> listés dans [ATTENDU.md](ATTENDU.md). Il est exclu du parcours normal du vérificateur.
>
> Lien volontairement cassé, pour le contrôle `LIEN` : [absent](CE-FICHIER-N-EXISTE-PAS.md)

## 4. Entrées

```
    montant_ht         : Décimal(EUR, 2 décimales, ≥ 0)
    devise_facturation : Chaîne(3 caractères)
    quantité_livrée    : Entier(≥ 1)
```

## 5. Sorties

```
    montant_ttc        : Décimal(EUR, 2 décimales, ≥ 0)
```

## 6. Paramètres

| Id | Libellé | Valeur | Unité | Qui peut le changer | Circuit de validation | Fréquence observée | Date d'effet |
|---|---|---|---|---|---|---|---|
| `P-01` | Taux de TVA | 0,200 | — | Conformité | Réglementaire | rare | 2026-01-01 |
| `P-02` | Paramètre jamais employé | 42 | — | Personne | — | jamais | 2026-01-01 |

## 7. Règles

### RG-010 — Montant toutes taxes

```
SI montant_ht > 0,00 ALORS
   montant_ttc = ARRONDIR(montant_ht × (1 + P-01), 2)
FIN SI
```

### RG-020 — Règle définie deux fois

```
SOIT x = montant_ht
```

### RG-020 — Règle définie deux fois

```
SOIT y = montant_ht
```

### RG-030 — Grandeur fantôme

```
SOIT z = montant_ht × taux_inconnu
```

## 10. Jeu d'essai

### CT-01 — Cas nominal

`montant_ht = 100,00` → `montant_ttc = 120,00`

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-010` | CT-01, CT-99 |

## 11. Contraintes et exigences

| Id | Énoncé | Source | Propriétaire | Vérification |
|---|---|---|---|---|
| `EX-01` | Le traitement s'exécute dans l'Union européenne | Politique `POL-DCP-1` | Protection des données | Revue d'hébergement |
| `EX-02` | Le code respecte le standard interne | Comité d'architecture | Architecture SI | |
| `EX-03` | La couverture de branches atteint 95 % | | | |

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| `Q-01` | Question sans décideur ni échéance | | | Ouverte |
| `Q-02` | Question tranchée, sans décideur ni échéance résiduels | | | Fermée |

## 13. Historique

| Version | Date | Changement | Impact sur les résultats |
|---|---|---|---|
| 1.0.0 | 2026-01-01 | Version initiale | — |
| 1.3.0 | 2026-02-01 | Changement du taux appliqué | **Oui** — tous les montants changent |
