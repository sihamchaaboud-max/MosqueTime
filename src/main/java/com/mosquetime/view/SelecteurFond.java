package com.mosquetime.view;

import com.mosquetime.service.ApparenceService;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Choix de l'arrière-plan, pour tout le monde : une grille de vignettes.
 * Un clic sur une vignette change tout de suite le fond de tous les écrans
 * et le choix est enregistré (apparence.json). Le bouton « + » permet d'ajouter
 * sa propre image (.jpg ou .png).
 */
public class SelecteurFond extends JPanel {

    private final ApparenceService service = new ApparenceService();
    private final JPanel grille = new JPanel(new GridLayout(0, 4, 12, 12));

    public SelecteurFond() {
        setOpaque(false);
        setLayout(new BorderLayout());
        grille.setOpaque(false);
        add(grille, BorderLayout.CENTER);
        recharger();
    }

    // Reconstruit les vignettes : au démarrage, après un ajout d'image,
    // et à chaque changement de langue (les infobulles changent).
    public void recharger() {
        grille.removeAll();
        String actuel = service.getFond();

        grille.add(new Tuile(null, I18n.t("parametres.fond_aucun"), actuel.isEmpty(),
                () -> choisir("")));

        for (String nom : service.fondsDisponibles()) {
            grille.add(new Tuile(service.chargerImage(nom), nomAffiche(nom),
                    nom.equals(actuel), () -> choisir(nom)));
        }

        Tuile ajout = new Tuile(null, I18n.t("parametres.fond_ajouter"), false, this::ajouterImage);
        ajout.estAjout = true;
        grille.add(ajout);

        grille.revalidate();
        grille.repaint();
    }

    private void choisir(String nom) {
        try {
            service.choisirFond(nom);
            FondEcran.rafraichirTout();
            recharger();
        } catch (IllegalArgumentException e) {
            erreur(e.getMessage());
        }
    }

    private void ajouterImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images (jpg, png)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String nom = service.ajouterImage(chooser.getSelectedFile());
            choisir(nom);
            Window fenetre = SwingUtilities.getWindowAncestor(this);
            if (fenetre != null) {
                fenetre.pack();
            }
        } catch (IllegalArgumentException | IOException e) {
            erreur(e.getMessage());
        }
    }

    // Nom lisible d'un fond : traduit pour les fonds intégrés, nom du fichier sinon
    private String nomAffiche(String nomFichier) {
        String texte = I18n.t("fond." + nomFichier);
        return texte.startsWith("!") ? nomFichier : texte;
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, I18n.t("parametres.fond"),
                JOptionPane.WARNING_MESSAGE);
    }

    // Réduit une image à la taille d'une vignette (en remplissant tout le cadre)
    private static BufferedImage miniature(Image source, int largeur, int hauteur) {
        BufferedImage mini = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mini.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double echelle = Math.max((double) largeur / source.getWidth(null),
                (double) hauteur / source.getHeight(null));
        int w = (int) (source.getWidth(null) * echelle);
        int h = (int) (source.getHeight(null) * echelle);
        g2.drawImage(source, (largeur - w) / 2, (hauteur - h) / 2, w, h, null);
        g2.dispose();
        return mini;
    }

    // Une vignette : image réduite, bordure dorée si c'est le fond actuel
    private static class Tuile extends JComponent {

        private static final int LARGEUR = 116;
        private static final int HAUTEUR = 66;

        private final BufferedImage miniature;
        private final String texte;
        private final boolean choisie;
        boolean estAjout;

        Tuile(Image image, String infobulle, boolean choisie, Runnable action) {
            this.miniature = (image == null) ? null : miniature(image, LARGEUR, HAUTEUR);
            this.texte = infobulle;
            this.choisie = choisie;

            setPreferredSize(new Dimension(LARGEUR, HAUTEUR));
            setToolTipText(infobulle);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int l = getWidth();
            int h = getHeight();
            RoundRectangle2D forme = new RoundRectangle2D.Float(1, 1, l - 3, h - 3, 14, 14);

            g2.setClip(forme);
            if (miniature != null) {
                g2.drawImage(miniature, 0, 0, l, h, null);
            } else {
                g2.setColor(estAjout ? new Color(255, 255, 255, 18) : Style.FOND);
                g2.fill(forme);
                g2.setColor(Style.TEXTE);
                if (estAjout) {
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(l / 2 - 10, h / 2, l / 2 + 10, h / 2);
                    g2.drawLine(l / 2, h / 2 - 10, l / 2, h / 2 + 10);
                } else {
                    g2.setFont(Style.PETIT.deriveFont(12f));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(texte, (l - fm.stringWidth(texte)) / 2,
                            (h + fm.getAscent() - fm.getDescent()) / 2);
                }
            }
            g2.setClip(null);

            g2.setStroke(new BasicStroke(choisie ? 3.5f : 1.2f));
            g2.setColor(choisie ? Style.OR : new Color(255, 255, 255, 90));
            g2.draw(forme);
            g2.dispose();
        }
    }
}
