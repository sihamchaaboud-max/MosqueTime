package com.mosquetime.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;

import com.mosquetime.util.Style;

/**
 * Petit bouton rond avec une roue dentée dessinée à la main.
 * Pas de texte : il ne dépend ni de la langue ni d'une police.
 */
class BoutonParametres extends JButton {

    static final int TAILLE = 46;

    private boolean survol = false;

    BoutonParametres() {
        setPreferredSize(new Dimension(TAILLE, TAILLE));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                survol = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                survol = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;

        // Fond rond semi-transparent
        g2.setColor(new Color(Style.CARTE.getRed(), Style.CARTE.getGreen(),
                Style.CARTE.getBlue(), survol ? 230 : 170));
        g2.fill(new Ellipse2D.Double(1, 1, getWidth() - 2, getHeight() - 2));

        // Roue dentée : disque + 8 dents - trou central
        Area roue = new Area(new Ellipse2D.Double(cx - 8, cy - 8, 16, 16));
        for (int i = 0; i < 8; i++) {
            AffineTransform rotation = AffineTransform.getRotateInstance(i * Math.PI / 4, cx, cy);
            roue.add(new Area(rotation.createTransformedShape(
                    new Rectangle2D.Double(cx - 2.5, cy - 12, 5, 8))));
        }
        roue.subtract(new Area(new Ellipse2D.Double(cx - 3.5, cy - 3.5, 7, 7)));

        g2.setColor(survol ? Style.OR : Style.TEXTE);
        g2.fill(roue);
        g2.dispose();
    }
}
