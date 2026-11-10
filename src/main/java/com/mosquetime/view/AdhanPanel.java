package com.mosquetime.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Écran affiché pendant l'adhan (état ADHAN).
 * L'animation de clignotement démarre et s'arrête automatiquement
 * quand le panneau est affiché / retiré.
 */
public class AdhanPanel extends JPanel {

    private static final Color FOND = new Color(0x0B3D2E);
    private static final Color OR = new Color(0xE8C766);
    private static final Color OR_PALE = new Color(0x9C8742);

    private final JLabel titre = new JLabel("حان الآن وقت الأذان", SwingConstants.CENTER);
    private final JLabel nomPriere = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel heure = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel sousTitre = new JLabel("C'est l'heure de la prière", SwingConstants.CENTER);

    private final Timer clignotement;
    private boolean clair = true;

    public AdhanPanel() {

        setLayout(new GridBagLayout());
        setBackground(FOND);

        titre.setFont(new Font("Traditional Arabic", Font.BOLD, 64));
        titre.setForeground(OR);

        nomPriere.setFont(new Font("SansSerif", Font.BOLD, 96));
        nomPriere.setForeground(Color.WHITE);

        heure.setFont(new Font("SansSerif", Font.PLAIN, 54));
        heure.setForeground(OR);

        sousTitre.setFont(new Font("SansSerif", Font.ITALIC, 28));
        sousTitre.setForeground(new Color(0xB9C9C0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(12, 20, 12, 20);

        c.gridy = 0; add(titre, c);
        c.gridy = 1; add(nomPriere, c);
        c.gridy = 2; add(heure, c);
        c.gridy = 3; add(sousTitre, c);

        clignotement = new Timer(800, e -> {
            clair = !clair;
            titre.setForeground(clair ? OR : OR_PALE);
        });
    }

    // Prière en cours : ex. setPriere("Dhuhr", "13:05")
    public void setPriere(String nom, String heureTexte) {
        nomPriere.setText(nom);
        heure.setText(heureTexte);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        clignotement.start();
    }

    @Override
    public void removeNotify() {
        clignotement.stop();
        super.removeNotify();
    }
}
