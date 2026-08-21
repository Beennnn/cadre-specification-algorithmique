# Guide 8 — Analyser les écarts

*La dernière étape, et celle qui referme la boucle. L'implémentation tourne et produit des
résultats ; ils ne coïncident pas exactement avec ceux du jeu d'essai. **C'est le métier
qui instruit l'écart**, avec le support des développeurs.*

---

## Pourquoi cette étape existe

Un écart entre le résultat obtenu et le résultat attendu n'est **ni normal ni anormal en
soi**. C'est une question à instruire, et personne d'autre que le métier ne peut la
trancher : lui seul sait si un dixième de degré, un centime ou trois kilomètres ont une
conséquence.

Sans cette étape, il ne reste que deux comportements, et ils sont mauvais tous les deux :
ouvrir un ticket de bogue pour chaque écart, ou les accepter tous en bloc parce que « ça a
l'air proche ».

## Qui fait quoi

| | |
|---|---|
| **Le métier conduit l'analyse** | Il sait ce qu'un écart signifie, et il décide s'il est acceptable |
| **Le développeur fournit les moyens de voir** | Il expose les valeurs intermédiaires, explique où elles sont produites, et corrige si le défaut est dans le code. **Il ne conclut pas sur la signification de l'écart** |

C'est exactement le partage du reste du cadre, appliqué à la recette.

## Le matériau : les valeurs propagées

C'est ici que le `profil`, le `détail_par_ligne` et les autres sorties intermédiaires
prennent tout leur sens.

> **Une spécification qui n'expose que son résultat final rend l'analyse d'écart
> impossible.** On constate que ça diverge ; on ne sait pas **où**.

D'où une exigence à poser **dès l'écriture**, pas au moment de la recette :

**Toute fonction dont on devra un jour analyser les écarts déclare en sortie les valeurs
intermédiaires de sa [chaîne de traitement](../CADRE.md).** Elles ne sont pas un journal
de débogage : ce sont des sorties de premier rang, avec leur type, leur unité et leur
précision comme les autres.

## La méthode : remonter la chaîne

On ne compare pas deux nombres finaux. **On compare étape par étape**, dans l'ordre de la
chaîne `ET-xx`, depuis les entrées jusqu'à la sortie, et **on s'arrête à la première
étape où les valeurs divergent**.

```
ET-01 forces          obtenu 542,57 N     attendu 542,57 N     ✓
ET-02 énergie méca.   obtenu 54,2568 MJ   attendu 54,2568 MJ   ✓
ET-03 traction        obtenu 60,2853 MJ   attendu 60,2853 MJ   ✓
ET-04 auxiliaires     obtenu 1,7160 MJ    attendu 1,6364 MJ    ← ici
ET-05 énergie segment obtenu 17,2226 kWh  attendu 17,2005 kWh    (symptôme)
```

Tout ce qui est en aval du premier point de divergence est un **symptôme**, pas une piste.
Chercher la cause dans le résultat final est la façon la plus sûre de perdre une journée.

C'est précisément à cela que sert la chaîne de traitement : elle donne l'ordre dans lequel
remonter.

## Qualifier l'écart — quatre questions, dans cet ordre

| # | Question | Si oui |
|---|---|---|
| 1 | **Le résultat attendu était-il juste ?** | L'erreur est dans le jeu d'essai. On le corrige — et on se demande comment il a été validé |
| 2 | Est-ce un écart de **reproductibilité** ? Deux implémentations conformes devraient donner le même nombre | Défaut d'implémentation, **ou spécification ambiguë**. La seconde hypothèse est la plus fréquente et la plus utile |
| 3 | Est-ce un écart de **justesse numérique** ? Le nombre n'est pas la vraie valeur des formules | La méthode de résolution ou son pas est inadapté |
| 4 | Est-ce un écart de **validité du modèle** ? Les formules ne décrivent pas exactement la réalité | **Ce n'est pas un défaut du programme.** C'est une question de modèle, et elle appartient au métier |

La première question est celle qu'on oublie, et c'est souvent la bonne : le résultat
attendu ayant été calculé à la main, il peut être faux. Le poser d'emblée évite de faire
chercher un développeur pendant deux jours dans un code correct.

## Décider : l'écart est-il significatif ?

Un écart n'est pas « grand » ou « petit » dans l'absolu. Il l'est **au regard d'une
tolérance déclarée** — celles du §8.2 de la spécification.

| L'écart… | Verdict | Qui décide |
|---|---|---|
| dépasse la tolérance de **reproductibilité** | défaut **bloquant** | technique |
| la respecte, mais dépasse la **justesse numérique** | défaut à corriger | technique |
| respecte les deux, mais dépasse la **validité du modèle** | question de modèle, à instruire | **métier** |
| respecte les trois | **conforme** — on trace et on passe | métier |

> **Le piège, et il est fréquent : ajuster la tolérance après coup pour faire passer le
> résultat.** C'est le geste qui vide toute la démarche de son sens, parce qu'il est
> toujours défendable sur le moment.
>
> Une tolérance se change comme une règle : par une décision **datée, motivée, validée**,
> avec sa notice de changement — **jamais dans le feu de la recette**, et jamais par celui
> que l'écart dérange.

## Ce que l'analyse produit

Trois issues, toutes tracées, aucune silencieuse :

| Issue | Ce qu'on écrit |
|---|---|
| **Le code est en cause** | une correction, et le cas de test qui l'aurait attrapée plus tôt |
| **La spécification est en cause** | une `Q-xx` ou une `SM-xxx` ([guide 5](5-VALIDER.md)), puis une nouvelle version |
| **L'écart est accepté** | une ligne motivée : quel écart, contre quelle tolérance, qui l'a accepté, quand |

La troisième est une décision, pas un abandon. Elle se relit dans deux ans.

## L'analyse d'écart est aussi un examen de la spécification

Deux constats à faire à chaque fois, parce qu'ils améliorent le cadre lui-même :

- **Le métier n'a pas pu instruire l'écart faute de valeurs intermédiaires ?** La
  spécification était incomplète : ses sorties ne permettaient pas de voir. À corriger
  dans la spécification, pas dans le code.
- **Le même type d'écart revient d'une fonction à l'autre ?** Ce n'est pas une
  coïncidence : c'est un manque du modèle de spécification ou de la liste de vérification.
  À remonter là.

## La liste de contrôle

- [ ] Les **valeurs intermédiaires** de la chaîne sont disponibles pour chaque cas comparé
- [ ] La comparaison est faite **étape par étape**, et on s'est arrêté au **premier** point
      de divergence
- [ ] Les **quatre questions** de qualification ont été posées, dans l'ordre — en
      commençant par « le résultat attendu était-il juste ? »
- [ ] L'écart est confronté aux **tolérances déclarées**, pas à une impression
- [ ] Aucune tolérance n'a été modifiée pendant l'analyse
- [ ] L'issue est **écrite** : correction, question ouverte, ou acceptation motivée
- [ ] Ce que l'écart révèle sur la **spécification elle-même** a été noté
