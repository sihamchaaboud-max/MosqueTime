package com.mosquetime.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Délai d'attente avant l'iqama, en minutes, pour chaque prière.
 * Une prière absente de la liste utilise le délai par défaut.
 */
public class ConfigIqama {

    public static final int DELAI_PAR_DEFAUT = 15;

    private int delaiParDefaut = DELAI_PAR_DEFAUT;
    private Map<Priere, Integer> delaisMinutes = new EnumMap<>(Priere.class);

    public ConfigIqama() {
    }

    public int getDelaiParDefaut() {
        return delaiParDefaut > 0 ? delaiParDefaut : DELAI_PAR_DEFAUT;
    }

    public void setDelaiParDefaut(int minutes) {
        this.delaiParDefaut = minutes;
    }

    // Délai de la prière, ou le délai par défaut si elle n'a pas de réglage
    public int getDelaiMinutes(Priere priere) {
        Integer delai = (delaisMinutes == null) ? null : delaisMinutes.get(priere);
        return (delai != null && delai > 0) ? delai : getDelaiParDefaut();
    }

    public void setDelaiMinutes(Priere priere, int minutes) {
        if (delaisMinutes == null) {
            delaisMinutes = new EnumMap<>(Priere.class);
        }
        delaisMinutes.put(priere, minutes);
    }

    public Map<Priere, Integer> getDelaisMinutes() {
        return delaisMinutes;
    }
}
