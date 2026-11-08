
package com.mosquetime;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        // Lancement de l'application Swing
        SwingUtilities.invokeLater(() -> {

            try {
                // Apparence native de Windows
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO : Ouvrir la fenêtre principale de MosqueTime
            System.out.println("MosqueTime démarre...");
        });
    }
}
