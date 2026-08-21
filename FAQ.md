# Objections fréquentes

Les réponses courtes aux objections qui reviennent à chaque fois qu'on présente cette
méthode. Aucune n'est illégitime : les traiter d'avance évite qu'elles reviennent en
travers de l'adoption.

---

### « C'est du cycle en V déguisé. »

Non, et la différence est précise : **ce qui est figé, c'est la répartition des
responsabilités, pas l'ordre des phases.**

- On travaille par **lots de 5 à 8 fonctions**, pas par grand document préalable. Le lot
  suivant s'écrit pendant que le précédent part en développement.
- La spécification **est révisée** quand le développement enseigne quelque chose : c'est
  exactement le rôle des questions ouvertes et du versionnement.
- Elle porte sur **ce qui est calculé**, jamais sur l'architecture ni le planning —
  contrairement à un cahier des charges de cycle en V.

Parnas et Clements l'ont formulé en 1986 : le processus réel n'est jamais linéaire, mais
la documentation qu'il *aurait* produite s'il l'avait été a de la valeur. On produit cette
documentation, on ne prétend pas que le chemin l'a été.

### « On n'a pas le temps d'écrire ça. »

Le temps n'est pas ajouté, il est **déplacé**. Les questions traitées en revue sont
exactement celles qui, sinon, se posent en développement — plus tard, plus cher, et
souvent tranchées par la mauvaise personne.

En revanche, **le délai augmente réellement sur les trois premières spécifications**. Il
faut le dire à la direction avant de commencer, sous peine d'arrêter la démarche
précisément au moment où elle allait devenir rentable.

Et surtout : **on ne spécifie pas tout.** L'échelle de maturité du
[guide 1](guides/1-DECOUPER.md) existe pour ça. Une exploration jetable dans un carnet de
calcul n'a besoin d'aucune spécification, et prétendre le contraire discrédite la méthode.

### « Pourquoi pas du Gherkin / du BDD ? »

Gherkin (*Étant donné / Quand / Alors*) est excellent pour des scénarios
comportementaux : un enchaînement d'actions et d'effets observables.

Il devient illisible dès qu'il s'agit d'un **calcul** : douze étapes intermédiaires, des
arrondis, une répartition au prorata, un départage d'ex æquo. On finit par écrire des
tableaux dans des *Examples* et à perdre tout ce que la syntaxe apportait.

Les deux se combinent très bien : Gherkin pour les parcours, cette méthode pour les
fonctions de calcul qu'ils appellent. Ce que Gherkin apporte — l'exemple concret comme
oracle — est déjà au §10 de chaque spécification.

### « Pourquoi ne pas générer le code depuis la spécification ? »

Parce qu'on perdrait exactement ce qu'on cherche.

La spécification est **volontairement incomplète du point de vue de la machine** : elle
ne dit ni les structures de données, ni la parallélisation, ni le cache, ni la gestion des
erreurs techniques. C'est ce vide qui laisse au développement la liberté de choisir
l'architecture et d'optimiser.

Une spécification exécutable serait un programme — écrit dans un langage de plus, sans
écosystème, sans outillage, et que le métier ne pourrait plus relire. On aurait déplacé le
problème, pas résolu.

### « Les experts métier ne voudront jamais écrire dans Git. »

L'inconfort est réel et dure **environ deux semaines**. Il se traite : édition directe
dans l'interface web de la forge, modèle pré-rempli, accompagnement en binôme sur les deux
premières spécifications.

Ne cédez pas sur ce point. Un traitement de texte partagé coûte, dès la troisième
spécification, plus cher que la semaine d'apprentissage qu'il prétendait éviter : plus
d'historique exploitable, plus de revue ligne à ligne, plus de comparaison entre versions,
plus de moyen de savoir quelle version a produit le chiffre de l'an dernier.

### « Et si la spécification et le code divergent ? »

