# FallehTech – Version Desktop (JavaFX)

FallehTech est une application desktop développée avec **JavaFX** dans le cadre du module **Projet Intégré : Développement Web Java** à **Esprit School of Engineering**.  
Elle est le prolongement de la version web et permet une utilisation locale pour gérer les interactions entre agriculteurs, clients et ouvriers sans passer par un navigateur.
Vous pouvez trouver la version web ici: [https://github.com/ghofraneAbidi/FallehTECH]

## Description du projet

- **Objectif** : Offrir une version locale et graphique de l’application FallehTech pour rendre les services accessibles même hors ligne.
- **Problème résolu** : Les utilisateurs du secteur agricole n’ont pas toujours un accès web stable. Cette version desktop permet une gestion locale complète et intuitive.
- **Fonctionnalités principales** :
  - Interface graphique avec JavaFX
  - Connexion à la base de données locale
  - Gestion des produits agricoles
  - Consultation des offres et postulation par les ouvriers
  - Interaction via un module blog

---

## Installation

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/ton-utilisateur/fallehtech-javafx.git
   cd fallehtech-javafx
2. **Ouvrir le projet dans votre IDE Java**
Utilisez IntelliJ IDEA ou Eclipse
Assurez-vous d’avoir Java 17 ou une version supérieure installée
3. **Configurer la connexion à la base de données**
- Ouvrez DatabaseConfig.java
- Mettez à jour : l’URL JDBC (jdbc:mysql://localhost:3306/fallehtech) et les identifiants (root, mot de passe)
4. **Créer la base de données dans MySQL**
   ```bash
   CREATE DATABASE fallehtech;
5. **Lancer l'application**
Depuis l’IDE : clic droit sur la classe principale > Run

---

## Utilisation
- Langage : Java 17
- Interface graphique : JavaFX
- Connexion base de données : JDBC
- Système : Authentification multi-rôle (agriculteur, client, ouvrier)

---

## Contribution
Nous remercions tous ceux qui ont contribué à ce projet !
### Contributeurs
- [Sarah FALEH](https://github.com/SarahFaleh) - Gestion PRODUITS
- [Ghofrane ABIDI](https://github.com/GhofraneAbidi) - Gestion BLOGS
- [Abderrazek CHAMEKH](https://github.com/AbderrazekChamekh) - Gestion E-COMMERCE
- [Chaima AJAILIA](https://github.com/ChaimaAjailia) - Gestion UTILISATEURS
- [Zied ALIMI](https://github.com/ZiedAlimi) - Gestion OFFRES
### Vous voulez contribuer aussi?
Vous êtes les bienvenues !
Veuillez créer une issue ou un fork, et soumettre une Pull Request après vos modifications.

---

## Topics
#javafx #desktop-application #agriculture #job-platform #blog #weather #marketplace #java #esprit #odd

