# Glossaire de la méthode

*Le vocabulaire de ce dépôt. À ne pas confondre avec le **glossaire du domaine**, que
chaque équipe produit pour son propre métier ([guide 2](guides/2-GLOSSAIRE.md)).*

---

| Terme | Définition |
|---|---|
| **Ancrage** | Pointeur d'une fiche de fonction vers l'endroit du code qui l'implémente. Vérifiable mécaniquement, sans quoi il ment au bout de six mois. |
| **Cas de test** (`CT-xx`) | Un jeu d'entrées et le résultat attendu correspondant, **calculé à la main** par le métier. Un cas de test dont le résultat provient d'une implémentation ne vérifie rien. |
| **Contrat** | Les entrées et sorties d'une fonction, typées avec leur unité, leur précision et leur domaine de validité. Niveau 3 de l'échelle de maturité. |
| **Découpage** | L'opération qui identifie et nomme les fonctions d'un périmètre. Précède toute écriture de règle. |
| **Faux ami** | Un point qui *ressemble* à une décision technique et qui est une décision métier : arrondi, ordre des opérations, départage d'ex æquo, valeur absente, borne, mode dégradé. Cause majoritaire des écarts entre deux implémentations honnêtes. |
| **Fiche de contraintes** | La section d'une spécification qui exprime, **en unités métier et chiffrées**, les volumes, la latence, l'exactitude, la rejouabilité, la fréquence de changement. C'est elle qui permet à l'IT de choisir le langage et l'architecture. |
| **Fiche de fonction** | Le document qui décrit une fonction : nom, description, propriétaire, ancrage, contrat, niveau de maturité. Aux niveaux 0 à 3, c'est le seul document existant. |
| **Fonction** (`FN-xxx`) | Une transformation qui produit un résultat identifiable à partir d'entrées identifiables, et dont le métier sait nommer le résultat sans parler de mise en œuvre. |
| **Invariant** (`INV-xx`) | Une propriété vraie pour **toutes** les entrées, et non seulement pour les cas du jeu d'essai. Se teste sur des entrées générées. |
| **Jeu d'essai** | L'ensemble des cas de test d'une spécification. C'est le **plan de recette**, pas une illustration. |
| **Justesse** | Écart entre le résultat calculé et une vérité connue — solution analytique, étalon, mesure. À distinguer de la reproductibilité. |
| **Niveau de maturité** | De 0 à 4, l'état d'une fonction : nommée, décrite, ancrée, contractualisée, spécifiée. Un niveau bas est un état **assumé**, pas un travail inachevé. |
| **Oracle** | La source de vérité d'un cas de test : un calcul à la main, une solution analytique, un étalon. Jamais le programme testé. |
| **Paramètre** (`P-xx`) | Une valeur qui peut changer sans que la logique change : seuil, taux, barème. Porte un propriétaire, un circuit de modification et une date d'effet. Ne vit jamais au milieu d'une règle. |
| **Propriétaire de la règle** | La personne qui **répond** d'une règle et arbitre les questions ouvertes qui la concernent. Une personne, jamais un comité. |
| **Pseudo-langage** | Le français discipliné dans lequel les règles sont écrites : structuré, typé, sans construction propre à un langage de programmation. |
| **Question ouverte** (`Q-xx`) | Un point non tranché, avec un décideur nommé et une date. Reste au document une fois fermée, avec sa réponse. Un désaccord se transforme en question ouverte, **jamais en compromis de rédaction**. |
| **Règle de gestion** (`RG-xxx`) | Un énoncé normatif décrivant une partie de ce qui est calculé. Porte un identifiant stable, jamais réutilisé. |
| **Répondant technique** | Le développeur qui co-écrit la fiche de contraintes, pose les questions qui font mal, et signale ce qui coûtera cher. Ne rédige pas les règles. |
| **Reproductibilité** | Propriété de deux implémentations conformes de produire le même résultat. S'évalue contre la spécification, pas contre la réalité. |
| **Spécification** (`SPEC-xxx`) | Le document complet d'une fonction : glossaire, contrat, paramètres, règles, invariants, cas d'erreur, jeu d'essai, fiche de contraintes, questions ouvertes. Niveau 4. |
| **Test de la double implémentation** | Le critère d'acceptation d'une spécification : deux développeurs qui ne se parlent pas, dans deux langages différents, produisent le même résultat. Se dédouble en reproductibilité et justesse sur des grandeurs continues. |
| **Validité du modèle** | Pour un calcul scientifique, l'adéquation entre les équations et la réalité mesurée. Un écart n'est **pas** un défaut du programme. |
