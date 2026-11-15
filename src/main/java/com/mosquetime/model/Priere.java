package com.mosquetime.model;

public enum Priere {
    FAJR("Fajr"),
    CHOROUK("Chourouq"),
    DHUHR("Dhuhr"),
    ASR("Asr"),
    MAGHRIB("Maghrib"),
    ISHA("Isha");

    private final String libelle;

    Priere(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}