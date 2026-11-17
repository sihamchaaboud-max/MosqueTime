package com.mosquetime.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mosquetime.util.Style;

/**
 * Écran d'ATTENTE : compte à rebours avant l'iqama, même style que NormalPanel.
 * Le panneau ne calcule rien : le contrôleur appelle setTempsRestant()
 * chaque seconde. Le compteur devient rouge sous une minute.
 */
public class AttentePanel extends JPanel {

    private static final Color ALERTE = new Color(0xFF6B5B);
    private static final int DUREE_PAR_DEFAUT = 15 * 60;

    private final Image fond = FondEcran.charger();

    private final JLabel titre = new JLabel("Iqama dans", SwingConstants.CENTER);
    private final JLabel titreAr = new JLabel("إقامة الصلاة بعد", SwingConstants.CENTER);
    private final JLabel nomPriere = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel compteur = new JLabel("00:00", SwingConstants.CENTER);
    private final JLabel consigne =
            new JLabel("Veuillez rejoindre les rangs", SwingConstants.CENTER);
    private final Barre barre = new Barre();

    private int dureeTotale = DUREE_PAR_DEFAUT;

    public AttentePanel() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        titre.setFont(Style.MOYEN);
        titre.setForeground(new Color(0xCBD5E1));

        titreAr.setFont(new Font("Traditional Arabic", Font.BOLD, 40));
        titreAr.setForeground(Style.OR);

        nomPriere.setFont(new Font("SansSerif", Font.BOLD, 64));
        nomPriere.setForeground(Style.TEXTE);

        compteur.setFont(new Font("Monospaced", Font.BOLD, 150));
        compteur.setForeground(Style.OR);

        consigne.setFont(new Font("SansSerif", Font.ITALIC, 26));
        consigne.setForeground(new Color(0xCBD5E1));

        CarteArrondie carte = new CarteArrondie(new GridBagLayout(), 190);
        carte.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);

        c.gridy = 0; carte.add(titre, c);
        c.gridy = 1; carte.add(titreAr, c);
        c.gridy = 2; carte.add(nomPriere, c);
        c.gridy = 3; carte.add(compteur, c);
        c.gridy = 4; carte.add(barre, c);
        c.gridy = 5; carte.add(consigne, c);

        GridBagConstraints centre = new GridBagConstraints();
        centre.weightx = 1;
        centre.fill = GridBagConstraints.HORIZONTAL;
        add(carte, centre);

        setTempsRestant(0);
    }

    public void setPriere(String nom) {
        nomPriere.setText(nom);
    }

    // Durée totale de l'attente (sert à calculer la barre de progression)
    public void setDureeTotale(int secondes) {
        dureeTotale = Math.max(secondes, 1);
    }

    // Met à jour le compte à rebours (rouge sous la minute)
    public void setTempsRestant(int secondes) {

        if (secondes < 0) {
            secondes = 0;
        }

        compteur.setText(String.format("%02d:%02d", secondes / 60, secondes % 60));

        Color couleur = secondes < 60 ? ALERTE : Style.OR;
        compteur.setForeground(couleur);

        barre.setProgression((double) secondes / dureeTotale, couleur);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        FondEcran.peindre(g, fond, getWidth(), getHeight());
    }

    // Barre de progression arrondie (temps restant)
    private static class Barre extends JPanel {

        private double progression = 1.0;
        private Color couleur = Style.OR;

        Barre() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 14));
        }

        void setProgression(double valeur, Color couleur) {
            this.progression = Math.max(0, Math.min(1, valeur));
            this.couleur = couleur;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int h = getHeight();
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);

            int w = (int) (getWidth() * progression);
            if (w > 0) {
                g2.setColor(couleur);
                g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }
}
