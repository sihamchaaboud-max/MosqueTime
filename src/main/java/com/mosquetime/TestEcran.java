package com.mosquetime;

import com.mosquetime.controller.EcranController;
import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;
import com.mosquetime.service.RotationService;
import com.mosquetime.view.NormalPanel;

import javax.swing.*;

public class TestEcran {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EcranController c = new EcranController();
            NormalPanel p = new NormalPanel(c, "Mosquée Test");
            c.setSurMiseAJour(p::rafraichir);

            RotationService rotation = new RotationService(
                    new VersetRepository(),
                    new InvocationRepository(),
                    p.getContenuPanel()::afficherVerset,
                    p.getContenuPanel()::afficherInvocation);

            JFrame f = new JFrame("MosqueTime");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 700);
            f.setContentPane(p);
            f.setVisible(true);

            c.demarrer();
            rotation.demarrer();
        });
    }
}