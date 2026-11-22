package com.mosquetime.service;

import com.mosquetime.model.Horaire;
import com.mosquetime.model.Priere;
import com.mosquetime.repository.HoraireRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class HoraireService {

    private final HoraireRepository repository = new HoraireRepository();

    public Optional<Horaire> horaireDuJour() {
        return repository.trouverParDate(LocalDate.now());
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

    public Optional<LocalTime> heureFajrDemain() {
        return repository.trouverParDate(LocalDate.now().plusDays(1))
                .map(h -> h.getHeure(Priere.FAJR));
    }

    public Optional<Priere> prochaineAdhan(LocalTime maintenant) {
        Optional<Horaire> horaire = horaireDuJour();
        if (horaire.isEmpty()) {
            return Optional.empty();
        }
        for (Priere p : Priere.values()) {
            if (p == Priere.CHOROUK) {
                continue;
            }
            LocalTime heure = horaire.get().getHeure(p);
            if (heure != null && heure.isAfter(maintenant)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public long minutesAvant(Priere priere, LocalTime maintenant) {
        Optional<Horaire> horaire = horaireDuJour();
        if (horaire.isEmpty()) {
            return -1;
        }
        LocalTime heure = horaire.get().getHeure(priere);
        if (heure == null) {
            return -1;
        }
        long secondes = ChronoUnit.SECONDS.between(maintenant, heure);
        return (secondes + 59) / 60;
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