# La vitesse moyenne d'un trajet — le plus petit exemple complet

**Deux fonctions, deux paramètres, trois règles, six cas de test.** C'est le plus petit
exemple du dépôt, et il est complet : la spécification, un document par cas, le code, les
tests et les deux rapports. Comptez cinq minutes pour la spécification, une minute par cas.

| | |
|---|---|
| **La spécification** | [en anglais](spec/SPEC-SPD-001.en.md) — fait foi · [en français](spec/SPEC-SPD-001.fr.md) |
| **Les cas de test** | [`tests/`](tests/) — une page courte par cas : ce qu'il attrape, ce qu'il laisse passer |
| **Le code** | [`code/`](code/) — disposition Maven, annoté `@ImplementsSpec` |
| **Les rapports** | [test](reports/TEST-REPORT.md) · [couverture](reports/COVERAGE-REPORT.md) |

```bash
cd code && javac -encoding UTF-8 -d /tmp/as $(find src -name '*.java')
cd .. && java -cp /tmp/as method.averagespeed.AverageSpeedTest \
    code/src/test/resources/reference-data.csv spec/SPEC-SPD-001.en.md reports/TEST-REPORT.md

java outils/Couverture.java exemples/average-speed     # depuis la racine du dépôt
```

---

## Le faux ami

**La vitesse moyenne n'est pas la moyenne des vitesses.**

60 km à 30 km/h puis 60 km à 60 km/h donnent **40 km/h**, pas 45. On passe deux heures sur
le segment lent et une sur le rapide : le lent pèse deux fois plus.

L'erreur n'est pas de l'étourderie. « Vitesse moyenne » se lit naturellement comme « la
moyenne des vitesses », et un développeur sans spécification écrira celle-là. Le nombre
obtenu est plausible, il se situe entre les deux vitesses, il varie dans le bon sens.

## Deux choses à voir, et rien de plus

**Un cas gardé bien qu'il ne discrimine pas.** [`CT-02`](tests/CT-02.md) donne 45 km/h par
les deux méthodes — elles coïncident quand les segments prennent le même temps. Il est
gardé pour montrer qu'**une suite verte ne prouve rien** : trois des six cas sont passés
par l'implémentation fausse. Le rapport de test le dit cas par cas.

**Un paramètre que rien ne décidait.** Le mode d'arrondi `P-01` était validé, daté,
implémenté et déclaré couvert — et aucun cas ne produisait un quotient qu'un changement de
mode déplace. [`CT-06`](tests/CT-06.md) a été ajouté pour cela : `74 ÷ 8 = 9,25` pile sur
l'égalité, où `HALF_EVEN` publie 9,2 et `HALF_UP` publierait 9,3.

← [Retour aux exemples](../README.md)
