package projet_java;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class bdd {
    // Chemin vers le fichier de la base de données SQLite
    private static final String URL = "jdbc:sqlite:data.db";

    // Établit la connexion avec le fichier .db
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            creerTableSiInexistante(conn); // Vérifie que les tables existent à chaque connexion
        } catch (SQLException e) {
            System.out.println("Erreur BDD : " + e.getMessage());
        }
        return conn;
    }

    // Crée les tables SQL si elles n'existent pas encore
    private static void creerTableSiInexistante(Connection conn) {
        // Définition des schémas de table (Colonnes, Types, Clés primaires)
        String sqlChapitres = "CREATE TABLE IF NOT EXISTS chapitres ("
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + " titre TEXT NOT NULL," 
                            + " page INTEGER NOT NULL"
                            + ");";

        String Pouvoirs = "CREATE TABLE IF NOT EXISTS pouvoirs ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nom_pouvoir TEXT NOT NULL UNIQUE,"
                + " valeur_Force INTEGER,"
                + " valeur_Defense INTEGER,"
                + " valeur_Ego INTEGER"
                + ");";

        String sqlUtilisateurs = "CREATE TABLE IF NOT EXISTS utilisateurs ("
                               + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                               + " nom TEXT NOT NULL UNIQUE," // UNIQUE empêche deux pseudos identiques
                               + " mot_de_passe TEXT NOT NULL,"
                               + " id_chapitre INTEGER NOT NULL,"
                               + " id_pouvoir_1 INTEGER," // Permet de stocker le pouvoir choisi par le joueur
                               + " id_pouvoir_2 INTEGER ,"
                               + " id_pouvoir_3 INTEGER,"
                               + " potentiel_de_charisme INTEGER DEFAULT 0," // Attributs de personnage
                               + " point_de_vie INTEGER DEFAULT 20,"
                               + " FOREIGN KEY(id_chapitre) REFERENCES chapitres(id)," // Lien vers la table chapitres
                               + " FOREIGN KEY(id_pouvoir_1) REFERENCES pouvoirs(id)," 
                               + " FOREIGN KEY(id_pouvoir_2) REFERENCES pouvoirs(id),"
                               + " FOREIGN KEY(id_pouvoir_3) REFERENCES pouvoirs(id)"
                               + ");";
        
        String sqlChoix = "CREATE TABLE IF NOT EXISTS choix ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " id_chapitre_origine INTEGER NOT NULL,"
                + " texte_choix TEXT NOT NULL,"
                + " id_chapitre_destination INTEGER NOT NULL,"
                + " FOREIGN KEY(id_chapitre_origine) REFERENCES chapitres(id)"
                + ");";

        String sqlPouvoirs = "INSERT OR IGNORE INTO pouvoirs(nom_pouvoir, valeur_Force, valeur_Defense, valeur_Ego) VALUES"
                + "('MegaForce', 4, 0, 2),"
                + "('Teleportation', 0, 1, 1),"
                + "('Magie', 0, 0, 2),"
                + "('Polymorphe', 0, 0, 2),"
                + "('Psionique', 0, 0, 2),"
                + "('Au choix', 0, 0, 0),"
                + "('Feu/Glace', 1, 0, 0),"
                + "('Garou', 1, 0, 0),"
                + "('Geant/Retrecir', 1, 0, 0),"
                + "('Rayon d Energie', 1, 0, 0),"
                + "('Super Force', 3, 0, 2),"
                + "('Super pouvoirs sensoriels', 1, 0, 1),"
                + "('Invisibilite', 0, 2, 1),"
                + "('Vitesse-Agilite', 0, 2, 0),"
                + "('Elasticite', 0, 2, 0),"
                + "('Armure', 0, 2, 1),"
                + "('Arsenal', 0, 1, 0),"
                + "('Champ de force', 0, 2, 0);";
                

        // Statement : Objet utilisé pour envoyer les requêtes SQL à la base
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlChapitres);
            stmt.execute(Pouvoirs);
            stmt.execute(sqlChoix);
            stmt.execute(sqlPouvoirs);
            stmt.execute(sqlUtilisateurs);
            
        } catch (SQLException e) {
            System.out.println("Erreur Table : " + e.getMessage());
        }
    }

    // Ajoute un nouveau joueur dans la table utilisateurs
    public static boolean ajouterUtilisateur(String nom, String mdp,int p1, int p2, int p3) {
        String sql = "INSERT INTO utilisateurs(nom, mot_de_passe, id_chapitre, potentiel_de_charisme, point_de_vie, id_pouvoir_1, id_pouvoir_2, id_pouvoir_3) VALUES(?,?,?,?,?,?,?,?)";
        // PreparedStatement : Protège contre les injections SQL grâce aux "?"
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, mdp);
            pstmt.setInt(3, 1); // Démarre au chapitre 1
            pstmt.setInt(4, 0); // potentiel_de_charisme
            pstmt.setInt(5, 20); // point_de_vie
            pstmt.setInt(6, p1); // id_pouvoir_1
            pstmt.setInt(7, p2); // id_pouvoir_2
            pstmt.setInt(8, p3); // id_pouvoir_3
            pstmt.executeUpdate(); // Exécute l'insertion
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur Inscription : " + e.getMessage());
            return false;
        }
    }

    // Vérifie les identifiants et renvoie un objet utilisateur si OK
    public static utilisateur verifierUtilisateur(String nom, String mdp) {
    String sql = "SELECT * FROM utilisateurs WHERE nom = ? AND mot_de_passe = ?";
    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, nom);
        pstmt.setString(2, mdp);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            //on prend toutes les infos de la BDD pour créer un objet utilisateur complet
            return new utilisateur(
                rs.getString("nom"),
                rs.getString("mot_de_passe"),
                rs.getInt("id_chapitre"),
                rs.getInt("potentiel_de_charisme"), 
                rs.getInt("point_de_vie"),
                rs.getInt("id_pouvoir_1"),
                rs.getInt("id_pouvoir_2"),
                rs.getInt("id_pouvoir_3")
            );
        }
    } catch (SQLException e) {
        System.out.println("Erreur Connexion : " + e.getMessage());
    }
    return null;
}

    // Récupère la liste des choix possibles pour un chapitre donné
    public static List<String[]> recupererChoix(int idChapitre) {
        List<String[]> listeChoix = new ArrayList<>();
        String sql = "SELECT texte_choix, id_chapitre_destination FROM choix WHERE id_chapitre_origine = ?";
        
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idChapitre);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // On crée un tableau de String : [0] texte du choix, [1] destination
                listeChoix.add(new String[]{rs.getString("texte_choix")+" "+rs.getString("id_chapitre_destination"), String.valueOf(rs.getInt("id_chapitre_destination"))});
            }
        } catch (SQLException e) {
            System.out.println("Erreur choix : " + e.getMessage());
        }
        return listeChoix;
    }

    // Met à jour la colonne id_chapitre pour sauvegarder où en est le joueur
    public static void sauvegarderProgression(String nom, int nouvelIdChapitre) {
        String sql = "UPDATE utilisateurs SET id_chapitre = ? WHERE nom = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nouvelIdChapitre);
            pstmt.setString(2, nom);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    public static void recupererPouvoirs(int idPouvoir){
        String sql = "SELECT nom_pouvoir FROM pouvoirs WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPouvoir);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Pouvoir : " + rs.getString("nom_pouvoir"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur récupération pouvoirs : " + e.getMessage());
        }
    }
}