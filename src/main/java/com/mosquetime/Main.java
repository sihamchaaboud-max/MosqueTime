package com.mosquetime;

import com.mosquetime.controller.EcranController;
import com.mosquetime.controller.MosqueeController;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;
import com.mosquetime.service.ApparenceService;
import com.mosquetime.service.HoraireService;
import com.mosquetime.service.MosqueeService;
import com.mosquetime.service.RotationService;
import com.mosquetime.util.Etat;
import com.mosquetime.util.SonAdhan;
import com.mosquetime.util.Style;
import com.mosquetime.view.AdhanPanel;
import com.mosquetime.view.AdminDashboard;
import com.mosquetime.view.ApparencePanel;
import com.mosquetime.view.AttentePanel;
import com.mosquetime.view.ContenuPanel;
import com.mosquetime.view.MosqueePanel;
import com.mosquetime.view.NormalPanel;
import com.mosquetime.view.PrierePanel;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;

/**
 * Point d'entrée de MosqueTime.
 * EcranController décide de l'état (NORMAL, ADHAN, ATTENTE, PRIERE) ;
 * Main change d'écran en conséquence et lance le son de l'adhan.
 */
public class Main {

    private static final String NORMAL = "NORMAL";
    private static final String ADHAN = "ADHAN";
    private static final String ATTENTE = "ATTENTE";
    private static final String PRIERE = "PRIERE";

    private static final boolean PLEIN_ECRAN = false;
    private static final boolean MODE_DEMO = true;

    private static final DateTimeFormatter FORMAT_HEURE =
            DateTimeFormatter.ofPattern("HH:mm");

    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneEcrans = new JPanel(cartes);

    private final EcranController controller = new EcranController();
    private final HoraireService horaireService = new HoraireService();

    private final NormalPanel normalPanel = new NormalPanel(controller, lireNomMosquee());
    private final AdhanPanel adhanPanel = new AdhanPanel();
    private final AttentePanel attentePanel = new AttentePanel();
    private final PrierePanel prierePanel = new PrierePanel();

    private final ContenuPanel bandeauBas = new ContenuPanel();
    private final JPanel zoneBas = new JPanel(new BorderLayout());

    private final SonAdhan sonAdhan = new SonAdhan();
    private RotationService rotation;
    private AdminDashboard admin;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
        zoneEcrans.add(prierePanel, PRIERE);

        zoneBas.setBackground(Style.FOND);
        zoneBas.add(bandeauBas, BorderLayout.CENTER);
        zoneBas.setVisible(false);

        JFrame fenetre = new JFrame("MosqueTime");
        fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fenetre.setLayout(new BorderLayout());
        fenetre.add(zoneEcrans, BorderLayout.CENTER);
        fenetre.add(zoneBas, BorderLayout.SOUTH);
        fenetre.setMinimumSize(new Dimension(900, 680));
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

        fenetre.getRootPane().registerKeyboardAction(
                e -> {
                    arreterTout();
                    System.exit(0);
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        fenetre.getRootPane().registerKeyboardAction(
                e -> ouvrirAdmin(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (MODE_DEMO) {
            fenetre.getRootPane().registerKeyboardAction(
                    e -> controller.declencherAdhan(Priere.DHUHR),
                    KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            fenetre.getRootPane().registerKeyboardAction(
                    e -> controller.declencherPriere(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }

        controller.setSurMiseAJour(this::rafraichir);
        controller.setSurChangementEtat(this::surChangementEtat);

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
                });

        cartes.show(zoneEcrans, NORMAL);
        fenetre.setVisible(true);

        controller.demarrer();
        rotation.demarrer();
    }

    private void ouvrirAdmin() {
        if (admin != null && admin.isDisplayable()) {
            admin.toFront();
            return;
        }
        admin = new AdminDashboard();
        admin.ajouterOnglet("Mosquées",
                new MosqueePanel(new MosqueeController(), normalPanel::setNomMosquee));
        admin.ajouterOnglet("Arrière-plan",
                new ApparencePanel(new ApparenceService(), normalPanel::actualiserFond));
        admin.setVisible(true);
    }

    private void rafraichir() {
        normalPanel.rafraichir();
        if (controller.getEtat() == Etat.ATTENTE) {
            attentePanel.setTempsRestant((int) controller.getSecondesRestantes());
        }
    }

    private void surChangementEtat(Etat etat) {
        Priere priere = controller.getPriereCourante();

        switch (etat) {
            case NORMAL -> {
                sonAdhan.arreter();
                zoneBas.setVisible(false);
                cartes.show(zoneEcrans, NORMAL);
            }
            case ADHAN -> {
                adhanPanel.setPriere(priere.getLibelle(), heureDe(priere));
                zoneBas.setVisible(true);
                cartes.show(zoneEcrans, ADHAN);
                sonAdhan.jouer(controller::terminerAdhan);
            }
            case ATTENTE -> {
                sonAdhan.arreter();
                attentePanel.setPriere(priere.getLibelle());
                attentePanel.setTempsRestant((int) controller.getSecondesRestantes());
                zoneBas.setVisible(true);
                cartes.show(zoneEcrans, ATTENTE);
            }
            case PRIERE -> {
                sonAdhan.arreter();
                zoneBas.setVisible(false);
                cartes.show(zoneEcrans, PRIERE);
            }
        }
    }

    private String heureDe(Priere priere) {
        return horaireService.horaireDuJour()
                .map(h -> h.getHeure(priere))
                .map(t -> t.format(FORMAT_HEURE))
                .orElse("");
    }

    private void arreterTout() {
        controller.arreter();
        sonAdhan.arreter();
        if (rotation != null) {
            rotation.arreter();
        }
    }

    private static String lireNomMosquee() {
        try {
            return new MosqueeService().nomMosqueeActive();
        } catch (RuntimeException e) {
            System.err.println("mosquees.json illisible : " + e.getMessage());
            return "MosqueTime";
        }
    }
}