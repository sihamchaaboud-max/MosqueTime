package com.mosquetime.view;

import com.mosquetime.controller.EcranController;
import com.mosquetime.model.Horaire;
import com.mosquetime.model.Priere;
import com.mosquetime.service.HoraireService;
import com.mosquetime.service.MosqueeService;
import com.mosquetime.util.I18n;
import com.mosquetime.util.Style;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class NormalPanel extends JPanel {

    private static final DateTimeFormatter FORMAT_HEURE =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_PRIERE =
            DateTimeFormatter.ofPattern("HH:mm");

    private final EcranController controller;
    private final HoraireService horaireService = new HoraireService();
    private final Image fond = chargerFond();
    private final ContenuPanel contenuPanel = new ContenuPanel();

    private final JLabel labelMosquee = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelDate = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelHeure = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelProchaine = new JLabel("", SwingConstants.CENTER);
    private final BoutonParametres boutonParametres = new BoutonParametres();

    private final Map<Priere, JLabel> labelsNoms = new EnumMap<>(Priere.class);
    private final Map<Priere, JLabel> labelsHeures = new EnumMap<>(Priere.class);

    private final MosqueeService mosqueeService;   // nom de la mosquée en 3 langues (peut être null)
    private String nomFixe;                        // utilisé si mosqueeService est null

    // Nom de mosquée fixe (utilisé par TestEcran)
    public NormalPanel(EcranController controller, String nomMosquee) {
        this(controller, null, nomMosquee);
    }

    // Le nom affiché est celui de la mosquée active, dans la langue choisie
    public NormalPanel(EcranController controller, MosqueeService mosqueeService) {
        this(controller, mosqueeService, "MosqueTime");
    }

    private NormalPanel(EcranController controller, MosqueeService mosqueeService,
            String nomMosquee) {
        this.controller = controller;
        this.mosqueeService = mosqueeService;
        this.nomFixe = nomMosquee;

        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        labelMosquee.setText(nomAffiche());
        labelMosquee.setFont(Style.TITRE);
        labelMosquee.setForeground(Style.OR);

        labelDate.setFont(Style.MOYEN);
        labelDate.setForeground(Style.TEXTE);

        labelHeure.setFont(Style.GRAND);
        labelHeure.setForeground(Style.TEXTE);

        labelProchaine.setFont(Style.MOYEN);
        labelProchaine.setForeground(Style.OR);

        JPanel haut = new JPanel(new GridLayout(2, 1));
        haut.setOpaque(false);
        haut.add(labelMosquee);
        haut.add(labelDate);

        // Bouton « Paramètres » (roue dentée) : à droite, ou à gauche en arabe.
        // Un espace de même taille de l'autre côté garde le titre bien centré.
        boutonParametres.addActionListener(e -> ouvrirParametres());

        JPanel boutonZone = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boutonZone.setOpaque(false);
        boutonZone.add(boutonParametres);

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(haut, BorderLayout.CENTER);
        entete.add(boutonZone, BorderLayout.LINE_END);
        entete.add(Box.createRigidArea(new Dimension(BoutonParametres.TAILLE + 8, 1)),
                BorderLayout.LINE_START);

        add(entete, BorderLayout.NORTH);
        add(construireCentre(), BorderLayout.CENTER);
        add(construireTableau(), BorderLayout.SOUTH);

        appliquerLangue();
        I18n.ajouterEcouteur(this::appliquerLangue);
    }

    public ContenuPanel getContenuPanel() {
        return contenuPanel;
    }

    // Appelé par l'administration quand une autre mosquée est choisie
    // (ou quand ses noms traduits changent) : on relit le nom dans la langue courante
    public void setNomMosquee(String nom) {
        if (mosqueeService == null) {
            nomFixe = nom;
        }
        labelMosquee.setText(nomAffiche());
    }

    // Nom de la mosquée dans la langue courante (repli : nom français)
    private String nomAffiche() {
        return (mosqueeService != null)
                ? mosqueeService.nomMosqueeActive(I18n.getLangue())
                : nomFixe;
    }

    // Appelé par l'administration quand un autre arrière-plan est choisi
    public void actualiserFond() {
        FondEcran.rafraichirTout();
    }

    private JPanel construireCentre() {
        Carte pastille = new Carte(new BorderLayout(), 190);
        pastille.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        pastille.add(labelProchaine, BorderLayout.CENTER);

        JPanel ligneProchaine = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ligneProchaine.setOpaque(false);
        ligneProchaine.add(pastille);

        JPanel temps = new JPanel(new GridLayout(2, 1));
        temps.setOpaque(false);
        temps.add(labelHeure);
        temps.add(ligneProchaine);

        Carte contenu = new Carte(new BorderLayout(), 190);
        contenu.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        contenu.add(contenuPanel, BorderLayout.CENTER);

        JPanel centre = new JPanel(new BorderLayout(0, 15));
        centre.setOpaque(false);
        centre.add(temps, BorderLayout.CENTER);
        centre.add(contenu, BorderLayout.SOUTH);
        return centre;
    }

    private JPanel construireTableau() {
        JPanel tableau = new JPanel(new GridLayout(1, 6, 12, 0));
        tableau.setOpaque(false);

        for (Priere p : Priere.values()) {
            JLabel nom = new JLabel(p.getLibelle(), SwingConstants.CENTER);
            nom.setFont(Style.PETIT);

            JLabel heure = new JLabel("--:--", SwingConstants.CENTER);
            heure.setFont(Style.MOYEN);

            labelsNoms.put(p, nom);
            labelsHeures.put(p, heure);

            Carte carte = new Carte(new GridLayout(2, 1), 190);
            carte.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));
            carte.add(nom);
            carte.add(heure);
            tableau.add(carte);
        }
        return tableau;
    }

    // Appelé au démarrage et à chaque changement de langue
    private void appliquerLangue() {
        labelMosquee.setText(nomAffiche());
        applyComponentOrientation(I18n.orientation());

        // Les versets et invocations sont en arabe : toujours de droite à gauche
        contenuPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        boutonParametres.setToolTipText(I18n.t("parametres.titre"));
        rafraichir();
        revalidate();
        repaint();
    }

    private void ouvrirParametres() {
        Window fenetre = SwingUtilities.getWindowAncestor(this);
        JDialog boite = new JDialog(fenetre, I18n.t("parametres.titre"),
                Dialog.ModalityType.APPLICATION_MODAL);
        boite.setContentPane(new ParametrePanel(boite::dispose));
        boite.pack();
        boite.setMinimumSize(new Dimension(520, 280));
        boite.setLocationRelativeTo(this);
        boite.setVisible(true);
    }

    public void rafraichir() {
        DateTimeFormatter formatDate =
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", I18n.getLocale());
        labelDate.setText(majuscule(LocalDate.now().format(formatDate)));
        labelHeure.setText(LocalTime.now().format(FORMAT_HEURE));

        Optional<Horaire> horaire = horaireService.horaireDuJour();
        Optional<Priere> prochaine = controller.getProchainePriere();

        if (prochaine.isPresent()) {
            LocalTime h = horaire.map(x -> x.getHeure(prochaine.get())).orElse(null);
            String texteHeure = (h == null) ? "" : h.format(FORMAT_PRIERE);
            labelProchaine.setText(I18n.t("normal.prochaine",
                    I18n.nomPriere(prochaine.get()), texteHeure));
        } else {
            labelProchaine.setText(I18n.t("normal.aucune"));
        }

        for (Priere p : Priere.values()) {
            LocalTime h = horaire.map(x -> x.getHeure(p)).orElse(null);
            labelsNoms.get(p).setText(I18n.nomPriere(p));
            labelsHeures.get(p).setText(h == null ? "--:--" : h.format(FORMAT_PRIERE));

            boolean estProchaine = prochaine.isPresent() && prochaine.get() == p;
            Color couleur = estProchaine ? Style.OR : Style.TEXTE;
            labelsNoms.get(p).setForeground(couleur);
            labelsHeures.get(p).setForeground(couleur);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (fond != null) {
            double echelle = Math.max(
                    (double) getWidth() / fond.getWidth(null),
                    (double) getHeight() / fond.getHeight(null));
            int w = (int) (fond.getWidth(null) * echelle);
            int h = (int) (fond.getHeight(null) * echelle);
            g2.drawImage(fond, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        } else {
            g2.setColor(Style.FOND);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private static Image chargerFond() {
        try {
            URL url = NormalPanel.class.getResource("/images/mosqimg.jpg");
            if (url == null) {
                return null;
            }
            return ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }
    }

    private String majuscule(String texte) {
        return texte.substring(0, 1).toUpperCase() + texte.substring(1);
    }

    private static class Carte extends JPanel {
        private final int alpha;

        Carte(LayoutManager layout, int alpha) {
            super(layout);
            this.alpha = alpha;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(Style.CARTE.getRed(), Style.CARTE.getGreen(),
                    Style.CARTE.getBlue(), alpha));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}