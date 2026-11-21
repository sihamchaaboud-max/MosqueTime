package com.mosquetime.util;

import java.awt.ComponentOrientation;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mosquetime.model.Langue;
import com.mosquetime.model.Parametre;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.ParametreRepository;

/**
 * Internationalisation : textes en français, arabe et anglais.
 *
 *   I18n.t("attente.titre")            -> texte dans la langue courante
 *   I18n.nomPriere(Priere.FAJR)        -> nom de la prière traduit
 *   I18n.changerLangue(Langue.AR)      -> change, enregistre, prévient les écrans
 *   I18n.ajouterEcouteur(this::maj)    -> appelé à chaque changement de langue
 *
 * Les textes sont dans Traductions.java (français, anglais, arabe).
 * La langue choisie est lue au démarrage dans parametres.json.
 */
public final class I18n {

    private static final List<Runnable> ECOUTEURS = new CopyOnWriteArrayList<>();
    private static final ParametreRepository REPOSITORY = new ParametreRepository();

    private static Langue langue = REPOSITORY.charger().getLangue();

    private I18n() {
    }

    // ---------- Langue courante ----------

    public static Langue getLangue() {
        return langue;
    }

    // Change la langue, l'enregistre dans parametres.json et prévient les écrans
    public static void changerLangue(Langue nouvelle) {
        if (nouvelle == null || nouvelle == langue) {
            return;
        }
        langue = nouvelle;

        try {
            Parametre parametre = REPOSITORY.charger();
            parametre.setLangue(nouvelle);
            REPOSITORY.enregistrer(parametre);
        } catch (RuntimeException e) {
            System.err.println("Langue non enregistrée : " + e.getMessage());
        }

        for (Runnable ecouteur : ECOUTEURS) {
            ecouteur.run();
        }
    }

    public static void ajouterEcouteur(Runnable ecouteur) {
        ECOUTEURS.add(ecouteur);
    }

    public static Locale getLocale() {
        return langue.getLocale();
    }

    public static boolean estDroiteAGauche() {
        return langue.estDroiteAGauche();
    }

    // Sens d'affichage des composants Swing (droite à gauche pour l'arabe)
    public static ComponentOrientation orientation() {
        return langue.estDroiteAGauche()
                ? ComponentOrientation.RIGHT_TO_LEFT
                : ComponentOrientation.LEFT_TO_RIGHT;
    }

    // ---------- Textes ----------

    public static String t(String cle) {
        String texte = chercher(langue, cle);
        if (texte == null) {
            texte = chercher(Langue.FR, cle);     // repli : français
        }
        return (texte != null) ? texte : "!" + cle + "!";
    }

    // Remplace {0}, {1}... par les arguments : t("normal.prochaine", nom, heure)
    public static String t(String cle, Object... arguments) {
        String texte = t(cle);
        for (int i = 0; i < arguments.length; i++) {
            texte = texte.replace("{" + i + "}", String.valueOf(arguments[i]));
        }
        return texte.trim();
    }

    public static String nomPriere(Priere priere) {
        return t("priere." + priere.name());
    }

    private static String chercher(Langue l, String cle) {
        return Traductions.get(l, cle);
    }
}