Ils divergeront. La méthode ne l'empêche pas — elle rend la divergence **détectable et
traitable** :

- Le jeu d'essai du §10 est exécuté automatiquement : s'il passe, le comportement
  spécifié est respecté.
- Chaque `RG-xxx` est citée dans le code et dans les tests : une règle sans citation est
  visible.

Et la règle est explicite : **quand les deux divergent, la spécification fait foi.** Soit
le code est corrigé, soit la spécification est amendée, avec une version et une trace.
Jamais de troisième voie.

### « Ça ne marche que pour les algorithmes, pas pour l'interface. »

C'est exact, et c'est voulu. Cette méthode vise les **fonctions de calcul et de
décision** : celles où une ambiguïté produit un résultat faux plutôt qu'un écran laid.

Pour l'interface, les maquettes, les parcours et les tests d'usage font mieux. Les deux
se rencontrent au bon endroit : l'interface **appelle** les fonctions spécifiées, et le
guide 1 rappelle qu'on ne découpe jamais par écran.

### « Nos règles changent tout le temps ; la spécification sera périmée. »

Si les règles changent souvent, c'est un argument **pour** la méthode, pas contre.

- Ce qui change souvent, ce sont presque toujours les **paramètres** (seuils, taux,
  barèmes), pas les règles. Les séparer — §6 du modèle — permet de changer une valeur sans
  livraison logicielle.
- Une règle qui change deux fois par an dans un document versionné, c'est une trace ; la
  même règle qui change deux fois par an dans du code, c'est une archéologie.
- Et la fiche de contraintes pose explicitement la question « à quelle fréquence, et par
  qui ? », dont découle l'architecture qui rend le changement peu coûteux.

### « On a déjà des spécifications fonctionnelles en traitement de texte. »

Prenez-en une et posez-lui les huit questions de l'aide-mémoire du
[CADRE.md](CADRE.md) : les unités y sont-elles ? les arrondis ? les ex æquo ? les valeurs
absentes ? le jeu d'essai calculé à la main ? les volumes chiffrés ?

Si les réponses sont là, vous n'avez pas besoin de cette méthode. En pratique, l'exercice
prend dix minutes et se répond tout seul.

### « Une IA ne pourrait-elle pas écrire la spécification à partir du code ? »

Elle peut en produire une **première ébauche**, et c'est utile pour démarrer sur un
existant volumineux — surtout aux niveaux 0 à 2 de l'échelle de maturité : lister les
fonctions, les nommer, les ancrer.

Mais elle ne peut pas produire les niveaux 3 et 4, pour une raison de principe : **le code
dit ce qui est fait, pas ce qui devrait l'être.** Il ne distingue pas la règle voulue du
contournement provisoire, ni la décision métier de l'accident. Un arrondi vers le bas
présent dans le code est-il une exigence de sécurité ou une étourderie de 2019 ? Seul le
métier le sait — et c'est précisément la question à laquelle la spécification doit
répondre.

Utilisée comme brouillon à corriger, elle fait gagner du temps. Utilisée comme source de
vérité, elle **grave les bogues existants dans le marbre** en leur donnant l'autorité d'une
spécification.

Et dans tous les cas, le principe reste le même : **une personne nommée assume ce qu'elle
a fait produire à l'IA**, et l'assume comme si elle l'avait écrit — voir « L'humain aux
commandes » au [guide 5](guides/5-VALIDER.md).

### « L'IA relit les specs : peut-elle aussi les valider ? »

Non, et la distinction n'est pas cosmétique. Valider est un **acte engageant** : il est
daté, nominatif, et opposable des années plus tard devant un auditeur. Une IA
n'a pas de mandat, ne rend de comptes à personne, et ne sera pas là.

Elle **pré-instruit** : elle produit des constats localisés qu'un humain vérifie et
retient ou écarte. C'est utile, mesurable, et ça libère le temps humain pour ce que seule
une lecture humaine peut faire. Ce n'est pas une signature.
