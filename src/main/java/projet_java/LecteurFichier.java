package projet_java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class LecteurFichier {

    // Méthode pour lire un fichier texte phrase par phrase
    public static void lireTXTParPhrase(String cheminFichier, Scanner scanner) {
        Path chemin = Paths.get(cheminFichier); // Convertit le String en chemin système
        
        if (!Files.exists(chemin)) {
            System.out.println("Erreur : Le fichier est introuvable au chemin : " + cheminFichier);
            return;
        }

        try {
            // Lit le contenu complet du fichier en une seule chaîne
            String texteComplet = Files.readString(chemin);
            
            // Divise le texte en un tableau de chaînes à chaque point
            String[] phrases = texteComplet.split("\\.");
            
            System.out.println("\n--- DÉBUT DE L'HISTOIRE ---");
            System.out.println("(Appuyez sur ENTRÉE pour lire la suite...)\n");
            
            for (String phrase : phrases) {
                String phraseNettoyee = phrase.trim(); // Supprime les retours à la ligne et espaces inutiles
                
                if (!phraseNettoyee.isEmpty()) {
                    System.out.print(phraseNettoyee + "."); // Affiche la phrase avec son point
                    
                    // Bloque l'exécution jusqu'à ce que l'utilisateur tape sur Entrée
                    scanner.nextLine();
                }
            }
            
            System.out.println("\n--- FIN DU CHAPITRE ---");
            
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier texte : " + e.getMessage());
        }
    }
}