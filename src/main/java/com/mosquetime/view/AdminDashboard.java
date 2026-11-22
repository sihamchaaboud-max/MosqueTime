package com.mosquetime.view;

import com.mosquetime.util.Style;

import javax.swing.*;
import java.awt.BorderLayout;

public class AdminDashboard extends JFrame {

    private final JTabbedPane onglets = new JTabbedPane();

    public AdminDashboard() {
        super("MosqueTime - Administration");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        onglets.setFont(Style.PETIT);
        setLayout(new BorderLayout());
        add(onglets, BorderLayout.CENTER);
    }

    public void ajouterOnglet(String titre, JComponent contenu) {
        onglets.addTab(titre, contenu);
    }
}