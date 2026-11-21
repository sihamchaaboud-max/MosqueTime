package com.mosquetime.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JToggleButton;

import com.mosquetime.util.Style;

/**
 * Bouton aux coins arrondis, même style que les cartes de l'écran :
 * doré quand il est sélectionné, sombre sinon.
 */
class BoutonArrondi extends JToggleButton {

    BoutonArrondi(String texte) {
        super(texte);
        setFont(new Font("SansSerif", Font.BOLD, 20));
        setPreferredSize(new Dimension(150, 50));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean choisi = isSelected();
        g2.setColor(choisi ? Style.OR : new Color(0x1F2D45));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

        if (!choisi) {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
        }
        g2.dispose();

        setForeground(choisi ? new Color(0x111827) : Style.TEXTE);
        super.paintComponent(g);
    }
}
