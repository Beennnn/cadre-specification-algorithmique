# SPEC-AST-001 — Heures de lever et de coucher du Soleil

| | |
|---|---|
| **Identifiant** | SPEC-AST-001 |
| **Version** | 1.0.0 |
| **Statut** | Validée |
| **Valideur métier** | Responsable éphémérides |
| **Co-auteur technique** | Équipe services géospatiaux |
| **Glossaire de référence** | [GLOSSAIRE.md](../../../GLOSSAIRE.md) v1.0.0 |
| **Traduction de** | [SPEC-AST-001.en.md](SPEC-AST-001.en.md) |

> **Version française, non normative.** La version qui fait foi est
> [`SPEC-AST-001.en.md`](SPEC-AST-001.en.md) : c'est elle que le code suit et que les
> outils vérifient. `C-42` contrôle que les deux portent les mêmes objets et les mêmes
> identités durables.

> **Pourquoi cet exemple.** Il apporte un régime qu'aucun autre du dépôt ne montre : **une
> équation qui n'a pas toujours de solution**. Au-delà des cercles polaires, le Soleil peut
> ne jamais se lever, ou ne jamais se coucher. Ce n'est **pas une erreur** : c'est un
> résultat, qu'il faut nommer, typer et rendre.
>
> Il porte aussi deux faux amis que les autres n'ont pas : les **conventions d'angle**, où
> un signe inversé donne un résultat plausible mais faux, et le **choix de la définition
> elle-même** — « le lever du Soleil » désigne quatre instants différents selon ce qu'on
> décide de mesurer.

---

## 1. Objectif et contexte

Un service doit publier, pour un lieu et une date, l'heure du lever et du coucher du
Soleil : affichage grand public, pilotage d'éclairage, planification d'observations.

La difficulté n'est pas dans les équations, qui sont connues et stables depuis longtemps.
Elle est dans les **conventions** : quatre décisions doivent être prises avant qu'un
développeur puisse écrire la moindre ligne, et aucune ne se lit dans les formules.

---

## 2. Périmètre

**Dans le périmètre.** Le calcul, pour un lieu et une date données, des instants de lever
et de coucher du Soleil, en temps universel coordonné.

**Hors périmètre.** Le crépuscule (civil, nautique, astronomique), la Lune, l'altitude de
l'observateur, le relief environnant, la conversion vers une heure locale et son fuseau —
qui relèvent de l'affichage, pas du calcul.

---

## 3. Glossaire

| Terme | Définition |
|---|---|
| **Déclinaison** (*declination*, `δ`) | L'angle entre la direction du Soleil et le plan de l'équateur céleste. Varie de −23,44° à +23,44° dans l'année ; c'est elle qui fait les saisons. |
| **Angle horaire** (*hour angle*, `ω`) | L'angle dont la Terre doit tourner entre l'instant considéré et le midi solaire local. 15° par heure. |
| **Midi solaire** (*solar transit*) | L'instant où le Soleil passe au méridien du lieu. Il ne coïncide **pas** avec 12 h : l'orbite terrestre n'est ni circulaire ni dans le plan de l'équateur. |
| **Hauteur de référence** (*reference altitude*, `h₀`) | La hauteur angulaire du Soleil qui **définit** le lever et le coucher. Ce n'est pas 0° : voir `P-01`. |
| **Jour polaire / nuit polaire** | Les régimes où le Soleil ne franchit jamais `h₀` — dans un sens ou dans l'autre. L'équation n'y admet **aucune** solution. |

---

## 4. Entrées

```
request :
    observation_date : Date(Gregorian calendar)                    [DateAndTime]
    latitude         : Angle(°, −90.0000 .. +90.0000, 4 decimals)  [Real]
    longitude        : Angle(°, −180.0000 .. +180.0000, 4 decimals) [Real]
```

**Préconditions :**
- `latitude` est comptée **positive vers le nord** (`P-02`).
- `longitude` est comptée **positive vers l'est** (`P-02`).
- `observation_date` est une date civile, sans heure ni fuseau : le calcul porte sur le
  jour entier.

> **La convention de signe est une entrée du contrat, pas une évidence.** Les deux
> conventions de longitude — est positive, ouest positive — coexistent dans la littérature
> et dans les bases de données. Les confondre déplace le résultat de deux fois la
> longitude : à Paris, **dix-neuf minutes**. Le résultat reste parfaitement plausible, ce
> qui est exactement ce qui rend l'erreur coûteuse.

