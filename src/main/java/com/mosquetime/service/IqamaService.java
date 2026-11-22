package com.mosquetime.service;

import com.mosquetime.model.ConfigIqama;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.IqamaRepository;

public class IqamaService {

    private final IqamaRepository repository = new IqamaRepository();
    private final ConfigIqama config;

    public IqamaService() {
        this.config = new ConfigIqama(repository.trouverTous());
    }

    public int getDureeMinutes(Priere priere) {
        return config.getDuree(priere);
    }

    public void modifierDuree(Priere priere, int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("La durée doit être positive");
        }
        config.setDuree(priere, minutes);
        repository.enregistrer(config.toMap());
    }
}