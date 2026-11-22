package com.mosquetime.model;

import java.util.HashMap;
import java.util.Map;

public class ConfigIqama {

    public static final int DUREE_PAR_DEFAUT = 15;

    private final Map<String, Integer> durees = new HashMap<>();

    public ConfigIqama(Map<String, Integer> durees) {
        if (durees != null) {
            this.durees.putAll(durees);
        }
    }

    public int getDuree(Priere priere) {
        return durees.getOrDefault(priere.name(), DUREE_PAR_DEFAUT);
    }

    public void setDuree(Priere priere, int minutes) {
        durees.put(priere.name(), minutes);
    }

    public Map<String, Integer> toMap() {
        return new HashMap<>(durees);
    }
}