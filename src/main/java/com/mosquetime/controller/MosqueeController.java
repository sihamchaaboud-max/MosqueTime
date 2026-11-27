package com.mosquetime.controller;

import com.mosquetime.model.Langue;
import com.mosquetime.model.Mosquee;
import com.mosquetime.service.MosqueeService;

import java.util.List;

public class MosqueeController {

    private final MosqueeService service;

    public MosqueeController() {
        this(new MosqueeService());
    }

    // Permet de partager le même service avec l'écran principal
    public MosqueeController(MosqueeService service) {
        this.service = service;
    }

    public List<Mosquee> getMosquees() {
        return service.getMosquees();
    }

    public String getNomActive() {
        return service.nomMosqueeActive();
    }

    public String getNomActive(Langue langue) {
        return service.nomMosqueeActive(langue);
    }

    public void ajouter(String nom, String ville) {
        service.ajouter(nom, ville);
    }

    public void ajouter(String nomFr, String nomAr, String nomEn, String ville) {
        service.ajouter(nomFr, nomAr, nomEn, ville);
    }

    public void traduire(String nomFr, String nomAr, String nomEn) {
        service.traduire(nomFr, nomAr, nomEn);
    }

    public void choisir(String nom) {
        service.choisir(nom);
    }
}
