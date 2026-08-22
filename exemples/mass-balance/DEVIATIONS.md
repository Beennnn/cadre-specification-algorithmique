# 5 — L'analyse des écarts

*Dernière étape de la chaîne, et celle que la plupart des démarches oublient. Les douze
étapes ne s'arrêtent pas à la livraison du code : elles s'arrêtent quand **le métier a jugé
les écarts au regard de ses tolérances**.*

---

## Ce qu'on constate

Le harnais de qualification passe : les huit cas de référence sont satisfaits, `INV-01` à
`INV-04` sont tenus.

```
  OK      CT-05    7 doses, résidu 0.300, INV-01 et INV-02 tenus
```

Et pourtant, en regardant les valeurs propagées sur CT-05, la responsable formulation
s'arrête :

| Composant | Fraction visée | Masse visée | **Masse pesée** | Écart relatif |
|---|---|---|---|---|
| `CMP-A` … `CMP-F` | 0,142857 | 0,142857 kg | **0,100 kg** | **−30 %** |
| `CMP-G` | 0,142858 | 0,142858 kg | **0,400 kg** | **+180 %** |

Le lot pèse exactement 1,000 kg. La conservation est parfaite. Et la recette n'est **pas
respectée du tout** : un composant reçoit près du triple de sa part.

---

## L'instruction de l'écart

C'est ici que le métier a besoin de la technique, sans que la technique décide.

**Analyse statique** — lire ce que le composant fait, étape par étape, et le confronter à
la règle. `RG-020` arrondit chaque dose au pas de balance : 0,1429 kg sur un pas de
0,100 kg donne 0,100 kg, six fois. `RG-030` calcule un résidu de 0,300 kg. `RG-040`
l'accepte, puisque la borne est de trois pas. `RG-050` le verse **en entier** sur la plus
grande fraction. **Le code applique exactement ce qui est écrit.**

**Analyse dynamique** — faire varier une entrée et vérifier que la sortie bouge comme la
règle le prédit. En ramenant le pas de balance à 0,001 kg, les mêmes fractions donnent des
doses à 0,143 kg et un résidu d'un seul pas. L'anomalie disparaît. **Elle n'est donc pas
dans le code : elle est dans le rapport entre le pas de balance et la finesse de la
recette.**

---

## Le verdict métier

> **Le code est conforme. La spécification est en défaut.**

`RG-050` — verser tout le résidu sur un seul composant — est juste tant que le résidu vaut
**un pas**. Il devient absurde quand il en vaut trois et que le pas est grossier devant les
doses. La règle n'a jamais été pensée pour ce régime, et rien dans la spécification
n'interdisait d'y entrer : `P-02 = 3` borne le résidu en **nombre de pas**, une grandeur
sans rapport avec la finesse de la recette.

**L'écart est significatif** : une part de 14 % livrée à 40 % sort de toute tolérance de
formulation. Le lot serait à rebuter.

---

## Ce que l'écart produit

| | |
|---|---|
| **`SM-001`** — suggestion du développeur | Borner le résidu **relativement à la plus petite dose**, et non en nombre de pas absolus |
| **Statut** | *Acceptée avec adaptation.* Le principe est retenu ; le seuil relève du métier, pas du développeur |
| **`Q-01`** — question déjà ouverte au §12 | « Faut-il répartir un résidu de plusieurs pas sur plusieurs composants ? » — l'analyse lui donne sa réponse et son urgence |
| **Décision** | Répartir le résidu par pas successifs, un pas par composant dans l'ordre des fractions décroissantes |
| **Version** | Le changement modifie des résultats : la spécification passera en **2.0.0**, avec notice `N-2.0.0` |

> **La question était déjà posée, et c'est le bon signe.** `Q-01` figurait au §12 depuis la
> rédaction, sans décideur pressé de trancher. L'analyse des écarts ne l'a pas découverte :
> elle lui a donné un chiffre — +180 % — et un cas reproductible. Une question ouverte
> **datée et nommée** est ce qui permet à un écart de devenir une décision, au lieu d'un
> correctif silencieux dans le code.

---

## Ce qu'il ne faut surtout pas faire

| Réflexe | Pourquoi c'est une faute |
|---|---|
| Corriger le code pour « lisser » le résidu | Le développeur trancherait seul une question de formulation. Le code deviendrait non conforme à une spécification qui, elle, resterait fausse |
| Modifier `CT-05` pour qu'il passe | Les données de référence sont un **engagement opposable**. Elles ne se modifient que par revalidation métier datée et motivée — jamais parce qu'un résultat déplaît |
| Retirer `CT-05` du jeu d'essai | C'est le seul cas qui atteint la borne. Le supprimer rendrait le défaut invisible sans le faire disparaître |
| Conclure « le test est trop sévère » | Le test a fait exactement son travail : il a rendu visible, sur un cas reproductible, une décision métier prise par défaut |

---

## Ce que la chaîne a permis

L'écart a été **constaté**, **instruit** et **imputé** en restant au bon endroit à chaque
étape :

1. le harnais a produit des valeurs propagées lisibles par le métier ;
2. l'analyse statique a établi que le code appliquait la règle écrite ;
3. l'analyse dynamique a isolé le régime qui déclenche l'anomalie ;
4. le métier a jugé l'écart significatif **au regard de ses tolérances** ;
5. la correction porte sur la **spécification**, versionnée, avec notice.

Sans cette chaîne, le scénario probable est connu : un opérateur constate un lot bizarre,
un développeur ajoute un correctif local, et la règle réelle diverge du document — sans que
personne ne puisse plus dire laquelle fait foi.

---

← [4 — le code](code/) · [Retour au parcours](README.md)
