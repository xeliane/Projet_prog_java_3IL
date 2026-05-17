package projet_java;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

/**
 * Classe utilitaire dédiée à la lecture et l'affichage des fichiers textes de l'histoire.
 */
public class LecteurFichier {
    
    /**
     * Lit un fichier texte et l'affiche phrase par phrase pour rendre la lecture agréable.
     * param cheminFichier L'emplacement du fichier (ex: "chapitre/chapitre1.txt").
     * param scanner Le scanner partagé du programme pour détecter la touche Entrée.
     */
    public static void lireTXTParPhrase(String cheminFichier, Scanner scanner) {
        Path chemin = Paths.get(cheminFichier); // Transforme le chemin texte en objet Path
        
        // Sécurité : vérifie que le fichier existe bien avant d'essayer de le lire
        if (!Files.exists(chemin)) {
            System.out.println("[Histoire non disponible pour ce chapitre]");
            return;
        }
        
        try {
            // Extrait l'intégralité du texte en une seule variable String
            String texte = Files.readString(chemin);
            
            // Découpe le texte en un tableau de phrases. Le double anti-slash (\\) est 
            // nécessaire car le point (.) est un caractère spécial en expression régulière.
            String[] phrases = texte.split("\\.");
            
            for (String p : phrases) {
                // Ignore les phrases vides (causées par de multiples espaces ou points d'affilée)
                if (!p.trim().isEmpty()) {
                    System.out.print(p.trim() + "."); // Affiche la phrase et remet le point supprimé par le split
                    
                    // Bloque l'exécution du programme jusqu'à ce que le joueur appuie sur Entrée
                    scanner.nextLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lecture : " + e.getMessage());
        }
    }
}