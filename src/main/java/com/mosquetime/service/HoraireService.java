package com.mosquetime.service;

import com.mosquetime.model.Horaire;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.HoraireRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class HoraireService {

    // Le fichier horaires.json est relu au plus toutes les 30 secondes
    private static final long DUREE_CACHE_MS = 30_000;

    private final HoraireRepository repository = new HoraireRepository();

    private Optional<Horaire> cache = Optional.empty();
    private LocalDate dateCache;
    private long tempsCache;

    public Optional<Horaire> horaireDuJour() {
        LocalDate aujourdhui = LocalDate.now();
        long maintenant = System.currentTimeMillis();

        if (dateCache == null
                || !aujourdhui.equals(dateCache)
                || maintenant - tempsCache > DUREE_CACHE_MS) {
            cache = repository.trouverParDate(aujourdhui);
            dateCache = aujourdhui;
            tempsCache = maintenant;
        }
        return cache;
    }

    public Optional<Priere> prochainePriere(LocalTime maintenant) {
        Optional<Horaire> horaire = horaireDuJour();
        if (horaire.isEmpty()) {
            return Optional.empty();
        }
        for (Priere p : Priere.values()) {
            LocalTime heure = horaire.get().getHeure(p);
            if (heure != null && heure.isAfter(maintenant)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public Optional<Priere> priereAdhan(LocalTime maintenant) {
        Optional<Horaire> horaire = horaireDuJour();
        if (horaire.isEmpty()) {
            return Optional.empty();
        }
        for (Priere p : Priere.values()) {
            if (p == Priere.CHOROUK) {
                continue;
            }
            LocalTime heure = horaire.get().getHeure(p);
            if (heure != null
                    && heure.truncatedTo(ChronoUnit.MINUTES)
                            .equals(maintenant.truncatedTo(ChronoUnit.MINUTES))) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}