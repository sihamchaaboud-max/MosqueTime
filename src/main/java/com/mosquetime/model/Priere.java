package com.mosquetime.model;

public enum Priere {
    FAJR("Fajr", "الفجر"),
    CHOROUK("Chourouq", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء");

    private final String libelle;
    private final String nomArabe;

    Priere(String libelle, String nomArabe) {
        this.libelle = libelle;
        this.nomArabe = nomArabe;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getNomArabe() {
        return nomArabe;
    }
}
