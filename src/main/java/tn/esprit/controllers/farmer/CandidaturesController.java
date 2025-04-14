package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.controllers.farmer.CandidaturesController;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandidaturesController {

    @FXML
    private VBox candidaturesContainer;

    private StackPane contentPaneRef;
    private OffreEmploi selectedOffre;
    private List<Candidature> candidatures; // store across refreshes

    private void initializeDummyData() {
        User ahmed = new User();
        ahmed.setName("Ahmed");
        ahmed.setEmail("ahmed@example.com");

        User yasmine = new User();
        yasmine.setName("Yasmine");
        yasmine.setEmail("yasmine@example.com");

        Candidature c1 = new Candidature();
        c1.setUser(ahmed);
        c1.setDateApplied(LocalDateTime.now().minusDays(1));
        c1.setStatut(StatutCandidature.EN_ATTENTE);

        Candidature c2 = new Candidature();
        c2.setUser(yasmine);
        c2.setDateApplied(LocalDateTime.now().minusDays(2));
        c2.setStatut(StatutCandidature.ACCEPTE);

        candidatures = new ArrayList<>(List.of(c1, c2));
    }

    public void setContentPaneRef(StackPane contentPaneRef) {
        this.contentPaneRef = contentPaneRef;
        loadCandidatures();
    }
    private void loadCandidatures() {
        candidaturesContainer.getChildren().clear();

        // 🧪 Sample users
        User ahmed = new User();
        ahmed.setName("Ahmed");
        ahmed.setEmail("ahmed@example.com");

        Candidature c1 = new Candidature();
        c1.setUser(ahmed);
        c1.setDateApplied(LocalDateTime.now().minusDays(1));
        c1.setStatut(StatutCandidature.EN_ATTENTE);

        List<Candidature> list = List.of(c1);

        for (Candidature c : list) {
            // Inner grid (content)
            GridPane row = new GridPane();
            row.setHgap(20);
            row.setVgap(5);
            row.setPadding(new Insets(10));

            Label name = new Label("👤 " + c.getNom());
            Label email = new Label("✉ " + c.getEmail());
            Label date = new Label("📅 " + c.getDateApplied().toLocalDate().toString());
            Label statut = new Label("⏳ Statut: " + c.getStatut());

            Button acceptBtn = new Button("Accepter");
            Button rejectBtn = new Button("Refuser");
            acceptBtn.setPrefWidth(90);
            rejectBtn.setPrefWidth(90);

            acceptBtn.setOnAction(e -> {
                c.setStatut(StatutCandidature.ACCEPTE);
                System.out.println("✅ Status changed to ACCEPTE");
                c1.setStatut(StatutCandidature.ACCEPTE); // ✅ Should turn light green

                loadCandidatures();
            });

            rejectBtn.setOnAction(e -> {
                c.setStatut(StatutCandidature.REJETE);
                System.out.println("❌ Status changed to REJETE");
                loadCandidatures();
            });

            row.add(name, 0, 0);
            row.add(email, 1, 0);
            row.add(date, 2, 0);
            row.add(statut, 3, 0);
            row.add(acceptBtn, 4, 0);
            row.add(rejectBtn, 5, 0);

            // 🎨 Background based on status
            String bgColor;
            switch (c.getStatut()) {
                case ACCEPTE -> bgColor = "#e1f5e1"; // light green
                case REJETE -> bgColor = "#fdecea"; // light red
                default -> bgColor = "#ffffff";     // neutral
            }

            System.out.println("➡ Candidature Status: " + c.getStatut());
            System.out.println("🎨 Applying background-color: " + bgColor);

            // Outer VBox wrapper with color applied
            VBox wrapper = new VBox(row);
            wrapper.setPadding(new Insets(5));
            wrapper.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ccc; -fx-background-color: " + bgColor + ";");

            System.out.println("📦 Final row style: " + wrapper.getStyle());

            candidaturesContainer.getChildren().add(wrapper);
        }
    }


    public void setOffre(OffreEmploi offre, StackPane pane) {
        this.selectedOffre = offre;
        this.contentPaneRef = pane;
        loadCandidatures(); // or any method you use to display data
    }

}
