# Sprint 1 - Cadrage du framework

## Objectif
Mettre en place la base du framework de type MVC autour d’un `FrontController` capable de :
- détecter les classes contrôleurs via annotation,
- charger les classes d’un package donné,
- initialiser la découverte au démarrage de l’application ou au premier appel,
- lire la configuration du package cible depuis `web.xml`.

L’idée de ce sprint n’est pas de tout réaliser immédiatement, mais de bien définir le mécanisme avant l’implémentation.

## Idée générale
Le framework doit pouvoir :
1. recevoir un nom de package depuis la configuration web,
2. parcourir les classes disponibles dans ce package,
3. identifier celles qui portent une annotation de type contrôleur,
4. garder une structure interne des contrôleurs détectés,
5. utiliser cette structure pour router les requêtes HTTP plus tard.

## Points importants sur les annotations
L’annotation servira à marquer explicitement une classe comme contrôleur.

Exemple conceptuel :
- une classe annotée est considérée comme contrôleur,
- une classe non annotée est ignorée,
- le `FrontController` se base uniquement sur cette annotation pour identifier les contrôleurs.

## Chargement des classes
Il faut prévoir une stratégie de découverte des classes.

### 1. Package spécifique
Le cas le plus simple est de scanner un package donné, par exemple `controller` ou `controllers`.

Avantages :
- simple à configurer,
- rapide à tester,
- adapté à un premier sprint.

### 2. Parcours de toutes les classes du classpath
Une stratégie plus large consiste à parcourir l’ensemble des classes disponibles dans le classpath, y compris celles de l’application de test.

Cette approche permet de détecter automatiquement les classes annotées, mais elle est plus coûteuse et plus complexe à mettre en place.

Pour ce sprint, la solution la plus raisonnable est :
- de partir d’un package paramétrable,
- puis d’étendre le mécanisme si nécessaire.

## Démarrage de l’application web
Il existe deux moments possibles pour lancer l’initialisation.

### Option A - au démarrage de l’application
On peut utiliser :
- un `ContextListener`,
- ou la méthode `init()` du `FrontController`.

Avantages :
- découverte des contrôleurs faite une seule fois,
- meilleure clarté,
- préparation immédiate du framework au lancement.

### Option B - au premier appel
L’initialisation peut aussi se faire au premier appel HTTP.

Avantages :
- démarrage plus léger,
- charge retardée.

Pour le sprint 1, il faut surtout décider du point d’entrée de l’initialisation. La solution la plus simple reste souvent `init()` ou un `ContextListener`.

## Configuration `web.xml`
Le `web.xml` doit contenir un paramètre d’initialisation indiquant le package à scanner.

Exemple de rôle attendu :
- `init-param` = nom du package à parcourir,
- le `FrontController` ou un listener récupère cette valeur,
- cette valeur est ensuite transmise à une classe utilitaire.

## Classe utilitaire nécessaire
Il faut une classe utilitaire dédiée à la découverte des classes.

Rôle attendu :
- recevoir un nom de package,
- retrouver les classes correspondantes,
- vérifier la présence de l’annotation,
- retourner la liste des contrôleurs détectés.

Cette classe doit rester indépendante du `FrontController` pour garder un code propre et réutilisable.

## Proposition d’architecture
### Composants
- `FrontController` : point d’entrée HTTP.
- Annotation de contrôle : marqueur de classe.
- Classe utilitaire de scan : découverte des classes du package.
- Configuration `web.xml` : fournit le package à scanner.
- `ContextListener` ou `init()` : déclenche l’initialisation.

### Flux attendu
1. Le serveur démarre.
2. Le framework lit le `init-param` dans `web.xml`.
3. Le package est envoyé à la classe utilitaire.
4. La classe utilitaire parcourt les classes du package.
5. Les classes annotées sont enregistrées comme contrôleurs.
6. `FrontController` pourra utiliser cette liste pour traiter les requêtes.

## Ce qu’il faut clarifier avant codage
- nom exact de l’annotation,
- structure des métadonnées à associer au contrôleur,
- méthode de scan des classes,
- moment exact d’initialisation,
- format du paramètre de package dans `web.xml`.

## Résultat attendu du sprint 1
À la fin de ce sprint, on doit avoir :
- une stratégie claire de détection des contrôleurs,
- une annotation de marquage,
- un mécanisme de chargement des classes par package,
- une récupération du package depuis `web.xml`,
- une base prête pour le routage des requêtes au sprint suivant.
