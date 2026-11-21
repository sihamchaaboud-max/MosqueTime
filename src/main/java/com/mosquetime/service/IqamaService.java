package com.mosquetime.service;

import com.mosquetime.model.ConfigIqama;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.JsonManager;

/**
 * Délai entre l'adhan et l'iqama, réglable pour chaque prière (iqama.json).
 * Le fichier est relu à chaque demande : un changement fait par l'admin
 * est donc pris en compte à la prochaine prière, sans redémarrer.
 */
public class IqamaService {

    private static final String FICHIER = "iqama.json";
    private static final int MINUTES_MIN = 1;
    private static final int MINUTES_MAX = 120;

    // Renseigné seulement pour les tests (évite de lire le fichier)
    private final ConfigIqama configImposee;

    public IqamaService() {
        this.configImposee = null;
    }

    public IqamaService(ConfigIqama configImposee) {
        this.configImposee = configImposee;
    }

    public int getDelaiMinutes(Priere priere) {
        return config().getDelaiMinutes(priere);
    }

    public int getDelaiSecondes(Priere priere) {
        return getDelaiMinutes(priere) * 60;
    }

    // Change le délai d'une prière et l'enregistre (utilisé par l'écran admin)
    public void setDelaiMinutes(Priere priere, int minutes) {
        if (minutes < MINUTES_MIN || minutes > MINUTES_MAX) {
            throw new IllegalArgumentException(
                    "Le délai doit être entre " + MINUTES_MIN + " et " + MINUTES_MAX + " minutes");
        }
        ConfigIqama config = config();
        config.setDelaiMinutes(priere, minutes);
        enregistrer(config);
    }

    public ConfigIqama getConfig() {
        return config();
    }

    private ConfigIqama config() {
        if (configImposee != null) {
            return configImposee;
        }
        try {
            ConfigIqama lue = JsonManager.lire(FICHIER, ConfigIqama.class);
            return (lue != null) ? lue : new ConfigIqama();
        } catch (RuntimeException e) {
            System.err.println("iqama.json illisible : " + e.getMessage());
            return new ConfigIqama();
        }
    }

    private void enregistrer(ConfigIqama config) {
        if (configImposee == null) {
            JsonManager.ecrire(FICHIER, config);
        }
    }
}
