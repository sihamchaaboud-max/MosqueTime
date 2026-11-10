package com.mosquetime.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Écran d'attente (état ATTENTE) : compte à rebours avant l'iqama.
 * Ce panneau ne calcule rien : le contrôleur appelle setTempsRestant()
 * chaque seconde avec le nombre de secondes restantes.
 */
public class AttentePanel extends JPanel {

    private static final Color FOND = new Color(0x0B3D2E);
    private static final Color OR = new Color(0xE8C766);
    private static final Color ALERTE = new Color(0xFF6B5B);

    private final JLabel titre = new JLabel("Iqama dans", SwingConstants.CENTER);
    private final JLabel nomPriere = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel compteur = new JLabel("00:00", SwingConstants.CENTER);
    private final JLabel consigne = new JLabel("Veuillez rejoindre les rangs", SwingConstants.CENTER);

    public AttentePanel() {

        setLayout(new GridBagLayout());
        setBackground(FOND);

        titre.setFont(new Font("SansSerif", Font.PLAIN, 40));
        titre.setForeground(new Color(0xB9C9C0));

        nomPriere.setFont(new Font("SansSerif", Font.BOLD, 64));
        nomPriere.setForeground(Color.WHITE);

        compteur.setFont(new Font("Monospaced", Font.BOLD, 170));
        compteur.setForeground(OR);

        consigne.setFont(new Font("SansSerif", Font.ITALIC, 28));
        consigne.setForeground(new Color(0xB9C9C0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 20, 10, 20);

        c.gridy = 0; add(titre, c);
        c.gridy = 1; add(nomPriere, c);
        c.gridy = 2; add(compteur, c);
        c.gridy = 3; add(consigne, c);
    }

    public void setPriere(String nom) {
        nomPriere.setText(nom);
    }

    // Met à jour le compte à rebours (devient rouge sous la minute)
    public void setTempsRestant(int secondes) {

        if (secondes < 0) {
            secondes = 0;
        }

        compteur.setText(String.format("%02d:%02d", secondes / 60, secondes % 60));
        compteur.setForeground(secondes < 60 ? ALERTE : OR);
    }
}
