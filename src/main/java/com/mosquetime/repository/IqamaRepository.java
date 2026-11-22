package com.mosquetime.repository;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class IqamaRepository {

    private static final String FICHIER = "iqama.json";

    private static final TypeToken<Map<String, Integer>> TYPE =
            new TypeToken<>() {};

    public Map<String, Integer> trouverTous() {
        return JsonManager.lire(FICHIER, TYPE);
    }

    public void enregistrer(Map<String, Integer> durees) {
        JsonManager.ecrire(FICHIER, durees);
    }
}