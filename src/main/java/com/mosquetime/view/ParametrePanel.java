package com.mosquetime.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mosquetime.model.Langue;
import com.mosquetime.model.Mosquee;
import com.mosquetime.service.MosqueeService;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

/**
 * Paramètres de l'utilisateur : langue (français, arabe, anglais),
 * mosquée affichée à l'écran et arrière-plan.
 *
 * La mosquée se CHOISIT dans la liste des mosquées déjà enregistrées
 * (mosquees.json) : l'utilisateur ne tape aucun nom. Le nom affiché suit la
 * langue choisie (les noms en français, arabe et anglais sont déjà dans les données).
 *
 * Chaque choix est appliqué tout de suite à tous les écrans et enregistré
 * pour le prochain lancement.
 */
public class ParametrePanel extends JPanel {

    private final JLabel titre = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel labelLangue = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel labelMosquee = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel labelFond = new JLabel(" ", SwingConstants.CENTER);
    private final SelecteurFond selecteurFond = new SelecteurFond();
    private final BoutonArrondi boutonFermer = new BoutonArrondi(" ");
    private final Map<Langue, BoutonArrondi> boutonsLangue = new EnumMap<>(Langue.class);

    // ----- Choix de la mosquée (absent si aucun MosqueeService n'est fourni) -----
    private final MosqueeService mosqueeService;
    private final Runnable surNomChange;                 // prévient l'écran principal (peut être null)
    private final List<Mosquee> mosquees = new ArrayList<>();
    private final List<BoutonArrondi> boutonsMosquee = new ArrayList<>();

    public ParametrePanel() {
        this(null, null, null);
    }

    // fermer : action du bouton « Fermer » (null = pas de bouton)
    public ParametrePanel(Runnable fermer) {
        this(fermer, null, null);
    }

    // mosqueeService : si fourni, la liste des mosquées est affichée
    // surNomChange   : appelé après le choix d'une mosquée (pour rafraîchir l'écran principal)
    public ParametrePanel(Runnable fermer, MosqueeService mosqueeService, Runnable surNomChange) {
        this.mosqueeService = mosqueeService;
        this.surNomChange = surNomChange;

        setLayout(new GridBagLayout());
        setBackground(Style.FOND);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        titre.setFont(Style.TITRE);
        titre.setForeground(Style.OR);

        for (JLabel l : new JLabel[] { labelLangue, labelMosquee, labelFond }) {
            l.setFont(Style.MOYEN);
            l.setForeground(Style.TEXTE);
        }

        JPanel choix = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        choix.setOpaque(false);

        ButtonGroup groupe = new ButtonGroup();
        for (Langue langue : Langue.values()) {
            BoutonArrondi bouton = new BoutonArrondi(langue.getNomAffiche());
            bouton.addActionListener(e -> I18n.changerLangue(langue));
            groupe.add(bouton);
            boutonsLangue.put(langue, bouton);
            choix.add(bouton);
        }

        boutonFermer.setVisible(fermer != null);
        if (fermer != null) {
            boutonFermer.addActionListener(e -> {
                boutonFermer.setSelected(false);
                fermer.run();
            });
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 18, 0); add(titre, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 12, 0); add(labelLangue, c);
        c.gridy = 2; c.insets = new Insets(0, 0, 22, 0); add(choix, c);

        // Liste des mosquées déjà enregistrées (seulement s'il y en a)
        JPanel listeMosquees = construireListeMosquees();
        if (!mosquees.isEmpty()) {
            c.gridy = 3; c.insets = new Insets(0, 0, 12, 0); add(labelMosquee, c);
            c.gridy = 4; c.insets = new Insets(0, 0, 22, 0); add(listeMosquees, c);
        }

        c.gridy = 5; c.insets = new Insets(0, 0, 12, 0); add(labelFond, c);
        c.gridy = 6; c.insets = new Insets(0, 0, 26, 0); add(selecteurFond, c);
        c.gridy = 7; c.insets = new Insets(0, 0, 0, 0); c.fill = GridBagConstraints.NONE;
        add(boutonFermer, c);

        appliquerLangue();
        I18n.ajouterEcouteur(this::appliquerLangue);
    }

    // ---------- Choix de la mosquée ----------

    // Un bouton par mosquée de la base ; la mosquée affichée est en doré
    private JPanel construireListeMosquees() {
        JPanel liste = new JPanel(new GridLayout(0, 1, 0, 8));
        liste.setOpaque(false);

        if (mosqueeService == null) {
            return liste;
        }
        mosquees.addAll(mosqueeService.getMosquees());

        Optional<Mosquee> active = mosqueeService.mosqueeActive();
        ButtonGroup groupe = new ButtonGroup();

        for (Mosquee m : mosquees) {
            BoutonArrondi bouton = new BoutonArrondi(" ");
            bouton.setPreferredSize(new Dimension(420, 46));
            bouton.setSelected(active.isPresent() && active.get().getNom().equals(m.getNom()));
            bouton.addActionListener(e -> choisirMosquee(m));
            groupe.add(bouton);
            boutonsMosquee.add(bouton);
            liste.add(bouton);
        }
        return liste;
    }

    private void choisirMosquee(Mosquee mosquee) {
        try {
            mosqueeService.choisir(mosquee.getNom());   // enregistré dans mosquees.json
            if (surNomChange != null) {
                surNomChange.run();
            }
        } catch (RuntimeException ex) {
            System.err.println("Mosquée non changée : " + ex.getMessage());
        }
    }

    // ---------- Langue ----------

    private void appliquerLangue() {
        applyComponentOrientation(I18n.orientation());

        titre.setText(I18n.t("parametres.titre"));
        labelLangue.setText(I18n.t("parametres.langue"));
        labelMosquee.setText(I18n.t("parametres.mosquee"));
        labelFond.setText(I18n.t("parametres.fond"));
        selecteurFond.recharger();
        boutonFermer.setText(I18n.t("parametres.fermer"));

        // Le nom de chaque mosquée s'affiche dans la langue choisie
        for (int i = 0; i < mosquees.size(); i++) {
            boutonsMosquee.get(i).setText(mosquees.get(i).getNom(I18n.getLangue()));
        }

        // La langue courante est mise en évidence (bouton doré)
        for (Map.Entry<Langue, BoutonArrondi> entree : boutonsLangue.entrySet()) {
            entree.getValue().setSelected(entree.getKey() == I18n.getLangue());
        }

        revalidate();
        repaint();
    }
}
