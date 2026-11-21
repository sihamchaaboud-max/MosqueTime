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

import com.mosquetime.model.Priere;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

/**
 * Écran d'ATTENTE : compte à rebours avant l'iqama (même style que NormalPanel).
 * Les textes suivent la langue choisie. Le compteur reste en chiffres
 * 00:00, de gauche à droite, quelle que soit la langue.
 */
public class AttentePanel extends JPanel {

    private static final Color ALERTE = new Color(0xFF6B5B);
    private static final int DUREE_PAR_DEFAUT = 15 * 60;

    private final Image fond = FondEcran.charger();

    private final JLabel titre = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel nomPriere = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel compteur = new JLabel("00:00", SwingConstants.CENTER);
    private final JLabel consigne = new JLabel(" ", SwingConstants.CENTER);
    private final Barre barre = new Barre();

    private int dureeTotale = DUREE_PAR_DEFAUT;
    private int secondes = 0;

    private Priere priere;
    private String nomLibre = "";

    public AttentePanel() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        titre.setForeground(new Color(0xCBD5E1));

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
        c.gridy = 1; carte.add(nomPriere, c);
        c.gridy = 2; carte.add(compteur, c);
        c.gridy = 3; carte.add(barre, c);
        c.gridy = 4; carte.add(consigne, c);

        GridBagConstraints centre = new GridBagConstraints();
        centre.weightx = 1;
        centre.fill = GridBagConstraints.HORIZONTAL;
        add(carte, centre);

        setTempsRestant(0);
        appliquerLangue();
        I18n.ajouterEcouteur(this::appliquerLangue);
    }

    // Prière concernée : le nom s'affiche dans la langue choisie
    public void setPriere(Priere priere) {
        this.priere = priere;
        this.nomLibre = "";
        appliquerLangue();
    }

    // Compatibilité avec l'ancien appel setPriere(nom)
    public void setPriere(String nom) {
        for (Priere p : Priere.values()) {
            if (p.getLibelle().equalsIgnoreCase(nom) || p.name().equalsIgnoreCase(nom)) {
                setPriere(p);
                return;
            }
        }
        this.priere = null;
        this.nomLibre = nom;
        appliquerLangue();
    }

    // Durée totale de l'attente (sert à calculer la barre de progression)
    public void setDureeTotale(int secondes) {
        dureeTotale = Math.max(secondes, 1);
    }

    // Met à jour le compte à rebours (rouge sous la minute)
    public void setTempsRestant(int secondes) {

        this.secondes = Math.max(secondes, 0);

        compteur.setText(String.format("%02d:%02d", this.secondes / 60, this.secondes % 60));

        Color couleur = this.secondes < 60 ? ALERTE : Style.OR;
        compteur.setForeground(couleur);

        barre.setProgression((double) this.secondes / dureeTotale, couleur);
    }

    private void appliquerLangue() {
        applyComponentOrientation(I18n.orientation());

        titre.setFont(I18n.estDroiteAGauche()
                ? new Font("Traditional Arabic", Font.BOLD, 46)
                : Style.MOYEN);

        titre.setText(I18n.t("attente.titre"));
        consigne.setText(I18n.t("attente.consigne"));
        nomPriere.setText(priere != null ? I18n.nomPriere(priere) : nomLibre);

        revalidate();
        repaint();
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
