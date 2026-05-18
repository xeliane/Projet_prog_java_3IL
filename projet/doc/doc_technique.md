1. Présentation du Projet
L'Aventure 3iL est un jeu de rôle (RPG) textuel interactif développé en Java. Le jeu propose une aventure narrative à embranchements où le joueur incarne un héros doté de super-pouvoirs générés aléatoirement. Le système inclut une persistance des données (sauvegarde de la progression, gestion de compte), une lecture dynamique de fichiers textes narratifs, et un système de combat au tour par tour avec calcul de statistiques.

2. Architecture et Technologies
Le projet repose sur une architecture modulaire s'inspirant du modèle MVC (Modèle-Vue-Contrôleur) afin de séparer la logique de jeu, l'accès aux données et l'affichage.

  Langage : Java (JDK 17).

  Gestionnaire de dépendances : Maven (pom.xml).

  Base de données : SQLite (via la dépendance sqlite-jdbc).

  Stockage de l'histoire : Fichiers textes externes (.txt) par chapitre, situés dans un répertoire /chapitre/.

3. Modèle de Données (Base SQLite : data.db)
La base de données relationnelle est générée automatiquement à l'exécution si elle n'existe pas. Elle est composée de 4 tables principales :

  chapitres : Stocke les métadonnées de chaque étape de l'histoire.

  Colonnes : id (PK), titre, page, ennemi_nom, ennemi_pv, ennemi_force.

  pouvoirs : Dictionnaire des capacités disponibles pour les joueurs.

  Colonnes : id (PK), nom_pouvoir, valeur_Force, valeur_Defense, valeur_Ego.

  utilisateurs : Gère les comptes, les sauvegardes et les statistiques dynamiques des joueurs.

  Colonnes : id (PK), nom (UNIQUE), mot_de_passe, id_chapitre (FK), id_pouvoir_1, id_pouvoir_2, id_pouvoir_3, point_de_vie, force_bonus.

  choix : Modélise les liens entre les chapitres sous forme de graphe orienté.

  Colonnes : id (PK), id_chapitre_origine (FK), texte_choix, id_chapitre_destination.

4. Structure des Classes Java
  Main.java (Contrôleur / Point d'entrée)
    C'est le chef d'orchestre de l'application. Il gère :

    L'interface textuelle (menu principal de connexion/inscription).

    La boucle principale de jeu (while (joueur.estEnVie())) : Affiche le texte narratif, récupère les choix possibles, et attend la saisie du joueur via un objet Scanner unique partagé.

    Le moteur de combat : Détecté via la présence du mot-clé [COMBAT] dans un choix. Gère le tour par tour, les chances de fuite (50%), le calcul des dégâts avec composante aléatoire (Random), l'application des
      soins et l'attribution de la force bonus en cas de victoire.

    La gestion de sortie : Intercepte le choix "Sauvegarder et quitter" généré dynamiquement en fin de liste.

  Bdd.java (Gestionnaire d'Accès aux Données / DAO)
    Encapsule toute la logique de persistance via JDBC.

   Sécurité : Utilisation exclusive de PreparedStatement pour prévenir les injections SQL lors de la manipulation des données utilisateur.

   Fonctionnalités clés :

   creerTableSiInexistante() : Initialise le schéma et gère les mises à jour silencieuses (ex: ALTER TABLE).

   ajouterUtilisateur() / verifierUtilisateur() : Gestion de l'authentification.

   sauvegarderProgression() : Met à jour la table utilisateurs avec les PV, la position et la force accumulée.

   recupererChoix() : Requête les embranchements disponibles pour un ID de chapitre donné.

  Utilisateur.java (Modèle de Données / POO)
    Représente l'état du joueur en mémoire vive.

   Gère le capital santé (pv) et vérifie les conditions de survie (estEnVie()).

   Intègre la logique métier lors de l'instanciation (Création de compte) : attribution aléatoire de 3 pouvoirs répartis dans 3 catégories de rareté distinctes, avec gestion d'une exception interactive (le pouvoir
    ID 6 "Au choix" requiert une saisie utilisateur).

   Stocke la statistique accumulable forceBonus.

  LecteurFichier.java (Utilitaire d'Affichage)
    Permet une narration immersive en rompant l'affichage en bloc.

   Utilise l'API java.nio.file (Path, Files) pour lire les fichiers chapitreX.txt.

   Découpe le texte via la méthode String.split("\\.") et impose une pause dans l'exécution (attente de la touche Entrée) pour afficher chaque phrase l'une après l'autre.

5. Mécaniques Métier Spécifiques
  Calcul des Dégâts : Lors d'une attaque, les dégâts du joueur sont calculés selon la formule suivante : Lancer de dé (1 à 6) + Somme des bonus de force des 3 pouvoirs (via requête SQL) + Force bonus gagnée en jeu.

  Récompense de Victoire : Plafond de points de vie géré dynamiquement par Math.min(20, PV actuels + soin aléatoire). Attribution d'un bonus de force permanent (0 à 4 points).

  Robustesse des Entrées : Le Scanner est purgé (sc.nextLine()) systématiquement après chaque récupération d'entier (sc.nextInt()) pour éviter les sauts de flux erratiques.
