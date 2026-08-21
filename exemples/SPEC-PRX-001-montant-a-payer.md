# SPEC-PRX-001 — Calcul du montant à payer d'une commande

| | |
|---|---|
| **Identifiant** | SPEC-PRX-001 |
| **Version** | 2.0.0 |
| **Statut** | Acceptée |
| **Auteur métier** | *(rôle : Responsable tarification)* |
| **Propriétaire de la règle** | *(rôle : Direction commerciale)* |
| **Glossaire de référence** | interne — §3 de ce document |
| **Répondant technique** | *(rôle : Architecte applicatif)* |
| **Date d'effet** | 2026-01-01 |
| **Dernière modification** | 2026-08-21 — voir §13 |

> **Note de lecture.** Ce document est un exemple pédagogique complet du cadre décrit
> dans [CADRE.md](../CADRE.md). Le domaine (un panier de commande) est volontairement
> universel : aucune connaissance métier n'est nécessaire pour juger si la
> spécification est claire, ce qui en fait un bon étalon.

---

## 1. Objectif et contexte

Calculer le **montant que le client doit payer** pour une commande, à partir de son
panier, des promotions en vigueur, de son solde de points de fidélité et de son adresse
de livraison.

Ce calcul est utilisé à trois moments, avec le **même résultat attendu** dans les trois
cas : l'affichage du panier, la validation de la commande, et l'édition de la facture.
Un écart entre ces trois moments est un incident de niveau 1.

## 2. Périmètre

**Dans le périmètre :**
- Le prix retenu par article, les remises de quantité et de panier.
- Le plafonnement global des remises.
- Les frais de livraison et leur franchise.
- L'imputation des points de fidélité.
- La TVA et les arrondis.
- Les cas d'erreur métier associés.

**Hors périmètre (traité ailleurs, ou pas traité) :**
- L'acquisition des points de fidélité (`SPEC-FID-002`).
- Les avoirs, retours et remboursements (`SPEC-RET-001`).
- Les commandes hors Union européenne, les droits de douane, l'autoliquidation.
- Les prix négociés des clients professionnels.
- Toute suggestion faite au client (« ajoutez 3,50 € pour la livraison offerte ») —
  voir `Q-01`.

## 3. Glossaire

| Terme | Définition |
|---|---|
| **Panier** | Ensemble non vide de lignes de commande, associé à un client et à une adresse de livraison. |
| **Ligne** | Une référence produit, une quantité, et le prix applicable à cette référence. |
| **Prix catalogue** | Prix de vente hors taxes de référence d'une référence produit. |
| **Prix promotionnel** | Prix de vente hors taxes temporaire, avec une date de début et une date de fin d'effet. |
| **Prix unitaire retenu** | Le prix effectivement utilisé pour le calcul (voir `RG-010`). |
| **Montant brut** | Prix unitaire retenu × quantité, avant toute remise. |
| **Remise de quantité** | Remise accordée sur une ligne du fait de la quantité commandée. |
| **Remise panier** | Remise accordée sur l'ensemble du panier, généralement via un code promotionnel. |
| **Montant net** | Montant hors taxes après toutes les remises. |
| **Franchise de port** | Seuil de montant net au-delà duquel les frais de livraison ne sont pas facturés. |
| **Point de fidélité** | Unité de compte convertible en réduction sur le montant à payer. |
| **Montant à payer** | Le résultat de ce calcul : le montant toutes taxes comprises effectivement dû. |

## 4. Entrées

```
panier :
    client_identifiant   : Identifiant(texte, 8 à 32 caractères)
    date_commande        : Horodatage(fuseau Europe/Paris)
    zone_livraison       : Énuméré { FRANCE_METRO, UE }
    code_promotionnel    : Identifiant(texte, 4 à 20 caractères) — facultatif
    points_fidélité_utilisés : Entier(≥ 0)
    lignes               : liste non vide de Ligne

Ligne :
    référence_produit    : Identifiant(texte, 3 à 20 caractères)
    quantité             : Entier(≥ 1)
    prix_catalogue_ht    : Montant(EUR, 2 décimales, > 0)
    prix_promotionnel_ht : Montant(EUR, 2 décimales, > 0) — facultatif
    promotion_début      : Date — obligatoire si prix_promotionnel_ht est présent
    promotion_fin        : Date — obligatoire si prix_promotionnel_ht est présent
    taux_tva             : Taux(0,000 .. 1,000, 3 décimales)
    poids_unitaire       : Poids(kg, 3 décimales, > 0)
```

**Préconditions** — si l'une n'est pas satisfaite, le calcul n'est pas effectué et
l'erreur correspondante du §9 est signalée :
- Le panier contient au moins une ligne.
- Chaque quantité est un entier strictement positif.
- La zone de livraison est desservie.
- Le nombre de points de fidélité utilisés est inférieur ou égal au solde du client.