---

## 5. Sorties

```
result :
    solar_regime  : Enumerated { NORMAL, POLAR_DAY, POLAR_NIGHT }   [Enumerated]
    sunrise_time  : TimeOfDay(UTC, to the minute) — ABSENT if solar_regime ≠ NORMAL
    sunset_time   : TimeOfDay(UTC, to the minute) — ABSENT if solar_regime ≠ NORMAL
    declination   : Angle(°, −23.5000 .. +23.5000, 4 decimals)      [Real]
```

> **`solar_regime` n'est pas un code d'erreur.** Un jour polaire est un fait
> astronomique, pas une défaillance : la fonction a parfaitement réussi. Le rendre comme
> une erreur obligerait chaque appelant à traiter en exception un cas qui survient six mois
> par an à Longyearbyen.

---

## 6. Paramètres

| Id | Nom | Valeur | Unité | Qui peut le changer | Date d'effet |
|---|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Hauteur de référence `h₀` | −0,833 | ° | Responsable éphémérides | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Convention de signe des coordonnées | nord et est positifs | — | Responsable éphémérides | 2026-01-01 |
| <a id="p-03"></a>`P-03` | Sens d'arrondi à la minute | `P-03.lever` = `CEILING`, `P-03.coucher` = `FLOOR` | — | Responsable éphémérides | 2026-01-01 |
| <a id="p-04"></a>`P-04` | Obliquité de l'écliptique | 23,4397 | ° | Responsable éphémérides | 2026-01-01 |

> **`P-01` porte la définition même du résultat, et c'est le faux ami central.**
> « Le Soleil se lève » peut désigner quatre instants distincts :
>
> | Définition | `h₀` | Écart à Paris |
> |---|---|---|
> | Centre du disque à l'horizon **géométrique** | 0° | référence |
> | Centre du disque, réfraction atmosphérique comprise | −0,583° | ≈ 2 min plus tôt |
> | **Bord supérieur du disque, réfraction comprise** | **−0,833°** | ≈ 3 à 4 min plus tôt |
> | Bord supérieur, sans réfraction | −0,250° | ≈ 1 min plus tôt |
>
> La troisième est la convention des éphémérides publiées, et c'est celle qu'on retient. Un
> développeur qui écrirait `0°` — le choix « naturel » — produirait un service dont toutes
> les valeurs sont fausses de quelques minutes, sans qu'aucun test ne le signale si les
> résultats attendus viennent du code lui-même.

> **`P-03` est asymétrique, et volontairement.** Arrondir le lever vers le haut et le
> coucher vers le bas **rétrécit** la fenêtre de jour annoncée. C'est le sens prudent pour
> les usages qui en dépendent — extinction d'éclairage, fin d'un chantier extérieur : on
> préfère annoncer le jour un peu plus court qu'il n'est. Un arrondi au plus proche, dans
> les deux sens, serait plus « juste » et moins sûr.

---

## 7. Règles

### 7.1 L'algorithme, en un seul morceau

**Les règles ci-dessous sont les étapes d'un seul algorithme, pas des fragments à
assembler.** Voici sa composition, telle que le métier l'énonce :

```
DEFINE compute_sunrise_sunset(request) : result

    PRECONDITIONS  E-AST-001, E-AST-002

    LET julian_day         = ...                    (RG-005)
    LET days_since_epoch   = ...                    (RG-010)
    LET mean_anomaly       = ...                    (RG-020)
    LET ecliptic_longitude = ...                    (RG-020)
    declination            = ...                    (RG-030)
    LET solar_transit      = ...                    (RG-040)
    LET cos_hour_angle     = ...                    (RG-050)

    IF cos_hour_angle IS OUTSIDE [−1, 1] THEN       (RG-050)
        solar_regime = POLAR_DAY OR POLAR_NIGHT, according to its sign
        sunrise_time = ABSENT
        sunset_time  = ABSENT
    ELSE
        solar_regime = NORMAL
        sunrise_time = ...                          (RG-060)
        sunset_time  = ...                          (RG-060)
    END IF

    RETURN result
```

