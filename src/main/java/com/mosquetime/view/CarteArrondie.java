package com.mosquetime.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import com.mosquetime.util.Style;

/** Carte semi-transparente aux coins arrondis (même style que NormalPanel). */
class CarteArrondie extends JPanel {

    // false : le texte s'écrit directement sur l'arrière-plan choisi, sans carte colorée.
    // true  : les cartes semi-transparentes bleues sont de retour (tous les écrans).
    static final boolean AFFICHER_CARTES = false;

    private final int alpha;

    CarteArrondie(LayoutManager layout, int alpha) {
        super(layout);
        this.alpha = alpha;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (AFFICHER_CARTES) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(Style.CARTE.getRed(), Style.CARTE.getGreen(),
                    Style.CARTE.getBlue(), alpha));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
