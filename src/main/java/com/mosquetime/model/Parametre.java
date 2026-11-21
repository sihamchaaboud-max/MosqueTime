package com.mosquetime.model;

/** Réglages enregistrés dans parametres.json (pour l'instant : la langue). */
public class Parametre {

    private Langue langue = Langue.FR;

    public Parametre() {
    }

    public Langue getLangue() {
        return (langue != null) ? langue : Langue.FR;
    }

    public void setLangue(Langue langue) {
        this.langue = langue;
    }
}
