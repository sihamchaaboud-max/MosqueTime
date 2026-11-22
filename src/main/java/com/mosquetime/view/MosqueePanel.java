package com.mosquetime.view;

import com.mosquetime.controller.MosqueeController;
import com.mosquetime.model.Mosquee;
import com.mosquetime.util.Style;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MosqueePanel extends JPanel {

    private final MosqueeController controller;
    private final Consumer<String> surChoix;

    private final DefaultListModel<Mosquee> modele = new DefaultListModel<>();
    private final JList<Mosquee> liste = new JList<>(modele);
    private final JLabel labelActive = new JLabel();
    private final JTextField champNom = new JTextField(18);
    private final JTextField champVille = new JTextField(12);

    public MosqueePanel(MosqueeController controller, Consumer<String> surChoix) {
        this.controller = controller;
        this.surChoix = surChoix;

        setLayout(new BorderLayout(0, 15));
        setBackground(Style.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titre = new JLabel("Mosquées");
        titre.setFont(Style.TITRE);
        titre.setForeground(Style.OR);

        labelActive.setFont(Style.PETIT);
        labelActive.setForeground(Style.TEXTE);

        JPanel haut = new JPanel(new GridLayout(2, 1));
        haut.setOpaque(false);
        haut.add(titre);
        haut.add(labelActive);

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
                setText(texteDe((Mosquee) valeur));
                return this;
            }
        });

        JButton boutonAjouter = new JButton("Ajouter");
        boutonAjouter.addActionListener(e -> ajouter());

        JButton boutonChoisir = new JButton("Afficher la mosquée sélectionnée");
        boutonChoisir.addActionListener(e -> choisir());

        JPanel formulaire = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formulaire.setOpaque(false);
        formulaire.add(etiquette("Nom :"));
        formulaire.add(champNom);
        formulaire.add(etiquette("Ville :"));
        formulaire.add(champVille);
        formulaire.add(boutonAjouter);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actions.setOpaque(false);
        actions.add(boutonChoisir);

        JPanel bas = new JPanel(new GridLayout(2, 1));
        bas.setOpaque(false);
        bas.add(formulaire);
        bas.add(actions);

        add(haut, BorderLayout.NORTH);
        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);

        recharger();
    }

    private void ajouter() {
        try {
            controller.ajouter(champNom.getText(), champVille.getText());
            champNom.setText("");
            champVille.setText("");
            recharger();
        } catch (IllegalArgumentException e) {
            erreur(e.getMessage());
        }
    }

    private void choisir() {
        Mosquee selection = liste.getSelectedValue();
        if (selection == null) {
            erreur("Sélectionne d'abord une mosquée dans la liste.");
            return;
        }
        try {
            controller.choisir(selection.getNom());
            recharger();
            surChoix.accept(controller.getNomActive());
        } catch (IllegalArgumentException e) {
            erreur(e.getMessage());
        }
    }

    private void recharger() {
        modele.clear();
        modele.addAll(controller.getMosquees());
        labelActive.setText("Mosquée affichée à l'écran : " + controller.getNomActive());
    }

    private String texteDe(Mosquee m) {
        String marque = m.isActive() ? "● " : "    ";
        String ville = (m.getVille() == null || m.getVille().isBlank())
                ? "" : "  —  " + m.getVille();
        return marque + m.getNom() + ville;
    }

    private JLabel etiquette(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(Style.PETIT);
        label.setForeground(Style.TEXTE);
        return label;
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Mosquées",
                JOptionPane.WARNING_MESSAGE);
    }
}