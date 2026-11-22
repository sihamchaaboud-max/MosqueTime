package com.mosquetime.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Écran de prière (état PRIERE) : affiché pendant la prière, après l'iqama.
 * Il demande de ne pas utiliser le téléphone et de le mettre en silence.
 * Ce panneau ne calcule rien : le contrôleur décide quand l'afficher.
 */
public class PrierePanel extends JPanel {

    private static final Color FOND = new Color(0x0B3D2E);
    private static final Color OR = new Color(0xE8C766);
    private static final Color ROUGE = new Color(0xE5484D);

    private static final String MESSAGE = "يُمنع استعمال الهاتف أثناء الصلاة";
    private static final String CONSIGNE = "المرجو إغلاق الهاتف أو وضعه في الوضع الصامت";

    private final JLabel message = new JLabel(MESSAGE, SwingConstants.CENTER);
    private final JLabel consigne = new JLabel(CONSIGNE, SwingConstants.CENTER);
    private final IconeTelephone icone = new IconeTelephone();

    public PrierePanel() {

        setLayout(new GridBagLayout());
        setBackground(FOND);

        message.setFont(new Font("SansSerif", Font.BOLD, 52));
        message.setForeground(Color.WHITE);

        consigne.setFont(new Font("SansSerif", Font.PLAIN, 38));
        consigne.setForeground(OR);

        icone.setPreferredSize(new Dimension(200, 200));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.insets = new Insets(10, 20, 10, 20);

        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(icone, c);

        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 1; add(message, c);
        c.gridy = 2; add(consigne, c);
    }

    // Dessine un téléphone barré d'une croix rouge, à la taille disponible
    private static class IconeTelephone extends JComponent {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int hauteur = (int) Math.min(getHeight() * 0.95, 360);
            int largeur = (int) (hauteur * 0.52);
            int x = (getWidth() - largeur) / 2;
            int y = (getHeight() - hauteur) / 2;
            int arrondi = largeur / 3;

            // corps du téléphone
            g2.setColor(new Color(0xF2F2F2));
            g2.fillRoundRect(x, y, largeur, hauteur, arrondi, arrondi);

            // écran
            int marge = largeur / 12;
            int hautEcran = y + hauteur / 9;
            int basEcran = y + hauteur - hauteur / 7;
            g2.setColor(new Color(0x1B2B34));
            g2.fillRoundRect(x + marge, hautEcran, largeur - 2 * marge,
                    basEcran - hautEcran, arrondi / 3, arrondi / 3);

            // haut-parleur et bouton
            g2.setColor(new Color(0x9AA5AB));
            int hpLargeur = largeur / 4;
            g2.fillRoundRect(x + (largeur - hpLargeur) / 2, y + hauteur / 18,
                    hpLargeur, Math.max(4, hauteur / 70), 6, 6);
            int bouton = Math.max(10, largeur / 8);
            g2.fillOval(x + (largeur - bouton) / 2,
                    basEcran + (y + hauteur - basEcran - bouton) / 2, bouton, bouton);

            // croix rouge par-dessus
            g2.setColor(ROUGE);
            g2.setStroke(new BasicStroke(Math.max(8f, largeur / 7f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + largeur / 2;
            int cy = y + hauteur / 2;
            int r = (int) (largeur * 0.6);
            g2.drawLine(cx - r, cy - r, cx + r, cy + r);
            g2.drawLine(cx - r, cy + r, cx + r, cy - r);

            g2.dispose();
        }
    }
}