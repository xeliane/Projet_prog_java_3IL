package projet_java;
import java.util.*;

/**
 * Classe principale qui sert de point d'entrée au programme.
 * Elle gère l'interface utilisateur dans la console, la boucle principale du jeu,
 * ainsi que le système de combat.
 */
public class Main {
    public static void main(String[] args) {
        // Le Scanner est instancié une seule fois ici et partagé pour éviter les bugs de flux d'entrée.
        Scanner sc = new Scanner(System.in);
        // Objet contenant toutes les statistiques du joueur courant.
        Utilisateur joueur = null; 

        // --- MENU PRINCIPAL ---
        System.out.println("=== RPG : L'AVENTURE 3iL ===");
        System.out.println("1. Nouveau Jeu\n2. Charger");
        int menu = sc.nextInt(); 
        sc.nextLine(); // Nettoie le retour à la ligne laissé par nextInt()

        // Gestion de la création d'un nouveau compte
        if (menu == 1) {
            System.out.print("Pseudo : "); String n = sc.nextLine();
            System.out.print("mot de passe : "); String m = sc.nextLine();
            
            // Instancie un nouveau joueur (génère les pouvoirs aléatoires)
            joueur = new Utilisateur(n, m, sc); 
            // Sauvegarde immédiatement le nouveau profil dans la base de données
            Bdd.ajouterUtilisateur(n, m, joueur.getpouvoir(0), joueur.getpouvoir(1), joueur.getpouvoir(2)); 
        } 
        // Gestion du chargement d'une partie existante
        else {
            System.out.print("Pseudo : "); String n = sc.nextLine();
            System.out.print("mot de passe : "); String m = sc.nextLine();
            
            // Tente de récupérer les données depuis SQLite
            joueur = Bdd.verifierUtilisateur(n, m); 
            if (joueur == null) { 
                System.out.println("Échec. Identifiants incorrects."); 
                return; // Arrête complètement le programme si la connexion échoue
            }
        }

        // --- BOUCLE PRINCIPALE DU JEU ---
        int idCap = joueur.getChapitre(); // Récupère le chapitre actuel
        
        // Le jeu continue tant que le joueur a des points de vie
        while (joueur.estEnVie()) {
            // Affiche le texte narratif du chapitre en cours, phrase par phrase
            LecteurFichier.lireTXTParPhrase("chapitre/chapitre" + idCap + ".txt", sc);
            
            // Auto-sauvegarde de la progression à chaque début de chapitre
            Bdd.sauvegarderProgression(joueur.getNom(), idCap, joueur.getPv(), joueur.getForceBonus());

            // Récupère la liste des embranchements possibles depuis la BDD
            List<String[]> choix = Bdd.recupererChoix(idCap);
            
            // Condition de victoire ou fin d'histoire (plus de choix disponibles)
            if (choix.isEmpty()) { 
                System.out.println("Fin de l'aventure."); 
                break; // Casse la boucle while
            }

            // Affiche dynamiquement les choix disponibles
            for (int i = 0; i < choix.size(); i++) {
                System.out.println((i + 1) + " - " + choix.get(i)[0]);
                // Ajoute l'option de sauvegarde comme toute dernière option de la liste
                if(i == (choix.size() - 1)){
                    System.out.println((i + 2) + " - Sauvegarder et quitter ");
                }
            }

            // Lecture de la décision du joueur
            int action = sc.nextInt(); 
            sc.nextLine();

            // --- OPTION : SAUVEGARDER ET QUITTER ---
            if (action == choix.size() + 1) {
                // Force la sauvegarde une dernière fois avant de quitter
                Bdd.sauvegarderProgression(joueur.getNom(), idCap, joueur.getPv(), joueur.getForceBonus());
                System.out.println("\nPartie sauvegardée avec succès. À bientôt " + joueur.getNom() + " !");
                break; 
            }

            // Sécurité pour éviter un plantage si le joueur tape un nombre hors limite
            if (action <= 0 || action > choix.size() + 1) {
                System.out.println("Choix invalide, veuillez réessayer.");
                continue; // Relance la boucle sans avancer le chapitre
            }

            // Récupère le texte du choix sélectionné (index 0 du tableau de String)
            String texteChoix = choix.get(action - 1)[0];

            // --- SYSTÈME DE COMBAT ---
            // Détecte la présence de la balise [COMBAT] dans le texte du choix
            if (texteChoix.contains("[COMBAT]")) {
                System.out.println("\n!!! COMBAT !!!");
                
                int pvE = 20, forceE = 3; // Valeurs par défaut si l'extraction échoue
                
                // Parse la chaîne de caractères pour extraire les PV et la Force de l'ennemi
                if (texteChoix.contains("PV:")) {
                    pvE = Integer.parseInt(texteChoix.split("PV: ")[1].split(" ")[0].trim());
                    forceE = Integer.parseInt(texteChoix.split("Force: ")[1].split("\n|\\*|$")[0].trim());
                }

                // Boucle de l'affrontement (tour par tour)
                while (pvE > 0 && joueur.estEnVie()) {
                    System.out.println("\nEnnemi: " + pvE + " PV | Vous: " + joueur.getPv() + " PV");
                    System.out.println("1. Attaquer\n2. Fuir");
                    int combatAction = sc.nextInt(); sc.nextLine();

                    // Phase d'attaque
                    if (combatAction == 1) {
                        // Calcul des dégâts : Dé(1-6) + Bonus des Pouvoirs + Bonus des Combats précédents
                        int degats = new Random().nextInt(6) + 1 
                                   + Bdd.calculerBonusForce(joueur.getpouvoir(0), joueur.getpouvoir(1), joueur.getpouvoir(2))
                                   + joueur.getForceBonus();
                        
                        pvE -= degats;
                        System.out.println("Vous infligez " + degats + " dégâts !");
                    } 
                    // Phase de fuite
                    else if (new Random().nextBoolean()) { // 50% de chance de réussite
                        System.out.println("Fuite réussie !"); 
                        break; 
                    } else {
                        System.out.println("Fuite échouée !");
                    }

                    // Contre-attaque de l'ennemi (s'il n'est pas mort)
                    if (pvE > 0) {
                        joueur.setPv(joueur.getPv() - forceE);
                        System.out.println("L'ennemi vous inflige " + forceE + " dégâts !");
                    }
                    // Résolution de la victoire
                    else {
                        Random rando = new Random();
                        int bonus_force = rando.nextInt(5); // Entre 0 et 4
                        int regen = rando.nextInt(3);       // Entre 0 et 2
                        
                        System.out.println("Félicitations, vous avez gagné !");
                        
                        joueur.ajouterForce(bonus_force);
                        // Restaure la vie sans dépasser le maximum de 20 PV
                        joueur.setPv(Math.min(20, joueur.getPv() + regen));
                        
                        System.out.println("Vos muscles se renforcent ! Vous gagnez " + bonus_force + " de Force permanente (Total Force Bonus : " + joueur.getForceBonus() + ").");
                        System.out.println("Vous regagnez " + regen +" point de vie (Vie actuelle : " + joueur.getPv() + ").");
                    }
                }
            }

            // Si le joueur n'est pas mort pendant le combat/chapitre, on le déplace vers le chapitre de destination
            if (joueur.estEnVie()) {
                // L'index 1 contient le numéro du chapitre de destination
                idCap = Integer.parseInt(choix.get(action - 1)[1]); 
            }
        }
        
        // Fin de boucle : si le joueur n'est plus en vie
        if (!joueur.estEnVie()) {
            System.out.println("\nGAME OVER");
        }
        
        // Libération de la ressource système (clavier)
        sc.close();
    }
}