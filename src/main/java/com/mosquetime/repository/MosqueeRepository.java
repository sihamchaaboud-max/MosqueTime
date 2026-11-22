package com.mosquetime.repository;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Mosquee;

import java.util.ArrayList;
import java.util.List;

public class MosqueeRepository {

    private static final String FICHIER = "mosquees.json";

    private static final TypeToken<List<Mosquee>> TYPE =
            new TypeToken<>() {};

    public List<Mosquee> trouverToutes() {
        List<Mosquee> liste = JsonManager.lire(FICHIER, TYPE);
        return liste == null ? new ArrayList<>() : liste;
    }

    public void enregistrer(List<Mosquee> mosquees) {
        JsonManager.ecrire(FICHIER, mosquees);
    }
}