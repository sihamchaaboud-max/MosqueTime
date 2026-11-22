package com.mosquetime;

import com.mosquetime.controller.MosqueeController;
import com.mosquetime.view.MosqueePanel;

import javax.swing.*;

public class TestMosqueePanel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MosqueePanel panneau = new MosqueePanel(
                    new MosqueeController(),
                    nom -> System.out.println("Mosquée affichée : " + nom));

            JFrame fenetre = new JFrame("Test - Mosquées");
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fenetre.setSize(800, 500);
            fenetre.setLocationRelativeTo(null);
            fenetre.setContentPane(panneau);
            fenetre.setVisible(true);
        });
    }
}