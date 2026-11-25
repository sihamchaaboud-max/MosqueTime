package com.mosquetime.model;

import java.util.Locale;

/** Langues de l'application. L'arabe s'écrit de droite à gauche. */
public enum Langue {

    FR("fr", "Français", false),
    AR("ar", "العربية", true),
    EN("en", "English", false);

    private final String code;
    private final String nomAffiche;
    private final boolean droiteAGauche;

    Langue(String code, String nomAffiche, boolean droiteAGauche) {
        this.code = code;
        this.nomAffiche = nomAffiche;
        this.droiteAGauche = droiteAGauche;
    }

    public String getCode() {
        return code;
    }

    // Nom de la langue écrit dans cette langue (affiché dans le choix de langue)
    public String getNomAffiche() {
        return nomAffiche;
    }

    public boolean estDroiteAGauche() {
        return droiteAGauche;
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(code);
    }
}