## 5. Sorties

```
résultat :
    montant_a_payer_ttc      : Montant(EUR, 2 décimales, ≥ 0)
    montant_brut_ht          : Montant(EUR, 2 décimales)
    total_remises_ht         : Montant(EUR, 2 décimales, ≥ 0)
    montant_net_ht           : Montant(EUR, 2 décimales, ≥ 0)
    frais_livraison_ht       : Montant(EUR, 2 décimales, ≥ 0)
    tva_par_taux             : liste de (taux : Taux, assiette : Montant, montant : Montant)
    reduction_fidélité_ttc   : Montant(EUR, 2 décimales, ≥ 0)
    points_fidélité_débités  : Entier(≥ 0)
    détail_par_ligne         : liste de DétailLigne

DétailLigne :
    référence_produit  : Identifiant
    prix_unitaire_retenu : Montant(EUR, 2 décimales)
    montant_brut_ht    : Montant(EUR, 2 décimales)
    remise_quantité_ht : Montant(EUR, 2 décimales, ≥ 0)
    remise_panier_ht   : Montant(EUR, 2 décimales, ≥ 0)
    montant_net_ht     : Montant(EUR, 2 décimales, ≥ 0)
```

> Le `détail_par_ligne` n'est pas un confort d'affichage : il est **exigé** par la
> contrainte d'auditabilité et d'explicabilité (§11). Il fait partie du résultat, au
> même titre que le montant.

## 6. Paramètres

Ces valeurs **ne sont pas des règles**. Elles changent sans que la logique change.

| Id | Libellé | Valeur | Unité | Propriétaire | Circuit | Fréquence observée | Date d'effet |
|---|---|---|---|---|---|---|---|
| `P-01` | Seuil de franchise de port | 60,00 | EUR HT | Direction commerciale | Validation N+1 | 3 à 4 fois par an, hebdomadaire en soldes | 2026-01-01 |
| `P-02` | Barème de port — FRANCE_METRO | ≤ 1,000 kg → 4,90 ; ]1,000 ; 5,000] kg → 6,90 ; > 5,000 kg → 9,90 | EUR HT | Logistique | Validation Direction | 1 à 2 fois par an | 2026-01-01 |
| `P-03` | Barème de port — UE | ≤ 1,000 kg → 9,90 ; ]1,000 ; 5,000] kg → 14,90 ; > 5,000 kg → 22,90 | EUR HT | Logistique | Validation Direction | 1 à 2 fois par an | 2026-01-01 |
| `P-04` | Taux de TVA des frais de livraison | 0,200 | — | Conformité | Réglementaire | rare | 2026-01-01 |
| `P-05` | Quantité déclenchant la remise de quantité | 3 | articles | Direction commerciale | Validation N+1 | rare | 2026-01-01 |
| `P-06` | Taux de la remise de quantité | 0,1000 | — | Direction commerciale | Validation N+1 | 2 à 3 fois par an | 2026-01-01 |
| `P-07` | Plafond global de remise | 0,6000 | — du montant brut | Direction financière | Comité tarifaire | très rare | 2026-01-01 |
| `P-08` | Valeur d'un point de fidélité | 0,01 | EUR TTC | Marketing | Validation Direction | 1 fois par an | 2026-01-01 |
| `P-09` | Mode d'arrondi par défaut | COMMERCIAL | — | Conformité | — | jamais | 2026-01-01 |

`P-09` vaut `COMMERCIAL` : arrondi au plus proche ; en cas d'exacte moitié, on s'éloigne de zéro
(0,005 → 0,01). Sauf mention contraire, tout arrondi de montant se fait à 2 décimales.

## 7. Règles

### RG-010 — Prix unitaire retenu

```
Pour chaque ligne, le PRIX UNITAIRE RETENU est :

  SI un prix promotionnel existe
     ET que promotion_début ≤ date_commande ≤ promotion_fin
  ALORS
     le plus petit du prix catalogue et du prix promotionnel
  SINON
     le prix catalogue
  FIN SI
```

> **Pourquoi « le plus petit » et pas « le prix promotionnel ».** Une promotion saisie
> au-dessus du prix catalogue est une erreur de saisie ; le client ne doit jamais en
> pâtir. Voir `Q-02` sur la remontée d'alerte.

### RG-020 — Montant brut

```
SOIT montant_brut_ligne = prix_unitaire_retenu × quantité      (par ligne)
SOIT montant_brut_ht    = SOMME DES montant_brut_ligne DES lignes
```

### RG-030 — Remise de quantité

```
Pour chaque ligne :
  SI quantité ≥ P-05 ALORS
     remise_quantité_ligne = ARRONDIR(montant_brut_ligne × P-06, 2, P-09)
  SINON
     remise_quantité_ligne = 0,00
  FIN SI
```

