package gestionemploidutemps; // Assure-toi que le package correspond au nom de ton projet

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GestionEmploiDuTemps {

    public static void main(String[] args) {
        // Informations de connexion à PostgreSQL
        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps"; // URL de la base de données
        String user = "postgres"; // Nom d'utilisateur PostgreSQL
        String password = "24268641"; // Ton mot de passe PostgreSQL

        // Essayer de se connecter
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connexion réussie à la base de données !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
