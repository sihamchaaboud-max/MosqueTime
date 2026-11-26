package com.mosquetime.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;

import com.mosquetime.service.ApparenceService;
import com.mosquetime.util.Style;

/**
 * Fond commun des écrans (même rendu que NormalPanel) :
 * photo de la mosquée plein écran + voile sombre.
 */
final class FondEcran {

    private FondEcran() {
    }

    // Image de fond choisie dans l'administration (apparence.json), mise en mémoire.
    // Tous les écrans (normal, adhan, attente) utilisent le même fond.
    // Force du voile sombre posé sur le fond (0 = aucun, 255 = noir) :
    // plus il est fort, plus les textes (sans carte) sont faciles à lire
    private static final int VOILE = 175;

    private static Image courant;
    private static boolean charge;

    static synchronized Image charger() {
        if (!charge) {
            courant = new ApparenceService().chargerFond();
            charge = true;
        }
        return courant;
    }

    // À appeler quand le fond change : il sera relu au prochain affichage
    static synchronized void recharger() {
        charge = false;
    }

    // Change le fond de tous les écrans tout de suite (fenêtres ouvertes comprises)
    static void rafraichirTout() {
        recharger();
        for (Window fenetre : Window.getWindows()) {
            fenetre.repaint();
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

        g2.setColor(new Color(0, 0, 0, VOILE));
        g2.fillRect(0, 0, largeur, hauteur);
        g2.dispose();
    }
}