La comparaison est **au sens large** : une quantité égale à `P-05` déclenche la remise.

### RG-040 — Éligibilité de la remise panier

| Code promotionnel | En vigueur à la date de commande | Cumulable avec la remise de quantité | Une remise de quantité s'applique | Résultat |
|---|---|---|---|---|
| absent | — | — | — | pas de remise panier |
| inconnu | — | — | — | erreur `E-PROMO-001` |
| connu | non | — | — | erreur `E-PROMO-002` |
| connu | oui | oui | — | remise panier appliquée (`RG-050`) |
| connu | oui | non | non | remise panier appliquée (`RG-050`) |
| connu | oui | non | oui | pas de remise panier (`RG-045`) |

### RG-045 — Non-cumul

Lorsqu'un code non cumulable rencontre une remise de quantité déjà appliquée, **la
remise de quantité est conservée et la remise panier est abandonnée**, sans erreur. Le
détail par ligne indique une remise panier nulle.

> Ce choix — conserver la remise déjà acquise plutôt que la meilleure des deux — est une
> décision de la Direction commerciale du 2025-11-14 : il est plus simple à expliquer au
> client qu'un arbitrage automatique.

### RG-050 — Montant de la remise panier

```
SOIT assiette_panier = SOMME DES (montant_brut_ligne − remise_quantité_ligne) DES lignes

SELON le type du code promotionnel :
  — taux    : remise_panier_brute = ARRONDIR(assiette_panier × taux, 2, P-09)
  — montant : remise_panier_brute = le plus petit du montant du code et de assiette_panier
```

La remise panier ne s'applique **jamais** aux frais de livraison.

### RG-060 — Plafond global de remise

```
SOIT total_remises_demandées = SOMME DES remise_quantité_ligne
                             + remise_panier_brute
SOIT plafond = ARRONDIR(montant_brut_ht × P-07, 2, P-09)

SI total_remises_demandées > plafond ALORS
   la remise panier est réduite de l'excédent :
   remise_panier_retenue = remise_panier_brute − (total_remises_demandées − plafond)
   SI remise_panier_retenue < 0,00 ALORS remise_panier_retenue = 0,00 FIN SI
SINON
   remise_panier_retenue = remise_panier_brute
FIN SI
```

> L'écrêtement porte sur la remise panier, jamais sur la remise de quantité : la remise
> de quantité est un droit acquis lié au volume commandé.
> Le cas où la seule remise de quantité dépasse déjà le plafond est théoriquement
> possible si `P-06` et `P-07` sont mal réglés ; la remise panier est alors nulle et la
> remise de quantité est conservée telle quelle. Ce n'est pas une erreur.

### RG-085 — Répartition de la remise panier et centime résiduel

La remise panier est répartie sur les lignes **au prorata de leur montant après remise
de quantité**, afin de pouvoir calculer la TVA par taux.

```
Pour chaque ligne :
   part = (montant_brut_ligne − remise_quantité_ligne) ÷ assiette_panier
   remise_panier_ligne = ARRONDIR(remise_panier_retenue × part, 2, P-09)

SOIT écart = remise_panier_retenue − SOMME DES remise_panier_ligne

SI écart ≠ 0,00 ALORS
   l'écart est imputé en totalité à la ligne dont le montant après remise de quantité
   est le plus élevé ; en cas d'égalité, à celle dont la référence produit est
   alphabétiquement la plus petite.
FIN SI
```

> **C'est le point le plus important du document.** Sans cette règle, deux
> implémentations correctes donnent deux totaux différents d'un centime, l'écart se
> retrouve en comptabilité, et personne ne sait qui a décidé quoi. Le départage
> alphabétique n'a aucune vertu métier : il existe uniquement pour que le résultat soit
> **reproductible**.

### RG-090 — Montant net

```
Pour chaque ligne :
   montant_net_ligne = montant_brut_ligne − remise_quantité_ligne − remise_panier_ligne
SOIT montant_net_ht = SOMME DES montant_net_ligne DES lignes
```

### RG-100 — Frais de livraison

```
SI montant_net_ht ≥ P-01 ALORS
   frais_livraison_ht = 0,00
SINON
   SOIT poids_total = SOMME DES (poids_unitaire × quantité) DES lignes
   frais_livraison_ht = la tranche de P-02 ou P-03 (selon zone_livraison)
                        correspondant à poids_total
FIN SI
```

La comparaison au seuil est **au sens large** : un montant net exactement égal à `P-01`
donne droit à la franchise. Les bornes du barème de poids sont incluses dans la tranche
inférieure (`1,000 kg` est facturé au tarif « ≤ 1 kg »).

### RG-110 — TVA

