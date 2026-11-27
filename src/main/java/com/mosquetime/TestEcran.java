package com.mosquetime;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.mosquetime.controller.EcranController;
import com.mosquetime.model.Langue;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;
import com.mosquetime.service.RotationService;
import com.mosquetime.util.I18n;
import com.mosquetime.util.SonAdhan;
import com.mosquetime.view.AdhanPanel;
import com.mosquetime.view.AttentePanel;
import com.mosquetime.view.NormalPanel;

/**
 * Banc de test : affiche chaque écran séparément, sans attendre l'heure
 * d'une prière.
 *
 *   NORMAL   : l'écran principal (date, heure, tableau des prières)
 *   ADHAN    : l'écran de l'adhan pour la prière choisie
 *   ATTENTE  : le compte à rebours (durée réglable en secondes)
 *   Son      : joue / arrête adhan.wav
 *   FR AR EN : change la langue de tous les écrans (enregistrée)
 *
 * Lancer : mvn compile exec:java "-Dexec.mainClass=com.mosquetime.TestEcran"
 */
public class TestEcran {

    private static final String NORMAL = "NORMAL";
    private static final String ADHAN = "ADHAN";
    private static final String ATTENTE = "ATTENTE";

    // Prières du test et heure affichée sur l'écran de l'adhan
    private static final Priere[] PRIERES = {
            Priere.FAJR, Priere.DHUHR, Priere.ASR, Priere.MAGHRIB, Priere.ISHA
    };
    private static final String[] HEURES = {
            "05:45", "12:30", "15:45", "18:20", "19:45"
    };

    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneEcrans = new JPanel(cartes);

    // Le contrôleur n'est PAS démarré : on teste seulement l'affichage
    private final EcranController controller = new EcranController();
    private final NormalPanel normal = new NormalPanel(controller, "Mosquée Test");
    private final AdhanPanel adhan = new AdhanPanel();
    private final AttentePanel attente = new AttentePanel();

    private final SonAdhan son = new SonAdhan();
    private RotationService rotation;

    private Timer timerAttente;
    private int secondesRestantes;

    private final JComboBox<Priere> choixPriere = new JComboBox<>(PRIERES);
    private final JSpinner choixSecondes =
            new JSpinner(new SpinnerNumberModel(30, 5, 120 * 60, 5));
    private final JLabel etat = new JLabel("Écran : NORMAL");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestEcran().ouvrir());
    }

    private void ouvrir() {

        zoneEcrans.add(normal, NORMAL);
        zoneEcrans.add(adhan, ADHAN);
        zoneEcrans.add(attente, ATTENTE);

        // Le nom des prières dans la liste suit la langue choisie
        choixPriere.setSelectedItem(Priere.DHUHR);
        choixPriere.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> liste, Object valeur,
                    int index, boolean selection, boolean focus) {
                super.getListCellRendererComponent(liste, valeur, index, selection, focus);
                if (valeur instanceof Priere p) {
                    setText(I18n.nomPriere(p));
                }
                return this;
            }
        });
        I18n.ajouterEcouteur(choixPriere::repaint);

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
        for (Langue langue : Langue.values()) {
            barre.add(bouton(langue.name(), e -> I18n.changerLangue(langue)));
        }
        barre.add(etat);

        JFrame fenetre = new JFrame("MosqueTime - Test des écrans");
        fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fenetre.setLayout(new BorderLayout());
        fenetre.add(barre, BorderLayout.NORTH);
        fenetre.add(zoneEcrans, BorderLayout.CENTER);
        fenetre.setSize(1400, 800);
        fenetre.setLocationRelativeTo(null);

        // Le bandeau verset/invocation tourne sur les trois écrans
        rotation = new RotationService(
                new VersetRepository(),
                new InvocationRepository(),
                v -> {
                    normal.getContenuPanel().afficherVerset(v);
                    adhan.getContenuPanel().afficherVerset(v);
                    attente.getContenuPanel().afficherVerset(v);
                },
                i -> {
                    normal.getContenuPanel().afficherInvocation(i);
                    adhan.getContenuPanel().afficherInvocation(i);
                    attente.getContenuPanel().afficherInvocation(i);
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
        cartes.show(zoneEcrans, NORMAL);
        etat.setText("Écran : NORMAL");
    }

    private void afficherAdhan() {
        arreterAttente();
        adhan.setPriere(prierechoisie(), HEURES[choixPriere.getSelectedIndex()]);
        cartes.show(zoneEcrans, ADHAN);
        etat.setText("Écran : ADHAN");
    }

    private void afficherAttente() {
        arreterAttente();

        secondesRestantes = (Integer) choixSecondes.getValue();

        attente.setPriere(prierechoisie());
        attente.setDureeTotale(secondesRestantes);
        attente.setTempsRestant(secondesRestantes);

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

    private Priere prierechoisie() {
        return (Priere) choixPriere.getSelectedItem();
    }

    private JButton bouton(String texte, java.awt.event.ActionListener action) {
        JButton b = new JButton(texte);
        b.addActionListener(action);
        return b;
    }
}
