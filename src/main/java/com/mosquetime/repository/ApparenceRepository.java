package com.mosquetime.repository;

import com.mosquetime.model.Apparence;

public class ApparenceRepository {

    private static final String FICHIER = "apparence.json";

    public Apparence lire() {
        Apparence apparence = JsonManager.lire(FICHIER, Apparence.class);
        return apparence == null ? new Apparence() : apparence;
    }

    public void enregistrer(Apparence apparence) {
        JsonManager.ecrire(FICHIER, apparence);
    }
}