```
POUR CHAQUE taux DISTINCT présent dans les lignes
   assiette = SOMME DES montant_net_ligne DES lignes de ce taux
   tva      = ARRONDIR(assiette × taux, 2, P-09)
FIN POUR

SI frais_livraison_ht > 0,00 ALORS
   tva_livraison = ARRONDIR(frais_livraison_ht × P-04, 2, P-09)
FIN SI
```

La TVA est calculée **par taux sur l'assiette agrégée**, et non ligne par ligne puis
sommée : les deux méthodes diffèrent d'un centime dans certains cas, et c'est
l'agrégation par taux qui fait foi (règle comptable).

### RG-120 — Montant avant fidélité

```
SOIT montant_articles_ttc = montant_net_ht + SOMME DES tva de chaque taux
SOIT montant_port_ttc     = frais_livraison_ht + tva_livraison
SOIT montant_avant_fidélité_ttc = montant_articles_ttc + montant_port_ttc
```

### RG-130 — Imputation des points de fidélité

```
SOIT réduction_demandée = ARRONDIR(points_fidélité_utilisés × P-08, 2, P-09)

réduction_fidélité_ttc = le plus petit de réduction_demandée et montant_articles_ttc
points_fidélité_débités = réduction_fidélité_ttc ÷ P-08          (nombre entier)
```

Les points s'imputent sur le **montant TTC des articles uniquement**, jamais sur les
frais de livraison — voir `Q-03`. Les points non utilisés du fait du plafonnement **ne
sont pas débités** et restent acquis au client.

### RG-140 — Montant à payer

```
montant_a_payer_ttc = montant_avant_fidélité_ttc − réduction_fidélité_ttc
```

## 8. Invariants

| Id | Propriété |
|---|---|
| `INV-01` | `montant_a_payer_ttc ≥ 0,00` |
| `INV-02` | La somme des `montant_net_ligne` est exactement égale à `montant_net_ht` |
| `INV-03` | La somme des `remise_panier_ligne` est exactement égale à la remise panier retenue |
| `INV-04` | `total_remises_ht ≤ ARRONDIR(montant_brut_ht × P-07, 2)` — sauf dépassement par la seule remise de quantité (voir `RG-060`) |
| `INV-05` | Le calcul est **déterministe** : deux exécutions sur les mêmes entrées, à la même date de commande, donnent des résultats identiques au centime |
| `INV-06` | Le calcul est **idempotent** : recalculer un panier inchangé ne change aucun montant |
| `INV-07` | Ajouter une ligne à un panier ne peut jamais diminuer `montant_brut_ht` |
| `INV-08` | `points_fidélité_débités × P-08 = réduction_fidélité_ttc` |

## 9. Cas d'erreur métier

| Code | Condition | Conséquence | Message destiné au client |
|---|---|---|---|
| `E-PANIER-001` | Le panier ne contient aucune ligne | Aucun montant retourné | « Votre panier est vide. » |
| `E-QTE-001` | Une quantité est nulle ou négative | Aucun montant retourné | « La quantité demandée n'est pas valide. » |
| `E-ZONE-001` | La zone de livraison n'est pas desservie | Aucun montant retourné | « Nous ne livrons pas encore à cette adresse. » |
| `E-PROMO-001` | Le code promotionnel n'existe pas | Aucun montant retourné | « Ce code promotionnel n'existe pas. » |
| `E-PROMO-002` | Le code existe mais n'est pas en vigueur à la date de commande | Aucun montant retourné | « Ce code promotionnel n'est plus valable. » |
| `E-FIDELITE-001` | Les points utilisés dépassent le solde du client | Aucun montant retourné | « Vous ne disposez pas d'assez de points. » |

En cas d'erreur, **aucun montant partiel n'est retourné** : le client ne doit jamais
voir un total calculé sur une base invalide.

## 10. Jeu d'essai

Tous les résultats ci-dessous ont été **calculés à la main** et vérifiés par le
relecteur métier. Ils ne proviennent d'aucune implémentation.

### Vue d'ensemble

| Id | Ce qu'il exerce | Montant à payer attendu |
|---|---|---|
| `CT-01` | Cas nominal minimal, un article, port dû | **20,88 EUR** |
| `CT-02` | Franchise de port au seuil exact (`RG-100`, borne `≥`) | **72,00 EUR** |
| `CT-03` | Cas riche : promotion, remise de quantité, remise panier fixe, centime résiduel, deux taux de TVA, fidélité | **66,06 EUR** |
| `CT-04` | Centime résiduel **et** départage d'ex æquo (`RG-085`) | **40,68 EUR** |
| `CT-05` | Écrêtement par le plafond global (`RG-060`) | **53,88 EUR** |
| `CT-06` | Code promotionnel expiré | erreur `E-PROMO-002` |
| `CT-07` | Points de fidélité supérieurs au montant des articles (`RG-130`) | **5,88 EUR** |
| `CT-08` | Quantité exactement au seuil de la remise de quantité (`RG-030`, borne `≥`) | **38,28 EUR** |
| `CT-09` | Panier vide | erreur `E-PANIER-001` |

