package com.mosquetime.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mosquetime.model.Langue;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

/**
 * Paramètres de l'utilisateur : choix de la langue (français, arabe, anglais).
 * Le choix est appliqué tout de suite à tous les écrans et enregistré dans
 * parametres.json, pour être retrouvé au prochain lancement.
 */
public class ParametrePanel extends JPanel {

    private final JLabel titre = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel labelLangue = new JLabel(" ", SwingConstants.CENTER);
    private final BoutonArrondi boutonFermer = new BoutonArrondi(" ");
    private final Map<Langue, BoutonArrondi> boutonsLangue = new EnumMap<>(Langue.class);

    public ParametrePanel() {
        this(null);
    }

    // fermer : action du bouton « Fermer » (null = pas de bouton)
    public ParametrePanel(Runnable fermer) {

        setLayout(new GridBagLayout());
        setBackground(Style.FOND);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        titre.setFont(Style.TITRE);
        titre.setForeground(Style.OR);

        labelLangue.setFont(Style.MOYEN);
        labelLangue.setForeground(Style.TEXTE);

        JPanel choix = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        choix.setOpaque(false);

        ButtonGroup groupe = new ButtonGroup();
        for (Langue langue : Langue.values()) {
            BoutonArrondi bouton = new BoutonArrondi(langue.getNomAffiche());
            bouton.addActionListener(e -> I18n.changerLangue(langue));
            groupe.add(bouton);
            boutonsLangue.put(langue, bouton);
            choix.add(bouton);
        }

        boutonFermer.setVisible(fermer != null);
        if (fermer != null) {
            boutonFermer.addActionListener(e -> {
                boutonFermer.setSelected(false);
                fermer.run();
            });
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 18, 0); add(titre, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 12, 0); add(labelLangue, c);
        c.gridy = 2; c.insets = new Insets(0, 0, 26, 0); add(choix, c);
        c.gridy = 3; c.insets = new Insets(0, 0, 0, 0); c.fill = GridBagConstraints.NONE;
        add(boutonFermer, c);

        appliquerLangue();
        I18n.ajouterEcouteur(this::appliquerLangue);
    }

    private void appliquerLangue() {
        applyComponentOrientation(I18n.orientation());

        titre.setText(I18n.t("parametres.titre"));
        labelLangue.setText(I18n.t("parametres.langue"));
        boutonFermer.setText(I18n.t("parametres.fermer"));

        // La langue courante est mise en évidence (bouton doré)
        for (Map.Entry<Langue, BoutonArrondi> entree : boutonsLangue.entrySet()) {
            entree.getValue().setSelected(entree.getKey() == I18n.getLangue());
        }

        revalidate();
        repaint();
    }
}
