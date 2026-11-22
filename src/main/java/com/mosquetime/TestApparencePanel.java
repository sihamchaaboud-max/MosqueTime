package com.mosquetime;

import com.mosquetime.controller.EcranController;
import com.mosquetime.service.ApparenceService;
import com.mosquetime.view.ApparencePanel;
import com.mosquetime.view.NormalPanel;

import javax.swing.*;
import java.awt.*;

public class TestApparencePanel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EcranController controller = new EcranController();
            NormalPanel ecran = new NormalPanel(controller, "Test arrière-plan");
            controller.setSurMiseAJour(ecran::rafraichir);

            ApparencePanel apparence = new ApparencePanel(
                    new ApparenceService(), ecran::actualiserFond);
            apparence.setPreferredSize(new Dimension(0, 340));

            JFrame fenetre = new JFrame("Test - Arrière-plan");
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fenetre.setLayout(new BorderLayout());
            fenetre.add(ecran, BorderLayout.CENTER);
            fenetre.add(apparence, BorderLayout.SOUTH);
            fenetre.setSize(1200, 950);
            fenetre.setLocationRelativeTo(null);
            fenetre.setVisible(true);

            controller.demarrer();
        });
    }
}