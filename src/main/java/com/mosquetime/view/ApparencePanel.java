package com.mosquetime.view;

import com.mosquetime.service.ApparenceService;
import com.mosquetime.util.Style;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;

public class ApparencePanel extends JPanel {

    private static final String AUCUN = "Couleur unie (sans image)";

    private final ApparenceService service;
    private final Runnable surChangement;

    private final DefaultListModel<String> modele = new DefaultListModel<>();
    private final JList<String> liste = new JList<>(modele);
    private final JLabel labelActuel = new JLabel();
    private final JLabel apercu = new JLabel("", SwingConstants.CENTER);

    public ApparencePanel(ApparenceService service, Runnable surChangement) {
        this.service = service;
        this.surChangement = surChangement;

        setLayout(new BorderLayout(0, 15));
        setBackground(Style.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titre = new JLabel("Arrière-plan");
        titre.setFont(Style.TITRE);
        titre.setForeground(Style.OR);

        labelActuel.setFont(Style.PETIT);
        labelActuel.setForeground(Style.TEXTE);

        JPanel haut = new JPanel(new GridLayout(2, 1));
        haut.setOpaque(false);
        haut.add(titre);
        haut.add(labelActuel);

        liste.setFont(Style.PETIT);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setBackground(Style.CARTE);
        liste.setForeground(Style.TEXTE);
        liste.setSelectionBackground(Style.OR);
        liste.setSelectionForeground(Style.FOND);
        liste.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object valeur,
                    int index, boolean selectionne, boolean focus) {
                super.getListCellRendererComponent(l, valeur, index, selectionne, focus);
                String nom = (String) valeur;
                setText((estCourant(nom) ? "● " : "    ") + nom);
                return this;
            }
        });
        liste.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                afficherApercu();
            }
        });

        apercu.setFont(Style.PETIT);
        apercu.setForeground(Style.TEXTE);
        apercu.setPreferredSize(new Dimension(360, 200));

        JPanel centre = new JPanel(new BorderLayout(20, 0));
        centre.setOpaque(false);
        centre.add(new JScrollPane(liste), BorderLayout.CENTER);
        centre.add(apercu, BorderLayout.EAST);

        JButton boutonAjouter = new JButton("Ajouter une image...");
        boutonAjouter.addActionListener(e -> ajouterImage());

        JButton boutonChoisir = new JButton("Utiliser ce fond");
        boutonChoisir.addActionListener(e -> choisir());

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        boutons.setOpaque(false);
        boutons.add(boutonAjouter);
        boutons.add(boutonChoisir);

        add(haut, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);

        recharger();
    }

    private void choisir() {
        String nom = liste.getSelectedValue();
        if (nom == null) {
            erreur("Sélectionne d'abord un fond dans la liste.");
            return;
        }
        try {
            service.choisirFond(nom.equals(AUCUN) ? "" : nom);
            recharger();
            liste.setSelectedValue(nom, true);
            surChangement.run();
        } catch (IllegalArgumentException e) {
            erreur(e.getMessage());
        }
    }

    private void ajouterImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (jpg, png)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String nom = service.ajouterImage(chooser.getSelectedFile());
            recharger();
            liste.setSelectedValue(nom, true);
        } catch (IllegalArgumentException | IOException e) {
            erreur("Impossible d'ajouter l'image : " + e.getMessage());
        }
    }

    private void recharger() {
        modele.clear();
        modele.addElement(AUCUN);
        modele.addAll(service.fondsDisponibles());
        String actuel = service.getFond();
        labelActuel.setText("Arrière-plan actuel : " + (actuel.isEmpty() ? AUCUN : actuel));
    }

    private void afficherApercu() {
        String nom = liste.getSelectedValue();
        apercu.setIcon(null);
        if (nom == null) {
            apercu.setText("");
            return;
        }
        if (nom.equals(AUCUN)) {
            apercu.setText("Couleur unie");
            return;
        }
        Image image = service.chargerImage(nom);
        if (image == null) {
            apercu.setText("Image illisible");
            return;
        }
        apercu.setText("");
        apercu.setIcon(new ImageIcon(image.getScaledInstance(-1, 180, Image.SCALE_SMOOTH)));
    }

    private boolean estCourant(String nom) {
        String actuel = service.getFond();
        return nom.equals(AUCUN) ? actuel.isEmpty() : nom.equals(actuel);
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Arrière-plan",
                JOptionPane.WARNING_MESSAGE);
    }
}