package projet_java;
import java.util.Random;

public class utilisateur {
    private String nom;
    private String motDePasse;
    private int chapitre;
    // Nouveaux attributs basés sur ta table BDD
    private int charisme;
    private int pv;
    private int[] id_pouvoirs = new int[3]; 

    // Constructeur pour un NOUVEAU compte
    public utilisateur(String nom, String motDePasse) {
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.chapitre = 1;
        this.charisme = 0; // Valeur par défaut dans ta BDD
        this.pv = 20;      // Valeur par défaut dans ta BDD
        Random r = new Random();
        this.id_pouvoirs[0] = r.nextInt(6) + 1; // renvoie un nombre entre 1 et 6 pour simuler le choix aléatoire d'un pouvoir
        if(this.id_pouvoirs[0] ==6){
            System.out.println("Vous pouvez obtenir le pouvoir de votre choix parmi la liste suivante :");
            for(int i=1; i<=5; i++) {
                System.out.print("    "+i+" - ");
                bdd.recupererPouvoirs(i);
            }
            System.out.print("Entrez le numéro du pouvoir que vous souhaitez : ");
            int choixPouvoir = new java.util.Scanner(System.in).nextInt();
            this.id_pouvoirs[0] = choixPouvoir; // Assigne le pouvoir choisi par l'utilisateur
        }
        this.id_pouvoirs[1] = r.nextInt(6) + 7; 
        while(this.id_pouvoirs[1] ==11 && this.id_pouvoirs[0] ==1){
            this.id_pouvoirs[1] = r.nextInt(6) + 7; // Regénère si le pouvoir 2 est "superForce" alors que le pouvoir 1 est "MegaForce"
        }
        
        this.id_pouvoirs[2] = r.nextInt(6) + 13;
    }

    // Constructeur pour charger un compte depuis la BDD
    public utilisateur(String nom, String motDePasse, int chapitre, int charisme, int pv, int p1, int p2, int p3) {
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.chapitre = chapitre;
        this.charisme = charisme;
        this.pv = pv;
        this.id_pouvoirs[0] = p1;
        this.id_pouvoirs[1] = p2;
        this.id_pouvoirs[2] = p3;
    }

    // Getters mis à jour
    public int getCharisme() { return charisme; }
    public int getPv() { return pv; }
    public int[] getIdPouvoirs() { return id_pouvoirs; }
    public int getpouvoir(int index) { return id_pouvoirs[index]; }
    public String getNom() { return nom; }
    public int getChapitre() { return chapitre; }
}
