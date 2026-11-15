package com.mosquetime.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.mosquetime.model.Invocation;
import com.mosquetime.model.Verset;
import com.mosquetime.util.Style;

public class ContenuPanel extends JPanel {

    private final JTextPane texteVerset = creerTexte(30, Style.TEXTE);
    private final JLabel refVerset = creerReference();

    private final JTextPane texteInvocation = creerTexte(26, Style.OR);
    private final JLabel refInvocation = creerReference();

    public ContenuPanel() {
        setLayout(new GridLayout(2, 1, 0, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        setPreferredSize(new Dimension(800, 190));

        add(creerLigne(texteVerset, refVerset));
        add(creerLigne(texteInvocation, refInvocation));
    }

    public void afficherVerset(Verset v) {
        texteVerset.setText(v.getTexteArabe());
        refVerset.setText("\uFD3F " + v.getReference() + " \uFD3E");
        centrer(texteVerset);
    }

    public void afficherInvocation(Invocation i) {
        texteInvocation.setText(i.getTexteArabe());
        refInvocation.setText(i.getReference());
        centrer(texteInvocation);
    }

    private static JPanel creerLigne(JTextPane texte, JLabel reference) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        ligne.add(texte, BorderLayout.CENTER);
        ligne.add(reference, BorderLayout.SOUTH);
        return ligne;
    }

    private static JTextPane creerTexte(int taille, Color couleur) {
        JTextPane t = new JTextPane();
        t.setEditable(false);
        t.setFocusable(false);
        t.setOpaque(false);
        t.setForeground(couleur);
        t.setFont(new Font("Traditional Arabic", Font.PLAIN, taille));
        t.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        return t;
    }

    private static JLabel creerReference() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setForeground(new Color(0xCBD5E1));
        l.setFont(new Font("Traditional Arabic", Font.PLAIN, 18));
        l.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        return l;
    }

    private static void centrer(JTextPane t) {
        StyledDocument doc = t.getStyledDocument();
        SimpleAttributeSet centre = new SimpleAttributeSet();
        StyleConstants.setAlignment(centre, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), centre, false);
    }
}