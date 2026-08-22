# 1 — Le besoin, tel qu'il arrive

*Première étape de la chaîne. Ce document est **antérieur** à toute formalisation : c'est
ce que le métier dit, dans ses mots, avant qu'on lui demande quoi que ce soit.*

---

## Ce que la responsable formulation raconte

> « On prépare des lots en mélangeant plusieurs composants. Pour chaque lot, la recette
> donne des pourcentages : tel composant 33,3 %, tel autre 20 %, et ainsi de suite. On
> multiplie par la masse du lot, ça donne la masse à peser pour chacun.
>
> Le problème, c'est que la balance ne descend pas en dessous du gramme. Donc on arrondit.
> Et quand on additionne les masses arrondies, **on ne retombe pas sur la masse du lot**.
> Il manque un gramme, ou il y en a un de trop.
>
> Aujourd'hui chaque opérateur se débrouille. Certains rattrapent sur le premier
> composant, d'autres sur le plus gros, d'autres ne rattrapent pas du tout. Résultat : deux
> opérateurs, deux lots différents, à partir de la même recette. Et à l'audit, on ne sait
> pas expliquer pourquoi.
>
> Ce qu'on veut, c'est que le calcul soit fait une fois pour toutes, de la même façon, et
> qu'on puisse le justifier. »

---

## Ce qu'on en retient, et ce qu'on ne sait pas encore

**Ce que le besoin dit clairement.** Il y a un calcul, il est simple, et il produit
aujourd'hui des résultats qui divergent entre opérateurs. La divergence n'est pas causée
par une erreur : elle vient d'une **décision que personne n'a prise explicitement**.

**Ce qu'il ne dit pas — et qu'il faudra trancher.** Ces questions ne sont pas des détails
d'implémentation. Ce sont les décisions métier que la spécification devra porter, et
qu'aucun développeur ne peut prendre à la place du métier :

| Question | Pourquoi elle est métier |
|---|---|
| Arrondir dans quel sens ? Au plus proche, et si on tombe pile au milieu ? | Sur des milliers de lots, « moitié vers le haut » consomme systématiquement plus de matière que prévu |
| Qui reçoit le gramme manquant ? | Chaque réponse donne un lot différent, et la responsable en répond devant l'audit |
| Et si deux composants sont à égalité pour le recevoir ? | Sans règle, le résultat dépend de l'ordre de saisie — donc il n'est pas reproductible |
| Jusqu'à quel écart accepte-t-on de rattraper ? | Un gramme est normal ; cinquante signalent une recette ou une balance inadaptée, et les absorber masquerait le défaut |
| Que fait-on si les pourcentages ne totalisent pas 100 % ? | C'est une recette fausse : la corriger silencieusement serait pire que la refuser |

> **C'est exactement ce que la méthode appelle les [faux amis](../../CADRE.md).** Ils
> ressemblent à des détails techniques — un arrondi, un ordre de parcours — et ce sont des
> décisions métier. Tant qu'elles ne sont pas écrites, **c'est le développeur qui les
> prendra**, sans le savoir et sans mandat.

---

## Ce qui est décidé à cette étape, et ce qui ne l'est pas

**Décidé.** Le périmètre : on calcule les masses à peser. On ne pilote pas la balance, on
ne trace pas les lots de matière première, on ne contrôle pas la qualité du mélange.

**Pas encore décidé, et volontairement.** Aucune structure de données, aucun langage,
aucun format d'échange, aucune performance. Le besoin ne dit pas non plus *comment*
arrondir : il dit qu'**il faut le décider**. La différence est toute la méthode.

---

→ Étape suivante : [2 — la spécification](2-SPEC-MAS-001-batch-mass-balance.md), où chaque
question ci-dessus reçoit une réponse écrite, numérotée et justifiée.
