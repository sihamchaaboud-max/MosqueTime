package com.mosquetime.view;

import com.mosquetime.controller.EcranController;
import com.mosquetime.model.Horaire;
import com.mosquetime.model.Priere;
import com.mosquetime.service.ApparenceService;
import com.mosquetime.service.HoraireService;
import com.mosquetime.util.DateUtil;
import com.mosquetime.util.Style;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class NormalPanel extends JPanel {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FORMAT_HEURE =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_PRIERE =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final int DECALAGE_HIJRI_JOURS = 0;

    private final EcranController controller;
    private final HoraireService horaireService = new HoraireService();
    private Image fond = new ApparenceService().chargerFond();
    private final ContenuPanel contenuPanel = new ContenuPanel();

    private final JLabel labelMosquee = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelDate = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelDateHijri = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelHeure = new JLabel("", SwingConstants.CENTER);
    private final JLabel labelProchaine = new JLabel("", SwingConstants.CENTER);

    private final Map<Priere, JLabel> labelsNoms = new EnumMap<>(Priere.class);
    private final Map<Priere, JLabel> labelsHeures = new EnumMap<>(Priere.class);

    public NormalPanel(EcranController controller, String nomMosquee) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        labelMosquee.setText(nomMosquee);
        labelMosquee.setFont(Style.TITRE);
        labelMosquee.setForeground(Style.OR);

        labelDate.setFont(Style.MOYEN);
        labelDate.setForeground(Style.TEXTE);

        labelDateHijri.setFont(Style.PETIT);
        labelDateHijri.setForeground(Style.TEXTE);

        labelHeure.setFont(Style.GRAND);
        labelHeure.setForeground(Style.TEXTE);

        labelProchaine.setFont(Style.MOYEN);
        labelProchaine.setForeground(Style.OR);

        JPanel dates = new JPanel(new GridLayout(2, 1));
        dates.setOpaque(false);
        dates.add(labelDate);
        dates.add(labelDateHijri);

        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(labelMosquee, BorderLayout.NORTH);
        haut.add(dates, BorderLayout.CENTER);

        labelHeure.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajusterPoliceHeure();
            }
        });

        add(haut, BorderLayout.NORTH);
        add(construireCentre(), BorderLayout.CENTER);
        add(construireTableau(), BorderLayout.SOUTH);

        rafraichir();
    }

    public ContenuPanel getContenuPanel() {
        return contenuPanel;
    }

    public void setNomMosquee(String nom) {
        labelMosquee.setText(nom);
    }

    public void actualiserFond() {
        fond = new ApparenceService().chargerFond();
        repaint();
    }

    private JPanel construireCentre() {
        Carte pastille = new Carte(new BorderLayout(), 190);
        pastille.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        pastille.add(labelProchaine, BorderLayout.CENTER);

        JPanel ligneProchaine = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ligneProchaine.setOpaque(false);
        ligneProchaine.add(pastille);

        JPanel temps = new JPanel(new BorderLayout());
        temps.setOpaque(false);
        temps.add(labelHeure, BorderLayout.CENTER);
        temps.add(ligneProchaine, BorderLayout.SOUTH);

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

    public void rafraichir() {
        LocalDate aujourdhui = LocalDate.now();
        labelDate.setText(majuscule(aujourdhui.format(FORMAT_DATE)));
        labelDateHijri.setText(DateUtil.dateHijri(aujourdhui, DECALAGE_HIJRI_JOURS));
        labelHeure.setText(LocalTime.now().format(FORMAT_HEURE));

        Optional<Horaire> horaire = horaireService.horaireDuJour();
        Optional<Priere> prochaine = controller.getProchainePriere();

        Optional<EcranController.Alerte> alerte = controller.getAlerte();
        if (alerte.isPresent()) {
            labelProchaine.setText("Il reste " + alerte.get().minutes()
                    + " min pour la prière " + alerte.get().priere().getLibelle());
        } else if (prochaine.isPresent()) {
            LocalTime h = horaire.map(x -> x.getHeure(prochaine.get())).orElse(null);
            String texteHeure = (h == null) ? "" : "  " + h.format(FORMAT_PRIERE);
            labelProchaine.setText("Prochaine prière : "
                    + prochaine.get().getLibelle() + texteHeure);
        } else {
            labelProchaine.setText(texteFajrDemain());
        }

        for (Priere p : Priere.values()) {
            LocalTime h = horaire.map(x -> x.getHeure(p)).orElse(null);
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

    private String texteFajrDemain() {
        Optional<LocalTime> fajr = horaireService.heureFajrDemain();
        if (fajr.isEmpty()) {
            return "Plus de prière aujourd'hui";
        }
        LocalDateTime debut = LocalDateTime.of(LocalDate.now().plusDays(1), fajr.get());
        Duration reste = Duration.between(LocalDateTime.now(), debut);
        return Priere.FAJR.getLibelle() + " demain " + fajr.get().format(FORMAT_PRIERE)
                + "  ·  dans " + reste.toHours() + " h "
                + String.format("%02d", reste.toMinutesPart()) + " min";
    }

    private void ajusterPoliceHeure() {
        int taille = (int) (labelHeure.getHeight() / 1.4);
        taille = Math.max(36, Math.min(120, taille));
        if (labelHeure.getFont().getSize() != taille) {
            labelHeure.setFont(Style.GRAND.deriveFont((float) taille));
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