package com.mosquetime.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.mosquetime.util.Style;

/**
 * Fond commun des écrans (même rendu que NormalPanel) :
 * photo de la mosquée plein écran + voile sombre.
 */
final class FondEcran {

    private FondEcran() {
    }

    static Image charger() {
        try {
            URL url = FondEcran.class.getResource("/images/mosqimg.jpg");
            if (url == null) {
                return null;
            }
            return ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }
    }

    static void peindre(Graphics g, Image fond, int largeur, int hauteur) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (fond != null) {
            double echelle = Math.max(
                    (double) largeur / fond.getWidth(null),
                    (double) hauteur / fond.getHeight(null));
            int w = (int) (fond.getWidth(null) * echelle);
            int h = (int) (fond.getHeight(null) * echelle);
            g2.drawImage(fond, (largeur - w) / 2, (hauteur - h) / 2, w, h, null);
        } else {
            g2.setColor(Style.FOND);
            g2.fillRect(0, 0, largeur, hauteur);
        }

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, largeur, hauteur);
        g2.dispose();
    }
}
