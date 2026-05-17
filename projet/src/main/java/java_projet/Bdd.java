package projet_java;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe gérant la connexion et toutes les requêtes vers la base de données SQLite.
 * Elle assure la persistance de l'univers (chapitres, pouvoirs) et des joueurs.
 */
public class Bdd {
    // Constante spécifiant le pilote JDBC et le chemin du fichier local
    private static final String URL = "jdbc:sqlite:data.db";

    /**
     * Ouvre une connexion à la base de données SQLite.
     * return L'objet Connection pour exécuter des requêtes, ou null en cas d'erreur.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            creerTableSiInexistante(conn); // Vérification structurelle à chaque connexion
        } catch (SQLException e) {
            System.out.println("Erreur BDD : " + e.getMessage());
        }
        return conn;
    }

    /**
     * Initialise le schéma de la base de données si le fichier est vide ou fraîchement créé.
     * param conn La connexion active à la base de données.
     */
    private static void creerTableSiInexistante(Connection conn) {
        // Table des chapitres contenant les métadonnées de l'histoire et des ennemis
        String sqlChapitres = "CREATE TABLE IF NOT EXISTS chapitres ("
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + " titre TEXT NOT NULL," 
                            + " page INTEGER NOT NULL,"
                            + " ennemi_nom TEXT,"
                            + " ennemi_pv INTEGER,"
                            + " ennemi_force INTEGER);";

        // Table référentielle des pouvoirs disponibles dans le jeu
        String sqlPouvoirs = "CREATE TABLE IF NOT EXISTS pouvoirs ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nom_pouvoir TEXT NOT NULL UNIQUE,"
                + " valeur_Force INTEGER,"
                + " valeur_Defense INTEGER,"
                + " valeur_Ego INTEGER);";

        // Table de sauvegarde des profils joueurs
        String sqlUtilisateurs = "CREATE TABLE IF NOT EXISTS utilisateurs ("
                               + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                               + " nom TEXT NOT NULL,"
                               + " mot_de_passe TEXT NOT NULL,"
                               + " id_chapitre INTEGER NOT NULL,"
                               + " id_pouvoir_1 INTEGER,"
                               + " id_pouvoir_2 INTEGER ,"
                               + " id_pouvoir_3 INTEGER,"
                               + " potentiel_de_charisme INTEGER DEFAULT 0,"
                               + " point_de_vie INTEGER DEFAULT 20,"
                               + " force_bonus INTEGER DEFAULT 0,"
                               + " FOREIGN KEY(id_chapitre) REFERENCES chapitres(id),"
                               + " FOREIGN KEY(id_pouvoir_1) REFERENCES pouvoirs(id)," 
                               + " FOREIGN KEY(id_pouvoir_2) REFERENCES pouvoirs(id),"
                               + " FOREIGN KEY(id_pouvoir_3) REFERENCES pouvoirs(id));";
        
        // Table modélisant les liens (graphe) entre les différents chapitres
        String sqlChoix = "CREATE TABLE IF NOT EXISTS choix ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " id_chapitre_origine INTEGER NOT NULL,"
                + " texte_choix TEXT NOT NULL,"
                + " id_chapitre_destination INTEGER NOT NULL,"
                + " FOREIGN KEY(id_chapitre_origine) REFERENCES chapitres(id));";

        // Insertion du dictionnaire des pouvoirs. 'INSERT OR IGNORE' évite les doublons.
        String sqlInsertPouvoirs = "INSERT OR IGNORE INTO pouvoirs(nom_pouvoir, valeur_Force, valeur_Defense, valeur_Ego) VALUES"
                + "('MegaForce', 4, 0, 2), ('Teleportation', 0, 1, 1), ('Magie', 0, 0, 2),"
                + "('Polymorphe', 0, 0, 2), ('Psionique', 0, 0, 2), ('Au choix', 0, 0, 0),"
                + "('Feu/Glace', 1, 0, 0), ('Garou', 1, 0, 0), ('Geant/Retrecir', 1, 0, 0),"
                + "('Rayon d Energie', 1, 0, 0), ('Super Force', 3, 0, 2), ('Super pouvoirs sensoriels', 1, 0, 1),"
                + "('Invisibilite', 0, 2, 1), ('Vitesse-Agilite', 0, 2, 0), ('Elasticite', 0, 2, 0),"
                + "('Armure', 0, 2, 1), ('Arsenal', 0, 1, 0), ('Champ de force', 0, 2, 0);";
                
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlChapitres);
            stmt.execute(sqlPouvoirs);
            stmt.execute(sqlChoix);
            stmt.execute(sqlInsertPouvoirs);
            stmt.execute(sqlUtilisateurs);
            
