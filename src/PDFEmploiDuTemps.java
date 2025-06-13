import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PDFEmploiDuTemps {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
        String user = "postgres";
        String password = "24268641";

        String idClasse = "L1";
        String dateDebut = "2025-04-08";
        String dateFin = "2025-04-14";

        String sql = """
            SELECT edt.cours, s.design AS salle,
                   p.nom || ' ' || p.prénoms AS professeur,
                   edt.datedebut, edt.datefin
            FROM emploi_du_temps edt
            JOIN salle s ON edt.idsalle = s.idsalle
            JOIN professeur p ON edt.idprof = p.idprof
            WHERE edt.idclasse = ?
              AND edt.datedebut::date BETWEEN ? AND ?
            ORDER BY edt.datedebut;
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, idClasse);
            pst.setDate(2, java.sql.Date.valueOf(dateDebut));
            pst.setDate(3, java.sql.Date.valueOf(dateFin));


            ResultSet rs = pst.executeQuery();

            // Création PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("emploi_du_temps_" + idClasse + ".pdf"));
            document.open();

            Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font texteFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Emploi du temps - Classe " + idClasse, titreFont));
            document.add(new Paragraph("Semaine du " + dateDebut + " au " + dateFin, texteFont));
            document.add(new Paragraph(" ")); // espace

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            table.addCell("Cours");
            table.addCell("Salle");
            table.addCell("Professeur");
            table.addCell("Début");
            table.addCell("Fin");

            boolean hasData = false;

            while (rs.next()) {
                table.addCell(rs.getString("cours"));
                table.addCell(rs.getString("salle"));
                table.addCell(rs.getString("professeur"));
                table.addCell(rs.getTimestamp("datedebut").toString());
                table.addCell(rs.getTimestamp("datefin").toString());
                hasData = true;
            }

            if (hasData) {
                document.add(table);
                System.out.println("PDF généré : emploi_du_temps_" + idClasse + ".pdf");
            } else {
                document.add(new Paragraph("Aucune session prévue pour cette période."));
            }

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
