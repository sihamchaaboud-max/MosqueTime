
package com.mosquetime;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.mosquetime.controller.EcranController;
import com.mosquetime.controller.MosqueeController;

import com.mosquetime.model.Mosquee;
import com.mosquetime.model.Priere;

import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;

import com.mosquetime.service.HoraireService;
import com.mosquetime.service.MosqueeService;
import com.mosquetime.service.RotationService;

import com.mosquetime.util.Etat;
import com.mosquetime.util.SonAdhan;
import com.mosquetime.util.Style;
import com.mosquetime.util.JsonManager;

import com.mosquetime.view.AdhanPanel;
import com.mosquetime.view.AttentePanel;
import com.mosquetime.view.ContenuPanel;
import com.mosquetime.view.NormalPanel;
import com.mosquetime.view.AdminDashboard;
import com.mosquetime.view.MosqueePanel;
import com.mosquetime.view.ParametrePanel;

/**
 * Point d'entrée de MosqueTime.
 *
 * EcranController gère les états :
 * NORMAL, ADHAN et ATTENTE.
 *
 * Main change l'écran affiché et lance le son de l'adhan.
 */
public class Main {

    private static final String NORMAL = "NORMAL";
    private static final String ADHAN = "ADHAN";
    private static final String ATTENTE = "ATTENTE";

    private static final boolean PLEIN_ECRAN = false;

    private static final DateTimeFormatter FORMAT_HEURE =
            DateTimeFormatter.ofPattern("HH:mm");

    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneEcrans = new JPanel(cartes);

    private final EcranController controller = new EcranController();
    private final HoraireService horaireService = new HoraireService();

    // Service partagé entre l'écran principal et l'administration
    private final MosqueeService mosqueeService = new MosqueeService();

    private final NormalPanel normalPanel =
            new NormalPanel(controller, mosqueeService);

    private final AdhanPanel adhanPanel = new AdhanPanel();
    private final AttentePanel attentePanel = new AttentePanel();

    private final ContenuPanel bandeauBas = new ContenuPanel();
    private final JPanel zoneBas = new JPanel(new BorderLayout());

    private final SonAdhan sonAdhan = new SonAdhan();

    private RotationService rotation;

    // Fenêtre d'administration
    private AdminDashboard admin;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());

            } catch (Exception e) {
                e.printStackTrace();
            }

            new Main().ouvrirFenetre();
        });
    }

    private void ouvrirFenetre() {

        zoneEcrans.add(normalPanel, NORMAL);
        zoneEcrans.add(adhanPanel, ADHAN);
        zoneEcrans.add(attentePanel, ATTENTE);

        zoneBas.setBackground(Style.FOND);
        zoneBas.add(bandeauBas, BorderLayout.CENTER);
        zoneBas.setVisible(false);

        JFrame fenetre = new JFrame("MosqueTime");

        fenetre.setDefaultCloseOperation(
                WindowConstants.EXIT_ON_CLOSE);

        fenetre.setLayout(new BorderLayout());

        fenetre.add(zoneEcrans, BorderLayout.CENTER);
        fenetre.add(zoneBas, BorderLayout.SOUTH);

        fenetre.setMinimumSize(new Dimension(900, 600));
        fenetre.setSize(1280, 760);
        fenetre.setLocationRelativeTo(null);

        if (PLEIN_ECRAN) {

            fenetre.setUndecorated(true);
            fenetre.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        fenetre.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                arreterTout();
            }
        });

        // Touche Echap pour quitter l'application
        fenetre.getRootPane().registerKeyboardAction(

                e -> {
                    arreterTout();
                    System.exit(0);
                },

                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),

                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        controller.setSurMiseAJour(this::rafraichir);
        controller.setSurChangementEtat(this::surChangementEtat);

        // Rotation des versets et des invocations
        rotation = new RotationService(

                new VersetRepository(),
                new InvocationRepository(),

                v -> {
                    normalPanel.getContenuPanel().afficherVerset(v);
                    bandeauBas.afficherVerset(v);
                },

                i -> {
                    normalPanel.getContenuPanel().afficherInvocation(i);
                    bandeauBas.afficherInvocation(i);
                }
        );

        cartes.show(zoneEcrans, NORMAL);

        fenetre.setVisible(true);

        controller.demarrer();
        rotation.demarrer();
    }

    /**
     * Ouvre la fenêtre d'administration.
     */
    private void ouvrirAdmin() {

        if (admin != null && admin.isDisplayable()) {
            admin.toFront();
            return;
        }

        admin = new AdminDashboard();

        admin.ajouterOnglet(
                "Mosquées",
                new MosqueePanel(
                        new MosqueeController(mosqueeService),
                        normalPanel::setNomMosquee
                )
        );

        admin.ajouterOnglet(
                "Paramètres",
                new ParametrePanel()
        );

        admin.setVisible(true);
    }

    /**
     * Rafraîchit l'écran principal.
     */
    private void rafraichir() {

        normalPanel.rafraichir();

        if (controller.getEtat() == Etat.ATTENTE) {

            attentePanel.setTempsRestant(
                    (int) controller.getSecondesRestantes()
            );
        }
    }

    /**
     * Retourne le nom arabe de la prière.
     */
    private static String obtenirNomArabe(Priere priere) {

        return switch (priere.getLibelle().toUpperCase()) {

            case "FAJR" -> "الفجر";
            case "DOHR" -> "الظهر";
            case "ASR" -> "العصر";
            case "MAGHRIB" -> "المغرب";
            case "ICHA" -> "العشاء";

            default -> priere.getLibelle();
        };
    }

    /**
     * Gère les changements d'état de l'application.
     */
    private void surChangementEtat(Etat etat) {

        Priere priere = controller.getPriereCourante();

        switch (etat) {

            case NORMAL -> {

                sonAdhan.arreter();

                zoneBas.setVisible(false);

                cartes.show(zoneEcrans, NORMAL);
            }

            case ADHAN -> {

                String nomFr = priere.getLibelle();
                String nomAr = obtenirNomArabe(priere);

                adhanPanel.setPriere(
                        nomFr,
                        nomAr,
                        heureDe(priere)
                );

                zoneBas.setVisible(true);

                cartes.show(zoneEcrans, ADHAN);

                sonAdhan.jouer(controller::terminerAdhan);
            }

            case ATTENTE -> {

                sonAdhan.arreter();

                attentePanel.setPriere(priere.getLibelle());

                attentePanel.setTempsRestant(
                        (int) controller.getSecondesRestantes()
                );

                zoneBas.setVisible(true);

                cartes.show(zoneEcrans, ATTENTE);
            }
        }
    }

    /**
     * Récupère l'heure de la prière.
     */
    private String heureDe(Priere priere) {

        return horaireService.horaireDuJour()
                .map(h -> h.getHeure(priere))
                .map(t -> t.format(FORMAT_HEURE))
                .orElse("");
    }

    /**
     * Arrête les services de l'application.
     */
    private void arreterTout() {

        controller.arreter();

        sonAdhan.arreter();

        if (rotation != null) {
            rotation.arreter();
        }
    }

    /**
     * Lit le nom de la mosquée depuis le fichier JSON.
     */
    private static String lireNomMosquee() {

        try {

            Mosquee mosquee =
                    JsonManager.lire("mosquee.json", Mosquee.class);

            if (mosquee != null
                    && mosquee.getNom() != null
                    && !mosquee.getNom().isBlank()) {

                return mosquee.getNom();
            }

        } catch (RuntimeException e) {

            System.err.println(
                    "mosquee.json illisible : " + e.getMessage()
            );
        }

        return "MosqueTime";
    }
}