### CT-01 — Cas nominal minimal

Zone `FRANCE_METRO`, pas de code promotionnel, 0 point.

| Réf | Prix cat. HT | Qté | TVA | Poids unit. |
|---|---|---|---|---|
| MUG-BLE | 12,50 | 1 | 20,0 % | 0,300 kg |

- Montant brut HT : `12,50` — aucune remise.
- Montant net HT : `12,50` → inférieur à `P-01` (60,00) → frais de port dus.
- Poids total `0,300 kg` → tranche « ≤ 1 kg » → `4,90` HT.
- TVA articles : `12,50 × 0,200 = 2,50`. TVA port : `4,90 × 0,200 = 0,98`.
- **Montant à payer : `12,50 + 2,50 + 4,90 + 0,98` = 20,88 EUR**

### CT-02 — Franchise de port au seuil exact

Un article à `60,00` HT, TVA 20 %, poids `0,400 kg`.

- Montant net HT : `60,00` — **exactement égal** à `P-01` → la franchise s'applique
  (`RG-100`, comparaison au sens large) → frais de port `0,00`.
- TVA : `60,00 × 0,200 = 12,00`.
- **Montant à payer : 72,00 EUR**

> Si l'implémentation utilisait `>` au lieu de `≥`, ce cas donnerait `77,88` : le test
> échoue immédiatement. C'est exactement le rôle d'un cas aux limites.

### CT-03 — Cas riche (trace de calcul complète)

Zone `FRANCE_METRO`, date de commande `2026-03-15`, code promotionnel `MERCI5`
(montant fixe de `5,00` EUR, cumulable), `350` points de fidélité utilisés.

| Réf | Prix cat. HT | Prix promo HT | Promo en vigueur | Qté | TVA | Poids unit. |
|---|---|---|---|---|---|---|
| CAF-500 | 10,00 | — | — | 4 | 5,5 % | 0,500 kg |
| MUG-BLE | 12,50 | — | — | 1 | 20,0 % | 0,300 kg |
| THE-VERT | 7,20 | 6,50 | oui | 2 | 5,5 % | 0,250 kg |

**Étape 1 — `RG-010` prix unitaire retenu**

| Réf | Retenu | Justification |
|---|---|---|
| CAF-500 | 10,00 | pas de promotion |
| MUG-BLE | 12,50 | pas de promotion |
| THE-VERT | 6,50 | `min(7,20 ; 6,50)`, promotion en vigueur |

**Étape 2 — `RG-020` montant brut**

| Réf | Calcul | Brut HT |
|---|---|---|
| CAF-500 | `10,00 × 4` | 40,00 |
| MUG-BLE | `12,50 × 1` | 12,50 |
| THE-VERT | `6,50 × 2` | 13,00 |
| | **Total** | **65,50** |

**Étape 3 — `RG-030` remise de quantité** (seuil `P-05` = 3, taux `P-06` = 10 %)

| Réf | Qté ≥ 3 ? | Remise |
|---|---|---|
| CAF-500 | oui (4) | `40,00 × 0,10` = **4,00** |
| MUG-BLE | non (1) | 0,00 |
| THE-VERT | non (2) | 0,00 |

Assiette panier (`RG-050`) : `36,00 + 12,50 + 13,00` = **61,50**

**Étape 4 — `RG-050` remise panier** : code à montant fixe → `min(5,00 ; 61,50)` = **5,00**

**Étape 5 — `RG-060` plafond global**
`total_remises_demandées = 4,00 + 5,00 = 9,00`
`plafond = 65,50 × 0,60 = 39,30` → `9,00 ≤ 39,30` → **pas d'écrêtement**

**Étape 6 — `RG-085` répartition et centime résiduel**

| Réf | Base | Part | Calcul | Arrondi |
|---|---|---|---|---|
| CAF-500 | 36,00 | 36,00/61,50 | `5,00 × 0,585365…` = 2,926829… | 2,93 |
| MUG-BLE | 12,50 | 12,50/61,50 | `5,00 × 0,203252…` = 1,016260… | 1,02 |
| THE-VERT | 13,00 | 13,00/61,50 | `5,00 × 0,211382…` = 1,056910… | 1,06 |
| | | | **Somme des arrondis** | **5,01** |

`écart = 5,00 − 5,01 = −0,01` → imputé à la ligne dont la base est la plus élevée
(CAF-500, 36,00) → **remise panier CAF-500 = 2,92**.

**Étape 7 — `RG-090` montants nets**

