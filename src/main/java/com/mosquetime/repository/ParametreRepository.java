package com.mosquetime.repository;

import com.mosquetime.model.Parametre;

public class ParametreRepository {

    private static final String FICHIER = "parametres.json";

    // Lit les réglages ; en cas de fichier absent ou illisible : réglages par défaut
    public Parametre charger() {
        try {
            Parametre parametre = JsonManager.lire(FICHIER, Parametre.class);
            return (parametre != null) ? parametre : new Parametre();
        } catch (RuntimeException e) {
            System.err.println("parametres.json illisible : " + e.getMessage());
            return new Parametre();
        }
    }

    public void enregistrer(Parametre parametre) {
        JsonManager.ecrire(FICHIER, parametre);
    }
}
