package com.mosquetime.repository;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Horaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HoraireRepository {

    private static final String FICHIER = "horaires.json";

    public List<Horaire> trouverTous() {
        List<Horaire> liste = JsonManager.lire(FICHIER, new TypeToken<List<Horaire>>() {});
        if (liste == null) {
            return new ArrayList<>();
        }
        return liste;
    }

    public Optional<Horaire> trouverParDate(LocalDate date) {
        for (Horaire h : trouverTous()) {
            if (date.equals(h.getDate())) {
                return Optional.of(h);
            }
        }
        return Optional.empty();
    }

    public void enregistrer(Horaire horaire) {
        List<Horaire> liste = trouverTous();
        boolean remplace = false;

        for (int i = 0; i < liste.size(); i++) {
            if (liste.get(i).getDate().equals(horaire.getDate())) {
                liste.set(i, horaire);
                remplace = true;
                break;
            }
        }
        if (!remplace) {
            liste.add(horaire);
        }
        JsonManager.ecrire(FICHIER, liste);
    }
}