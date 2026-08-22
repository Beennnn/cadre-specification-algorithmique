# La première séance

*Une page, pour la réunion où l'on présente la démarche à l'équipe. Elle donne un ordre,
des chiffres vérifiables, et une demande. Elle ne remplace aucun document : elle dit
lesquels ouvrir, dans quel ordre, et lesquels laisser fermés.*

---

## Avant

| | |
|---|---|
| **Ce qu'on demande de lire** | [`exemples/average-speed/`](exemples/average-speed/) — le README et la spécification. **Dix minutes, pas plus.** |
| **Ce qu'on ne demande pas** | [`CADRE.md`](CADRE.md). Deux mille lignes envoyées avant une réunion ne se lisent pas, et donnent le sentiment d'un dispositif lourd |
| **Ce qu'on prépare** | Un terminal avec le dépôt cloné et Java installé. Rien d'autre — pas de diaporama |
| **Ce qu'on décide avant** | **La fonction d'étalonnage** (voir la demande, plus bas). Sans elle, la séance se termine sur « intéressant » |

> **Vérifier une fois, la veille :** `java outils/Verifier.java` doit finir sur
> *0 échec, 0 avertissement*. C'est ce qu'on montrera en direct — et répéter une fois la
> manipulation du point 3, pour ne pas la chercher devant la salle.

---

## Le déroulé — 25 minutes

### 1. Le faux ami · 3 min

Poser la question à la salle, sans commentaire :

> Un trajet de 120 km. Les 60 premiers kilomètres à 30 km/h, les 60 suivants à 60 km/h.
> **Quelle est la vitesse moyenne ?**

Laisser répondre. La salle dit **45**. La réponse est **40** : on passe deux heures sur le
segment lent et une seule sur le rapide, donc le lent pèse deux fois plus.

Ne pas enchaîner tout de suite. Ce silence est le meilleur argument de la séance : **des
gens compétents, attentifs, unanimes — et unanimement faux.** Un développeur seul face à
« vitesse moyenne » écrit la moyenne des vitesses, et rien dans son code n'aura l'air
anormal.

→ [`exemples/average-speed/`](exemples/average-speed/) · c'est `RG-030`, et `CT-01` est le
seul cas de test qui sépare les deux réponses.

### 2. Ce que ça donne à l'échelle d'un vrai calcul · 6 min

Ouvrir [`exemples/mass-balance/`](exemples/mass-balance/) — doser les composants d'un lot
sur une balance de résolution finie. Montrer trois choses, dans cet ordre :

