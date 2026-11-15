package com.mosquetime.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

public class Horaire {

    private LocalDate date;
    private Map<Priere, LocalTime> heures = new EnumMap<>(Priere.class);

    public Horaire() {
    }

    public Horaire(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<Priere, LocalTime> getHeures() {
        return heures;
    }

    public void setHeure(Priere priere, LocalTime heure) {
        heures.put(priere, heure);
    }

    public LocalTime getHeure(Priere priere) {
        return heures.get(priere);
    }
}