> **L'algorithme intégré se lit d'un trait ; les règles numérotées sont adressables.** Ce
> sont deux vues du même calcul, pas deux options. Sans la première, l'ordre resterait à
> deviner ; sans les secondes, on ne pourrait rattacher ni un cas de test, ni une notice de
> changement, ni un commentaire de code à un point précis.

### 7.2 Chaîne de traitement

| Étape | Consomme | Produit | Règles |
|---|---|---|---|
| `ET-01` Jour julien | `observation_date` | `julian_day` | `RG-005` |
| `ET-02` Jour local | `julian_day`, `longitude` | `days_since_epoch` | `RG-010` |
| `ET-03` Position orbitale | `days_since_epoch` | `mean_anomaly`, `ecliptic_longitude` | `RG-020` |
| `ET-04` Déclinaison | `ecliptic_longitude`, `P-04` | `declination` | `RG-030` |
| `ET-05` Midi solaire | `days_since_epoch`, `longitude`, `mean_anomaly`, `ecliptic_longitude` | `solar_transit` | `RG-040` |
| `ET-06` Régime solaire | `latitude`, `declination`, `ecliptic_longitude`, `P-01` | `cos_hour_angle`, `solar_regime` | `RG-050` |
| `ET-07` Instants | `cos_hour_angle`, `solar_regime`, `solar_transit`, `P-03` | `sunrise_time`, `sunset_time` | `RG-060` |

```bash
java outils/Verifier.java --chaine exemples/sunrise/spec/SPEC-AST-001.fr.md
```

> **Contrairement au bilan de masse, cette chaîne n'est pas une file.** `ET-04`
> (déclinaison) et `ET-05` (midi solaire) ne dépendent pas l'une de l'autre : elles sont
> **indépendantes et calculables en parallèle**. L'outil le dérive de la table, sans qu'on
> l'écrive. La spécification dit ce qui est indépendant ; elle ne dit pas d'en profiter —
> ce choix appartient au développeur.

### 7.3 Grandeurs internes

*Portée **interne** : visibles dans le corps de cette fonction seulement. Elles ne
figurent ni au contrat, ni au catalogue des données — mais elles sont décrites avec la
même rigueur, parce qu'un développeur doit savoir quoi instancier.*

```
julian_day         : Real(days, > 0, 6 decimals)      -- jour julien de observation_date à 0 h UTC
days_since_epoch   : Integer(days)                    -- jours entiers depuis J2000.0
mean_anomaly       : Angle(°, 0.0000 .. 360.0000, 4 decimals)
equation_of_centre : Angle(°, −2.0000 .. +2.0000, 4 decimals)
ecliptic_longitude : Angle(°, 0.0000 .. 360.0000, 4 decimals)
solar_transit      : Real(days, > 0, 6 decimals)      -- midi solaire, en jour julien
cos_hour_angle     : Real(dimensionless, 5 decimals)  -- domaine NON borné à [−1, 1] : voir RG-050
hour_angle         : Angle(°, 0.0000 .. 180.0000, 4 decimals)
```

> **`cos_hour_angle` n'est pas borné à `[−1, 1]`, et c'est délibéré.** C'est un cosinus,
> donc on l'attendrait dans cet intervalle — mais la grandeur calculée en sort
> légitimement, et c'est précisément ce qui porte l'information de `RG-050`. Déclarer un
> domaine `−1 .. 1` conduirait un développeur à valider l'entrée de l'arc cosinus et à
> rejeter les cas polaires comme des erreurs.

### RG-005 — Jour julien de la date d'observation

```
julian_day = jour julien de observation_date à 0 h UTC, calendrier grégorien
```

> **On ne récrit pas la conversion calendaire.** C'est un algorithme normalisé, stable
> depuis des siècles et disponible dans toute bibliothèque de dates. La spécification dit
> *quelle grandeur est attendue et dans quelle échelle* ; réimplémenter la conversion
> serait exactement le genre de décision technique que le §1.4 laisse au développeur.

### RG-010 — Jour à midi solaire

```
LET days_since_epoch = ROUND( julian_day(observation_date) − 2451545.0
                              + 0.0008 + longitude ÷ 360, 0, HALF_UP )
```

> **`− 2451545,0`** ramène à l'époque J2000,0. **`+ 0,0008`** corrige l'écart des secondes
> intercalaires accumulées. **`+ longitude ÷ 360`** rattache la date au jour solaire
> **local** : c'est ici que la convention de `P-02` entre dans le calcul, et un signe
> inversé y passe inaperçu.