| | Quoi | Où |
|---|---|---|
| **a** | Le besoin tel que le métier l'écrit, avec ses questions non tranchées | [`NEED.md`](exemples/mass-balance/NEED.md) |
| **b** | La règle qui tranche le faux ami : qui reçoit le résidu de pesée, **et comment on départage une égalité** | [`RG-050`](exemples/mass-balance/spec/SPEC-MAS-001.en.md#rg-050--allocation-of-the-residual) |
| **c** | La conclusion de l'analyse des écarts : **le code est conforme, la spécification est en défaut** | [`DEVIATIONS.md`](exemples/mass-balance/DEVIATIONS.md) |

Le point **c** est celui qui surprend. La démarche ne s'arrête pas quand le code est
écrit : elle s'arrête quand le métier a confirmé, chiffres en main, que ce qui tourne
calcule ce qu'il avait décrit. Ici, la confrontation a mis en cause le document, pas le
programme — et la correction est remontée au document, datée et versionnée.

### 3. L'outil, en direct · 4 min

C'est le moment qui règle d'avance la question « qui va maintenir tout ça ».

```bash
java outils/Verifier.java
# 85 fichier(s) examiné(s), dont 12 spécification(s) — 0 échec(s), 0 avertissement(s)
```

Puis **casser une règle devant eux**. Dans `exemples/average-speed/spec/SPEC-SPD-001.en.md`,
la règle `RG-010` écrit `leg_duration = distance ÷ speed`. Renommer `speed` en `leg_speed`
— une faute de frappe parfaitement plausible — et relancer :

```
ÉCHEC  C-03  …/SPEC-SPD-001.en.md  « leg_speed » est employé dans une règle
                                     mais déclaré nulle part (fantôme)

85 fichier(s) examiné(s), dont 12 spécification(s) — 1 échec(s), 0 avertissement(s)
```

*(Manipulation vérifiée : elle produit exactement ce message. Annuler la modification
après la démonstration.)*

Trois choses à dire pendant que le message est à l'écran :

- **C'est un fichier Java unique, sans dépendance ni construction.** Il tourne en
  intégration continue tel quel, et rend `1` sur échec.
- **25 des 41 contrôles du catalogue sont mécanisés.** Les seize autres demandent un
  jugement — le document le dit, plutôt que de laisser croire à une couverture totale.
- **Ces contrôles ont trouvé des défauts réels dans nos propres exemples**, pas seulement
  dans un corpus de démonstration. `C-03` en a trouvé deux le jour où il a été écrit.

### 4. Ce que ça coûte · 5 min

La question viendra ; autant la poser soi-même.

| L'objection | La réponse |
|---|---|
| « On n'a pas le temps » | Le temps n'est pas ajouté, il est **déplacé** : les questions traitées en revue sont celles qui, sinon, se posent en développement — plus tard, plus cher, et tranchées par la mauvaise personne |
| « Combien ça ralentit ? » | **Le délai augmente réellement sur les trois premières spécifications.** À dire à la direction avant de commencer, sous peine d'arrêter précisément quand ça allait devenir rentable |
| « Il faut tout spécifier ? » | Non. L'échelle de maturité en cinq niveaux existe pour ça : une exploration jetable n'a besoin d'aucune spécification, et prétendre le contraire discrédite la méthode |
| « C'est du cycle en V » | Non : la spécification est versionnée à côté du code, relue en demande de fusion, et corrigée quand l'analyse des écarts la met en défaut — ce qui est arrivé dans l'exemple du point 2 |

→ Huit autres objections, avec leurs réponses : [`FAQ.md`](FAQ.md).

### 5. La demande · 5 min

Une seule, et elle est concrète.

> **Prenons une fonction que nous avons déjà.** Une fonction connue, moyennement complexe,
> dont le code tourne aujourd'hui. Écrivons-en la spécification *a posteriori*, sans
> regarder le code. Puis faisons-la implémenter par quelqu'un qui ne connaît pas le
> domaine. Et comparons.

Ce qu'il faut arrêter **en séance**, sinon rien ne se passera :

| | |
|---|---|
| **Quelle fonction** | Ni triviale, ni monstrueuse. Elle doit avoir au moins un faux ami : un arrondi, un ex æquo, une valeur absente, une borne |
| **Qui valide** | Une personne, nommée. C'est elle qui signe que les règles sont les bonnes |
| **Qui implémente à l'aveugle** | Quelqu'un d'extérieur au domaine. C'est la moitié qui donne le verdict |
| **Pour quand** | Une date. Deux semaines suffisent pour une fonction de cette taille |

> **Le verdict ne se discute pas, il se constate.** Soit les deux implémentations donnent
> le même résultat sur tout le jeu d'essai, soit elles divergent — et chaque divergence est
> une question que la spécification n'avait pas tranchée. **Les écarts convainquent ; le
> discours, non.**

---

## Ce qu'on ne montre pas

Une réunion de découverte se perd sur ces sujets. Ils viendront d'eux-mêmes, plus tard.

| | Pourquoi pas maintenant |
|---|---|
| `CADRE.md` en entier | C'est un document de référence, pas de présentation. On l'ouvre quand on écrit |
| Le pseudo-langage et son lexique | Ça ressemble à « une syntaxe de plus à apprendre ». Le [relevé météo](exemples/weather-summary/) est là pour ceux que ça intéresse, après |
| Les 41 contrôles, un par un | Le catalogue est un actif, pas un argument |
| Le fil rouge complet | Douze fonctions, c'est la démonstration du passage à l'échelle — utile à la deuxième séance |

---

## Les questions qui reviennent, et la réponse courte

| | |
|---|---|
| « Les experts métier ne voudront jamais écrire dans Git » | Ils écrivent du markdown ; quelqu'un d'autre pousse. La contrainte réelle n'est pas l'outil, c'est la relecture par une paire d'yeux différente |
| « Et si la spécification et le code divergent ? » | Le code cite les `RG-xxx` qu'il implémente ; un outil moissonne ces citations et signale ce qui n'est implémenté nulle part ou ne correspond plus à rien. Voir [le rapport de couverture](exemples/average-speed/reports/COVERAGE-REPORT.md) |
| « Pourquoi ne pas générer le code depuis la spécification ? » | Parce qu'on perdrait la seule chose qui a de la valeur : deux lectures indépendantes du même énoncé |
| « Une IA ne peut-elle pas écrire la spec à partir du code ? » | Elle produit une description de ce que le code **fait**, pas de ce qu'il **devrait** faire. Les deux se ressemblent, et c'est le piège |
| « Ça ne marche que pour les algorithmes ? » | La question est légitime — le dépôt s'appelle encore *cadre-specification-algorithmique*. La méthode couvre aussi l'architecture fonctionnelle : le découpage, le glossaire, le catalogue de données |

→ Les réponses longues : [`FAQ.md`](FAQ.md).

---

## Après la séance

1. Écrire la fonction d'étalonnage retenue, avec son valideur — **une seule**.
2. La faire relire dans l'ordre des trois étages : l'outil, puis le métier, puis un
   développeur extérieur. Le critère de sortie du troisième est **zéro question**.
3. La faire implémenter à l'aveugle.
4. Comparer, et présenter les écarts à la même équipe. C'est cette réunion-là qui décide,
   pas la première.

> La seule règle non négociable de la méthode : **la spécification s'écrit avant le code.**
> Tout le reste est un outil, pas un règlement.
