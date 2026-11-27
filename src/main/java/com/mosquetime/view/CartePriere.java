package com.mosquetime.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.mosquetime.util.Style;

/**
 * Carte élégante d'une prière (nom + heure) pour l'écran principal.
 *
 *  - verre sombre semi-transparent, bordure fine, léger dégradé ;
 *  - la prière suivante a une bordure dorée, une lueur et un petit trait doré ;
 *  - les prières déjà passées sont atténuées ;
 *  - interactive : au survol la carte grandit, s'illumine en doré et le texte
 *    devient doré (animation douce) ; au clic elle s'enfonce un peu.
 *
 * Elle dessine son propre fond : elle ne dépend pas de CarteArrondie.AFFICHER_CARTES.
 */
class CartePriere extends JPanel {

    // ---------- Réglages d'apparence (faciles à modifier) ----------
    private static final int MARGE = 8;                 // espace autour de la carte (pour la lueur)
    private static final int ARRONDI = 28;              // arrondi des coins
    private static final Color VERRE = new Color(8, 14, 24);       // couleur de base du verre
    private static final Color TEXTE_PASSE = new Color(255, 246, 229, 120);

    private final JLabel nom = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel heure = new JLabel("--:--", SwingConstants.CENTER);

    private boolean prochaine;
    private boolean passee;
    private boolean survolee;
    private boolean pressee;

    // 0 = repos, 1 = survolée (valeur intermédiaire pendant l'animation)
    private float effet = 0f;
    private final Timer animation = new Timer(16, e -> avancer());

    CartePriere() {
        setLayout(new GridLayout(2, 1, 0, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(MARGE + 18, MARGE + 6, MARGE + 16, MARGE + 6));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        nom.setFont(new Font("SansSerif", Font.BOLD, 22));
        heure.setFont(new Font("SansSerif", Font.BOLD, 36));
        add(nom);
        add(heure);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                survolee = true;
                animation.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                survolee = false;
                pressee = false;
                animation.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressee = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressee = false;
                repaint();
            }
        });

        appliquerCouleurs();
    }

    // Appelé à chaque seconde par NormalPanel
    void mettreAJour(String texteNom, String texteHeure, boolean prochaine, boolean passee) {
        nom.setText(texteNom);
        heure.setText(texteHeure);
        this.prochaine = prochaine;
        this.passee = passee;
        appliquerCouleurs();
        repaint();
    }

    // ---------- Animation ----------

    private void avancer() {
        float cible = survolee ? 1f : 0f;
        effet += (cible - effet) * 0.25f;
        if (Math.abs(cible - effet) < 0.02f) {
            effet = cible;
            animation.stop();
        }
        appliquerCouleurs();
        repaint();
    }

    // Texte : doré pour la prochaine prière, atténué pour les prières passées,
    // et qui devient doré progressivement au survol
    private void appliquerCouleurs() {
        Color base = passee ? TEXTE_PASSE : Style.TEXTE;
        Color couleur = prochaine ? Style.OR : melanger(base, Style.OR, effet);
        nom.setForeground(couleur);
        heure.setForeground(couleur);
    }

    private static Color melanger(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    private static Color avecAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    // ---------- Dessin ----------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // La carte grandit au survol, rétrécit un peu au clic
        double marge = MARGE - 5 * effet + (pressee ? 2 : 0);
        double x = marge;
        double y = marge - 2 * effet;
        double l = getWidth() - 2 * marge;
        double h = getHeight() - 2 * marge;
        RoundRectangle2D.Double forme = new RoundRectangle2D.Double(x, y, l, h, ARRONDI, ARRONDI);

        // Lueur dorée (prochaine prière en permanence, autres cartes au survol)
        float lueur = prochaine ? Math.max(effet, 0.6f) : effet;
        if (lueur > 0f) {
            for (int i = 3; i >= 1; i--) {
                g2.setStroke(new BasicStroke(i * 3f));
                g2.setColor(avecAlpha(Style.OR, Math.round(20 * lueur * (4 - i))));
                g2.draw(forme);
            }
        }

        // Verre sombre + reflet clair en haut
        g2.setColor(avecAlpha(VERRE, Math.round(150 + 40 * effet)));
        g2.fill(forme);
        g2.setPaint(new GradientPaint(0f, (float) y, new Color(255, 255, 255, Math.round(40 + 25 * effet)),
                0f, (float) (y + h), new Color(255, 255, 255, 0)));
        g2.fill(forme);

        // Teinte dorée pour la prochaine prière
        if (prochaine) {
            g2.setColor(avecAlpha(Style.OR, 38));
            g2.fill(forme);
        }

        // Bordure fine : dorée si prochaine ou survolée, sinon blanche discrète
        float accent = prochaine ? Math.max(effet, 0.85f) : effet;
        Color bordure = melanger(new Color(255, 255, 255, 60), avecAlpha(Style.OR, 230), accent);
        g2.setStroke(new BasicStroke(prochaine ? 2f : 1.3f));
        g2.setColor(bordure);
        g2.draw(forme);

        // Petit trait doré en haut de la prochaine prière
        if (prochaine) {
            g2.setColor(Style.OR);
            int largeurTrait = 38;
            g2.fillRoundRect((int) (getWidth() / 2.0 - largeurTrait / 2.0), (int) y + 10, largeurTrait, 4, 4, 4);
        }

        // Fine ligne de séparation entre le nom et l'heure
        int milieu = (int) (y + h * 0.46);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(avecAlpha(Style.OR, Math.round(70 + 60 * effet)));
        g2.drawLine((int) (x + l * 0.28), milieu, (int) (x + l * 0.72), milieu);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void removeNotify() {
        animation.stop();
        super.removeNotify();
    }
}
