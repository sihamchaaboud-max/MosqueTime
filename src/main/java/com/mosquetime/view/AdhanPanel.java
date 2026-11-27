package com.mosquetime.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.mosquetime.model.Priere;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

/**
 * Écran de l'ADHAN (même style que NormalPanel).
 * Les textes suivent la langue choisie (français, arabe, anglais) et la
 * prière affichée est retraduite si la langue change pendant l'adhan.
 */
public class AdhanPanel extends JPanel {

    private static final Color OR_PALE = new Color(0x9C7B35);


    private final JLabel titre = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel nomPriere = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel heure = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel sousTitre = new JLabel(" ", SwingConstants.CENTER);

    // Verset et invocation en bas de l'écran (fond transparent : on voit l'arrière-plan)
    private final ContenuPanel contenuPanel = new ContenuPanel();

    private final Timer clignotement;
    private boolean clair = true;

    // Prière affichée (pour pouvoir la retraduire)
    private Priere priere;
    private String nomLibre = "";          // repli si la prière est inconnue

    public AdhanPanel() {

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        titre.setForeground(Style.OR);

        nomPriere.setFont(new Font("SansSerif", Font.BOLD, 90));
        nomPriere.setForeground(Style.TEXTE);

        heure.setFont(new Font("SansSerif", Font.BOLD, 56));
        heure.setForeground(Style.OR);

        sousTitre.setFont(Style.MOYEN);
        sousTitre.setForeground(new Color(0xCBD5E1));

        // Panneau transparent : les textes s'écrivent directement sur l'arrière-plan
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(false);
        carte.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 8, 0);

        c.gridy = 0; carte.add(titre, c);
        c.gridy = 1; carte.add(nomPriere, c);
        c.gridy = 2; carte.add(heure, c);
        c.gridy = 3; carte.add(sousTitre, c);

        GridBagConstraints centre = new GridBagConstraints();
        centre.weightx = 1;
        centre.fill = GridBagConstraints.HORIZONTAL;
        JPanel zoneCentre = new JPanel(new GridBagLayout());
        zoneCentre.setOpaque(false);
        zoneCentre.add(carte, centre);

        add(zoneCentre, BorderLayout.CENTER);
        add(contenuPanel, BorderLayout.SOUTH);

        clignotement = new Timer(800, e -> {
            clair = !clair;
            titre.setForeground(clair ? Style.OR : OR_PALE);
        });

        appliquerLangue();
        I18n.ajouterEcouteur(this::appliquerLangue);
    }

    public ContenuPanel getContenuPanel() {
        return contenuPanel;
    }

    // Prière en cours : le nom s'affiche dans la langue choisie
    public void setPriere(Priere priere, String heureTexte) {
        this.priere = priere;
        this.nomLibre = "";
        heure.setText(heureTexte);
        appliquerLangue();
    }

    // Compatibilité avec l'ancien appel setPriere(nomFr, nomAr, heure)
    public void setPriere(String nomFr, String nomAr, String heureTexte) {
        Priere trouvee = trouver(nomFr);
        if (trouvee != null) {
            setPriere(trouvee, heureTexte);
        } else {
            this.priere = null;
            this.nomLibre = nomFr;
            heure.setText(heureTexte);
            appliquerLangue();
        }
    }

    private static Priere trouver(String nom) {
        for (Priere p : Priere.values()) {
            if (p.getLibelle().equalsIgnoreCase(nom) || p.name().equalsIgnoreCase(nom)) {
                return p;
            }
        }
        return null;
    }

    private void appliquerLangue() {
        applyComponentOrientation(I18n.orientation());
        // Versets et invocations : toujours de droite à gauche
        contenuPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        boolean arabe = I18n.estDroiteAGauche();
        titre.setFont(arabe
                ? new Font("Traditional Arabic", Font.BOLD, 62)
                : new Font("SansSerif", Font.BOLD, 44));

        titre.setText(I18n.t("adhan.titre"));
        sousTitre.setText(I18n.t("adhan.sous_titre"));
        nomPriere.setText(priere != null ? I18n.nomPriere(priere) : nomLibre);

        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        FondEcran.peindre(g, FondEcran.charger(), getWidth(), getHeight());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        clignotement.start();
    }

    @Override
    public void removeNotify() {
        clignotement.stop();
        super.removeNotify();
    }
}
