package org.example;

import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class OffreManagementGUI extends JFrame {

    private final ServiceOffreEmploi service;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField idField, titreField, descriptionField, salaireField, lieuField;

    public OffreManagementGUI() {
        service = new ServiceOffreEmploi();
        setTitle("Gestion des Offres d'Emploi");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Table ===
        tableModel = new DefaultTableModel(new Object[]{"ID", "Titre", "Description", "Salaire", "Lieu"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // === Form Panel ===
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Formulaire"));

        idField = new JTextField();
        titreField = new JTextField();
        descriptionField = new JTextField();
        salaireField = new JTextField();
        lieuField = new JTextField();

        formPanel.add(new JLabel("ID (pour modifier/supprimer):"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Titre:"));
        formPanel.add(titreField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Salaire:"));
        formPanel.add(salaireField);
        formPanel.add(new JLabel("Lieu:"));
        formPanel.add(lieuField);

        // === Buttons ===
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        JButton addButton = new JButton("Ajouter");
        JButton modifyButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton refreshButton = new JButton("Rafraîchir");

        buttonPanel.add(addButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // === Add Panels ===
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // === Actions ===
        addButton.addActionListener(this::addOffre);
        modifyButton.addActionListener(this::modifyOffre);
        deleteButton.addActionListener(this::deleteOffre);
        refreshButton.addActionListener(e -> refreshTable());

        // === Init Table ===
        refreshTable();
        setVisible(true);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<OffreEmploi> offres = service.getAll();
        for (OffreEmploi o : offres) {
            tableModel.addRow(new Object[]{
                    o.getId(), o.getTitre(), o.getDescription(), o.getSalaire(), o.getLieu()
            });
        }
    }

    private void addOffre(ActionEvent e) {
        OffreEmploi o = new OffreEmploi();
        o.setTitre(titreField.getText());
        o.setDescription(descriptionField.getText());
        o.setSalaire(Float.parseFloat(salaireField.getText()));
        o.setLieu(lieuField.getText());

        service.ajouter(o);
        refreshTable();
    }

    private void modifyOffre(ActionEvent e) {
        OffreEmploi o = new OffreEmploi();
        o.setId(Integer.parseInt(idField.getText()));
        o.setTitre(titreField.getText());
        o.setDescription(descriptionField.getText());
        o.setSalaire(Float.parseFloat(salaireField.getText()));
        o.setLieu(lieuField.getText());

        service.modifier(o);
        refreshTable();
    }

    private void deleteOffre(ActionEvent e) {
        OffreEmploi o = new OffreEmploi();
        o.setId(Integer.parseInt(idField.getText()));
        service.supprimer(o);
        refreshTable();
    }
}