| Réf | Brut | Remise qté | Remise panier | Net HT |
|---|---|---|---|---|
| CAF-500 | 40,00 | 4,00 | 2,92 | **33,08** |
| MUG-BLE | 12,50 | 0,00 | 1,02 | **11,48** |
| THE-VERT | 13,00 | 0,00 | 1,06 | **11,94** |
| | | | **Total** | **56,50** |

Contrôle `INV-02` : `33,08 + 11,48 + 11,94 = 56,50` ✓ et `61,50 − 5,00 = 56,50` ✓

**Étape 8 — `RG-100` frais de livraison**
`56,50 < 60,00` → port dû.
Poids total : `4 × 0,500 + 1 × 0,300 + 2 × 0,250` = `2,000 + 0,300 + 0,500` = **2,800 kg**
→ tranche `]1 ; 5]` → **6,90 HT**

**Étape 9 — `RG-110` TVA**

| Taux | Assiette | Calcul | TVA |
|---|---|---|---|
| 5,5 % | `33,08 + 11,94` = 45,02 | `45,02 × 0,055` = 2,4761 | **2,48** |
| 20,0 % | 11,48 | `11,48 × 0,200` = 2,296 | **2,30** |
| 20,0 % (port) | 6,90 | `6,90 × 0,200` = 1,38 | **1,38** |
| | | **Total TVA** | **6,16** |

**Étape 10 — `RG-120` montant avant fidélité**
- Articles TTC : `56,50 + 2,48 + 2,30` = **61,28**
- Port TTC : `6,90 + 1,38` = **8,28**
- Total avant fidélité : **69,56**

**Étape 11 — `RG-130` fidélité**
`350 × 0,01` = `3,50` ; `min(3,50 ; 61,28)` = **3,50** ; points débités : **350**

**Étape 12 — `RG-140` résultat**

> ### Montant à payer : **66,06 EUR**

### CT-04 — Centime résiduel et départage d'ex æquo

Trois lignes strictement équivalentes en montant, TVA 20 %, poids `0,300 kg` chacune,
code promotionnel à montant fixe de `1,00` EUR, 0 point.

| Réf | Prix cat. HT | Qté | Brut |
|---|---|---|---|
| AAA-01 | 10,00 | 1 | 10,00 |
| BBB-01 | 10,00 | 1 | 10,00 |
| CCC-01 | 10,00 | 1 | 10,00 |

- Aucune remise de quantité. Assiette panier : `30,00`.
- Répartition : `1,00 × 10,00/30,00` = `0,333…` → `0,33` sur chaque ligne.
- Somme des arrondis : `0,99`. Écart : `1,00 − 0,99 = +0,01`.
- Les trois bases sont **à égalité** → départage par référence alphabétiquement la plus
  petite → `AAA-01` reçoit `0,34`.

| Réf | Remise panier | Net HT |
|---|---|---|
| AAA-01 | 0,34 | 9,66 |
| BBB-01 | 0,33 | 9,67 |
| CCC-01 | 0,33 | 9,67 |
| | **Total** | **29,00** |

- `29,00 < 60,00` → port dû ; poids total `0,900 kg` → **4,90**.
- TVA : `29,00 × 0,200 = 5,80` ; port `4,90 × 0,200 = 0,98`.
- **Montant à payer : `29,00 + 5,80 + 4,90 + 0,98` = 40,68 EUR**

> Ce cas est le détecteur de la mauvaise implémentation la plus fréquente : celle qui
> impute le résidu « sur la dernière ligne rencontrée ». Elle donne le même total, mais
> un détail par ligne différent — donc une facture différente, et un écart d'audit.

### CT-05 — Écrêtement par le plafond global

Un article `REF-LUX` à `100,00` HT, quantité 1, TVA 20 %, poids `0,800 kg`, code
promotionnel `MEGA80` (taux de 80 %, cumulable).

- Brut : `100,00`. Remise de quantité : `0,00` (quantité 1).
- Remise panier brute : `100,00 × 0,80` = `80,00`.
- Plafond : `100,00 × 0,60` = `60,00`. `80,00 > 60,00` → remise retenue = **60,00**.
- Net HT : `40,00` → inférieur à 60,00 → port dû ; `0,800 kg` → **4,90**.
- TVA : `40,00 × 0,200 = 8,00` ; port `0,98`.
- **Montant à payer : `40,00 + 8,00 + 4,90 + 0,98` = 53,88 EUR**

### CT-06 — Code promotionnel expiré

Panier de `CT-01`, code `NOEL2025` dont la fin d'effet est le `2025-12-31`, date de
commande `2026-03-15`.
→ **Erreur `E-PROMO-002`**, aucun montant retourné (`RG-040`).

### CT-07 — Points de fidélité supérieurs au montant des articles

Panier de `CT-01` (articles TTC `15,00`, port TTC `5,88`), `10 000` points utilisés.

