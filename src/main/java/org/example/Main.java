package org.example;

import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Offre Emploi App ===");
        System.out.println("1. Terminal Mode");
        System.out.println("2. GUI Mode (Offre uniquement)");
        System.out.println("3. Tableau de bord (tous les modules)");
        System.out.print("Choix: ");

        int mode = getValidInteger(scanner);

        if (mode == 1) {
            runTerminalMode();
        } else if (mode == 2) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Offre Management GUI");
                frame.setContentPane(new OffreManagementPanel());
                frame.setSize(800, 500);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        } else if (mode == 3) {
            SwingUtilities.invokeLater(DashboardGUI::new);
        } else {
            System.out.println("Choix invalide !");
        }
    }

    // === Terminal Mode ===
    private static void runTerminalMode() {
        Scanner scanner = new Scanner(System.in);
        ServiceOffreEmploi service = new ServiceOffreEmploi();

        System.out.println("=== Offre Emploi Management ===");

        while (true) {
            System.out.println("\n1. Ajouter une offre");
            System.out.println("2. Modifier une offre");
            System.out.println("3. Supprimer une offre");
            System.out.println("4. Afficher toutes les offres");
            System.out.println("5. Quitter");
            System.out.print("Choix: ");

            int choix = getValidInteger(scanner);

            switch (choix) {
                case 1 -> ajouterOffre(service, scanner);
                case 2 -> modifierOffre(service, scanner);
                case 3 -> supprimerOffre(service, scanner);
                case 4 -> afficherOffres(service);
                case 5 -> {
                    System.out.println("Fermeture du programme.");
                    return;
                }
                default -> System.out.println("Choix invalide !");
            }
        }
    }

    private static void ajouterOffre(ServiceOffreEmploi service, Scanner scanner) {
        System.out.print("Titre: ");
        String titre = scanner.nextLine();

        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Salaire (DT): ");
        float salaire = Float.parseFloat(scanner.nextLine());

        System.out.print("Lieu: ");
        String lieu = scanner.nextLine();

        OffreEmploi offre = new OffreEmploi();
        offre.setTitre(titre);
        offre.setDescription(description);
        offre.setSalaire(salaire);
        offre.setLieu(lieu);

        service.ajouter(offre);
        System.out.println("✅ Offre ajoutée avec succès !");
    }

    private static void modifierOffre(ServiceOffreEmploi service, Scanner scanner) {
        System.out.print("ID de l'offre à modifier: ");
        int id = getValidInteger(scanner);

        System.out.print("Nouveau titre: ");
        String titre = scanner.nextLine();

        System.out.print("Nouvelle description: ");
        String description = scanner.nextLine();

        System.out.print("Nouveau salaire (DT): ");
        float salaire = Float.parseFloat(scanner.nextLine());

        System.out.print("Nouveau lieu: ");
        String lieu = scanner.nextLine();

        OffreEmploi offre = new OffreEmploi();
        offre.setId(id);
        offre.setTitre(titre);
        offre.setDescription(description);
        offre.setSalaire(salaire);
        offre.setLieu(lieu);

        service.modifier(offre);
    }

    private static void supprimerOffre(ServiceOffreEmploi service, Scanner scanner) {
        System.out.print("ID de l'offre à supprimer: ");
        int id = getValidInteger(scanner);

        OffreEmploi offre = new OffreEmploi();
        offre.setId(id);
        service.supprimer(offre);
    }

    private static void afficherOffres(ServiceOffreEmploi service) {
        List<OffreEmploi> offres = service.getAll();

        if (offres.isEmpty()) {
            System.out.println("Aucune offre trouvée.");
        } else {
            System.out.println("\n=== Liste des offres ===");
            for (OffreEmploi o : offres) {
                System.out.println("ID: " + o.getId());
                System.out.println("Titre: " + o.getTitre());
                System.out.println("Description: " + o.getDescription());
                System.out.println("Salaire: " + o.getSalaire() + " DT");
                System.out.println("Lieu: " + o.getLieu());
                System.out.println("Date début: " + o.getStartDate());
                System.out.println("Date expiration: " + o.getDateExpiration());
                System.out.println("------------------------------");
            }
        }
    }

    private static int getValidInteger(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Veuillez entrer un nombre valide: ");
            }
        }
    }

    // === GUI: Embedded Offre Panel ===
    static class OffreManagementPanel extends JPanel {
        private final ServiceOffreEmploi service;
        private final JTable table;
        private final DefaultTableModel model;
        private final JTextField idField, titreField, descriptionField, salaireField, lieuField;

        public OffreManagementPanel() {
            service = new ServiceOffreEmploi();
            setLayout(new BorderLayout());

            model = new DefaultTableModel(new Object[]{"ID", "Titre", "Description", "Salaire", "Lieu"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
            idField = new JTextField();
            titreField = new JTextField();
            descriptionField = new JTextField();
            salaireField = new JTextField();
            lieuField = new JTextField();

            form.add(new JLabel("ID:"));
            form.add(idField);
            form.add(new JLabel("Titre:"));
            form.add(titreField);
            form.add(new JLabel("Description:"));
            form.add(descriptionField);
            form.add(new JLabel("Salaire:"));
            form.add(salaireField);
            form.add(new JLabel("Lieu:"));
            form.add(lieuField);

            JPanel buttons = new JPanel(new GridLayout(1, 4, 5, 5));
            JButton add = new JButton("Ajouter");
            JButton mod = new JButton("Modifier");
            JButton del = new JButton("Supprimer");
            JButton ref = new JButton("Rafraîchir");

            buttons.add(add);
            buttons.add(mod);
            buttons.add(del);
            buttons.add(ref);

            add(form, BorderLayout.NORTH);
            add(buttons, BorderLayout.SOUTH);

            add.addActionListener(this::ajouter);
            mod.addActionListener(this::modifier);
            del.addActionListener(this::supprimer);
            ref.addActionListener(e -> rafraichir());

            rafraichir();
        }

        private void rafraichir() {
            model.setRowCount(0);
            for (OffreEmploi o : service.getAll()) {
                model.addRow(new Object[]{
                        o.getId(), o.getTitre(), o.getDescription(), o.getSalaire(), o.getLieu()
                });
            }
        }

        private void ajouter(ActionEvent e) {
            OffreEmploi o = new OffreEmploi();
            o.setTitre(titreField.getText());
            o.setDescription(descriptionField.getText());
            o.setSalaire(Float.parseFloat(salaireField.getText()));
            o.setLieu(lieuField.getText());
            service.ajouter(o);
            rafraichir();
        }

        private void modifier(ActionEvent e) {
            OffreEmploi o = new OffreEmploi();
            o.setId(Integer.parseInt(idField.getText()));
            o.setTitre(titreField.getText());
            o.setDescription(descriptionField.getText());
            o.setSalaire(Float.parseFloat(salaireField.getText()));
            o.setLieu(lieuField.getText());
            service.modifier(o);
            rafraichir();
        }

        private void supprimer(ActionEvent e) {
            OffreEmploi o = new OffreEmploi();
            o.setId(Integer.parseInt(idField.getText()));
            service.supprimer(o);
            rafraichir();
        }
    }

    // === GUI: Full Dashboard with Tabs ===
    static class DashboardGUI extends JFrame {
        public DashboardGUI() {
            setTitle("Tableau de bord - Application Gestion");
            setSize(1000, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Offres de Travail", new OffreManagementPanel());
            tabs.addTab("Commandes", new JPanel()); // Placeholder
            tabs.addTab("Produits", new JPanel());  // Placeholder
            tabs.addTab("Blog", new JPanel());      // Placeholder
            tabs.addTab("Candidatures", new CandidaturePanel());

            add(tabs);
            setVisible(true);
        }
    }
    static class CandidaturePanel extends JPanel {
        private final tn.esprit.services.ServiceCandidature service = new tn.esprit.services.ServiceCandidature();
        private final tn.esprit.services.ServiceOffreEmploi offreService = new tn.esprit.services.ServiceOffreEmploi();

        private final JTable table;
        private final DefaultTableModel model;
        private final JComboBox<String> offreCombo;
        private final JTextField statutField, ratingField, idField;

        public CandidaturePanel() {
            setLayout(new BorderLayout());

            model = new DefaultTableModel(new Object[]{"ID", "Offre ID", "Statut", "Date", "Rating"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
            idField = new JTextField();
            offreCombo = new JComboBox<>();
            statutField = new JTextField("EN_ATTENTE");
            ratingField = new JTextField();

            form.add(new JLabel("ID (pour modification/suppression):"));
            form.add(idField);
            form.add(new JLabel("Offre:"));
            form.add(offreCombo);
            form.add(new JLabel("Statut:"));
            form.add(statutField);
            form.add(new JLabel("Note (facultatif):"));
            form.add(ratingField);

            JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 5));
            JButton addBtn = new JButton("Ajouter");
            JButton delBtn = new JButton("Supprimer");
            JButton refBtn = new JButton("Rafraîchir");

            buttons.add(addBtn);
            buttons.add(delBtn);
            buttons.add(refBtn);

            add(form, BorderLayout.NORTH);
            add(buttons, BorderLayout.SOUTH);

            addBtn.addActionListener(this::ajouter);
            delBtn.addActionListener(this::supprimer);
            refBtn.addActionListener(e -> rafraichir());

            chargerOffres();
            rafraichir();
        }

        private void chargerOffres() {
            offreCombo.removeAllItems();
            for (OffreEmploi o : offreService.getAll()) {
                offreCombo.addItem(o.getId() + " - " + o.getTitre());
            }
        }

        private void rafraichir() {
            model.setRowCount(0);
            for (var c : service.getAll()) {
                model.addRow(new Object[]{
                        c.getId(),
                      //  c.getIdOffre() != null ? c.getIdOffre().getId() : "N/A",
                        c.getStatut(),
                        c.getDateApplied(),
                        c.getRating()
                });
            }
        }

        private void ajouter(ActionEvent e) {
            tn.esprit.entities.Candidature c = new tn.esprit.entities.Candidature();
            try {
                String selected = (String) offreCombo.getSelectedItem();
                if (selected == null) return;
                int offreId = Integer.parseInt(selected.split(" - ")[0]);

                c.setStatut(tn.esprit.entities.StatutCandidature.valueOf(statutField.getText()));
                if (!ratingField.getText().isEmpty())
                    c.setRating(Integer.parseInt(ratingField.getText()));

                service.ajouter(c, offreId);
                rafraichir();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        }

        private void supprimer(ActionEvent e) {
            tn.esprit.entities.Candidature c = new tn.esprit.entities.Candidature();
            try {
                c.setId(Integer.parseInt(idField.getText()));
                service.supprimer(c);
                rafraichir();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        }
    }

}
