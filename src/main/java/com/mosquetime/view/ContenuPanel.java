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

/**
 * Bandeau du bas : un verset (ligne du haut) et une invocation (ligne du bas).
 * Les deux lignes sont mises à jour indépendamment par RotationService :
 *   new RotationService(vRepo, iRepo, contenu::afficherVerset, contenu::afficherInvocation)
 */
public class ContenuPanel extends JPanel {

    private static final Color FOND = new Color(0x0B3D2E);
    private static final Color OR = new Color(0xE8C766);
    private static final Color BLANC = new Color(0xF5F5F0);

    private final JTextPane texteVerset = creerTexte(30, BLANC);
    private final JLabel refVerset = creerReference();

    private final JTextPane texteInvocation = creerTexte(26, OR);
    private final JLabel refInvocation = creerReference();

    public ContenuPanel() {

        setLayout(new GridLayout(2, 1, 0, 6));
        setBackground(FOND);
        setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        setPreferredSize(new Dimension(800, 190));

        add(creerLigne(texteVerset, refVerset));
        add(creerLigne(texteInvocation, refInvocation));
    }

    // Affiche un verset (appelé par la rotation, dans le thread Swing)
    public void afficherVerset(Verset v) {
        texteVerset.setText(v.getTexteArabe());
        refVerset.setText("﴿ " + v.getReference() + " ﴾");
        centrer(texteVerset);
    }

    // Affiche une invocation
    public void afficherInvocation(Invocation i) {
        texteInvocation.setText(i.getTexteArabe());
        refInvocation.setText(i.getReference());
        centrer(texteInvocation);
    }

    // ---------- Construction ----------

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
        // Arabe : lecture de droite à gauche
        t.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        return t;
    }

    private static JLabel creerReference() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setForeground(new Color(0xB9C9C0));
        l.setFont(new Font("Traditional Arabic", Font.PLAIN, 18));
        l.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        return l;
    }

    // Le centrage d'un JTextPane se fait par attribut de paragraphe
    private static void centrer(JTextPane t) {
        StyledDocument doc = t.getStyledDocument();
        SimpleAttributeSet centre = new SimpleAttributeSet();
        StyleConstants.setAlignment(centre, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), centre, false);
    }
}
