package com.mosquetime.controller;

import com.mosquetime.model.Mosquee;
import com.mosquetime.service.MosqueeService;

import java.util.List;

public class MosqueeController {

    private final MosqueeService service = new MosqueeService();

    public List<Mosquee> getMosquees() {
        return service.getMosquees();
    }

    public String getNomActive() {
        return service.nomMosqueeActive();
    }

    public void ajouter(String nom, String ville) {
        service.ajouter(nom, ville);
    }

    public void choisir(String nom) {
        service.choisir(nom);
    }
}