import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SupprimerProfesseur {

    public static void supprimerProfesseur(int idProf) {
        // Informations de connexion à la base de données
        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
        String user = "postgres";
        String password = "24268641";

        // Requête SQL pour supprimer un professeur en fonction de l'ID
        String sql = "DELETE FROM PROFESSEUR WHERE idprof = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProf);

            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Professeur supprimé avec succès !");
            } else {
                JOptionPane.showMessageDialog(null, "Aucun professeur trouvé avec cet ID !");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la suppression : " + e.getMessage());
        }
    }
}
