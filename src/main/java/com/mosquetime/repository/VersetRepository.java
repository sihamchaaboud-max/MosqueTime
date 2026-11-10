
package com.mosquetime.repository;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Verset;

public class VersetRepository {

    private static final String FICHIER =
            "/data/versets.json";

    private List<Verset> versets;

    public VersetRepository() {
        chargerVersets();
    }

    // Charger tous les versets
    private void chargerVersets() {

        Type type = new TypeToken<List<Verset>>() {}.getType();

        versets = JsonManager.lire(FICHIER, type);
    }

    // Récupérer tous les versets
    public List<Verset> getTous() {
        return versets;
    }

    // Récupérer les versets d'un thème
    public List<Verset> getParTheme(int themeId) {

        return versets.stream()
                .filter(v -> v.getThemeId() == themeId)
                .collect(Collectors.toList());
    }
}