### RG-020 — Position du Soleil sur son orbite

```
LET mean_anomaly = ( 357.5291 + 0.98560028 × days_since_epoch ) modulo 360

LET equation_of_centre = 1.9148 × sin(mean_anomaly)
                       + 0.0200 × sin(2 × mean_anomaly)
                       + 0.0003 × sin(3 × mean_anomaly)

LET ecliptic_longitude = ( mean_anomaly + equation_of_centre + 180 + 102.9372 ) modulo 360
```

> **`modulo 360` est une décision, pas une commodité.** Un angle est défini modulo un tour.
> Sans réduction explicite, une implémentation accumule au fil des siècles des valeurs de
> plusieurs milliers de degrés — mathématiquement correctes, et catastrophiques pour la
> précision d'un sinus en virgule flottante.

### RG-030 — Déclinaison

```
declination = arcsin( sin(ecliptic_longitude) × sin(P-04) )
```

### RG-040 — Midi solaire

```
LET solar_transit = 2451545.0 + days_since_epoch − longitude ÷ 360
                  + 0.0053 × sin(mean_anomaly)
                  − 0.0069 × sin(2 × ecliptic_longitude)
```

> **Le midi solaire est plus tôt vers l'est.** D'où `− longitude ÷ 360`, et non `+`. Les
> deux derniers termes sont l'**équation du temps** : ils valent jusqu'à ±16 minutes, et
> les omettre est l'erreur la plus fréquente de ce calcul.

### RG-050 — Angle horaire, et existence d'une solution

```
LET cos_hour_angle = ( sin(P-01) − sin(latitude) × sin(declination) )
                     ÷ ( cos(latitude) × cos(declination) )

IF cos_hour_angle < −1 THEN
    solar_regime = POLAR_DAY
ELSE IF cos_hour_angle > 1 THEN
    solar_regime = POLAR_NIGHT
ELSE
    solar_regime = NORMAL
END IF
```

> **C'est le cœur métier de cette spécification.** L'arc cosinus n'est défini que sur
> `[−1, 1]`. Hors de cet intervalle, il n'y a pas d'instant où le Soleil franchit `h₀` —
> et le **signe** dit lequel des deux régimes on observe : trop bas pour redescendre
> (`< −1`, jour polaire), ou trop haut pour monter (`> 1`, nuit polaire).
>
> Une implémentation qui appellerait `arccos` sans ce test produirait, selon le langage,
> une exception, un `NaN` silencieux, ou une valeur repliée sur la borne — trois
> comportements différents pour une même spécification. C'est exactement le genre d'écart
> que le [test de la double implémentation](../../../CADRE.md) est fait pour révéler.

### RG-060 — Instants du lever et du coucher

```
IF solar_regime = NORMAL THEN
    LET hour_angle = arccos( cos_hour_angle )
    sunrise_time = ROUND( solar_transit − hour_angle ÷ 360, minute, P-03.lever )
    sunset_time  = ROUND( solar_transit + hour_angle ÷ 360, minute, P-03.coucher )
ELSE
    sunrise_time = ABSENT
    sunset_time  = ABSENT
END IF
```

