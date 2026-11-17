package com.mosquetime;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.mosquetime.controller.EcranController;
import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;
import com.mosquetime.service.RotationService;
import com.mosquetime.util.SonAdhan;
import com.mosquetime.util.Style;
import com.mosquetime.view.AdhanPanel;
import com.mosquetime.view.AttentePanel;
import com.mosquetime.view.ContenuPanel;
import com.mosquetime.view.NormalPanel;

/**
 * Banc de test : affiche chaque écran séparément, sans attendre l'heure
 * d'une prière.
 *
 *   NORMAL   : l'écran principal (date, heure, tableau des 6 prières)
 *   ADHAN    : l'écran de l'adhan pour la prière choisie
 *   ATTENTE  : le compte à rebours (durée réglable en secondes)
 *   Son      : joue / arrête adhan.wav
 *
 * Lancer : mvn compile exec:java "-Dexec.mainClass=com.mosquetime.TestEcran"
 */
public class TestEcran {

    private static final String NORMAL = "NORMAL";
    private static final String ADHAN = "ADHAN";
    private static final String ATTENTE = "ATTENTE";

    // Prières du test : nom français, nom arabe, heure affichée
    private static final String[][] PRIERES = {
            {"FAJR", "الفجر", "05:45"},
            {"DOHR", "الظهر", "12:30"},
            {"ASR", "العصر", "15:45"},
            {"MAGHRIB", "المغرب", "18:20"},
            {"ICHA", "العشاء", "19:45"}
    };

    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneEcrans = new JPanel(cartes);

    // Le contrôleur n'est PAS démarré : on teste seulement l'affichage
    private final EcranController controller = new EcranController();
    private final NormalPanel normal = new NormalPanel(controller, "Mosquée Test");
    private final AdhanPanel adhan = new AdhanPanel();
    private final AttentePanel attente = new AttentePanel();

    private final ContenuPanel bandeauBas = new ContenuPanel();
    private final JPanel zoneBas = new JPanel(new BorderLayout());

    private final SonAdhan son = new SonAdhan();
    private RotationService rotation;

    private Timer timerAttente;
    private int secondesRestantes;

    private final JComboBox<String> choixPriere = new JComboBox<>(nomsPrieres());
    private final JSpinner choixSecondes =
            new JSpinner(new SpinnerNumberModel(30, 5, 15 * 60, 5));
    private final JLabel etat = new JLabel("Écran : NORMAL");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestEcran().ouvrir());
    }

    private void ouvrir() {

        zoneEcrans.add(normal, NORMAL);
        zoneEcrans.add(adhan, ADHAN);
        zoneEcrans.add(attente, ATTENTE);

        zoneBas.setBackground(Style.FOND);
        zoneBas.add(bandeauBas, BorderLayout.CENTER);
        zoneBas.setVisible(false);

        choixPriere.setSelectedItem("DOHR");

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        barre.add(bouton("NORMAL", e -> afficherNormal()));
        barre.add(new JLabel("Prière :"));
        barre.add(choixPriere);
        barre.add(bouton("ADHAN", e -> afficherAdhan()));
        barre.add(new JLabel("Attente (s) :"));
        barre.add(choixSecondes);
        barre.add(bouton("ATTENTE", e -> afficherAttente()));
        barre.add(bouton("Son ▶", e -> son.jouer(null)));
        barre.add(bouton("Son ■", e -> son.arreter()));
        barre.add(etat);

        JFrame fenetre = new JFrame("MosqueTime - Test des écrans");
        fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fenetre.setLayout(new BorderLayout());
        fenetre.add(barre, BorderLayout.NORTH);
        fenetre.add(zoneEcrans, BorderLayout.CENTER);
        fenetre.add(zoneBas, BorderLayout.SOUTH);
        fenetre.setSize(1280, 800);
        fenetre.setLocationRelativeTo(null);

        // Le bandeau verset/invocation tourne sur les deux bandeaux
        rotation = new RotationService(
                new VersetRepository(),
                new InvocationRepository(),
                v -> {
                    normal.getContenuPanel().afficherVerset(v);
                    bandeauBas.afficherVerset(v);
                },
                i -> {
                    normal.getContenuPanel().afficherInvocation(i);
                    bandeauBas.afficherInvocation(i);
                });

        // L'horloge de l'écran NORMAL avance chaque seconde
        new Timer(1000, e -> normal.rafraichir()).start();

        fenetre.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                arreterTout();
            }
        });

        fenetre.setVisible(true);
        rotation.demarrer();
    }

    // ---------- Les trois tests ----------

    private void afficherNormal() {
        arreterAttente();
        zoneBas.setVisible(false);
        cartes.show(zoneEcrans, NORMAL);
        etat.setText("Écran : NORMAL");
    }

    private void afficherAdhan() {
        arreterAttente();
        String[] p = prieres();
        adhan.setPriere(p[0], p[1], p[2]);
        zoneBas.setVisible(true);
        cartes.show(zoneEcrans, ADHAN);
        etat.setText("Écran : ADHAN");
    }

    private void afficherAttente() {
        arreterAttente();

        String[] p = prieres();
        secondesRestantes = (Integer) choixSecondes.getValue();

        attente.setPriere(p[0]);
        attente.setDureeTotale(secondesRestantes);
        attente.setTempsRestant(secondesRestantes);

        zoneBas.setVisible(true);
        cartes.show(zoneEcrans, ATTENTE);
        etat.setText("Écran : ATTENTE");

        timerAttente = new Timer(1000, e -> {
            secondesRestantes--;
            attente.setTempsRestant(secondesRestantes);
            if (secondesRestantes <= 0) {
                arreterAttente();   // le compteur reste affiché à 00:00
            }
        });
        timerAttente.start();
    }

    private void arreterAttente() {
        if (timerAttente != null) {
            timerAttente.stop();
            timerAttente = null;
        }
    }

    private void arreterTout() {
        arreterAttente();
        son.arreter();
        if (rotation != null) {
            rotation.arreter();
        }
    }

    // Ligne {nom, nom arabe, heure} de la prière sélectionnée
    private String[] prieres() {
        return PRIERES[choixPriere.getSelectedIndex()];
    }

    private static String[] nomsPrieres() {
        String[] noms = new String[PRIERES.length];
        for (int i = 0; i < PRIERES.length; i++) {
            noms[i] = PRIERES[i][0];
        }
        return noms;
    }

    private JButton bouton(String texte, java.awt.event.ActionListener action) {
        JButton b = new JButton(texte);
        b.addActionListener(action);
        return b;
    }
}
