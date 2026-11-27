package com.mosquetime.view;

import com.mosquetime.controller.MosqueeController;
import com.mosquetime.model.Mosquee;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Administration des mosquées : liste, ajout, mosquée affichée à l'écran,
 * et nom de chaque mosquée en français, en arabe et en anglais.
 * Tous les textes suivent la langue de l'application.
 */
public class MosqueePanel extends JPanel {

    private final MosqueeController controller;
    private final Consumer<String> surChoix;

    private final DefaultListModel<Mosquee> modele = new DefaultListModel<>();
    private final JList<Mosquee> liste = new JList<>(modele);

    private final JLabel titre = new JLabel();
    private final JLabel labelActive = new JLabel();
    private final JLabel labelStatut = new JLabel(" ");

    private final JLabel etiquetteNomFr = etiquette();
    private final JLabel etiquetteNomAr = etiquette();
    private final JLabel etiquetteNomEn = etiquette();
    private final JLabel etiquetteVille = etiquette();

    private final JTextField champNom = new JTextField(22);
    private final JTextField champNomAr = new JTextField(22);
    private final JTextField champNomEn = new JTextField(22);
    private final JTextField champVille = new JTextField(14);

    private final JButton boutonAjouter = new JButton();
    private final JButton boutonTraduire = new JButton();
    private final JButton boutonChoisir = new JButton();

    public MosqueePanel(MosqueeController controller, Consumer<String> surChoix) {
        this.controller = controller;
        this.surChoix = surChoix;

        setLayout(new BorderLayout(0, 15));
        setBackground(Style.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
        // Choisir une mosquée remplit le formulaire avec ses noms (pour les traduire)
        liste.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                remplirChamps(liste.getSelectedValue());
            }
        });

        boutonAjouter.addActionListener(e -> ajouter());
        boutonTraduire.addActionListener(e -> traduire());
        boutonChoisir.addActionListener(e -> choisir());

        labelStatut.setFont(Style.PETIT);
        labelStatut.setForeground(Style.OR);

        // Formulaire : une ligne par champ (nom français, arabe, anglais, ville)
        JPanel formulaire = new JPanel(new GridBagLayout());
        formulaire.setOpaque(false);
        ligne(formulaire, 0, etiquetteNomFr, champNom);
        ligne(formulaire, 1, etiquetteNomAr, champNomAr);
        ligne(formulaire, 2, etiquetteNomEn, champNomEn);
        ligne(formulaire, 3, etiquetteVille, champVille);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        actions.setOpaque(false);
        actions.add(boutonAjouter);
        actions.add(boutonTraduire);
        actions.add(boutonChoisir);

        JPanel bas = new JPanel(new BorderLayout());
        bas.setOpaque(false);
        bas.add(formulaire, BorderLayout.NORTH);
        bas.add(actions, BorderLayout.CENTER);
        bas.add(labelStatut, BorderLayout.SOUTH);

        add(haut, BorderLayout.NORTH);
        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);

        recharger();
        appliquerLangue();
        I18n.ajouterEcouteur(this::appliquerLangue);
    }

    private void ajouter() {
        try {
            controller.ajouter(champNom.getText(), champNomAr.getText(),
                    champNomEn.getText(), champVille.getText());
            viderChamps();
            recharger();
            statut("mosquees.enregistre");
        } catch (IllegalArgumentException e) {
            erreur(e.getMessage());
        } catch (RuntimeException e) {
            erreur("mosquees.erreur_ecriture");
        }
    }

    // Enregistre le nom arabe et le nom anglais de la mosquée sélectionnée
    private void traduire() {
        Mosquee selection = liste.getSelectedValue();
        if (selection == null) {
            erreur("mosquees.erreur_selection");
            return;
        }
        try {
            controller.traduire(selection.getNom(), champNomAr.getText(), champNomEn.getText());
            recharger();
            surChoix.accept(controller.getNomActive());   // l'écran relit le nom traduit
            statut("mosquees.enregistre");
        } catch (IllegalArgumentException e) {
            erreur(e.getMessage());
        } catch (RuntimeException e) {
            erreur("mosquees.erreur_ecriture");
        }
    }

    private void choisir() {
        Mosquee selection = liste.getSelectedValue();
        if (selection == null) {
            erreur("mosquees.erreur_selection");
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

    // Recharge la liste en gardant la sélection
    private void recharger() {
        String nomSelection = liste.getSelectedValue() == null
                ? null : liste.getSelectedValue().getNom();

        modele.clear();
        modele.addAll(controller.getMosquees());

        if (nomSelection != null) {
            for (int i = 0; i < modele.size(); i++) {
                if (nomSelection.equals(modele.get(i).getNom())) {
                    liste.setSelectedIndex(i);
                    break;
                }
            }
        }
        majActive();
    }

    private void majActive() {
        labelActive.setText(I18n.t("mosquees.active",
                controller.getNomActive(I18n.getLangue())));
    }

    private void remplirChamps(Mosquee m) {
        if (m == null) {
            return;
        }
        champNom.setText(texte(m.getNom()));
        champNomAr.setText(texte(m.getNomAr()));
        champNomEn.setText(texte(m.getNomEn()));
        champVille.setText(texte(m.getVille()));
    }

    private void viderChamps() {
        champNom.setText("");
        champNomAr.setText("");
        champNomEn.setText("");
        champVille.setText("");
    }

    // Appelé au démarrage et à chaque changement de langue
    private void appliquerLangue() {
        applyComponentOrientation(I18n.orientation());

        // Les noms latins se lisent de gauche à droite, le nom arabe de droite à gauche
        champNom.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        champNomEn.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        champVille.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        champNomAr.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        titre.setText(I18n.t("mosquees.titre"));
        etiquetteNomFr.setText(I18n.t("mosquees.nom_fr"));
        etiquetteNomAr.setText(I18n.t("mosquees.nom_ar"));
        etiquetteNomEn.setText(I18n.t("mosquees.nom_en"));
        etiquetteVille.setText(I18n.t("mosquees.ville"));
        boutonAjouter.setText(I18n.t("mosquees.ajouter"));
        boutonTraduire.setText(I18n.t("mosquees.traduire"));
        boutonChoisir.setText(I18n.t("mosquees.afficher"));
        labelStatut.setText(" ");
        majActive();

        liste.repaint();
        revalidate();
        repaint();
    }

    private String texteDe(Mosquee m) {
        String marque = m.isActive() ? "● " : "    ";
        String ville = (m.getVille() == null || m.getVille().isBlank())
                ? "" : "  —  " + m.getVille();
        return marque + m.getNom(I18n.getLangue()) + ville;
    }

    private void ligne(JPanel formulaire, int numero, JLabel label, JTextField champ) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = numero;
        c.insets = new Insets(3, 5, 3, 10);
        c.anchor = GridBagConstraints.LINE_START;

        c.gridx = 0;
        formulaire.add(label, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        formulaire.add(champ, c);
    }

    private static JLabel etiquette() {
        JLabel label = new JLabel();
        label.setFont(Style.PETIT);
        label.setForeground(Style.TEXTE);
        return label;
    }

    private void statut(String cle) {
        labelStatut.setText(I18n.t(cle));
    }

    // Le message d'une exception est une clé de traduction
    private void erreur(String cle) {
        JOptionPane.showMessageDialog(this, I18n.t(cle), I18n.t("mosquees.titre"),
                JOptionPane.WARNING_MESSAGE);
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur;
    }
}