> **`ABSENT`, et non minuit, ni une chaîne vide, ni `00:00`.** L'absence est ici une
> information métier : *il n'existe pas d'instant de lever ce jour-là*. Une valeur par
> défaut la ferait disparaître, et un appelant afficherait « lever à 00:00 » un jour de
> nuit polaire.

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-005` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-010` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-020` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-030` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-040` | CT-01, CT-02, CT-03 |
| `RG-050` | CT-01, CT-02, CT-03 (`NORMAL`), CT-04 (`POLAR_DAY`), CT-05 (`POLAR_NIGHT`) |
| `RG-060` | CT-01, CT-02, CT-03 ; CT-04 et CT-05 pour la branche `ABSENT` |

---

## 8. Invariants

| Id | Énoncé |
|---|---|
| <a id="inv-01"></a>`INV-01` | Si `solar_regime = NORMAL`, alors `sunrise_time < sunset_time` — le lever précède toujours le coucher du même jour solaire. |
| <a id="inv-02"></a>`INV-02` | `sunrise_time` et `sunset_time` sont **tous deux** `ABSENT` ou tous deux présents. Jamais l'un sans l'autre. |
| <a id="inv-03"></a>`INV-03` | `−23,44° ≤ declination ≤ +23,44°`, quelle que soit la date. Une valeur hors de cet intervalle signale une erreur de conversion d'angle — le symptôme le plus courant d'une confusion degrés / radians. |
| <a id="inv-04"></a>`INV-04` | **Symétrie par la longitude** : à latitude égale, décaler la longitude de `Δ` degrés vers l'est avance le lever **et** le coucher de `Δ ÷ 15` heures, à la minute d'arrondi près. C'est ce qui attrape une convention de signe inversée. |
| <a id="inv-05"></a>`INV-05` | `solar_regime ≠ NORMAL` implique `|latitude| > 66,0°`. En deçà du cercle polaire, il y a toujours un lever et un coucher. |

> `INV-04` et `INV-05` se testent sur des entrées **engendrées**, pas sur les cinq cas du
> jeu d'essai. `INV-05` en particulier balaie tout le globe : c'est le seul contrôle qui
> vérifie que le régime polaire ne se déclenche jamais là où il ne doit pas.

---

## 9. Cas d'erreur métier

| Id | Condition | Comportement |
|---|---|---|
| <a id="e-ast-001"></a>`E-AST-001` | `latitude` hors de `[−90, +90]` | Rejet : la coordonnée n'existe pas |
| <a id="e-ast-002"></a>`E-AST-002` | `longitude` hors de `[−180, +180]` | Rejet. **On ne replie pas** silencieusement : une longitude de 200° signale une convention non respectée en amont, et la corriger masquerait le défaut |

> **Le jour polaire n'est pas dans cette table, et c'est le point de la spécification.**
> Ce n'est pas un cas d'erreur : c'est un résultat, porté par `solar_regime`.

---

## 10. Jeu d'essai

### CT-01 — Paris, solstice d'été

`2026-06-21`, latitude `48,8566`, longitude `2,3522`

| | |
|---|---|
| `declination` | **+23,4393°** |
| `cos_hour_angle` | −0,52032 |
| `solar_regime` | **NORMAL** |
| lever, avant arrondi | 03:46:52 UTC → **03:47** (au supérieur) |
| coucher, avant arrondi | 19:57:42 UTC → **19:57** (à l'inférieur) |

### CT-02 — Paris, solstice d'hiver

`2026-12-21`, latitude `48,8566`, longitude `2,3522`

| | |
|---|---|
| `declination` | **−23,4371°** |
| `cos_hour_angle` | +0,47210 |
| `solar_regime` | **NORMAL** |
| lever | 07:41:13 UTC → **07:42** |
| coucher | 15:55:51 UTC → **15:55** |

### CT-03 — Quito, équinoxe, longitude négative

`2026-03-20`, latitude `−0,1807`, longitude `−78,4678`

| | |
|---|---|
| `declination` | **−0,6159°** |
| `cos_hour_angle` | −0,01457 |
| `solar_regime` | **NORMAL** |
| lever | 11:18:23 UTC → **11:19** |
| coucher | 23:25:04 UTC → **23:25** |

**C'est le cas qui discrimine la convention de signe.** Une longitude ouest, un lever et un
coucher en fin de journée UTC. Avec la convention inverse, le résultat reste plausible —
et faux de plus de dix heures.

### CT-04 — Tromsø, jour polaire

`2026-06-01`, latitude `69,6492`, longitude `18,9553`

| | |
|---|---|
| `declination` | **+22,0328°** |
| `cos_hour_angle` | **−1,13615** → hors de `[−1, 1]` |
| `solar_regime` | **POLAR_DAY** |
| `sunrise_time`, `sunset_time` | **ABSENT** tous les deux |

### CT-05 — Tromsø, nuit polaire

`2026-12-01`, latitude `69,6492`, longitude `18,9553`

| | |
|---|---|
| `declination` | **−21,7751°** |
| `cos_hour_angle` | **+1,03195** → hors de `[−1, 1]` |
| `solar_regime` | **POLAR_NIGHT** |
| `sunrise_time`, `sunset_time` | **ABSENT** tous les deux |

**CT-04 et CT-05 ne diffèrent que par le signe** de `cos_hour_angle`. Une implémentation
qui testerait `|cos_hour_angle| > 1` sans regarder le signe passerait les deux cas en
confondant jour et nuit polaires — l'erreur la plus grave que cette fonction puisse
commettre, et la seule que ces deux cas révèlent.

### Provenance et validation

| | |
|---|---|
| **Provenance** | Équations des éphémérides classiques, appliquées indépendamment de toute implémentation du composant |
| **Comment ils ont été examinés** | Les trois cas `NORMAL` ont été confrontés aux heures publiées pour ces lieux et ces dates : accord à la minute. Les deux cas polaires ont été vérifiés par le signe de `cos_hour_angle` et confrontés au fait astronomique connu |
| **Ce que l'examen a produit** | Une erreur de signe sur la longitude a été trouvée **par cette confrontation**, et non par relecture : le calcul plaçait le lever parisien 19 minutes trop tard, un écart parfaitement plausible sur un affichage. C'est ce qui a motivé `P-02`, `INV-04` et CT-03 |
| **Validé par** | Responsable éphémérides, 2026-08-21 |

---

## 11. Contraintes et exigences

| Id | Énoncé | Source | Propriétaire | Vérification |
|---|---|---|---|---|
| <a id="ex-01"></a>`EX-01` | **Justesse : ± 2 minutes** par rapport aux éphémérides de référence, pour toute latitude `|φ| < 65°`. Au-delà, l'erreur croît fortement : le Soleil y rase l'horizon, et un écart d'angle infime déplace l'instant de plusieurs minutes | Engagement de service | Responsable éphémérides | Comparaison annuelle sur 200 lieux |
| <a id="ex-02"></a>`EX-02` | La **double précision binaire suffit**. La grandeur de sortie est arrondie à la minute, soit 4 × 10⁻⁴ jour, très au-dessus de tout bruit numérique | Analyse d'exactitude | Architecture SI | Revue de conception |
| <a id="ex-03"></a>`EX-03` | Le calcul est **rejouable à l'identique** : aucune dépendance à l'horloge courante ni au fuseau de la machine | Exigence de service | Architecture SI | Rejeu sur archive |
| <a id="ex-04"></a>`EX-04` | Volumétrie : jusqu'à 5 000 appels par seconde en pointe, latence au 99ᵉ centile sous 5 ms | Mesure de production | Responsable service | Tir de charge trimestriel |

> **`EX-01` distingue la justesse du modèle de l'exactitude du calcul** ([CADRE
> §2.9](../../../CADRE.md)). Les ±2 minutes ne viennent pas d'une imprécision numérique : elles
> viennent du **modèle**, qui néglige la réfraction réelle du jour, l'altitude de
> l'observateur et le relief. Un calcul en précision infinie donnerait le même écart.
> Confondre les deux conduirait à chercher un bogue là où il n'y en a pas.

---

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| <a id="q-01"></a>`Q-01` | Faut-il exposer les crépuscules civil, nautique et astronomique ? Ce sont les mêmes règles avec `h₀` valant −6°, −12° et −18° : `P-01` deviendrait une entrée | Responsable éphémérides | 2026-11-01 | Ouverte |
| <a id="q-02"></a>`Q-02` | Faut-il corriger de l'altitude de l'observateur ? L'effet atteint 4 minutes à 1 000 m | Responsable éphémérides | 2026-11-01 | Ouverte |
| <a id="q-03"></a>`Q-03` | Le service doit-il rendre l'heure locale plutôt qu'UTC ? | Responsable éphémérides | | Fermée : non, le fuseau relève de l'affichage |

---

## 13. Historique et notices de changement

| Version | Date | Changement | Impact sur les résultats | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-21 | Version initiale | — | — |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-AST-001` | `428e65ae-0ed5-4a6b-8eb4-e41d0215211a` | document | SPEC-AST-001 — Heures de lever et de coucher du Soleil |
| `RG-005` | `f278d30e-0fd0-4d13-a1d8-fcbce08fde09` | règle | Jour julien de la date d'observation |
| `RG-010` | `af97c323-6b9f-4fd6-9713-4e06c6d4672e` | règle | Jour à midi solaire |
| `RG-020` | `beef87a2-3576-4109-91f9-89d9fc288add` | règle | Position du Soleil sur son orbite |
| `RG-030` | `3b88e87e-2677-41d1-b5fc-c8c951311eca` | règle | Déclinaison |
| `RG-040` | `16b13a55-55df-4eb7-b868-bc369b1138c0` | règle | Midi solaire |
| `RG-050` | `de6df248-be98-4400-8d44-f15007369dd1` | règle | Angle horaire, et existence d'une solution |
| `RG-060` | `8f627688-ce42-47d8-a4b5-0e55eb8d1571` | règle | Instants du lever et du coucher |
| `CT-01` | `66ca83b9-80a9-49ed-8d97-9836840ca7d7` | cas de test | Paris, solstice d'été |
| `CT-02` | `caf14734-10f4-4794-92ac-b92659005d00` | cas de test | Paris, solstice d'hiver |
| `CT-03` | `6a936d8a-f897-4ada-883b-4d430c07ad49` | cas de test | Quito, équinoxe, longitude négative |
| `CT-04` | `a3113841-3709-4029-9ccd-7ceef61af128` | cas de test | Tromsø, jour polaire |
| `CT-05` | `ad06693c-79b6-44d2-a7eb-bf351321c88a` | cas de test | Tromsø, nuit polaire |
| `P-01` | `103ce181-d9a9-43da-8828-8b39052e7b99` | paramètre | Hauteur de référence `h₀` |
| `P-02` | `c8a555a6-878d-4f81-afce-2434795a0886` | paramètre | Convention de signe des coordonnées |
| `P-03` | `5b432b90-2b5c-482f-a032-922be7159f4e` | paramètre | Sens d'arrondi à la minute |
| `P-04` | `a9c2da1c-b965-4ba5-90c9-d4635518a4ea` | paramètre | Obliquité de l'écliptique |
| `EX-01` | `e9900cc4-7c76-48db-a91f-521b12b216d1` | exigence | Justesse : ± 2 minutes par rapport aux éphémérides de référence, pour |
| `EX-02` | `4a8d293a-238f-4600-89b0-3a587757d3e8` | exigence | La **double précision binaire suffit**. La grandeur de sortie est arro |
| `EX-03` | `3125ec69-b46f-4d89-b533-f5b6985a4e50` | exigence | Le calcul est **rejouable à l'identique** : aucune dépendance à l'horl |
| `EX-04` | `871209e3-ca46-4ea2-8f77-455f0575b2f4` | exigence | Volumétrie : jusqu'à 5 000 appels par seconde en pointe, latence au 99 |
| `INV-01` | `3dd144ed-ae0c-4122-a8e9-6cfe601315c0` | invariant | Si `solar_regime = NORMAL`, alors `sunrise_time < sunset_time` — le le |
| `INV-02` | `53eadd31-c842-4683-974c-e02c56b7aeb3` | invariant | `sunrise_time` et `sunset_time` sont **tous deux** `ABSENT` ou tous de |
| `INV-03` | `062582ac-178f-486e-ab1e-94ccce05a0ca` | invariant | `−23,44° ≤ declination ≤ +23,44°`, quelle que soit la date. Une valeur |
| `INV-04` | `8cad23b6-1739-4778-899a-8b3d7c2958fd` | invariant | Symétrie par la longitude : à latitude égale, décaler la longitude d |
| `INV-05` | `77f14f85-1c37-4cb0-b59c-3cd5e9ea5c33` | invariant | `solar_regime ≠ NORMAL` implique |
| `E-AST-001` | `baea8eed-a39c-46c8-96e6-543b35802c95` | cas d'erreur | `latitude` hors de `[−90, +90]` |
| `E-AST-002` | `b7f23f9b-aa20-4597-9d4b-ca157a334135` | cas d'erreur | `longitude` hors de `[−180, +180]` |
| `Q-01` | `68ad3237-3c15-4f58-a8d6-ee9383bf46e2` | question | Faut-il exposer les crépuscules civil, nautique et astronomique ? Ce s |
| `Q-02` | `392ff06a-a1d6-4f50-9cb2-858811eb569b` | question | Faut-il corriger de l'altitude de l'observateur ? L'effet atteint 4 mi |
| `Q-03` | `b35f41d9-46c8-441d-a4be-11abad2464f7` | question | Le service doit-il rendre l'heure locale plutôt qu'UTC ? |