- Réduction demandée : `10 000 × 0,01` = `100,00`.
- Assiette d'imputation : montant TTC des **articles** = `15,00`.
- Réduction retenue : `min(100,00 ; 15,00)` = **15,00** ; points débités : **1 500** ;
  **8 500 points restent acquis au client**.
- **Montant à payer : `20,88 − 15,00` = 5,88 EUR** — soit exactement les frais de port.

### CT-08 — Quantité exactement au seuil de la remise

Un article à `10,00` HT, quantité **3**, TVA 20 %, poids `0,300 kg`.

- Brut : `30,00`. `3 ≥ P-05` → remise de quantité `3,00`. Net : `27,00`.
- Port : `27,00 < 60,00` → poids `0,900 kg` → `4,90`.
- TVA : `27,00 × 0,200 = 5,40` ; port `0,98`.
- **Montant à payer : `27,00 + 5,40 + 4,90 + 0,98` = 38,28 EUR**

### CT-09 — Panier vide

→ **Erreur `E-PANIER-001`**, aucun montant retourné.

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-010` | CT-03 |
| `RG-020` | tous |
| `RG-030` | CT-03, CT-08 |
| `RG-040` | CT-03, CT-06 |
| `RG-045` | *non couverte — voir `Q-04`* |
| `RG-050` | CT-03, CT-04, CT-05 |
| `RG-060` | CT-05 |
| `RG-085` | CT-03, CT-04 |
| `RG-090` | tous |
| `RG-100` | CT-01, CT-02, CT-03 |
| `RG-110` | CT-03 (deux taux), CT-01 |
| `RG-120` | tous |
| `RG-130` | CT-03, CT-07 |
| `RG-140` | tous |

> Une case vide dans cette table est un défaut visible. `RG-045` n'a pas de cas de
> test : c'est un manque, et il est nommé plutôt que caché.

## 11. Fiche de contraintes

| Dimension | Contrainte métier |
|---|---|
| **Volumétrie** | 200 000 commandes par jour ; 4 lignes par panier en moyenne, 200 au maximum ; croissance attendue de 25 % par an sur 3 ans |
| **Profil de charge** | 3 000 appels par minute en moyenne aux heures ouvrées ; **pointe × 12 pendant 4 heures** lors des deux périodes de soldes ; le recalcul d'affichage du panier représente 90 % des appels |
| **Mode d'appel** | À la demande, synchrone, à chaque modification du panier et à la validation |
| **Latence** | 95 % des appels sous 200 ms, 99 % sous 500 ms. **Au-delà de 2 s, le client abandonne** : il est préférable d'afficher une erreur explicite que de faire attendre |
| **Fraîcheur** | Les prix et promotions applicables sont ceux en vigueur **à l'instant de l'appel** ; un décalage supérieur à 5 minutes après une mise à jour de prix est un incident |
| **Exactitude** | Les montants sont **exacts au centime**. Aucune erreur de représentation n'est tolérée : un écart d'un centime entre l'affichage du panier, la commande et la facture est un incident de niveau 1 |
| **Déterminisme** | Deux appels identiques doivent donner un résultat identique, **détail par ligne compris** — pas seulement le total |
| **Rejouabilité** | Toute commande doit pouvoir être **recalculée à l'identique pendant 10 ans** (obligation de conservation comptable), avec les règles, les paramètres et les prix **en vigueur à la date de la commande** |
| **Auditabilité** | Le détail par ligne, les remises appliquées et la TVA par taux sont conservés et restituables commande par commande |
| **Explicabilité** | Le client doit pouvoir voir, sur sa facture, l'origine de chaque remise |
| **Criticité et mode dégradé** | **Si le service de fidélité est indisponible : la commande est calculée sans imputation de points, le client en est informé, et aucun point n'est débité.** Si le référentiel de prix est indisponible : la commande est refusée |
| **Confidentialité** | Le calcul manipule un identifiant client, jamais de nom, d'adresse ni de moyen de paiement. La zone de livraison suffit — l'adresse complète ne doit pas entrer dans le calcul |
| **Conformité** | Facturation et TVA ; conservation 10 ans |
| **Fréquence de changement** | **Paramètres** (`P-01` à `P-08`) : jusqu'à une fois par semaine en période de soldes, à l'initiative du marketing. **Règles** (`RG-xxx`) : 1 à 2 fois par an, sur décision de la Direction commerciale |
| **Qui modifie** | Le marketing doit pouvoir changer un seuil ou un barème **sans livraison logicielle**, avec une validation N+1 et une date d'effet future. Aucune modification de règle sans passer par ce document |
| **Durée de vie** | Ce calcul est au cœur du système de vente : durée de vie attendue supérieure à 10 ans |

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| `Q-01` | Faut-il indiquer au client le montant manquant pour atteindre la franchise de port ? Si oui, ce calcul entre-t-il dans cette spécification ou dans celle de l'affichage ? | Direction commerciale | 2026-09-30 | Ouverte |
| `Q-02` | Quand un prix promotionnel est supérieur au prix catalogue (`RG-010`), faut-il émettre une alerte vers le référentiel produit ? | Direction commerciale | 2026-09-30 | Ouverte |
| `Q-03` | Les points de fidélité doivent-ils pouvoir s'imputer sur les frais de livraison lorsque la franchise n'est pas atteinte ? | Marketing | 2026-10-15 | Ouverte |
| `Q-04` | Aucun cas de test ne couvre `RG-045` (non-cumul). Quel code promotionnel non cumulable prendre comme référence ? | Auteur métier | 2026-09-15 | Ouverte |
| `Q-05` | *Tranchée le 2026-05-12 :* la TVA est-elle calculée par taux agrégé ou ligne à ligne ? → **par taux agrégé**, cf. `RG-110`. | Conformité | — | Fermée |

## 13. Historique

| Version | Date | Changement | Impact sur les résultats |
|---|---|---|---|
| 1.0.0 | 2025-11-20 | Version initiale | — |
| 1.1.0 | 2026-01-08 | Ajout de `RG-045` (non-cumul) et de la table de décision `RG-040` | Aucun sur les cas existants |
| **2.0.0** | 2026-05-12 | `RG-110` : TVA par taux agrégé au lieu de ligne à ligne (`Q-05`) | **Oui** — écarts d'un centime sur les paniers à plusieurs lignes de même taux. Rejeu non nécessaire : applicable aux commandes postérieures au 2026-06-01 |

---

## Annexe — Lecture technique de cette spécification

> **Cette section n'est pas écrite par le métier.** Elle est ajoutée par l'architecte,
> après acceptation, pour montrer ce que la spécification a permis de décider — et
> vérifier qu'elle contenait bien tout le nécessaire. C'est le bouclage du cadre.

| Contrainte lue au §11 | Décision technique qui en découle |
|---|---|
| « exacts au centime, aucune erreur de représentation » | **Type décimal exact obligatoire** (`BigDecimal`, `decimal`, `Decimal`) — le flottant binaire est exclu. Cela élimine de fait les langages sans décimal natif ou sans bibliothèque décimale éprouvée, et interdit un portage naïf vers un moteur de calcul en virgule flottante |
| « déterminisme, détail par ligne compris » | Interdiction de toute agrégation parallèle non ordonnée ; le tri de départage de `RG-085` est explicite et stable |
| 3 000 appels/min, pointe × 12, p95 < 200 ms | Service **sans état**, dimensionné horizontalement pour 36 000 appels/min ; interdiction d'un appel de persistance par ligne de panier ; prix et paramètres chargés en mémoire |
| « 90 % des appels sont des recalculs d'affichage » | Le calcul doit être **pur** (mêmes entrées → mêmes sorties, aucun effet de bord), ce qui rend le recalcul systématique acceptable et rend la mise en cache légitime |
| Paramètres modifiés **hebdomadairement par le marketing**, sans livraison | Les paramètres `P-01` à `P-08` vivent dans un **référentiel externe versionné et daté**, avec une interface d'administration et un circuit de validation. Les règles `RG-xxx`, qui changent 1 à 2 fois par an, restent en code |
| Rejouabilité à 10 ans, règles et paramètres de l'époque | **Capture immuable** du jeu de paramètres et des prix applicables au moment de la commande, conservée avec la commande ; le code embarque un numéro de version de spécification. Un rejeu ne relit jamais les paramètres courants |
| Auditabilité et explicabilité | Le `détail_par_ligne` est **une sortie de premier rang**, pas un journal technique : il est persisté avec la commande et restitué sur la facture |
| Mode dégradé fidélité | Le service de fidélité est une **dépendance isolée** : délai d'attente court, disjoncteur, et repli explicite sur « aucun point imputé, aucun point débité », remonté au client — jamais un échec de la commande |
| « aucune donnée personnelle dans le calcul » | Le service ne reçoit qu'un identifiant client et une zone : il peut être déployé et journalisé sans contrainte de données personnelles, ce qui simplifie considérablement son exploitation |
| Durée de vie > 10 ans, 1 à 2 changements de règle par an | L'investissement dans les tests issus du §10 et dans les tests de propriété issus du §8 est rentable : ils constituent le **filet de sécurité de dix ans de modifications** |

**Ce que la spécification n'a volontairement pas dit, et c'est normal** : le langage, le
format de stockage, le protocole d'appel, le mécanisme de cache, le découpage en
services, la stratégie de déploiement. Chacun de ces choix peut changer sans qu'une
seule ligne de ce document ne bouge — et c'est exactement le but.
