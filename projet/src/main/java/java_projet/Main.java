package projet_java;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Pour lire les entrées clavier
        utilisateur joueurActuel = null; // Contiendra le joueur connecté
        bdd.connect(); // Assure que la BDD est prête à être utilisée

        System.out.println("--- BIENVENUE ---");
        System.out.println("1 - Créer un compte");
        System.out.println("2 - Se connecter");
        System.out.println("3 - Mode invité");
        
        int choix = scanner.nextInt();
        scanner.nextLine(); // Nettoie la ligne après la lecture d'un nombre (évite les bugs de saisie)

        switch (choix) {
            case 1: // Inscription
                System.out.print("Nom : ");
                String n = scanner.nextLine();
                System.out.print("Mot de passe : ");
                String m = scanner.nextLine();
                joueurActuel = new utilisateur(n, m);// Création de l'objet joueur avec les pouvoirs aléatoires
                int p1 = joueurActuel.getpouvoir(0);
                int p2 = joueurActuel.getpouvoir(1);
                int p3 = joueurActuel.getpouvoir(2);
                if (bdd.ajouterUtilisateur(n, m, p1, p2, p3)) {
                    System.out.println("Compte créé !");
                    System.out.println("voici la liste de vos pouvoirs :");
                    for(int i=0; i<3; i++) {
                        System.out.print("    ");
                        bdd.recupererPouvoirs(joueurActuel.getpouvoir(i));
                    }
                } else {
                    System.out.println("Erreur : Nom déjà utilisé.");
                    joueurActuel = null; // Annule la création du joueur si l'inscription a échoué
                }
                break;

            case 2: // Connexion
                System.out.print("Nom : ");
                String n2 = scanner.nextLine();
                System.out.print("Mot de passe : ");
                String m2 = scanner.nextLine();
                joueurActuel = bdd.verifierUtilisateur(n2, m2);
                if (joueurActuel != null) {
                System.out.println("Content de vous revoir " + joueurActuel.getNom() + " !");
                // Affichage des nouvelles stats
                System.out.println("Points de vie : " + joueurActuel.getPv());
                System.out.println("Charisme : " + joueurActuel.getCharisme());
                    if (joueurActuel.getIdPouvoirs()[0] != 0) {
                        System.out.println("voici la liste de vos pouvoirs :");
                        for(int i=0; i<3; i++) {
                            System.out.print("    ");
                            bdd.recupererPouvoirs(joueurActuel.getpouvoir(i));
                        }
                    } else {
                        System.out.println("Pouvoirs : Aucun");
                    }
                } else {
                    System.out.println("Identifiants incorrects.");
                }
                break;

            case 3: // Mode sans sauvegarde
                joueurActuel = new utilisateur("Invité", "");
                System.out.println("Mode invité activé.");
                System.out.println("voici la liste de vos pouvoirs :");
                for(int i=0; i<3; i++) {
                    System.out.print("    ");
                    bdd.recupererPouvoirs(joueurActuel.getpouvoir(i));
                }
                break;
        }

        // Si on a un joueur (connecté ou invité), on lance la boucle de jeu
        if (joueurActuel != null) {
            boolean enJeu = true;
            int idActuel = joueurActuel.getChapitre(); // On récupère son chapitre actuel

            while (enJeu) {
                // 1. Affiche le texte du chapitre correspondant (ex: chapitre1.txt)
                LecteurFichier.lireTXTParPhrase("chapitre/"+ "chapitre" + idActuel + ".txt", scanner);

                // 2. Cherche en BDD quels sont les embranchements possibles
                List<String[]> choixDispo = bdd.recupererChoix(idActuel);

                if (choixDispo.isEmpty()) {
                System.out.println("FIN DE L'AVENTURE.");
                enJeu = false;
                } else {
                    System.out.println("\nQue voulez-vous faire ?");
                    for (int i = 0; i < choixDispo.size(); i++) {
                    // Comme ta BDD envoie déjà "Texte + ID", on affiche juste l'élément [0]
                        System.out.println((i + 1) + " - " + choixDispo.get(i)[0]);
                        if (i==(choixDispo.size()-1)){
                            System.out.println((i+2) + " - Sauvegardez votre progression et quiter.");
                        }
                    }   
                int decision = scanner.nextInt();
                scanner.nextLine(); // Nettoie la ligne après la lecture d'un nombre
                if (decision == choixDispo.size() + 1) { // Option de sauvegarde et quitter
                    bdd.sauvegarderProgression(joueurActuel.getNom(), idActuel);
                    System.out.println("Progression sauvegardée. À bientôt !");
                    enJeu = false;
                    continue; // Sort de la boucle de jeu
                }
                // On utilise toujours l'index [1] pour récupérer l'ID de destination pur (pour le code)
                idActuel = Integer.parseInt(choixDispo.get(decision - 1)[1]);

                // Sauvegarde de la progression
                bdd.sauvegarderProgression(joueurActuel.getNom(), idActuel);
                }
            }
        }

        scanner.close(); // Ferme proprement le scanner à la fin du programme
    }
}