            // Mécanisme de mise à jour (migration) pour les bases de données existantes.
            // Si la table existait déjà sans la colonne 'force_bonus', on l'ajoute.
            try {
                stmt.execute("ALTER TABLE utilisateurs ADD COLUMN force_bonus INTEGER DEFAULT 0;");
            } catch (SQLException e) {
                // Exception ignorée silencieusement : la colonne existe déjà.
            }
            
        } catch (SQLException e) {
            System.out.println("Erreur Table : " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau profil joueur lors de l'inscription.
     * return true si l'insertion a réussi, false sinon (ex: pseudo déjà pris).
     */
    public static boolean ajouterUtilisateur(String nom, String mdp, int p1, int p2, int p3) {
        // Préparation de la requête avec des '?' pour contrer les injections SQL
        String sql = "INSERT INTO utilisateurs(nom, mot_de_passe, id_chapitre, id_pouvoir_1, id_pouvoir_2, id_pouvoir_3, force_bonus) VALUES(?,?,1,?,?,?,0)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, mdp);
            pstmt.setInt(3, p1);
            pstmt.setInt(4, p2);
            pstmt.setInt(5, p3);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur Inscription : " + e.getMessage());
            return false;
        }
    }

    /**
     * Authentifie un joueur et charge ses données de sauvegarde.
     * return L'objet Utilisateur reconstitué, ou null en cas de mauvais identifiants.
     */
    public static Utilisateur verifierUtilisateur(String nom, String mdp) {
        String sql = "SELECT * FROM utilisateurs WHERE nom = ? AND mot_de_passe = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, mdp);
            ResultSet rs = pstmt.executeQuery(); 
            
            // Si une ligne est trouvée, on reconstruit l'état du joueur
            if (rs.next()) {
                return new Utilisateur(
                    rs.getString("nom"), rs.getString("mot_de_passe"),
                    rs.getInt("id_chapitre"), rs.getInt("potentiel_de_charisme"), 
                    rs.getInt("point_de_vie"), rs.getInt("id_pouvoir_1"),
                    rs.getInt("id_pouvoir_2"), rs.getInt("id_pouvoir_3"),
                    rs.getInt("force_bonus")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur Connexion : " + e.getMessage());
        }
        return null; 
    }

    /**
     * Calcule la somme des forces octroyées par les 3 pouvoirs du joueur.
     */
    public static int calculerBonusForce(int p1, int p2, int p3) {
        int totalForce = 0;
        String sql = "SELECT SUM(valeur_Force) as total FROM pouvoirs WHERE id IN (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, p1);
            pstmt.setInt(2, p2);
            pstmt.setInt(3, p3);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) totalForce = rs.getInt("total");
        } catch (SQLException e) {
            System.out.println("Erreur calcul force : " + e.getMessage());
        }
        return totalForce;
    }

    /**
     * Récupère les embranchements narratifs liés à un chapitre spécifique.
     * return Une liste de tableaux. Index 0 = Texte du choix, Index 1 = ID de destination.
     */
    public static List<String[]> recupererChoix(int idChapitre) {
        List<String[]> listeChoix = new ArrayList<>();
        String sql = "SELECT texte_choix, id_chapitre_destination FROM choix WHERE id_chapitre_origine = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idChapitre);
            ResultSet rs = pstmt.executeQuery();
            // Itère sur tous les résultats retournés par la requête
            while (rs.next()) {
                listeChoix.add(new String[]{rs.getString("texte_choix"), String.valueOf(rs.getInt("id_chapitre_destination"))});
            }
        } catch (SQLException e) {
            System.out.println("Erreur choix : " + e.getMessage());
        }
        return listeChoix;
    }

    /**
     * Met à jour la base de données avec l'état actuel du joueur (chapitre, santé, force).
     */
    public static void sauvegarderProgression(String nom, int idChapitre, int pv, int forceBonus) {
        String sql = "UPDATE utilisateurs SET id_chapitre = ?, point_de_vie = ?, force_bonus = ? WHERE nom = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idChapitre);
            pstmt.setInt(2, pv);
            pstmt.setInt(3, forceBonus);
            pstmt.setString(4, nom);
            pstmt.executeUpdate(); 
        } catch (SQLException e) {
            System.out.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Interroge la base pour trouver le nom textuel d'un pouvoir via son ID et l'affiche.
     */
    public static void afficherNomPouvoir(int idPouvoir){
        String sql = "SELECT nom_pouvoir FROM pouvoirs WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPouvoir);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) System.out.print(rs.getString("nom_pouvoir"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}