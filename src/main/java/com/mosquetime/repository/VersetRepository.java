package com.mosquetime.repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Verset;

public class VersetRepository {

    private static final String FICHIER = "versets.json";

    private List<Verset> versets;

    public VersetRepository() {
        chargerVersets();
    }

    private void chargerVersets() {
        Type type = new TypeToken<List<Verset>>() {}.getType();
        List<Verset> lus = JsonManager.lire(FICHIER, type);
        versets = (lus == null) ? new ArrayList<>() : lus;
    }

    public List<Verset> getTous() {
        return versets;
    }

    public List<Verset> getParTheme(int themeId) {
        return versets.stream()
                .filter(v -> v.getThemeId() == themeId)
                .collect(Collectors.toList());
    }
}