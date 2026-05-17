package projet_java;
import java.util.Random;
import java.util.Scanner;

/**
 * Modèle de données (POJO) représentant un joueur, ses caractéristiques et ses pouvoirs.
 * Permet de manipuler le joueur en mémoire vive sans faire de requêtes SQL permanentes.
 */
public class Utilisateur {
    private String nom;
    private String motDePasse;
    private int chapitre;
    private int charisme; // systeme a implanter
    private int pv;
    private int forceBonus; // Force additionnelle gagnée dynamiquement via les combats
    private int[] id_pouvoirs = new int[3]; // Tableau contenant les identifiants en BDD des 3 pouvoirs

    /**
     * Constructeur utilisé pour la CRÉATION d'un compte.
     * Génère les données initiales et s'occupe de la loterie des pouvoirs.
     */
    public Utilisateur(String nom, String motDePasse, Scanner sc) {
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.chapitre = 1; // Le point de départ officiel
        this.charisme = 0; 
        this.pv = 20;      // Capital santé max initial
        this.forceBonus = 0; 
        
        Random r = new Random();
        
        // --- Attribution du 1er Pouvoir ---
        int p1 = r.nextInt(6) + 1; // Nombre aléatoire entre 1 et 6
        
        // Gestion de l'exception : le pouvoir ID 6 correspond au joker "Au choix"
        if(p1 == 6){
            System.out.println("\n--- POUVOIR AU CHOIX ---");
            for(int i = 1; i <= 5; i++) {
                System.out.print(i + " - ");
                Bdd.afficherNomPouvoir(i);
                System.out.println();
            }
            System.out.print("Entrez le numéro choisi : ");
            p1 = sc.nextInt();
            sc.nextLine(); 
        }
        this.id_pouvoirs[0] = p1;
        
        // --- Attribution des autres pouvoirs ---
        this.id_pouvoirs[1] = r.nextInt(6) + 7;  // Catégorie 2 (ID 7 à 12)
        this.id_pouvoirs[2] = r.nextInt(6) + 13; // Catégorie 3 (ID 13 à 18)
    }

    /**
     * Constructeur utilisé pour le CHARGEMENT d'une sauvegarde.
     * Ne fait aucun traitement aléatoire, restaure simplement l'état exact du joueur.
     */
    public Utilisateur(String nom, String motDePasse, int chapitre, int charisme, int pv, int p1, int p2, int p3, int forceBonus) {
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.chapitre = chapitre;
        this.charisme = charisme;
        this.pv = pv;
        this.id_pouvoirs[0] = p1;
        this.id_pouvoirs[1] = p2;
        this.id_pouvoirs[2] = p3;
        this.forceBonus = forceBonus;
    }

    // --- Getters et Setters ---

    public void setPv(int pv) { this.pv = pv; }
    
    /**
     * Vérifie si le joueur est mort ou vif.
     * return true si les PV sont supérieurs à 0.
     */
    public boolean estEnVie() { return this.pv > 0; }
    
    public int getPv() { return pv; }
    
    public int getForceBonus() { return forceBonus; }
    
    /**
     * Cumule un nouveau bonus de force à celui déjà existant.
     */
    public void ajouterForce(int bonus) { this.forceBonus += bonus; }
    
    public int getpouvoir(int index) { return id_pouvoirs[index]; }
    public String getNom() { return nom; }
    public int getChapitre() { return chapitre; }
}