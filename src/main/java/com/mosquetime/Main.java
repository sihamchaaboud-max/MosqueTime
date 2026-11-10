package com.mosquetime;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;
import com.mosquetime.service.RotationService;
import com.mosquetime.util.SonAdhan;
import com.mosquetime.view.AdhanPanel;
import com.mosquetime.view.AttentePanel;
import com.mosquetime.view.ContenuPanel;

/**
 * Point d'entrée de MosqueTime.
 *
 * Cette version est une fenêtre de DÉMONSTRATION de la partie du membre B :
 * elle relie le bandeau, la rotation, le son de l'adhan, AdhanPanel et
 * AttentePanel, et simule la machine à états NORMAL -> ADHAN -> ATTENTE
 * avec des boutons. Quand EcranController (membre A) sera prêt, c'est lui
 * qui pilotera les états à la place des boutons.
 */
public class Main {

    // Noms des "cartes" du CardLayout (un écran = une carte)
    private static final String NORMAL = "NORMAL";
    private static final String ADHAN = "ADHAN";
    private static final String ATTENTE = "ATTENTE";

    // Durée réelle du compte à rebours : 15 minutes
    private static final int ATTENTE_SECONDES = 15 * 60;

    // Composants partagés
    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneEcrans = new JPanel(cartes);

    private final AdhanPanel adhanPanel = new AdhanPanel();
    private final AttentePanel attentePanel = new AttentePanel();
    private final ContenuPanel bandeau = new ContenuPanel();

    private final SonAdhan sonAdhan = new SonAdhan();
    private RotationService rotation;

    // Timer du compte à rebours (1 tick par seconde)
    private Timer timerAttente;
    private int secondesRestantes;

    public static void main(String[] args) {

        // Swing doit être lancé dans son thread dédié (EDT)
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

        // ----- Les trois écrans -----
        zoneEcrans.add(creerEcranNormalProvisoire(), NORMAL);
        zoneEcrans.add(adhanPanel, ADHAN);
        zoneEcrans.add(attentePanel, ATTENTE);

        // ----- Barre de boutons de démonstration -----
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        barre.add(bouton("Normal", e -> afficherNormal()));
        barre.add(bouton("Adhan (avec son)", e -> demarrerAdhan()));
        barre.add(bouton("Attente 15 min", e -> demarrerAttente(ATTENTE_SECONDES)));

        // ----- Fenêtre -----
        JFrame fenetre = new JFrame("MosqueTime");
        fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fenetre.setLayout(new BorderLayout());
        fenetre.add(barre, BorderLayout.NORTH);
        fenetre.add(zoneEcrans, BorderLayout.CENTER);
        fenetre.add(bandeau, BorderLayout.SOUTH);
        fenetre.setMinimumSize(new Dimension(900, 600));
        fenetre.setSize(1280, 760);
        fenetre.setLocationRelativeTo(null);

        // Nettoyage à la fermeture : on coupe le son et les timers
        fenetre.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                arreterTout();
            }
        });

        // ----- Rotation versets / invocations -----
        rotation = new RotationService(
                new VersetRepository(),
                new InvocationRepository(),
                bandeau::afficherVerset,
                bandeau::afficherInvocation);
        rotation.demarrer();

        fenetre.setVisible(true);
        afficherNormal();
    }

    // ---------- Les trois états ----------

    private void afficherNormal() {
        arreterAttente();
        sonAdhan.arreter();
        cartes.show(zoneEcrans, NORMAL);
    }

    private void demarrerAdhan() {
        arreterAttente();

        adhanPanel.setPriere("Dhuhr", "13:05");
        cartes.show(zoneEcrans, ADHAN);

        // Quand le son est fini (ou impossible à lire) -> état ATTENTE
        sonAdhan.jouer(() -> demarrerAttente(ATTENTE_SECONDES));
    }

    private void demarrerAttente(int secondes) {
        arreterAttente();
        sonAdhan.arreter();

        secondesRestantes = secondes;
        attentePanel.setPriere("Dhuhr");
        attentePanel.setTempsRestant(secondesRestantes);
        cartes.show(zoneEcrans, ATTENTE);

        timerAttente = new Timer(1000, e -> {
            secondesRestantes--;
            attentePanel.setTempsRestant(secondesRestantes);

            if (secondesRestantes <= 0) {
                afficherNormal();   // fin du compte à rebours -> iqama
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
        sonAdhan.arreter();
        if (rotation != null) {
            rotation.arreter();
        }
    }

    // ---------- Utilitaires ----------

    // Écran NORMAL provisoire : sera remplacé par NormalPanel (membre A)
    private JPanel creerEcranNormalProvisoire() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x0B3D2E));

        JLabel l = new JLabel("Écran NORMAL (NormalPanel : partie du membre A)",
                SwingConstants.CENTER);
        l.setForeground(new Color(0xE8C766));
        l.setFont(new Font("SansSerif", Font.PLAIN, 32));

        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JButton bouton(String texte, java.awt.event.ActionListener action) {
        JButton b = new JButton(texte);
        b.addActionListener(action);
        return b;
    }
}