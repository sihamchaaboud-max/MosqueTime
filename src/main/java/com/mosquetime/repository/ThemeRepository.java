package com.mosquetime.repository;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Theme;

public class ThemeRepository {

    private static final String FICHIER = "/data/themes.json";

    private List<Theme> themes;

    public ThemeRepository() {
        Type type = new TypeToken<List<Theme>>() {}.getType();
        themes = JsonManager.lire(FICHIER, type);
    }

    // Récupérer tous les thèmes
    public List<Theme> getTous() {
        return themes;
    }

    // Récupérer un thème par son identifiant (null si inconnu)
    public Theme getParId(int id) {
        return themes.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
